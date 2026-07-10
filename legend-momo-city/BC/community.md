# community BC

## 1. 개요

community 바운디드 컨텍스트는 게시글(post), 게시글 콘텐츠(post_content), 좋아요(post_like), 댓글/대댓글(comment)을 관리하는 커뮤니티 기능을 담당한다. 게시글 작성/수정/삭제, 이미지 업로드, 목록/검색/추천 조회, 댓글과 대댓글, 좋아요, 마이페이지·상대방 페이지·대시보드 통계까지 게시판 성격의 기능 전체를 이 BC에서 처리한다.

## 2. 패키지 구조

### domain
- `domain.model` : `Post`, `PostContent`, `PostCategory`(enum), `PostLike`, `Comment` — 순수 자바 객체, JPA 의존 없음
- `domain.repository` : `PostRepository`, `PostContentRepository`, `PostLikeRepository`, `CommentRepository` — 인터페이스
- `domain.event` : `CommentCreatedEvent`, `ReplyCreatedEvent`, `PostLikedEvent` — record 형태 도메인 이벤트
- `domain.exception` : `CommunityAccessDeniedException`(403), `CommunityNotFoundException`(404)

### application
- `application.post`
  - `command` : `PostContentCommand`
  - `port` : `UserInfoPort` (user BC를 직접 참조하지 않기 위한 포트)
  - `result` : `PostCreateResult`, `PostWithContents`
  - `service` : `PostCommandService`(쓰기), `PostQueryService`(읽기)
  - `usecase` : `PostCommandUseCase`, `PostQueryUseCase`
- `application.like`
  - `result` : `LikeResult`
  - `service` : `LikeCommandService`(쓰기), `LikeQueryService`(읽기)
  - `usecase` : `LikeCommandUseCase`, `LikeQueryUseCase`
- `application.comment`
  - `service` : `CommentCommandService`(쓰기), `CommentQueryService`(읽기)
  - `usecase` : `CommentCommandUseCase`, `CommentQueryUseCase`

### infrastructure
- `adapter` : `PostRepositoryAdapter`, `PostContentRepositoryAdapter`, `PostLikeRepositoryAdapter`, `CommentRepositoryAdapter` — domain repository 인터페이스 구현체. `PostStatsAdapter`(admin BC가 선언한 `PostStatsPort` 구현, 월별 게시글 통계 제공), `CommentContentAdapter`(report BC가 선언한 `CommentContentPort` 구현, 댓글 텍스트 제공)
- `catalog` : `UserInfoAdapter` — `UserInfoPort` 구현체, auth BC의 `LoadUserPort`를 통해 사용자 정보 조회
- `config` : `CommunityRedisCacheConfig` — 게시글 목록(`PostListResponse`) 전용 Redis 캐시 설정(Jackson2JsonRedisSerializer, TTL 1시간)
- `metrics` : `CommunityMetrics` — Micrometer 기반 카운터(게시글 작성, 좋아요, 이미지 업로드 실패) 및 타이머(이미지 업로드, 목록 조회, 검색) 등록/조회
- `persistence` : `PostJpaEntity`/`PostJpaRepository`, `PostContentJpaEntity`/`PostContentJpaRepository`, `PostLikeJpaEntity`/`PostLikeJpaRepository`, `CommentJpaEntity`/`CommentJpaRepository`
- `scheduler` : `CommunityCleanupScheduler` — 매일 자정 소프트딜리트 6개월 경과 데이터(comment → post 순) 하드딜리트

### presentation
- `api` : `PostController`, `LikeController`, `CommentController`
- `api.common` : `CommunityExceptionHandler`(403/404 전용 예외 처리), `CommunityResponseCode`(응답 코드 상수)
- `api.request` : `CreateCommentRequest`, `CreatePostRequest`, `UpdatePostRequest`, `UploadContentsRequest`
- `api.response` : `CommentCreateResponse`, `CommentResponse`, `DashboardResponse`, `LikeResponse`, `PostCommentResponse`, `PostContentResponse`, `PostCreateResponse`, `PostDetailResponse`, `PostLikeListResponse`, `PostListResponse`, `PostRecommendationResponse`, `PostReplyResponse`, `ReplyCreateResponse`, `ReplyResponse`, `UserPostListResponse`

## 3. 진행 상태

