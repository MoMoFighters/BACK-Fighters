package com.wanted.momocity.auth.presentation.api.response;

public final class AuthResponseCode {

    private AuthResponseCode(){}

    public static final String CREATED = "USER-CREATED";
    public static final String SUCCESS = "SUCCESS";

    public static final String EMAIL_VALIDATION_ERROR = "USER-EMAIL_VALIDATION_ERROR";
    public static final String PASSWORD_VALIDATION_ERROR = "USER-PASSWORD_VALIDATION_ERROR";
    public static final String EMAIL_DUPLICATE = "USER-EMAIL_DUPLICATE";


}
