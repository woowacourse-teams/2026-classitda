package com.classitda.authentication.domain;

public enum TokenUse {

    SIGNUP("SIGNUP"),
    ACCESS("MEMBER");

    private final String authority;

    TokenUse(String authority) {
        this.authority = authority;
    }

    public String authority() {
        return authority;
    }
}
