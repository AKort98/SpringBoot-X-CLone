package com.twitter.XClone.api.model;

public class LoginResponseBody {
    private String jwtToken;
    private boolean success;
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getJWTToken() {
        return jwtToken;
    }

    public void setJWTToken(String token) {
        this.jwtToken = token;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
