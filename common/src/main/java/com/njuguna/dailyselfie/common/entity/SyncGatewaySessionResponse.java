package com.njuguna.dailyselfie.common.entity;

import com.google.gson.annotations.SerializedName;

public class SyncGatewaySessionResponse {
	
	@SerializedName("session_id")
	private String sessionId;

	private String expires;

	@SerializedName("cookie_name")
	private String cookieName;
	
	public SyncGatewaySessionResponse() {
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getExpires() {
		return expires;
	}
	public void setExpires(String expires) {
		this.expires = expires;
	}
	public String getCookieName() {
		return cookieName;
	}
	public void setCookieName(String cookieName) {
		this.cookieName = cookieName;
	}

}
