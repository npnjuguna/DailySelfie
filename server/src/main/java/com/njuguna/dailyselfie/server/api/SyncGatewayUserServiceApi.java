package com.njuguna.dailyselfie.server.api;

import com.njuguna.dailyselfie.server.Config;
import com.njuguna.dailyselfie.server.entity.SyncGatewayUser;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;

public interface SyncGatewayUserServiceApi {
	
	// The path where we expect the UserService to live
	public static final String END_POINT_USER = "_user";
	public static final String END_POINT_SELFIE_USER = "/" + Config.SELFIE_SYNC_GATEWAY + "/" + END_POINT_USER + "/";
	public static final String END_POINT_SELFIE_USER_RECORD = END_POINT_SELFIE_USER + "{id}";
	public static final String END_POINT_SELFIE_USER_SESSION_RECORD = END_POINT_SELFIE_USER + "{id}" + "/_session/" + "{sessionId}";
	public static final String END_POINT_SELFIE_USER_SESSION = END_POINT_SELFIE_USER + "{id}" + "/_session";
	
    @POST(END_POINT_SELFIE_USER)
    public Response createSelfieUser(@Body SyncGatewayUser ur);

    @GET(END_POINT_SELFIE_USER_RECORD)
    public SyncGatewayUser getSelfieUser(@Path("id") String id);

    @PUT(END_POINT_SELFIE_USER_RECORD)
    public Response updateSelfieUser(@Path("id") String id, @Body SyncGatewayUser ur);

    @DELETE(END_POINT_SELFIE_USER_RECORD)
    public Response deleteSelfieUser(@Path("id") String id);

    @DELETE(END_POINT_SELFIE_USER_SESSION_RECORD)
    public Response deleteSelfieUserSession(@Path("id") String id, @Path("sessionId") String sessionId);

    @DELETE(END_POINT_SELFIE_USER_SESSION)
    public Response deleteSelfieUserSessions(@Path("id") String id);

}
