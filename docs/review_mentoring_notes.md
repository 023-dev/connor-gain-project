# Mentoring Review Feedback & Action Items

이 문서는 3주차 및 4주차 과제 진행 과정에서 수집된 멘토링 피드백 및 코드 리뷰 내용을 정리한 문서입니다. 마이그레이션 작업 중에 항상 참고하며 설계 및 비즈니스 로직에 반영합니다.

---

## 1. 아키텍처 및 공통 예외 처리

* **성공/오류 공통 응답 포맷 단순화**:
  - 본문이 없는 로그아웃, 탈퇴, 삭제 등의 API 응답은 HTTP Status `200`이 아닌 `204 (No Content)`를 사용합니다.
  - 성공 응답은 DTO 포맷 그대로 반환하고, 오류 발생 시에만 `message`를 담는 구조로 응답 스펙을 명확하게 분리합니다.
* **입력 검증 및 예외 세분화**:
  - `GlobalExceptionHandler`에서 `MethodArgumentNotValidException` 발생 시 필드별 에러 원인을 구분할 수 있도록 구체적인 정보를 내려줍니다.
  - 단순 `invalid_request` 오류 외에 비즈니스 상황에 맞춰 `ErrorType`을 늘려 오류 코드를 세분화합니다.
  - `IllegalStateException`은 서버 상태 문제이므로 `IllegalArgumentException`과 구분하여 400 에러로 단순 처리하지 말고 내부 메시지가 무분별하게 노출되지 않도록 다듬습니다.

---

## 2. 도메인 및 데이터베이스 스키마 설계

* **식별자 분리 (UUID와 Long PK)**:
  - 외부 API 및 클라이언트에 노출하는 ID는 기존의 UUID(String, `key`)를 그대로 사용합니다.
  - 데이터베이스의 내부 식별용 기본 키(PK)는 `Long` 기반의 ID(`BIGINT`)를 사용합니다.
* **PK/FK 데이터 타입 일치**:
  - Java의 `Long`은 Signed(부호 있음) 타입이므로, 데이터베이스 상의 모든 PK/FK 컬럼도 `BIGINT UNSIGNED`가 아닌 **`BIGINT` (Signed)**로 일치시켜 타입 불일치로 인한 외래 키 제약조건 생성 실패를 방지합니다.
* **삭제 상태 매핑 정정 (`deleted_at`)**:
  - `posts` 및 `comments` 테이블의 삭제 상태는 불리언(Default False)이 아닌, **`deleted_at DATETIME NULL DEFAULT NULL`**로 처리하여 삭제 시점에 현재 시각을 채우는 방식으로 수정합니다.
  - 엔티티 레벨에서는 `deletedAt` 필드로 매핑하며, 기존 로직과의 호환을 위해 `deleted()` 메서드가 `deletedAt != null` 여부를 반환하도록 유틸리티성 메서드를 제공합니다.
* **제목 길이 제한 반영**:
  - 기획 및 비즈니스 정책상 게시글 제목은 26자 제한이므로, `posts.title` 컬럼의 길이를 `VARCHAR(26)`로 제한합니다.
* **데이터베이스 제약조건 보완**:
  - `comments.post_id`, `comments.user_id`, `post_images.post_id` 등 연관 관계가 맺어지는 컬럼에는 명확하게 **외래 키(FK) 제약조건**을 DDL 상에 명시하여 참조 무결성을 보장합니다.
  - 게시글 내 이미지 순서를 보장하기 위해 `post_images` 테이블에 `UNIQUE(post_id, sequence)` 복합 유니크 제약조건 설정을 반영합니다.
* **조회 최적화를 위한 인덱스(Index) 구성**:
  - 자주 검색되거나 정렬 조건으로 쓰이는 핵심 컬럼들에 대해 보조 인덱스를 지정합니다:
    - 최신순 목록 조회용: `posts.created_at`
    - 게시글별 댓글 조회용: `comments.post_id`
    - 작성자별 게시글 검색용: `posts.user_id`

---

## 3. 동시성 제어 및 카운트 정합성

* **조회수 및 좋아요 카운트 동시성 안전성 확보**:
  - 조회수(`viewCount`), 좋아요(`likeCount`), 댓글수(`commentCount`)의 증가/감소 연산 시 동시 요청으로 인해 카운트가 누락되거나 어긋나는 것을 방지하기 위해 DB 레벨 벌크 업데이트 연산(`@Modifying` 활용) 또는 원자적 수정 연산을 적용합니다.
* **좋아요 중복 클릭 방지 제약**:
  - 한 사용자가 동일한 게시글에 중복으로 좋아요를 누를 수 없도록 `post_likes` 테이블에 **`UNIQUE(user_id, post_id)`** 제약조건을 설정합니다.
* **비즈니스 정합성 및 권한 검증 선행**:
  - 댓글 삭제(`CommentApplicationService.delete`) 시, **작성자 검증(`validateWriter`)을 먼저 호출한 후**에 성공 시에만 카운트 차감(`decreaseCommentCount`) 및 삭제를 수행하도록 작업 순서를 정정합니다.
  - 포스트 시드 데이터와 댓글 시드 데이터 간의 카운트가 불일치하지 않도록 `InitialDataLoader`에서 연계된 카운트를 정확하게 맞춰줍니다.

---

## 4. API 및 쿼리 최적화 (N+1 문제 해결)

* **익명 조회 시 만료 토큰 처리 개선**:
  - `AuthenticationInterceptor`가 공개 게시글 조회처럼 익명 접근이 허용된 API 호출 시, 클라이언트의 유효기간 만료 또는 손상된 Bearer 토큰으로 인해 401 Unauthorized 에러를 뱉는 오동작을 개선합니다. 유효한 토큰일 때만 인증 정보를 채우고, 그렇지 않으면 정상적으로 익명 사용자로 처리하여 통과시킵니다.
* **목록 조회 시 N+1 문제 해결 (QueryDSL DTO Projection)**:
  - 페이징 목록 조회 시 글마다 작성자 및 좋아요 상태를 개별적으로 다시 조회하는 비효율을 방지하기 위해 QueryDSL 커스텀 리포지토리를 구현하고, 필요한 정보만 한 번에 조인하여 셀렉트하는 DTO Projection 방식을 도입합니다.
* **탈퇴 회원 정보 노출 예외 처리**:
  - 작성자가 탈퇴한 글 상세 조회 또는 글 목록 조회 시 `USER_NOT_FOUND` 예외로 인해 조회가 통째로 404 실패하는 버그를 수정합니다.
  - 작성자 조회 로직을 탈퇴 회원까지 포함하여 찾을 수 있게 다듬고, 닉네임 마스킹 처리는 도메인 엔티티의 게터가 아닌 **DTO / QueryDSL Projection 시점에 `"알 수 없음"`으로 변환**하여 내려주도록 도메인과 표현 레이어의 역할을 분리합니다.

---

## 5. 리팩토링 및 미사용 코드 정리

* **미사용 메서드 완전 정리**:
  - 호출처가 없거나 미사용 상태인 아래 메서드들은 셀프 리뷰 시점에 깨끗이 제거합니다.
    - `UserApplicationService.delete(String)`
    - `PostQueryService.findAll()` 및 `findAllActive()`
    - `LikeQueryService.countByPostId` 계열 메서드