### 구현되어 있는 기능
- 게시글 생성/수정(제목·카테고리)/삭제(소프트딜리트), 콘텐츠 업로드·수정(TEXT/IMAGE 타입, 이미지 최대 5장), 썸네일 자동/수동 결정 로직
- 이미지 업로드(S3 업로드 후 CloudFront URL 변환)
- 게시글 목록 조회(카테고리 필터 + 커서 페이지네이션, Redis 캐시 적용), 단건 조회(콘텐츠 fetch join, 조회수 비동기 증가)
- 게시글 키워드 검색(제목+콘텐츠, LIKE 와일드카드 이스케이프 처리)
- 연관 게시글 추천(같은 카테고리 인기글 3개 + 같은 작성자 최신글 2개)
- 마이페이지/상대방 페이지 게시글 목록, 대시보드(게시글 수·조회수·좋아요 수·댓글 수 통계)
- 좋아요/좋아요 취소(중복 방지, 게시글 좋아요 수 증감), 좋아요 누른 사용자 목록 조회
- 댓글/대댓글 작성·삭제(소프트딜리트), 댓글 목록(대댓글 포함 fetch, N+1 개선) 및 대댓글 목록 커서 페이지네이션 조회
- 댓글/좋아요 작성 시 알림용 도메인 이벤트 발행(`CommentCreatedEvent`, `ReplyCreatedEvent`, `PostLikedEvent`) — 본인 게시글/본인 작성물에 대한 액션은 이벤트 발행 제외
- 소프트딜리트 데이터 6개월 경과 시 하드딜리트 스케줄러
- admin BC(`PostStatsPort`)·report BC(`CommentContentPort`)에 대한 포트 구현 제공 (community가 다른 BC의 필요를 어댑터로 충족)

### 비어있거나 미완성으로 보이는 부분
- `PostContentCommand`에 `orderNo` 필드가 주석 처리되어 있음 (`//int orderNo,`) — orderNo는 서비스 계층에서 인덱스 기반으로 자동 부여되는 방식으로 대체된 것으로 보임 (확인 필요)
- `DashboardResponse`에 `MostViewedPost` 관련 필드/레코드가 전부 주석 처리되어 있음 — "가장 많이 조회된 게시글" 기능이 설계되었다가 보류된 것으로 추정 (확인 필요)
- `application.comment` 패키지에는 `command`/`result`/`port` 하위 패키지가 없음 (post/like와 달리 커맨드·결과 객체를 따로 두지 않고 서비스가 원시값을 직접 받고 presentation response를 직접 반환)
- `CommunityMetrics`에 `imageUploadTimer`, `imageUploadFailedCounter`, `postCreatedCounter`, `postLikedCounter`가 정의·등록되어 있지만, 실제로 `PostCommandService`/`LikeCommandService` 코드 내에서 `recordPostCreated()`, `recordPostLiked()`, `recordImageUploadFailed()`, `getImageUploadTimer()` 호출부는 발견되지 않음 — 메트릭 등록만 되어 있고 실제 기록 호출은 목록/검색 쿼리 타이머(`getPostListQueryTimer`, `getPostSearchQueryTimer`)만 사용 중 (확인 필요)
- `CommentContentAdapter.java`에 동일한 import가 주석 처리된 채 중복으로 남아있음 (`//import ...CommentContentPort;` 바로 아래 동일 import 재선언) — 정리되지 않은 코드로 보임

### TODO/FIXME 주석
grep 결과 community 패키지 내에는 TODO/FIXME 주석이 존재하지 않음.

## 4. API 목록

기준 URL prefix: `/api/v2/posts` (PostController, LikeController, CommentController 공통)

