package com.njuguna.dailyselfie.server.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.njuguna.dailyselfie.common.Constants;
import com.njuguna.dailyselfie.common.util.IDUtils;
import com.njuguna.dailyselfie.server.entity.Session;
import com.njuguna.dailyselfie.server.entity.SessionBuilder;
import com.njuguna.dailyselfie.server.entity.User;
import com.njuguna.dailyselfie.server.entity.UserBuilder;

import java.text.SimpleDateFormat;
import java.util.concurrent.ThreadLocalRandom;

public class TestData {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Construct and return a GoogleUser object with a values
	 * 
	 * @return User
	 */
	public static User randomGoogleUser() {
		// Construct a random identifier using Java's UUID class
		int uNum = ThreadLocalRandom.current().nextInt(1,1000);
		String id = IDUtils.generateBase64GUIDwNum(uNum);
		String username = "user-" + id + "@gmail.com";
		String password = IDUtils.generateBase64GUID();
		String fullname = "Full Name - " + id;
		String email = username;
		Integer authType = Constants.AUTH_TYPE_GOOGLE_PLUS;
		return new UserBuilder()
				.setId(id)
				.setUsername(username)
				.setPassword(password)
				.setFullname(fullname)
				.setEmail(email)
				.setAuthType(authType)
				.createUser();
	}

	/**
	 * Construct and return a TwitterUser object with a values
	 * 
	 * @return User
	 */
	public static User randomTwitterUser() {
		// Construct a random identifier using Java's UUID class
		int uNum = ThreadLocalRandom.current().nextInt(1,1000);
		String id = IDUtils.generateBase64GUIDwNum(uNum);
		String username = "twitter-" + id;
		String password = IDUtils.generateBase64GUID();
		String telephone = "+254700000000";
		Integer authType = Constants.AUTH_TYPE_TWITTER_DIGITS;
		return new UserBuilder()
				.setId(id)
				.setUsername(username)
				.setPassword(password)
				.setTelephone(telephone)
				.setAuthType(authType)
				.createUser();
	}

	/**
	 * Construct and return a Session object with a values
	 * 
	 * @return Session
	 */
	public static Session randomGoogleSession() {
		// Construct a random identifier using Java's UUID class
		int uNum = ThreadLocalRandom.current().nextInt(1,1000);
		String id = IDUtils.generateBase64GUIDwNum(uNum);
		String userId = IDUtils.generateBase64GUIDwNum(uNum);
		int authType = Constants.AUTH_TYPE_GOOGLE_PLUS;
		String sessionId = "session-" + id;
		String token = "token-" + id;
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SZ");
        String expires = dateFormatter.format(System.currentTimeMillis());
		return new SessionBuilder()
				.setId(id)
				.setUserId(userId)
				.setAuthType(authType)
				.setSessionId(sessionId)
				.setToken(token)
				.setExpires(expires)
				.createSession();
	}

	/**
	 * Construct and return a Session object with a values
	 * 
	 * @return Session
	 */
	public static Session randomTwitterSession() {
		// Construct a random identifier using Java's UUID class
		int uNum = ThreadLocalRandom.current().nextInt(1,1000);
		String id = IDUtils.generateBase64GUIDwNum(uNum);
		String userId = IDUtils.generateBase64GUIDwNum(uNum);
		int authType = Constants.AUTH_TYPE_TWITTER_DIGITS;
		String sessionId = "session-" + id;
		String token = "token-" + id;
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SZ");
        String expires = dateFormatter.format(System.currentTimeMillis());
		return new SessionBuilder()
				.setId(id)
				.setUserId(userId)
				.setAuthType(authType)
				.setSessionId(sessionId)
				.setToken(token)
				.setExpires(expires)
				.createSession();
	}

	/**
	 * Convert an object to JSON using Jackson's ObjectMapper
	 * 
	 * @param o
	 * @return
	 * @throws Exception
	 */
	public static String toJson(Object o) throws Exception {
		return objectMapper.writeValueAsString(o);
	}
}
