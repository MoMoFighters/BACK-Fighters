package com.wanted.momocity.friend.fmexception;



public record FMErrorResponse(
        boolean success,
        int statusCode,
        String message
) {
    public static FMErrorResponse of(int statusCode, String message) {
        return new FMErrorResponse(false, statusCode, message);
    }
}
