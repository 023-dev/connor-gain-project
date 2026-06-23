# Concepts

> Shared domain vocabulary for this project — entities, named processes, and status concepts with project-specific meaning. Seeded with core domain vocabulary, then accretes as ce-compound and ce-compound-refresh process learnings; direct edits are fine. Glossary only, not a spec or catch-all.

## User (회원)
시스템에 가입하여 인증 및 인가 권한을 가진 계정 엔티티.
* **PK / FK**: 데이터베이스 내부 식별을 위해 `Long` 타입의 PK를 사용하며, 외부 노출 및 클라이언트 통신을 위해 `UUID` 기반의 `user_key`를 사용합니다.

## Deleted User History (탈퇴 회원 이력)
법적 규정 준수 및 소송 대응 등을 목적으로 탈퇴 회원의 원래 정보를 보관하는 격리된 로그 데이터.
* **이력 데이터**: `deleted_users` 테이블에 영구 또는 한시적으로 격리 보관되며, 실서비스 비즈니스 흐름에는 참여하지 않는 감사(Audit) 전용 정보입니다.

## Anonymization (익명화)
회원 탈퇴 시 활성 데이터베이스(`users` 테이블) 내의 개인식별정보(PII)를 플레이스홀더 값으로 대체하여 개인정보를 보호하는 과정.
* **참조 무결성 유지**: 회원 레코드 자체를 완전히 물리 삭제하지 않고 데이터만 비식별화(닉네임 ➡️ `"알 수 없음"`, 이메일 ➡️ `"deleted_" + UUID + "@kbt.com"`)함으로써, 해당 회원이 작성한 게시글(`posts`)이나 댓글(`comments`)의 외래 키 관계를 정상 유지합니다.
