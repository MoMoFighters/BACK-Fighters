package com.wanted.momocity.global.domain.profile;

public final class Profile {

    private Profile(){}

    public static final String DEFAULT_PROFILE_IMAGE_URL =
            "https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png";
    public static final String DEFAULT_PROFILE_ITEM_NAME = "기본 프로필";
    public static final Long DEFAULT_PROFILE_ITEM_ID = 0L; // store 상품이 아님을 나타냄
}