| Method | URL | 컨트롤러.메서드명 | 설명 |
|---|---|---|---|
| POST | /api/v2/posts | PostController.createPost | 제목과 카테고리로 게시글을 생성합니다. |
| POST | /api/v2/posts/images | PostController.uploadImage | 게시글 이미지를 S3에 업로드하고 CloudFront URL을 반환합니다. |
| POST | /api/v2/posts/{postId}/contents | PostController.uploadContents | 게시글 콘텐츠를 업로드합니다. |
| GET | /api/v2/posts | PostController.getPosts | 게시글 목록을 조회합니다. |
| GET | /api/v2/posts/{postId} | PostController.getPost | 게시글을 조회합니다. |
| PATCH | /api/v2/posts/{postId} | PostController.updatePost | 게시글 제목과 카테고리를 수정합니다. |
| PUT | /api/v2/posts/{postId}/contents | PostController.updateContents | 게시글 콘텐츠를 전체 교체합니다. |
| DELETE | /api/v2/posts/{postId} | PostController.deletePost | 게시글을 삭제합니다. |
| GET | /api/v2/posts/me | PostController.getMyPosts | 내 게시글 목록을 조회합니다. |
| GET | /api/v2/posts/users/{targetUserId} | PostController.getUserPosts | 상대방 게시글 목록을 조회합니다. |
| GET | /api/v2/posts/me/dashboard | PostController.getDashboard | 내 게시글 통계를 조회합니다. |
| GET | /api/v2/posts/users/{targetUserId}/dashboard | PostController.getUserDashboard | 상대방 게시글 통계를 조회합니다. |
| GET | /api/v2/posts/search | PostController.searchPosts | 키워드로 게시글을 검색합니다. |
| GET | /api/v2/posts/{postId}/recommendations | PostController.getRecommendations | 같은 카테고리 인기글 + 작성자의 다른 글을 추천합니다. |
| POST | /api/v2/posts/{postId}/likes | LikeController.likePost | 게시글에 좋아요를 누릅니다. |
| DELETE | /api/v2/posts/{postId}/likes | LikeController.unlikePost | 게시글 좋아요를 취소합니다. |
| GET | /api/v2/posts/{postId}/likes | LikeController.getLikes | 게시글에 좋아요를 누른 사용자 목록을 조회합니다. |
| POST | /api/v2/posts/{postId}/comments | CommentController.createComment | 게시글에 댓글을 작성합니다. |
| DELETE | /api/v2/posts/{postId}/comments/{commentId} | CommentController.deleteComment | 댓글을 삭제합니다. |
| POST | /api/v2/posts/{postId}/comments/{commentId}/replies | CommentController.createReply | 댓글에 대댓글을 작성합니다. |
| DELETE | /api/v2/posts/{postId}/comments/{commentId}/replies/{replyId} | CommentController.deleteReply | 대댓글을 삭제합니다. |
| GET | /api/v2/posts/{postId}/comments | CommentController.getComments | 게시글 댓글 목록을 조회합니다. |
| GET | /api/v2/posts/{postId}/comments/{commentId}/replies | CommentController.getReplies | 댓글의 대댓글 목록을 조회합니다. |

## 5. 도메인 모델

### Post
- 필드 : `id`, `userId`, `title`, `category`(PostCategory), `thumbnailUrl`, `viewCount`, `likeCount`, `createdAt`, `updatedAt`, `deletedAt`
- 비즈니스 메서드
  - `create(userId, title, category, thumbnailUrl)` : 신규 생성용 정적 팩토리 (viewCount/likeCount 0으로 초기화)
  - `reconstitute(...)` : DB 복원용 정적 팩토리
  - `update(title, category)` : 제목/카테고리 수정
  - `updateThumbnail(thumbnailUrl)` : 썸네일 수정
  - `delete()` : `deletedAt`을 현재 시각으로 설정하는 소프트딜리트
  - `isDeleted()` : `deletedAt != null` 여부 반환
  - `increaseViewCount()` : 조회수 1 증가
  - `increaseLikeCount()` / `decreaseLikeCount()` : 좋아요 수 증가/감소 (감소 시 0 미만으로 내려가지 않도록 가드)

### PostCategory (enum)
- 값 : `FITNESS`, `STUDY`, `COOK`, `BEAUTY`, `ART`, `FREE`

### PostContent
- 내부 enum `Type` : `TEXT`, `IMAGE`
- 필드 : `id`, `postId`, `orderNo`, `type`, `content`, `imageUrl`, `createdAt`
- 비즈니스 메서드 : `create(...)`(신규 생성), `reconstitute(...)`(DB 복원). TEXT 타입은 `content`에 텍스트, IMAGE 타입은 `imageUrl`에 S3 URL을 저장하는 구조이며, 그 외 별도 비즈니스 로직 메서드는 없음(getter만 존재)

### PostLike
- 필드 : `id`, `postId`, `userId`, `createdAt`
- `(post_id, user_id)` UNIQUE 제약으로 중복 좋아요 방지 (DB 제약 + 서비스 계층 사전 검증 병행)
- 비즈니스 메서드 : `create(postId, userId)`, `reconstitute(...)` 외 별도 로직 없음(getter만 존재)

