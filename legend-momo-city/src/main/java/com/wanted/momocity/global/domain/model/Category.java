package com.wanted.momocity.global.domain.model;

public enum Category {

    FITNESS("https://s3.ap-northeast-2.amazonaws.com/버킷명/default/teacher-fitness.png"),
    STUDY  ("https://s3.ap-northeast-2.amazonaws.com/버킷명/default/teacher-study.png"),
    COOK   ("https://s3.ap-northeast-2.amazonaws.com/버킷명/default/teacher-cook.png"),
    BEAUTY ("https://s3.ap-northeast-2.amazonaws.com/버킷명/default/teacher-beauty.png"),
    ART    ("https://s3.ap-northeast-2.amazonaws.com/버킷명/default/teacher-art.png");

    private final String categoryProfileImage;

    Category(String categoryProfileImage) {
        this.categoryProfileImage = categoryProfileImage;
    }

    public String getCategoryProfileImage() {
        return categoryProfileImage;
    }
}


