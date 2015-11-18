package com.njuguna.dailyselfie.common.entity;

public class SessionRequest {
    private String userId;
    private String password;
    private Integer authType;
    private String token;
    private Long ts;

    public SessionRequest() {
    }

    public SessionRequest(String userId, Integer authType, String token) {
        this.userId = userId;
        this.authType = authType;
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getAuthType() {
        return authType;
    }

    public void setAuthType(Integer authType) {
        this.authType = authType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "SessionRequest [userId=" + userId + ", password=" + password + ", authType=" + authType + ", token="
				+ token + ", ts=" + ts + "]";
	}
}