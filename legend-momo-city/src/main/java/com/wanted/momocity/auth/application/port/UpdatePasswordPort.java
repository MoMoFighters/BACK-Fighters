package com.wanted.momocity.auth.application.port;

public interface UpdatePasswordPort {
    void updatePassword(String email, String encodedPassword);

}
