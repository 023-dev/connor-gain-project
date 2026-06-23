-- 1. User 시드 데이터 주입
INSERT INTO users (id, external_id, email, password, nickname, profile_image, deleted, created_at, updated_at)
VALUES (1, '11111111-1111-4111-8111-111111111111', 'test@startupcode.kr', 'test1234', 'startup', 'https://image.kr/img.jpg', false, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));

-- User Identity 카운터 동기화 (JPA IDENTITY PK 충돌 방지)
ALTER TABLE users ALTER COLUMN id RESTART WITH 2;

-- 2. Post 시드 데이터 주입
INSERT INTO posts (id, user_id, external_id, user_key, title, content, image_url, deleted, created_at, updated_at)
VALUES (1, 1, '22222222-2222-4222-8222-222222222222', '11111111-1111-4111-8111-111111111111', 'Seed post', 'This post is loaded from JSON seed data.', 'https://image.kr/post.jpg', false, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));

-- PostSequence 시작점 동기화 (JPA SEQUENCE PK 충돌 방지)
ALTER SEQUENCE post_seq RESTART WITH 2;

-- 3. PostStat 시드 데이터 주입 (댓글 개수가 1개이므로 comment_count를 1로 설정, created_at/updated_at 없음)
INSERT INTO post_stat (post_id, id2, like_count, comment_count, view_count)
VALUES (1, 1, 0, 1, 0);

-- 4. Comment 시드 데이터 주입
INSERT INTO comments (id, external_id, post_id, post_key, user_id, user_key, content, deleted_at, created_at, updated_at)
VALUES (1, '33333333-3333-4333-8333-333333333333', 1, '22222222-2222-4222-8222-222222222222', 1, '11111111-1111-4111-8111-111111111111', 'This comment is loaded from JSON seed data.', NULL, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));

-- Comment Identity 카운터 동기화 (JPA IDENTITY PK 충돌 방지)
ALTER TABLE comments ALTER COLUMN id RESTART WITH 2;
