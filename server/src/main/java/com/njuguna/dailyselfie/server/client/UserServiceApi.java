package com.njuguna.dailyselfie.server.client;

import com.njuguna.dailyselfie.server.entity.User;

import java.util.List;
import java.util.Map;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.QueryMap;

public interface UserServiceApi {

    // The path where we expect the UserService to live
    public static final String USER_SVC_PATH = "/user";

    // search parameters
    public static final String USERNAME_PARAMETER = "username";
    public static final String FULLNAME_PARAMETER = "fullname";
    public static final String EMAIL_PARAMETER = "email";
    public static final String TELEPHONE_PARAMETER = "telephone";

    // The path where we expect the UserMapService to live
    public static final String USER_MAP_SVC_PATH = "/user/user_map";

    // The path to individual user record by map id
    public static final String USER_MAP_RECORD_PATH = USER_MAP_SVC_PATH + "/{id:.+}";

    // The path to users count
    public static final String USER_COUNT_PATH = USER_SVC_PATH + "/count";

    // The path to search users
    public static final String USER_SEARCH_PATH = USER_SVC_PATH + "/find";

    // The path to individual user record
    public static final String USER_RECORD_PATH = USER_SVC_PATH + "/{id}";

    // The path to check if user exists
    public static final String USER_EXISTS_PATH = USER_SVC_PATH + "/{id}/exists";

    @GET(USER_SVC_PATH)
    public List<User> getUserList();

    @POST(USER_SVC_PATH)
    public User addUser(@Body User u);

    @PUT(USER_RECORD_PATH)
    public User updateUser(@Path("id") String id, @Body User u);

    @DELETE(USER_RECORD_PATH)
    public Boolean deleteUser(@Path("id") String id);

    @GET(USER_RECORD_PATH)
    public User getUser(@Path("id") String id);

    @GET(USER_COUNT_PATH)
    public Long getUserCount();

    @GET(USER_SEARCH_PATH)
    public List<User> findUsers(@QueryMap Map<String, String> options);

    @GET(USER_EXISTS_PATH)
    public Boolean userExists(@Path("id") String id);

    @GET(USER_MAP_RECORD_PATH)
    public User getMapUser(@Path("id") String id);

}