package com.wanted.momocity.enrollment.presentation.api;

import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/enrollments")
public class EnrollmentProgressController {

    // 강의 진척도 (건물)
    @GetMapping("/progress")
    @PreAuthorize("hasAuthority('Role_STUDENT')")
    public ResponseEntity<?> getProgress(
            Authorization authorization,
            @RequestParam(required = false) String category
    ) {
        // 로그인한 userId를 꺼낸다.
        Long userId = Long.parseLong(authorization.getName());

        return null;
    }
}
