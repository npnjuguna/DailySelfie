package com.njuguna.dailyselfie.api;

import retrofit.client.Response;
import retrofit.http.GET;

public interface ProfileServerHealthServiceApi {

    // The path where we expect the ProfileServerHealthService to live
    public static final String PROFILE_SERVER_HEALTH_SVC_PATH = "/health";

    @GET(PROFILE_SERVER_HEALTH_SVC_PATH)
    public Response getHealth();

}