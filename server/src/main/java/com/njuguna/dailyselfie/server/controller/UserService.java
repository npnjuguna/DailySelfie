package com.njuguna.dailyselfie.server.controller;

import com.couchbase.client.java.CouchbaseBucket;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.njuguna.dailyselfie.common.Constants;
import com.njuguna.dailyselfie.common.util.IDUtils;
import com.njuguna.dailyselfie.server.Config;
import com.njuguna.dailyselfie.server.api.SyncGatewayUserServiceApi;
import com.njuguna.dailyselfie.server.client.UserServiceApi;
import com.njuguna.dailyselfie.server.entity.SyncGatewayUser;
import com.njuguna.dailyselfie.server.entity.User;
import com.njuguna.dailyselfie.server.entity.UserMap;
import com.njuguna.dailyselfie.server.repository.UserMapRepository;
import com.njuguna.dailyselfie.server.repository.UserRepository;
import com.squareup.okhttp.OkHttpClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

@RestController
public class UserService implements UserServiceApi {

	private final UserRepository userRepository;

	private final UserMapRepository userMapRepository;
	
	@Autowired
	private CouchbaseBucket couchbaseBucket;

	@Autowired
	UserService(UserRepository userRepository, UserMapRepository userMapRepository) {
		this.userRepository = userRepository;
		this.userMapRepository = userMapRepository;
	}
	
	@Override
	@RequestMapping(value=UserServiceApi.USER_SVC_PATH, method= RequestMethod.GET)
	public List<User> getUserList() {
		return Lists.newArrayList(userRepository.findAll());
	}

	@Override
	@RequestMapping(value=UserServiceApi.USER_SVC_PATH, method= RequestMethod.POST)
	public User addUser(@RequestBody User u) {
        // verify the token sent by client
        // Set up the HTTP transport and JSON factory
        try {
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(httpTransport, jsonFactory)
                    .setAudience(Arrays.asList(Config.GOOGLE_CLIENT_ID))
                    .setIssuer("https://accounts.google.com")
                    .build();
            GoogleIdToken idToken = verifier.verify(u.getAuthToken());
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                System.out.println("User ID: " + payload.getSubject() + " for user " + u.getUsername() + " validated.");
            } else {
                System.out.println("Invalid ID token for user " + u.getUsername());
                throw new InvalidGoogleTokenException(u.getUsername());
            }
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            throw new GoogleTokenValidationException(u.getUsername());
        } catch (IOException e) {
            e.printStackTrace();
            throw new GoogleTokenValidationException(u.getUsername());
        }

		// set a random decoy password
		u.setPassword(IDUtils.generateBase64GUID());

        // get the next user number
        Long uNum = couchbaseBucket.counter(Config.USER_NUMBER_KEY, 1L, 10L, Config.COUCHBASE_OPS_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS).content();

        if (uNum <= 10L) {
            throw new UserNumberGenerationException(u.getUsername());
        }
        u.setuNum(uNum);
        u.setId(IDUtils.generateBase64GUIDwNum(uNum));
        u.setCrTime(System.currentTimeMillis());
        u.setCrDate(getCurrentDateTime());

		// create the user name to id map
        //TODO: check that returned user map is not null and throw appropriate error
		UserMap userMap = createUserMapFromUser(u);
		
		// throw an exception is a username already exists
		if (null != userMapRepository.findOne(userMap.getId())) {
			throw new UserNameExitsException(u.getUsername());
		}

