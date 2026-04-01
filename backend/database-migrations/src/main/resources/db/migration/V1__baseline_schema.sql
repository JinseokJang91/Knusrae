-- Knusrae 공유 PostgreSQL 초기 스키마 (JPA 엔티티 기준)
-- Flyway는 auth-service 기동 시에만 실행됩니다(동일 DB 중복 마이그레이션 방지).

CREATE TABLE member (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(20) NOT NULL,
    nickname        VARCHAR(20),
    phone           VARCHAR(13),
    email           VARCHAR(50) NOT NULL,
    active          VARCHAR(255) NOT NULL,
    birth           VARCHAR(10),
    gender          VARCHAR(255),
    social_role     VARCHAR(255) NOT NULL,
    profile_image   VARCHAR(500),
    bio             VARCHAR(500),
    follower_count  BIGINT,
    following_count BIGINT,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP
);

CREATE TABLE common_code (
    code_id    VARCHAR(30) PRIMARY KEY,
    code_group VARCHAR(30) NOT NULL,
    code_name  VARCHAR(50) NOT NULL,
    use_yn     VARCHAR(2),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE common_code_detail (
    code_id         VARCHAR(30) NOT NULL,
    detail_code_id  VARCHAR(30) NOT NULL,
    code_name       VARCHAR(50) NOT NULL,
    sort            INTEGER,
    use_yn          VARCHAR(2),
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    PRIMARY KEY (code_id, detail_code_id),
    CONSTRAINT fk_ccd_code FOREIGN KEY (code_id) REFERENCES common_code (code_id)
);

CREATE TABLE follow (
    id           BIGSERIAL PRIMARY KEY,
    follower_id  BIGINT NOT NULL,
    following_id BIGINT NOT NULL,
    created_at   TIMESTAMP,
    CONSTRAINT uk_follower_following UNIQUE (follower_id, following_id)
);

CREATE INDEX idx_follow_follower ON follow (follower_id);
CREATE INDEX idx_follow_following ON follow (following_id);
CREATE INDEX idx_follow_created_at ON follow (created_at);

CREATE TABLE refresh_tokens (
    token      VARCHAR(500) PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP
);

CREATE TABLE token_blacklist (
    token          VARCHAR(500) PRIMARY KEY,
    expires_at     TIMESTAMP NOT NULL,
    blacklisted_at TIMESTAMP
);

CREATE TABLE recipe (
    id            BIGSERIAL PRIMARY KEY,
    title         VARCHAR(255) NOT NULL,
    introduction  VARCHAR(1000),
    status        VARCHAR(255) NOT NULL,
    visibility    VARCHAR(255) NOT NULL,
    thumbnail     VARCHAR(255),
    hits          BIGINT,
    member_id     BIGINT NOT NULL,
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP
);

CREATE TABLE recipe_detail (
    id          BIGSERIAL PRIMARY KEY,
    step        BIGINT NOT NULL,
    image       VARCHAR(500),
    description TEXT NOT NULL,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    recipe_id   BIGINT NOT NULL,
    CONSTRAINT fk_rd_recipe FOREIGN KEY (recipe_id) REFERENCES recipe (id)
);

CREATE TABLE recipe_image (
    id            BIGSERIAL PRIMARY KEY,
    url           VARCHAR(255),
    storage_key   VARCHAR(255),
    file_name     VARCHAR(255),
    content_type  VARCHAR(255),
    size          BIGINT,
    sort_order    INTEGER,
    is_main_image BOOLEAN NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP,
    recipe_id     BIGINT NOT NULL,
    CONSTRAINT fk_ri_recipe FOREIGN KEY (recipe_id) REFERENCES recipe (id)
);

CREATE TABLE recipe_comment (
    id                 BIGSERIAL PRIMARY KEY,
    parent_id          BIGINT,
    content            TEXT NOT NULL,
    image_url          VARCHAR(255),
    image_storage_key  VARCHAR(255),
    member_id          BIGINT NOT NULL,
    created_at         TIMESTAMP,
    updated_at         TIMESTAMP,
    recipe_id          BIGINT NOT NULL,
    CONSTRAINT fk_rc_recipe FOREIGN KEY (recipe_id) REFERENCES recipe (id)
);

CREATE TABLE recipe_category (
    recipe_id       BIGINT NOT NULL,
    code_id         VARCHAR(30) NOT NULL,
    detail_code_id  VARCHAR(30) NOT NULL,
    code_group      VARCHAR(255),
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    PRIMARY KEY (recipe_id, code_id, detail_code_id),
    CONSTRAINT fk_rcat_recipe FOREIGN KEY (recipe_id) REFERENCES recipe (id),
    CONSTRAINT fk_rcat_detail FOREIGN KEY (code_id, detail_code_id)
        REFERENCES common_code_detail (code_id, detail_code_id)
);

CREATE TABLE recipe_ingredient_group (
    id                   BIGSERIAL PRIMARY KEY,
    group_order          INTEGER NOT NULL,
    created_at           TIMESTAMP,
    updated_at           TIMESTAMP,
    recipe_id            BIGINT NOT NULL,
    type_code_id         VARCHAR(30),
    type_detail_code_id  VARCHAR(30),
    custom_type_name     VARCHAR(50),
    CONSTRAINT fk_rig_recipe FOREIGN KEY (recipe_id) REFERENCES recipe (id),
    CONSTRAINT fk_rig_type FOREIGN KEY (type_code_id, type_detail_code_id)
        REFERENCES common_code_detail (code_id, detail_code_id)
);

CREATE TABLE recipe_ingredient_item (
    id                        BIGSERIAL PRIMARY KEY,
    name                      VARCHAR(255) NOT NULL,
    quantity                  VARCHAR(50),
    item_order                INTEGER NOT NULL,
    created_at                TIMESTAMP,
    updated_at                TIMESTAMP,
    recipe_ingredient_group_id BIGINT NOT NULL,
    unit_code_id              VARCHAR(30),
    unit_detail_code_id       VARCHAR(30),
    custom_unit_name          VARCHAR(20),
    CONSTRAINT fk_rii_group FOREIGN KEY (recipe_ingredient_group_id)
        REFERENCES recipe_ingredient_group (id),
    CONSTRAINT fk_rii_unit FOREIGN KEY (unit_code_id, unit_detail_code_id)
        REFERENCES common_code_detail (code_id, detail_code_id)
);

CREATE TABLE recipe_favorite (
    id         BIGSERIAL PRIMARY KEY,
    member_id  BIGINT NOT NULL,
    recipe_id  BIGINT NOT NULL,
    created_at TIMESTAMP,
    CONSTRAINT uk_recipe_favorite_member_recipe UNIQUE (member_id, recipe_id),
    CONSTRAINT fk_rf_recipe FOREIGN KEY (recipe_id) REFERENCES recipe (id)
);

CREATE TABLE recipebook (
    id          BIGSERIAL PRIMARY KEY,
    member_id   BIGINT NOT NULL,
    name        VARCHAR(50) NOT NULL,
    color       VARCHAR(20),
    sort_order  INTEGER NOT NULL,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
);

CREATE INDEX idx_recipebook_member_id ON recipebook (member_id);
CREATE INDEX idx_recipebook_member_sort ON recipebook (member_id, sort_order);

CREATE TABLE recipe_bookmark (
    id            BIGSERIAL PRIMARY KEY,
    recipebook_id BIGINT NOT NULL,
    recipe_id     BIGINT NOT NULL,
    member_id     BIGINT NOT NULL,
    memo          VARCHAR(500),
    created_at    TIMESTAMP,
    CONSTRAINT uk_recipe_bookmark_book_recipe UNIQUE (recipebook_id, recipe_id),
    CONSTRAINT fk_rb_book FOREIGN KEY (recipebook_id) REFERENCES recipebook (id),
    CONSTRAINT fk_rb_recipe FOREIGN KEY (recipe_id) REFERENCES recipe (id)
);

CREATE INDEX idx_recipe_bookmark_recipebook_id ON recipe_bookmark (recipebook_id);
CREATE INDEX idx_recipe_bookmark_member_id ON recipe_bookmark (member_id);
CREATE INDEX idx_recipe_bookmark_recipe_id ON recipe_bookmark (recipe_id);

CREATE TABLE recipe_view (
    id         BIGSERIAL PRIMARY KEY,
    member_id  BIGINT NOT NULL,
    recipe_id  BIGINT NOT NULL,
    viewed_at  TIMESTAMP NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_recipe_view_member_recipe UNIQUE (member_id, recipe_id),
    CONSTRAINT fk_rv_recipe FOREIGN KEY (recipe_id) REFERENCES recipe (id)
);

CREATE INDEX idx_member_viewed_at ON recipe_view (member_id, viewed_at DESC);
CREATE INDEX idx_recipe_viewed_at ON recipe_view (recipe_id, viewed_at DESC);

CREATE TABLE recipe_popularity (
    recipe_id               BIGINT PRIMARY KEY,
    popularity_score        DOUBLE PRECISION NOT NULL DEFAULT 0,
    hits_24h                BIGINT,
    hits_7d                 BIGINT,
    hits_30d                BIGINT,
    favorite_count          BIGINT,
    comment_count           BIGINT,
    favorite_increase_24h   BIGINT,
    calculated_at           TIMESTAMP NOT NULL,
    updated_at              TIMESTAMP,
    CONSTRAINT fk_rp_recipe FOREIGN KEY (recipe_id) REFERENCES recipe (id)
);

CREATE INDEX idx_popularity_score ON recipe_popularity (popularity_score DESC);
CREATE INDEX idx_calculated_at ON recipe_popularity (calculated_at DESC);
CREATE INDEX idx_hits_24h ON recipe_popularity (hits_24h DESC);
CREATE INDEX idx_hits_7d ON recipe_popularity (hits_7d DESC);
CREATE INDEX idx_hits_30d ON recipe_popularity (hits_30d DESC);

CREATE TABLE recipe_popularity_history (
    id                BIGSERIAL PRIMARY KEY,
    recipe_id         BIGINT NOT NULL,
    "rank"            INTEGER NOT NULL,
    popularity_score  DOUBLE PRECISION NOT NULL,
    recorded_at       TIMESTAMP NOT NULL,
    CONSTRAINT fk_rph_recipe FOREIGN KEY (recipe_id) REFERENCES recipe (id)
);

CREATE INDEX idx_recipe_recorded_at ON recipe_popularity_history (recipe_id, recorded_at DESC);
CREATE INDEX idx_recorded_at ON recipe_popularity_history (recorded_at DESC);

CREATE TABLE ingredient_group (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL,
    image_url   VARCHAR(200),
    sort_order  INTEGER,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
);

CREATE INDEX idx_sort_order ON ingredient_group (sort_order);

CREATE TABLE ingredient (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    group_id    BIGINT NOT NULL,
    image_url   VARCHAR(200),
    sort_order  INTEGER,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    CONSTRAINT fk_ing_group FOREIGN KEY (group_id) REFERENCES ingredient_group (id)
);

CREATE INDEX idx_group_id ON ingredient (group_id);
CREATE INDEX idx_name ON ingredient (name);
CREATE INDEX idx_group_sort ON ingredient (group_id, sort_order);

CREATE TABLE ingredient_storage (
    id            BIGSERIAL PRIMARY KEY,
    ingredient_id BIGINT NOT NULL UNIQUE,
    content       TEXT NOT NULL,
    summary       VARCHAR(500),
    created_by    BIGINT,
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP,
    CONSTRAINT fk_ist_ing FOREIGN KEY (ingredient_id) REFERENCES ingredient (id)
);

CREATE TABLE ingredient_preparation (
    id            BIGSERIAL PRIMARY KEY,
    ingredient_id BIGINT NOT NULL UNIQUE,
    content       TEXT NOT NULL,
    summary       VARCHAR(500),
    created_by    BIGINT,
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP,
    CONSTRAINT fk_ip_ing FOREIGN KEY (ingredient_id) REFERENCES ingredient (id)
);

CREATE TABLE ingredient_request (
    id              BIGSERIAL PRIMARY KEY,
    ingredient_name VARCHAR(100) NOT NULL,
    request_type    VARCHAR(20) NOT NULL,
    message         TEXT,
    member_id       BIGINT,
    status          VARCHAR(20) NOT NULL,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP
);

CREATE INDEX idx_ingredient_request_member_id ON ingredient_request (member_id);
CREATE INDEX idx_ingredient_request_status ON ingredient_request (status);
CREATE INDEX idx_ingredient_request_created_at ON ingredient_request (created_at DESC);

CREATE TABLE theme_collection (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(100) NOT NULL,
    description      VARCHAR(500),
    thumbnail_image  VARCHAR(500),
    start_date       DATE,
    end_date         DATE,
    status           VARCHAR(20) NOT NULL,
    sort_order       INTEGER,
    created_at       TIMESTAMP,
    updated_at       TIMESTAMP
);

CREATE INDEX idx_theme_status_sort ON theme_collection (status, sort_order);
CREATE INDEX idx_theme_start_end_date ON theme_collection (start_date, end_date);

CREATE TABLE theme_collection_recipe (
    id             BIGSERIAL PRIMARY KEY,
    collection_id  BIGINT NOT NULL,
    recipe_id      BIGINT NOT NULL,
    sort_order     INTEGER,
    created_at     TIMESTAMP,
    CONSTRAINT uk_theme_collection_recipe UNIQUE (collection_id, recipe_id),
    CONSTRAINT fk_tcr_collection FOREIGN KEY (collection_id) REFERENCES theme_collection (id),
    CONSTRAINT fk_tcr_recipe FOREIGN KEY (recipe_id) REFERENCES recipe (id)
);

CREATE INDEX idx_theme_collection_sort ON theme_collection_recipe (collection_id, sort_order);

CREATE TABLE inquiry (
    id            BIGSERIAL PRIMARY KEY,
    member_id     BIGINT NOT NULL,
    inquiry_type  VARCHAR(30) NOT NULL,
    title         VARCHAR(100) NOT NULL,
    content       TEXT NOT NULL,
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP
);

CREATE INDEX idx_inquiry_member_id ON inquiry (member_id);
CREATE INDEX idx_inquiry_created_at ON inquiry (created_at DESC);

CREATE TABLE inquiry_image (
    id          BIGSERIAL PRIMARY KEY,
    inquiry_id  BIGINT NOT NULL,
    image_url   VARCHAR(512) NOT NULL,
    sort_order  INTEGER NOT NULL,
    CONSTRAINT fk_inimg_inquiry FOREIGN KEY (inquiry_id) REFERENCES inquiry (id)
);

CREATE INDEX idx_inquiry_image_inquiry_id ON inquiry_image (inquiry_id);

CREATE TABLE inquiry_reply (
    id          BIGSERIAL PRIMARY KEY,
    inquiry_id  BIGINT NOT NULL UNIQUE,
    content     TEXT NOT NULL,
    reply_by    BIGINT,
    replied_at  TIMESTAMP,
    CONSTRAINT fk_inrep_inquiry FOREIGN KEY (inquiry_id) REFERENCES inquiry (id)
);

CREATE UNIQUE INDEX idx_inquiry_reply_inquiry_id ON inquiry_reply (inquiry_id);
