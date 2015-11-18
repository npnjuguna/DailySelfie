package com.njuguna.dailyselfie.profile.entity;

import com.google.common.base.Objects;
import com.njuguna.dailyselfie.common.Constants;

/**
 * User model class
 */
public class User {

    private String id; // unique system id based on UUID and encoded with base 62 and the numeric number to reduce chances of collision
    private Long uNum; // unique numeric sequential id com
    private String password; // decoy password
    private String username; // username provided by auth provider
    private String email; // email provided by auth provider
    private String telephone; // users telephone. may be provided by auth provider or user
    private String fullname; // full name
    private String googleUserPhotoUrl; // profile photo url
    private Integer authType; // type of authentication provider internal number e.g. google, twitter, facebook, basic
    private String authToken; // token obtained from auth provider
    private String type; // NoSQL doc type
    private String crDate; // date created as Unix Epoch milliseconds
    private Long crTime; // date created in human readable format
    private String upDate; // date updated as Unix Epoch milliseconds
    private Long upTime; // date updated in human readable format

    public User() {
        this.type = Constants.DOC_TYPE_USER;
    }

    public User(String id, Long uNum, String password, String username, String email, String telephone, String fullname, String googleUserPhotoUrl, Integer authType, String authToken, String crDate, Long crTime, String upDate, Long upTime) {
        this.id = id;
        this.uNum = uNum;
        this.password = password;
        this.username = username;
        this.email = email;
        this.telephone = telephone;
        this.fullname = fullname;
        this.googleUserPhotoUrl = googleUserPhotoUrl;
        this.authType = authType;
        this.authToken = authToken;
        this.crDate = crDate;
        this.crTime = crTime;
        this.upDate = upDate;
        this.upTime = upTime;
        this.type = Constants.DOC_TYPE_USER;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", uNum=" + uNum +
                ", password='" + password + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", telephone='" + telephone + '\'' +
                ", fullname='" + fullname + '\'' +
                ", googleUserPhotoUrl='" + googleUserPhotoUrl + '\'' +
                ", authType=" + authType +
                ", authToken='" + authToken + '\'' +
                ", type='" + type + '\'' +
                ", crDate='" + crDate + '\'' +
                ", crTime=" + crTime +
                ", upDate='" + upDate + '\'' +
                ", upTime=" + upTime +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getuNum() {
        return uNum;
    }

    public void setuNum(Long uNum) {
        this.uNum = uNum;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public Integer getAuthType() {
        return authType;
    }

    public void setAuthType(Integer authType) {
        this.authType = authType;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCrDate() {
        return crDate;
    }

    public void setCrDate(String crDate) {
        this.crDate = crDate;
    }

    public Long getCrTime() {
        return crTime;
    }

    public void setCrTime(Long crTime) {
        this.crTime = crTime;
    }

    public String getUpDate() {
        return upDate;
    }

    public void setUpDate(String upDate) {
        this.upDate = upDate;
    }

    public Long getUpTime() {
        return upTime;
    }

    public void setUpTime(Long upTime) {
        this.upTime = upTime;
    }

    public String getGoogleUserPhotoUrl() {
        return googleUserPhotoUrl;
    }

    public void setGoogleUserPhotoUrl(String googleUserPhotoUrl) {
        this.googleUserPhotoUrl = googleUserPhotoUrl;
    }

    /**
     * Two Users will generate the same hashcode if they have exactly the same
     * values for their username and fullname.
     *
     */
    @Override
    public int hashCode() {
        // Google Guava provides great utilities for hashing
        return Objects.hashCode(username, fullname);
    }

    /**
     * Two Users are considered equal if they have exactly the same values for
     * their username and fullname.
     *
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof User) {
            User other = (User) obj;
            // Google Guava provides great utilities for equals too!
            return Objects.equal(username, other.username)
                    && Objects.equal(fullname, other.fullname);
        } else {
            return false;
        }
    }
}