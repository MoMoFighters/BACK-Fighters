package com.wanted.momocity.lecture.application.service; // lecture application service 패키지 선언

import org.springframework.beans.factory.annotation.Value; // application.yml 값을 주입받기 위한 import
import org.springframework.stereotype.Component; // Spring Bean으로 등록하기 위한 import

@Component // Spring Bean으로 등록해서 LectureQueryService 등에서 주입받을 수 있게 함
public class LectureS3UrlResolver { // lecture 응답에 필요한 S3 URL 변환 전용 컴포넌트

    private final String bucket; // S3 버킷명

    private final String region; // S3 리전

    public LectureS3UrlResolver( // 생성자를 통해 yml 설정값을 주입받음

                                 @Value("${cloud.aws.s3.bucket}") String bucket, // application.yml의 cloud.aws.s3.bucket 값 주입

                                 @Value("${cloud.aws.s3.region}") String region // application.yml의 cloud.aws.s3.region 값 주입

    ) {

        this.bucket = bucket; // 주입받은 bucket 값을 필드에 저장

        this.region = region; // 주입받은 region 값을 필드에 저장

    }

    public String toUrl(String key) { // DB에 저장된 S3 key를 프론트 응답용 전체 URL로 변환하는 메서드

        if (key == null || key.isBlank()) { // key가 null이거나 빈 문자열이면

            return null; // 변환할 값이 없으므로 null 반환

        }

        if (key.startsWith("http://") || key.startsWith("https://")) { // 이미 전체 URL로 저장된 값이면

            return key; // 중복으로 URL을 붙이지 않고 그대로 반환

        }

        return "https://" + bucket // https://momocity-bucket
                + ".s3." + region // https://momocity-bucket.s3.ap-northeast-2
                + ".amazonaws.com/" // https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/
                + key; // 최종 S3 key를 붙여 전체 URL 생성

    }
}