		// create the user in sync gateway
		OkHttpClient c = new OkHttpClient();
		c.setConnectTimeout(Config.HTTP_CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		c.setReadTimeout(Config.HTTP_CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		
		RestAdapter sgRestAdapter = new RestAdapter.Builder()
		.setLogLevel(RestAdapter.LogLevel.FULL)
	    .setEndpoint(Config.SERVER_SYNC_GATEWAY)
	    .setClient(new OkClient(c))
	    .build();
		
		List<String> userAdminChannels = Arrays.asList(new String[] {u.getId()});

		SyncGatewayUserServiceApi syncGatewayUserService = sgRestAdapter.create(SyncGatewayUserServiceApi.class);
		
		// Create and populate a simple object to be used in the request
		SyncGatewayUser syncGatewayUser = new SyncGatewayUser();
		syncGatewayUser.setName(u.getId());
		syncGatewayUser.setPassword(IDUtils.generateBase64GUID()+IDUtils.generateBase64GUID());
		syncGatewayUser.setEmail(u.getUsername());
		syncGatewayUser.setAdminChannels(userAdminChannels);
		
		// create the user in Selfie sync gateway
		Response createUserResponse;
		try {
			createUserResponse = syncGatewayUserService.createSelfieUser(syncGatewayUser);
		} catch (Exception e) {
			e.printStackTrace();
			throw new SyncGatewayUserInsertException(u.getId(), -1, e.getMessage());
		}
		
		if ((createUserResponse.getStatus() < 200) && (createUserResponse.getStatus() >= 300)) {
			throw new SyncGatewayUserInsertException(u.getId(), createUserResponse.getStatus(), createUserResponse.getReason());
		}
		
		// save the user map
		userMapRepository.save(userMap);
		
		return userRepository.save(u);
	}

	@Override
	@RequestMapping(value=UserServiceApi.USER_RECORD_PATH, method= RequestMethod.PUT)
	public User updateUser(@PathVariable String id, @RequestBody User u) {
		
		// check is the user is actually existing
		User oldUser = userRepository.findOne(u.getId());
		if (null == oldUser) {
			throw new UserNotFoundException(u.getId());
		}
		
		// update the user in sync gateway
		OkHttpClient c = new OkHttpClient();
		c.setConnectTimeout(Config.HTTP_CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		c.setReadTimeout(Config.HTTP_CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		
		RestAdapter sgRestAdapter = new RestAdapter.Builder()
		.setLogLevel(RestAdapter.LogLevel.FULL)
	    .setEndpoint(Config.SERVER_SYNC_GATEWAY)
	    .setClient(new OkClient(c))
	    .build();
		
		SyncGatewayUserServiceApi syncGatewayUserService = sgRestAdapter.create(SyncGatewayUserServiceApi.class);
		
		// Create and populate a simple object to be used in the request
		SyncGatewayUser syncGatewayUser = new SyncGatewayUser();
		syncGatewayUser.setName(u.getId());
		syncGatewayUser.setPassword(IDUtils.generateBase64GUID() + IDUtils.generateBase64GUID());
		syncGatewayUser.setEmail(u.getUsername());
		
		// update user in Selfie sync gateway
		Response updateUserResponse;
		try {
			updateUserResponse = syncGatewayUserService.updateSelfieUser(id, syncGatewayUser);
		} catch (Exception e) {
			e.printStackTrace();
			throw new SyncGatewayUserUpdateException(u.getId(), -1, e.getMessage());
		}
		if ((updateUserResponse.getStatus() < 200) && (updateUserResponse.getStatus() >= 300)) {
			throw new SyncGatewayUserUpdateException(u.getId(), updateUserResponse.getStatus(), updateUserResponse.getReason());
		}

		// create the old user name to id map and delete the map
		UserMap oldUserMap = createUserMapFromUser(oldUser);
		userMapRepository.delete(oldUserMap);
		
		// create the user name to id map and insert it
		UserMap userMap = createUserMapFromUser(u);
		userMapRepository.save(userMap);
		
		return userRepository.save(u);
	}

	@Override
	@RequestMapping(value=UserServiceApi.USER_SEARCH_PATH, method= RequestMethod.GET)
	public List<User> findUsers(@RequestParam Map<String, String> options) {
		throw new FeatureNotImplementedException();
	}

	@Override
	@RequestMapping(value=UserServiceApi.USER_COUNT_PATH, method= RequestMethod.GET)
	public Long getUserCount() {
		return userRepository.count();
	}

	@Override
	@RequestMapping(value=UserServiceApi.USER_EXISTS_PATH, method= RequestMethod.GET)
	public Boolean userExists(@PathVariable String id) {
		return userRepository.exists(id);
	}

	@Override
	@RequestMapping(value=UserServiceApi.USER_RECORD_PATH, method= RequestMethod.GET)
	public User getUser(@PathVariable String id) {
		return userRepository.findOne(id);
	}

	@Override
	@RequestMapping(value=UserServiceApi.USER_MAP_RECORD_PATH, method= RequestMethod.GET)
	public User getMapUser(@PathVariable String id) {
		
		UserMap userMap = userMapRepository.findOne(id);
		if (userMap == null) {
			return null;
		} else {
			String userId = userMap.getUserId();
			return userRepository.findOne(userId);
		}
	}

	@Override
	@RequestMapping(value=UserServiceApi.USER_RECORD_PATH, method= RequestMethod.DELETE)
	public Boolean deleteUser(@PathVariable String id) {
		
		// get the user object
		User user = userRepository.findOne(id);
		if (null == user) {
			throw new UserNotFoundException(id);
		}

		// delete the user in sync gateway
		OkHttpClient c = new OkHttpClient();
		c.setConnectTimeout(Config.HTTP_CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		c.setReadTimeout(Config.HTTP_CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		
		RestAdapter sgRestAdapter = new RestAdapter.Builder()
		.setLogLevel(RestAdapter.LogLevel.FULL)
	    .setEndpoint(Config.SERVER_SYNC_GATEWAY)
	    .setClient(new OkClient(c))
	    .build();
		
		SyncGatewayUserServiceApi syncGatewayUserService = sgRestAdapter.create(SyncGatewayUserServiceApi.class);
		
		// delete all user sessions from sync gateway
		Response deleteUserSessionsResponse;
		try {
			deleteUserSessionsResponse = syncGatewayUserService.deleteSelfieUserSessions(id);
		} catch (Exception e) {
			e.printStackTrace();
			throw new SyncGatewayUserDeleteException(id, -1, e.getMessage());
		}
		if ((deleteUserSessionsResponse.getStatus() < 200) && (deleteUserSessionsResponse.getStatus() >= 300)) {
			throw new SyncGatewayUserDeleteException(id, deleteUserSessionsResponse.getStatus(), deleteUserSessionsResponse.getReason());
		}

		// delete user from sync gateway
		Response deleteUserResponse;
		try {
			deleteUserResponse = syncGatewayUserService.deleteSelfieUser(id);
		} catch (RetrofitError e) {
			e.printStackTrace();
			deleteUserResponse = e.getResponse();
			if (deleteUserResponse.getStatus() != 404) {
				throw new SyncGatewayUserDeleteException(id, -1, e.getMessage());
			} 
		} catch (Exception e) {
			e.printStackTrace();
			throw new SyncGatewayUserDeleteException(id, -1, e.getMessage());
		}
		if ((deleteUserResponse.getStatus() < 200) 
				&& (deleteUserResponse.getStatus() >= 300)
				&& (deleteUserResponse.getStatus() != 404)
				) {
			throw new SyncGatewayUserDeleteException(id, deleteUserResponse.getStatus(), deleteUserResponse.getReason());
		}

		// delete the user name to id map
		// create the user name to id map and insert it
		UserMap userMap = createUserMapFromUser(user);
		userMapRepository.delete(userMap);

		userRepository.delete(id);
		return true;
	}

    public static String getCurrentDateTime() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(Constants.PROPERTY_DATE_FORMAT);
        Calendar calendar = GregorianCalendar.getInstance();
        return dateFormatter.format(calendar.getTime());
    }

    private UserMap createUserMapFromUser(User user) {
        String userMapKey = generateUserMapKey(user);
		UserMap userMap = (!Strings.isNullOrEmpty(userMapKey) ? new UserMap(userMapKey, user.getId()) : null);
		return userMap;
	}

    private String generateUserMapKey(User user) {
        String userMap = null;
        switch (user.getAuthType()) {
            case Constants.AUTH_TYPE_GOOGLE_PLUS: {
                userMap = (!Strings.isNullOrEmpty(user.getEmail()) ? Constants.ID_PREFIX_GOOGLE_USER_MAP + user.getEmail() : null );
            }
            break;
            case Constants.AUTH_TYPE_FACEBOOK: {
                userMap = (!Strings.isNullOrEmpty(user.getEmail()) ? Constants.ID_PREFIX_FACEBOOK_USER_MAP + user.getEmail() : null );
            }
            break;
            case Constants.AUTH_TYPE_TWITTER_DIGITS: {
                userMap = (!Strings.isNullOrEmpty(user.getTelephone()) ? Constants.ID_PREFIX_TWITTER_DIGITS_USER_MAP + user.getTelephone() : null );
            }
            break;
            case Constants.AUTH_TYPE_BASIC: {
                userMap = (!Strings.isNullOrEmpty(user.getUsername()) ? Constants.ID_PREFIX_BASIC_USER_MAP + user.getUsername() : null );
            }
            break;
        }
        return userMap;
    }


}

@ControllerAdvice
class UserControllerAdvice {

    @ResponseBody
    @ExceptionHandler(GoogleTokenValidationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    String googleTokenValidationException(GoogleTokenValidationException ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(InvalidGoogleTokenException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String invalidGoogleTokenException(InvalidGoogleTokenException ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(UserNumberGenerationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    String userNumberGenerationExceptionHandler(UserNumberGenerationException ex) {
        return ex.getMessage();
    }

    @ResponseBody
	@ExceptionHandler(UserNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	String userNotFoundExceptionHandler(UserNotFoundException ex) {
		return ex.getMessage();
	}

	@ResponseBody
	@ExceptionHandler(SyncGatewayUserInsertException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	String syncGatewayUserInsertException(SyncGatewayUserInsertException ex) {
		return ex.getMessage();
	}

	@ResponseBody
	@ExceptionHandler(SyncGatewayUserUpdateException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	String syncGatewayUserUpdateException(SyncGatewayUserUpdateException ex) {
		return ex.getMessage();
	}

	@ResponseBody
	@ExceptionHandler(UserNameExitsException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	String userNameExitsException(UserNameExitsException ex) {
		return ex.getMessage();
	}

	@ResponseBody
	@ExceptionHandler(SyncGatewayUserSessionsDeleteException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	String syncGatewayUserSessionsDeleteException(SyncGatewayUserSessionsDeleteException ex) {
		return ex.getMessage();
	}

	@ResponseBody
	@ExceptionHandler(SyncGatewayUserDeleteException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	String syncGatewayUserDeleteException(SyncGatewayUserDeleteException ex) {
		return ex.getMessage();
	}

}

@SuppressWarnings("serial")
class SyncGatewayUserSessionsDeleteException extends RuntimeException {
	public SyncGatewayUserSessionsDeleteException(String userId, int sgStatusCode, String sgErrorMessage) {
		super("Could not delete user '" + userId + "' session in Sync Gateway. SG Code: " + sgStatusCode + "; Message: " + sgErrorMessage);
    }
}

@SuppressWarnings("serial")
class SyncGatewayUserDeleteException extends RuntimeException {
	public SyncGatewayUserDeleteException(String userId, int sgStatusCode, String sgErrorMessage) {
		super("Could not delete user '" + userId + "' from Sync Gateway. SG Code: " + sgStatusCode + "; Message: " + sgErrorMessage);
    }
}

@SuppressWarnings("serial")
class SyncGatewayUserInsertException extends RuntimeException {
	public SyncGatewayUserInsertException(String userId, int sgStatusCode, String sgErrorMessage) {
		super("Could not insert user '" + userId + "' into Sync Gateway. SG Code: " + sgStatusCode + "; Message: " + sgErrorMessage);
    }
}

@SuppressWarnings("serial")
class SyncGatewayUserUpdateException extends RuntimeException {
	public SyncGatewayUserUpdateException(String userId, int sgStatusCode, String sgErrorMessage) {
		super("Could not update user '" + userId + "' in Sync Gateway. SG Code: " + sgStatusCode + "; Message: " + sgErrorMessage);
    }
}

@SuppressWarnings("serial")
class UserNotFoundException extends RuntimeException {
	public UserNotFoundException(String userId) {
		super("Could not find user '" + userId + "'.");
    }
}

@SuppressWarnings("serial")
class UserNameExitsException extends RuntimeException {
	public UserNameExitsException(String userName) {
		super("User already exists '" + userName + "'.");
    }
}

@SuppressWarnings("serial")
class UserNumberGenerationException extends RuntimeException {
    public UserNumberGenerationException(String userName) {
        super("Error generating user number for '" + userName + "'.");
    }
}

@SuppressWarnings("serial")
class InvalidGoogleTokenException extends RuntimeException {
    public InvalidGoogleTokenException(String userName) {
        super("Invalid google token for '" + userName + "'.");
    }
}

@SuppressWarnings("serial")
class GoogleTokenValidationException extends RuntimeException {
    public GoogleTokenValidationException(String userName) {
        super("General google token validation error for '" + userName + "'.");
    }
}