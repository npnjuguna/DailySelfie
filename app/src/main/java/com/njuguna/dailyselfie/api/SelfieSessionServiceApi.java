package com.njuguna.dailyselfie.api;

import com.njuguna.dailyselfie.common.entity.SessionRequest;
import com.njuguna.dailyselfie.common.entity.SyncGatewaySessionResponse;
import com.njuguna.dailyselfie.profile.entity.Session;

import java.util.List;
import java.util.Map;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.QueryMap;

public interface SelfieSessionServiceApi {
	
	// The path where we expect the SessionService to live
	public static final String SESSION_SVC_PATH = "/session/selfie";
	
    // The path for a session request
    public static final String SESSION_REQUEST_SVC_PATH = SESSION_SVC_PATH + "/sync";

	public static final String USER_ID_PARAMETER = "userId";
	public static final String DEV_ID_PARAMETER = "devId";
	public static final String ORG_ID_PARAMETER = "orgId";
	public static final String TOKEN_PARAMETER = "token";
	public static final String SESSION_ID_PARAMETER = "sessionId";
	
	// The path to search sessions
	public static final String SESSION_SEARCH_PATH = SESSION_SVC_PATH + "/find";
	
	// The path to sessions count
	public static final String SESSION_COUNT_PATH = SESSION_SVC_PATH + "/count";
	
	// The path to check if session exists
	public static final String SESSION_EXISTS_PATH = SESSION_SVC_PATH + "/{id}/exists";
	
	// The path to individual session record
	public static final String SESSION_RECORD_PATH = SESSION_SVC_PATH + "/{id}";
	
	@GET(SESSION_SVC_PATH)
	public List<Session> getSessionList();
	
	@GET(SESSION_SEARCH_PATH)
	public List<Session> findSessions(@QueryMap Map<String, String> options);

	@GET(SESSION_COUNT_PATH)
	public Long getSessionCount();

	@GET(SESSION_EXISTS_PATH)
	public Boolean sessionExists(@Path("id") String id);

	@GET(SESSION_RECORD_PATH)
	public Session getSession(@Path("id") String id);

	@POST(SESSION_SVC_PATH)
	public Session addSession(@Body Session s);
	
    @POST(SESSION_REQUEST_SVC_PATH)
    public SyncGatewaySessionResponse createSyncSession(@Body SessionRequest sr);

	@DELETE(SESSION_RECORD_PATH)
	public Boolean deleteSession(@Path("id") String id);

}
