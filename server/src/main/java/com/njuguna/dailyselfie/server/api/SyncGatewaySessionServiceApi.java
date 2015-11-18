package com.njuguna.dailyselfie.server.api;

import com.njuguna.dailyselfie.server.Config;
import com.njuguna.dailyselfie.common.entity.SyncGatewaySessionRequest;
import com.njuguna.dailyselfie.common.entity.SyncGatewaySessionResponse;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

public interface SyncGatewaySessionServiceApi {
	
	// The path where we expect the SessionService to live
	public static final String END_POINT_SESSION = "_session";
	public static final String END_POINT_SELFIE_SESSION = "/" + Config.SELFIE_SYNC_GATEWAY + "/" + END_POINT_SESSION;
	public static final String END_POINT_SELFIE_SESSION_RECORD = END_POINT_SELFIE_SESSION + "/" + "{id}";
	
    @GET(END_POINT_SELFIE_SESSION_RECORD)
    public Response getSelfieSession(@Path("id") String id);

    @POST(END_POINT_SELFIE_SESSION)
    public SyncGatewaySessionResponse createSelfieSession(@Body SyncGatewaySessionRequest sr);

    @DELETE(END_POINT_SELFIE_SESSION_RECORD)
    public Response deleteSFSession(@Path("id") String id);

}
