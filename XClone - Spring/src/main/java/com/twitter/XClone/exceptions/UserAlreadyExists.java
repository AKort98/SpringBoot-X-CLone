package com.twitter.XClone.exceptions;

public class UserAlreadyExists extends Exception{
    private String message;

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
