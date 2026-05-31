package com.wanted.momocity.viewing.application.port;

import com.wanted.momocity.viewing.domain.model.Chapter;

import java.util.List;
import java.util.Optional;

/*
* comment.
*  catalog 컨텍스트 소유의 Chapter 를 READ 전용으로 조회
*  viewing 컨텍스트가 catalog 컨텍스트를 직접 참조하지 않고 해당 포트를 통해서 접근함
*  실제 구현체 : infrastructure.caltalog.ChapterCatalogAdater 가 담당
* */

public interface ChapterPort {

    // 단건 챕터 조회
    Chapter findById (Long chapterId);

    // 강의의 전체 챕터 조회 목록
    // totalProgress 계산 시 durationSec 합산에 사용
    List<Chapter> findAllByLectureId (Long lectureId);

    // 이전 챕터 조회용
    Optional<Chapter> findByLectureIdAndOrderNo(Long lectureId, int orderNo);

}
