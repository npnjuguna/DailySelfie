package com.njuguna.dailyselfie.profile.entity;

public class UserBuilder {
    private String id;
    private Long uNum;
    private String password;
    private String username;
    private String email;
    private String telephone;
    private String fullname;
    private String googleUserPhotoUrl;
    private Integer authType;
    private String authToken;
    private String crDate;
    private Long crTime;
    private String upDate;
    private Long upTime;

    public UserBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public UserBuilder setuNum(Long uNum) {
        this.uNum = uNum;
        return this;
    }

    public UserBuilder setPassword(String password) {
        this.password = password;
        return this;
    }

    public UserBuilder setUsername(String username) {
        this.username = username;
        return this;
    }

    public UserBuilder setEmail(String email) {
        this.email = email;
        return this;
    }

    public UserBuilder setTelephone(String telephone) {
        this.telephone = telephone;
        return this;
    }

    public UserBuilder setFullname(String fullname) {
        this.fullname = fullname;
        return this;
    }

    public UserBuilder setGoogleUserPhotoUrl(String googleUserPhotoUrl) {
        this.googleUserPhotoUrl = googleUserPhotoUrl;
        return this;
    }

    public UserBuilder setAuthType(Integer authType) {
        this.authType = authType;
        return this;
    }

    public UserBuilder setAuthToken(String authToken) {
        this.authToken = authToken;
        return this;
    }

    public UserBuilder setCrDate(String crDate) {
        this.crDate = crDate;
        return this;
    }

    public UserBuilder setCrTime(Long crTime) {
        this.crTime = crTime;
        return this;
    }

    public UserBuilder setUpDate(String upDate) {
        this.upDate = upDate;
        return this;
    }

    public UserBuilder setUpTime(Long upTime) {
        this.upTime = upTime;
        return this;
    }

    public User createUser() {
        return new User(id, uNum, password, username, email, telephone, fullname, googleUserPhotoUrl, authType, authToken, crDate, crTime, upDate, upTime);
    }
}