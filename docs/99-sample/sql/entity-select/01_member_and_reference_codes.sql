-- =============================================================================
-- 회원·소셜·공통코드 (common-service 엔티티)
-- Member, Follow, CommonCode, CommonCodeDetail
-- 대상 DB: PostgreSQL (Knusrae baseline 스키마 기준)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Entity: Member → member
-- -----------------------------------------------------------------------------
SELECT id,
       name,
       nickname,
       phone,
       email,
       active,
       birth,
       gender,
       social_role,
       profile_image,
       bio,
       follower_count,
       following_count,
       created_at,
       updated_at
FROM member;

-- -----------------------------------------------------------------------------
-- Entity: Follow → follow
-- -----------------------------------------------------------------------------
SELECT id,
       follower_id,
       following_id,
       created_at
FROM follow;

-- -----------------------------------------------------------------------------
-- Entity: CommonCode → common_code
-- -----------------------------------------------------------------------------
SELECT code_id,
       code_group,
       code_name,
       use_yn,
       created_at,
       updated_at
FROM common_code;

-- -----------------------------------------------------------------------------
-- Entity: CommonCodeDetail → common_code_detail
-- -----------------------------------------------------------------------------
SELECT code_id,
       detail_code_id,
       code_name,
       sort,
       use_yn,
       created_at,
       updated_at
FROM common_code_detail;