### Comment
- 필드 : `id`, `postId`, `userId`, `parentId`, `content`, `createdAt`, `deletedAt`
- `parentId == null` → 댓글, `parentId != null` → 대댓글
- 비즈니스 메서드
  - `create(postId, userId, content)` : 댓글 생성
  - `createReply(postId, userId, parentId, content)` : 대댓글 생성
  - `reconstitute(...)` : DB 복원
  - `delete()` : 소프트딜리트 (`deletedAt` 설정)
  - `isDeleted()` : 삭제 여부 확인
  - `isReply()` : `parentId != null` 여부로 대댓글인지 확인

### 도메인 이벤트 (record)
- `CommentCreatedEvent(postId, postOwnerId, commentUserId, commentUserName)` : 댓글 작성 시 게시글 작성자에게 알릴 정보
- `ReplyCreatedEvent(postId, postOwnerId, parentCommentOwnerId, replyUserId, replyUserName)` : 대댓글 작성 시 게시글 작성자 + 부모 댓글 작성자에게 알릴 정보
- `PostLikedEvent(postId, postOwnerId, likeUserId, likedUserName)` : 좋아요 시 게시글 작성자에게 알릴 정보

### 예외
- `CommunityAccessDeniedException` : 본인 게시글/댓글이 아닐 때 발생하는 403 전용 예외
- `CommunityNotFoundException` : 게시글/댓글/대댓글을 찾을 수 없을 때 발생하는 404 전용 예외

## 6. ERD 스키마 대조

대조 대상 JPA 엔티티: `PostJpaEntity`, `PostContentJpaEntity`, `CommentJpaEntity` (경로: `infrastructure/persistence/`)

### `post` 테이블

| DB 컬럼 | DB 타입 | 대응 JPA 필드 | 비고 |
|---|---|---|---|
| id | BIGINT NOT NULL | `id` (Long, `@Id`) | |
| user_id | BIGINT NULL | `userId` (Long) | DB는 NULL 허용, JPA는 `nullable = false` — 제약 강도 불일치 (확인 필요) |
| title | VARCHAR(200) NULL | `title` (String) | DB는 NULL 허용, JPA는 `nullable = false` — 제약 강도 불일치 |
| post_like | INT NULL DEFAULT 0 | `likeCount` (int, `@Column(name = "post_like")`) | 필드명과 컬럼명 다름(의도된 매핑). DB는 NULL 허용/기본값 0, JPA는 `nullable = false`이고 primitive int라 null 자체가 불가능 — 제약 강도 불일치 |
| view_count | INT NULL | `viewCount` (int) | DB는 NULL 허용, JPA는 `nullable = false` — 제약 강도 불일치 |
| category | ENUM('HEALTH','STUDY','COOK','BEAUTY','ART','FREE') NULL | `category` (PostCategory enum, `@Enumerated(STRING)`) | **enum 값 불일치**: DB는 `HEALTH`, 코드 `PostCategory`는 `FITNESS`로 되어 있음(BC/community.md 5번 섹션 참고). 나머지 STUDY/COOK/BEAUTY/ART/FREE는 동일. `HEALTH` vs `FITNESS` 확인 필요. 또한 DB는 NULL 허용, JPA는 `nullable = false` — 제약 강도도 불일치 |
| thumbnail_url | VARCHAR(500) NULL | `thumbnailUrl` (String) | 일치 |
| created_at | DATETIME NULL | `createdAt` (BaseTimeEntity 상속, `@CreatedDate`, `nullable = false`) | DB는 NULL 허용, JPA는 `nullable = false` — 제약 강도 불일치 |
| updated_at | DATETIME NULL | `updatedAt` (BaseTimeEntity 상속, `@LastModifiedDate`, `nullable = false`) | DB는 NULL 허용, JPA는 `nullable = false` — 제약 강도 불일치 |
| deleted_at | DATETIME NULL | `deletedAt` (LocalDateTime) | 일치 |

DB에 없는 JPA 필드: `contents` (`@OneToMany` 연관관계 필드, 실제 컬럼 아님 — 매핑 정상)

### `post_content` 테이블

