package com.wanted.momocity.community.presentation.api.common;

/*
* comment.
*  community 컨텍스트 전용 API 응답 코드 상수
*  COMMUNITY-*
* */

public class CommunityResponseCode {

    private CommunityResponseCode() {}

    // 게시글
    public static final String POST_CREATED          = "COMMUNITY-POST-CREATED";
    public static final String IMAGE_UPLOADED        = "COMMUNITY-IMAGE-UPLOADED";
    public static final String POST_CONTENT_UPLOADED = "COMMUNITY-POST-CONTENT-UPLOADED";
    public static final String POST_LIST_FOUND       = "COMMUNITY-POST-LIST-FOUND";
    public static final String POST_FOUND            = "COMMUNITY-POST-FOUND";
    public static final String POST_UPDATED          = "COMMUNITY-POST-UPDATED";
    public static final String POST_CONTENT_UPDATED  = "COMMUNITY-POST-CONTENT-UPDATED";
    public static final String POST_DELETED          = "COMMUNITY-POST-DELETED";
    public static final String POST_SEARCH_FOUND     = "COMMUNITY-POST-SEARCH-FOUND";
    public static final String POST_RECOMMENDATIONS_FOUND = "COMMUNITY-POST-RECOMMENDATIONS-FOUND";

    // 좋아요
    public static final String LIKE_CREATED          = "COMMUNITY-LIKE-CREATED";
    public static final String LIKE_LIST_FOUND       = "COMMUNITY-LIKE-LIST-FOUND";
    public static final String LIKE_DELETED          = "COMMUNITY-LIKE-DELETED";

    // 댓글
    public static final String COMMENT_CREATED       = "COMMUNITY-COMMENT-CREATED";
    public static final String COMMENT_DELETED       = "COMMUNITY-COMMENT-DELETED";
    public static final String COMMENT_FOUND         = "COMMUNITY-COMMENT-FOUND";

    // 대댓글
    public static final String REPLY_CREATED         = "COMMUNITY-REPLY-CREATED";
    public static final String REPLY_DELETED         = "COMMUNITY-REPLY-DELETED";

}
