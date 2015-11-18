package com.njuguna.dailyselfie.profile.entity;

import com.njuguna.dailyselfie.common.Constants;

public class UserMap {

    private String id;
    private String userId;
    private String type;
    
    public UserMap() {}

    public UserMap(String id, String userId) {
        this.id = id;
        this.userId = userId;
        this.type = Constants.DOC_TYPE_USER_MAP;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "UserMap [id=" + id + ", userId=" + userId + ", type=" + type + "]";
	}
	
}