| DB 컬럼 | DB 타입 | 대응 JPA 필드 | 비고 |
|---|---|---|---|
| id | BIGINT NOT NULL | `id` (Long, `@Id`) | |
| post_id | BIGINT NULL | `postId` (Long) | DB는 NULL 허용, JPA는 `nullable = false` — 제약 강도 불일치 |
| order_no | TINYINT NULL | `orderNo` (int) | **이전 조사에서 언급된 미완성 부분은 여기가 아니라 application 계층의 `PostContentCommand`임.** `PostContentJpaEntity`와 도메인 모델 `PostContent`에는 `orderNo` 필드가 정상적으로 구현되어 있고 getter/생성 로직도 존재함. 반면 `application/post/command/PostContentCommand.java` 13~14번째 줄에서 `//int orderNo,`로 주석 처리되어 있어, 콘텐츠 업로드/수정 API 요청 단계에서는 클라이언트가 orderNo를 지정할 수 없고 서비스 계층에서 리스트 인덱스 기반으로 자동 채워지는 구조로 보임(확인 필요). 즉 DB·엔티티·도메인은 완성 상태, 커맨드 DTO만 비활성화된 상태. DB는 NULL 허용, JPA는 `nullable = false`인 것도 별도 불일치 |
| type | ENUM('TEXT','IMAGE') NOT NULL | `type` (PostContent.Type enum, `@Enumerated(STRING)`, `nullable = false`) | 일치 |
| image_url | VARCHAR(500) NULL | `imageUrl` (String) | 일치 |
| content | TEXT NULL | `content` (String) | 일치 |
| created_at | DATETIME NULL | `createdAt` (LocalDateTime, `@PrePersist`로 직접 세팅, `nullable = false`) | DB는 NULL 허용, JPA는 `nullable = false` — 제약 강도 불일치 |

DB에 없는 JPA 필드: `post` (`@ManyToOne`, `insertable = false, updatable = false`로 post_id 컬럼과 중복 관리 방지 처리됨 — 매핑 정상)

### `comment` 테이블

| DB 컬럼 | DB 타입 | 대응 JPA 필드 | 비고 |
|---|---|---|---|
| id | BIGINT NOT NULL | `id` (Long, `@Id`) | |
| post_id | BIGINT NULL | `postId` (Long) | DB는 NULL 허용, JPA는 `nullable = false` — 제약 강도 불일치 |
| user_id | BIGINT NULL | `userId` (Long) | DB는 NULL 허용, JPA는 `nullable = false` — 제약 강도 불일치 |
| parent_id | BIGINT NULL | `parentId` (Long) | 일치 |
| content | VARCHAR(500) NULL | `content` (String) | DB는 NULL 허용, JPA는 `nullable = false` — 제약 강도 불일치 |
| created_at | DATETIME NULL | `createdAt` (BaseTimeEntity 상속, `@CreatedDate`, `nullable = false`, `@Column(name = "created_at")`) | 컬럼명은 일치. DB는 NULL 허용, JPA는 `nullable = false` — 제약 강도 불일치 |
| update_at | DATETIME NULL | `updatedAt` (BaseTimeEntity 상속, `@Column(name = "updated_at")`) | **컬럼명 불일치**: DB 컬럼명은 `update_at`(d 없음)인데 `BaseTimeEntity`는 `updated_at`으로 매핑을 시도함. 실제 운영 DB 컬럼명이 ERD 표기 그대로 `update_at`이라면 애플리케이션 구동/쿼리 시 컬럼을 찾지 못해 오류가 날 수 있는 사안 — 확인 필요 |
| deleted_at | DATETIME NULL | `deletedAt` (LocalDateTime) | 일치 |

DB에 없는 JPA 필드: 없음

### `CopyOfpost_image` 테이블

코드베이스 전체(`community` 패키지 및 프로젝트 전역)에서 `PostImage`, `post_image`, `CopyOfpost_image` 관련 클래스/테이블 매핑을 검색했으나 대응하는 JPA 엔티티를 찾지 못함 — 코드에 대응 엔티티 없음. 이름부터("CopyOf...") ERD 툴에서 실수로 복제된 테이블로 추정되며, 팀에 확인 필요.

참고로 게시글 이미지는 별도 테이블이 아니라 `post_content` 테이블의 `type = IMAGE` 행에서 `image_url` 컬럼으로 관리되는 구조로 보임(확인 필요).
