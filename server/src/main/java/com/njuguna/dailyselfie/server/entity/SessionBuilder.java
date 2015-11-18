package com.njuguna.dailyselfie.server.entity;

public class SessionBuilder {
    private String id;
    private String userId;
    private Integer authType;
    private String token;
    private String expires;
    private String sessionId;

    public SessionBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public SessionBuilder setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public SessionBuilder setAuthType(Integer authType) {
        this.authType = authType;
        return this;
    }

    public SessionBuilder setToken(String token) {
        this.token = token;
        return this;
    }

    public SessionBuilder setExpires(String expires) {
        this.expires = expires;
        return this;
    }

    public SessionBuilder setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public Session createSession() {
        return new Session(id, userId, authType, token, expires, sessionId);
    }
}