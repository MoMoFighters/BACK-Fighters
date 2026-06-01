package com.wanted.momocity.user.domain.repository;

import com.wanted.momocity.user.application.command.UpdateUserInfoCommand;
import com.wanted.momocity.user.domain.model.Role;
import com.wanted.momocity.user.domain.model.Status;
import com.wanted.momocity.user.domain.model.TeacherApplication;
import com.wanted.momocity.user.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    // 유저 아이디로 유저 찾기 - 마이페이지용
    Optional<User> findById(Long userId);

    // 닉네임 등록
    void registerNickname(Long aLong, String nickname);

    // 동일한 닉네임 있는지 확인
    boolean existsByNickname(String nickname);

    // 사용자 정보 수정
    void updateUserInfo(UpdateUserInfoCommand command);

    // 임시비밀번호 죽이기 - 임시비번으로 로그인 해서 비번 바꾸면 is_tempPwd false로 변경
    void clearTempPwd(Long userId);

    // id로 기존 비밀번호 찾기
    String findPasswordById(Long aLong);

    // 강사 신청자 목록 조회용 - role 이 티처이고 상태가 팬딩인 사람
    List<TeacherApplication> findByRoleAndStatus(Role role, Status status, int page, int size);

    // 대기 강사 상세 조회
    Optional<TeacherApplication> findTeacherApplicationById(Long userId);

    // 조건에 해당하는 유저가 총 몇 명인지 숫자만 가져오는 것
    long countByRoleAndStatus(Role role, Status status);

    // 변경된 user를 db에 저장
    void save(User user);

    // 강사 승인 여부에 따른 status 업데이트
    void updateRoleAndStatus(Long userId, Role role, Status status);

    // 관리자 회원관리에서 사용자 조회
    List<User> findAllForAdmin(Role role, Status status, int page, int size);

    long countForAdmin(Role role, Status status);

}
