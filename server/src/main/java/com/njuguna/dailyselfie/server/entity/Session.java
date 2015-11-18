package com.njuguna.dailyselfie.server.entity;

import com.couchbase.client.java.repository.annotation.Field;
import com.njuguna.dailyselfie.common.Constants;

import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

@Document
public class Session {

    @Id
    private String id;

    @Field
    private String userId;

    @Field
    private Integer authType;

    @Field
    private String token;

    @Field
    private String expires;
    
    @Field
    private String sessionId;
    
    @Field
    private String type;
    
    public Session() {}

    public Session(String id, String userId, Integer authType, String token, String expires, String sessionId) {
        this.id = id;
        this.userId = userId;
        this.authType = authType;
        this.token = token;
        this.expires = expires;
        this.sessionId = sessionId;
        this.type = Constants.DOC_TYPE_SESSION;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getExpires() {
		return expires;
	}

	public String getToken() {
		return token;
	}

	public Integer getAuthType() {
		return authType;
	}

	public void setAuthType(Integer authType) {
		this.authType = authType;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setExpires(String expires) {
		this.expires = expires;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Session [id=" + id + ", userId=" + userId + ", authType=" + authType + ", token=" + token + ", expires="
				+ expires + ", sessionId=" + sessionId + ", type=" + type + "]";
	}
    
}