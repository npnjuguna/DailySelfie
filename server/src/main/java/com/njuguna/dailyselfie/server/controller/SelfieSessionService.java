package com.njuguna.dailyselfie.server.controller;

import com.google.common.collect.Lists;
import com.njuguna.dailyselfie.common.util.IDUtils;
import com.njuguna.dailyselfie.server.Config;
import com.njuguna.dailyselfie.server.api.SyncGatewaySessionServiceApi;
import com.njuguna.dailyselfie.server.client.SelfieSessionServiceApi;
import com.njuguna.dailyselfie.server.entity.Session;
import com.njuguna.dailyselfie.server.entity.SessionBuilder;
import com.njuguna.dailyselfie.common.entity.SessionRequest;
import com.njuguna.dailyselfie.common.entity.SyncGatewaySessionRequest;
import com.njuguna.dailyselfie.common.entity.SyncGatewaySessionResponse;
import com.njuguna.dailyselfie.server.repository.SessionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import retrofit.RestAdapter;

@RestController
public class SelfieSessionService implements SelfieSessionServiceApi {

	@Autowired
	private SessionRepository sessionRepository;
	
	@Override
	@RequestMapping(value=SelfieSessionServiceApi.SESSION_SVC_PATH, method= RequestMethod.GET)
	public List<Session> getSessionList() {
		return Lists.newArrayList(sessionRepository.findAll());
	}

	@Override
	@RequestMapping(value=SelfieSessionServiceApi.SESSION_SVC_PATH, method= RequestMethod.POST)
	public Session addSession(@RequestBody Session s) {
		return sessionRepository.save(s);
	}

	@Override
	@RequestMapping(value=SelfieSessionServiceApi.SESSION_REQUEST_SVC_PATH, method= RequestMethod.POST)
	public SyncGatewaySessionResponse createSyncSession(@RequestBody SessionRequest sr) {
		
		// validate that the session request has all the field values
		if ((sr.getToken() == null) || (sr.getUserId() == null)) {
			return null;
		}
		
		// validate token with authority - google
		//TODO: implement token validation
		
		RestAdapter sgRestAdapter = new RestAdapter.Builder()
		.setLogLevel(RestAdapter.LogLevel.FULL)
	    .setEndpoint(Config.SERVER_SYNC_GATEWAY)
	    .build();

		SyncGatewaySessionServiceApi syncGatewaySessionService = sgRestAdapter.create(SyncGatewaySessionServiceApi.class);

		// Create and populate a simple object to be used in the request
		SyncGatewaySessionRequest syncGatewaySessionRequest = new SyncGatewaySessionRequest(sr.getUserId(), Config.SYNC_GATEWAY_SESSION_TTL);
		
		SyncGatewaySessionResponse syncGatewaySessionResponse = syncGatewaySessionService.createSelfieSession(syncGatewaySessionRequest);
		
		if (syncGatewaySessionResponse.getSessionId() == null) { return null;  }
		
		Session session = new SessionBuilder()
				.setId(IDUtils.generateBase64GUID())
                .setUserId(sr.getUserId())
                .setAuthType(sr.getAuthType())
                .setToken(null)
                .setExpires(syncGatewaySessionResponse.getExpires())
                .setSessionId(syncGatewaySessionResponse.getSessionId())
                .createSession();
		
		sessionRepository.save(session);
		return syncGatewaySessionResponse; 
			
	}

	@Override
	@RequestMapping(value=SelfieSessionServiceApi.SESSION_SEARCH_PATH, method= RequestMethod.GET)
	public List<Session> findSessions(@RequestParam Map<String, String> options) {
		throw new FeatureNotImplementedException();
	}

	@Override
	@RequestMapping(value=SelfieSessionServiceApi.SESSION_COUNT_PATH, method= RequestMethod.GET)
	public Long getSessionCount() {
		return sessionRepository.count();
	}

	@Override
	@RequestMapping(value=SelfieSessionServiceApi.SESSION_EXISTS_PATH, method= RequestMethod.GET)
	public Boolean sessionExists(@PathVariable String id) {
		return sessionRepository.exists(id);
	}

	@Override
	@RequestMapping(value=SelfieSessionServiceApi.SESSION_RECORD_PATH, method= RequestMethod.GET)
	public Session getSession(@PathVariable String id) {
		return sessionRepository.findOne(id);
	}

	@Override
	@RequestMapping(value=SelfieSessionServiceApi.SESSION_RECORD_PATH, method= RequestMethod.DELETE)
	public Boolean deleteSession(@PathVariable String id) {
		sessionRepository.delete(id);
		return true;
	}

}
