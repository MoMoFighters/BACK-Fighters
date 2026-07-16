package com.wanted.momocity.global.infrastructure.s3;

import com.wanted.momocity.global.application.s3.S3DeletePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3DeleteAdapter implements S3DeletePort {

    // AWS S3에 삭제 및 조회 요청을 보내는 클라이언트
    private final S3Client s3Client;

    // 어플리케이션 설정에 실제 S3 버킷을 주입
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // 전달받은 key에 해당하는 S3 객체 하나 삭제
    @Override
    public void delete(String key) {

        // null 또는 빈 key로 잘못된 삭제 요청이 실행 되는 것을 차단
        validateKey(key);

        try {
            // 삭제할 버킷과 객체 key를 담은 요청을 생성
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            // 단일 객체 삭제 요청 전달
            s3Client.deleteObject(request);

            log.info("[S3] 객체 삭제 완료 - key={}", key);
        } catch (S3Exception exception) {
            throw new IllegalStateException("S3 객체 삭제에 실패했습니다. key={}" + key, exception);
        }
    }

    // 전달받은 prefix로 시작하는 모든 S3 객체를 삭제
    @Override
    public void deleteByPrefix(String prefix) {
        // 삭제할 prefix에 포함된 객체를 조회하는 요청을 생성
        validatePrefix(prefix);

        try{
            // 삭제할 prefix에 포함된 객체를 조회하는 요청을 생성
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(prefix)
                    .build();

            // 살제로 삭제한 객체 개수를 기록하기 위한 변수
            int deletedCount = 0;

            // 조회 결과가 여러 페이지인 경우에도 모든 페이지를 순회
            for (var page : s3Client.listObjectsV2Paginator(listRequest)) {
                // 조회된 S3 객체들을 일괄 삭제용 식별자로 변환
                List<ObjectIdentifier > objectIdentifiers = page.contents()
                        .stream()
                        .map(s3Object -> ObjectIdentifier.builder()
                                // 조회된 객체의 key를 삭제 대상으로 지정
                                .key(s3Object.key())
                                // 삭제 대상 식별자 생성을 완료
                                .build())
                        .toList();

                // 현재 페이지에 삭제할 객체가 없으면 다음 페이지로 넘어감
                if (objectIdentifiers.isEmpty()) {
                    continue;
                }

                // 현재 페이지에서 조회된 객체들을 한 번에 삭제할 요청을 생성
                DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                        .bucket(bucket)
                        .delete(Delete.builder()
                                // 삭제할 객체 key 목록
                                .objects(objectIdentifiers)
                                // 성공 객체별 응답을 생략하여 응답 크기를 줄입니다.
                                .quiet(true)
                                // 일괄 삭제 정보 완성
                                .build())
                        .build();

                // AWS S3에 삭제 요청
                s3Client.deleteObjects(deleteRequest);

                // 삭제 요청한 객체 개수를 누적
                // +=는 기존 값에 오른쪽 값을 더한 뒤 다시 저장하는 연산자
                // deletedCount = deletedCount + objectIdentifiers.size();
                deletedCount += objectIdentifiers.size();
            }
            log.info(
                    "[S3] prefix 객체 삭제 완료 - prefix={}, deletedCount={}",
                    prefix,
                    deletedCount
            );
        } catch (S3Exception exception) {
            throw new IllegalStateException("S3 prefix 삭제에 실패했습니다. prefix=" + prefix, exception);
        }
    }

    // 단일 객체에 사용되는 S3 객체 key를 검증
    private void validateKey(String key) {
        if (key== null || key.isBlank()) {
            throw new IllegalArgumentException("삭제할 S3 객체 key는 필수입니다.");
        }
    }

    // 여러 객체 삭제에 사용되는 prefix 검증
    private void validatePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("삭제할 S3 prefix는 필수입니다.");
        }
    }
}
