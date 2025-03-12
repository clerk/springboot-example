package com.clerk.springbootexample.schema.response;

public class VerifiedJwtResponse {

    private String userId;

    public VerifiedJwtResponse() {
    }

    public VerifiedJwtResponse(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
