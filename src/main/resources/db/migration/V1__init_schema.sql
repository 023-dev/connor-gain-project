-- 1. Sequences 생성
CREATE SEQUENCE post_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE like_seq START WITH 1 INCREMENT BY 1;

-- 2. users 테이블 생성
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    external_id CHAR(36) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(255) NOT NULL,
    profile_image VARCHAR(255),
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- 3. deleted_users 테이블 생성
CREATE TABLE deleted_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_key CHAR(36) NOT NULL,
    email VARCHAR(255) NOT NULL,
    nickname VARCHAR(255) NOT NULL,
    deleted_at TIMESTAMP NOT NULL
);

-- 4. posts 테이블 생성
CREATE TABLE posts (
    id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    external_id CHAR(36) NOT NULL UNIQUE,
    user_key CHAR(36) NOT NULL,
    title VARCHAR(26) NOT NULL,
    content VARCHAR(2000) NOT NULL,
    image_url VARCHAR(255),
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id, user_id)
);

-- posts 인덱스 설정
CREATE INDEX idx_posts_created_at ON posts (created_at);
CREATE INDEX idx_posts_user_id ON posts (user_id);

-- 5. post_stat 테이블 생성
CREATE TABLE post_stat (
    post_id BIGINT NOT NULL,
    id2 BIGINT NOT NULL,
    like_count BIGINT NOT NULL,
    comment_count BIGINT NOT NULL,
    view_count BIGINT NOT NULL,
    PRIMARY KEY (post_id, id2)
);

-- 6. comments 테이블 생성
CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    external_id CHAR(36) NOT NULL UNIQUE,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    post_key CHAR(36) NOT NULL,
    user_key CHAR(36) NOT NULL,
    content VARCHAR(2000) NOT NULL,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- comments 인덱스 설정
CREATE INDEX idx_comments_post_id ON comments (post_id);

-- 7. post_likes 테이블 생성
CREATE TABLE post_likes (
    id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    id2 BIGINT NOT NULL,
    external_id CHAR(36) NOT NULL UNIQUE,
    user_key CHAR(36) NOT NULL,
    post_key CHAR(36) NOT NULL,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id, user_id, post_id, id2),
    UNIQUE (user_id, post_id)
);

-- 8. deleted_posts 테이블 생성 (신규)
CREATE TABLE deleted_posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    post_key CHAR(36) NOT NULL,
    user_key CHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content VARCHAR(2000) NOT NULL,
    image_url VARCHAR(255),
    deleted_at TIMESTAMP NOT NULL
);

-- 9. deleted_comments 테이블 생성 (신규)
CREATE TABLE deleted_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    comment_key CHAR(36) NOT NULL,
    post_key CHAR(36) NOT NULL,
    user_key CHAR(36) NOT NULL,
    content VARCHAR(2000) NOT NULL,
    deleted_at TIMESTAMP NOT NULL
);
