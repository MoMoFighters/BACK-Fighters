package com.wanted.momocity.admin.application.port;

/* comment.
    다른 BC 데이터를 받아오는 것이 아닌 내 admin BC 가 직접 인프라 상태를 체크하는 계약
 */

public interface SystemHealthPort {

    HealthStatus checkAll();

    // 상태 값만 담는 단순한 자료구조라서 record 적합
    record HealthStatus(
            String webService,
            String database,
            String fileStorage,
            String mailService
    ) {}
}
