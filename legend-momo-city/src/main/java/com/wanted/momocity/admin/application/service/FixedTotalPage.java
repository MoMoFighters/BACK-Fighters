package com.wanted.momocity.admin.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/* comment.
    페이지네이션에 필요한 정보(목록, 페이지 번호, 전체 공지 개수) 를 우리가 넣어준 그대로
    돌려주기만 하는 클래스이다.
 */

// <T> 는 이 상자 안에 어떤걸 담을지는 나중에 정한다라는 뜻
// AdminNotice 를 담을 예정이라, 실제로 쓸 때는 AdminNotice 가 들어감
public class FixedTotalPage<T> implements Page<T> {

    private final List<T> content;
    private final Pageable pageable;
    private final long total;

    public FixedTotalPage(List<T> content, Pageable pageable, long total) {
        this.content = content;
        this.pageable = pageable;
        this.total = total;
    }

    // 전체 페이지는 몇 개인지 알기 위한 구문
    @Override
    public int getTotalPages() {
        return total == 0 ? 1 : (int) Math.ceil((double) total / (double) pageable.getPageSize());
    }

    // 전체 항목이 몇 개인지 알기 위한 구문
    @Override
    public long getTotalElements() {
        return total;
    }

    // 이 상자 안 내용물을 다른 타입으로 바꿔서 새 상자 만들기
    // AdminNotice -> 응답 DTO
    @Override
    public <U> Page<U> map(Function<? super T, ? extends U> converter) {
        return new FixedTotalPage<>(content.stream().map(converter).collect(Collectors.toList()), pageable, total);
    }

    // 지금 몇 번째 페이지인지 알기 위한 구문
    @Override
    public int getNumber() { return pageable.getPageNumber(); }

    // 한 페이지에 얼마나 담는지 알기 위한 구문
    @Override
    public int getSize() { return pageable.getPageSize(); }

    // 현재 이 페이지에 몇 개의 공지가 들어있는지 알기 위한 구문
    @Override
    public int getNumberOfElements() { return content.size(); }

    // 이 페이지의 실제 목록들을 알기 위한 구문
    @Override
    public List<T> getContent() { return content; }

    // 이 페이지에 내용이 있는지 확인하기 위한 구문
    @Override
    public boolean hasContent() { return !content.isEmpty(); }

    // 정렬 기준이 뭔지 알기 위한 구문
    @Override
    public Sort getSort() { return pageable.getSort(); }

    // 이게 첫 페이지인지 알기 위한 구문
    @Override
    public boolean isFirst() { return !hasPrevious(); }

    // 마지막 페이지인지 알기 위한 구문
    @Override
    public boolean isLast() { return !hasNext(); }

    // 다음 페이지가 있는지 알기 위한 구문
    @Override
    public boolean hasNext() { return getNumber() + 1 < getTotalPages(); }

    // 이전 페이지가 있는지 확인하기 위한 구문
    @Override
    public boolean hasPrevious() { return getNumber() > 0; }

    // 다음 페이지 요청하려면 어떤걸 보내야하는지 알기 위한 구문
    @Override
    public Pageable nextPageable() { return hasNext() ? pageable.next() : Pageable.unpaged(); }

    // 이전 페이지 요청하려면 어떤걸 보내야하는지 알기 위한 구문
    @Override
    public Pageable previousPageable() { return hasPrevious() ? pageable.previousOrFirst() : Pageable.unpaged(); }

    // 이 목록을 for 문 같은 걸로 하나씩 돌 수 있게 만드는 구문
    @Override
    public Iterator<T> iterator() { return content.iterator(); }
}


