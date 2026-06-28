package com.wanted.momocity.global.domain.model;

public enum Category {

    FITNESS("https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/FITNESSProfile.png"),
    STUDY  ("https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/STUDYProfile.png"),
    COOK   ("https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/COOKProfile.png"),
    BEAUTY ("https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/BEAUTYProfile.png"),
    ART    ("https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/ARTProfile.png");

    private final String categoryProfileImage;

    Category(String categoryProfileImage) {
        this.categoryProfileImage = categoryProfileImage;
    }

    public String getCategoryProfileImage() {
        return categoryProfileImage;
    }
}


