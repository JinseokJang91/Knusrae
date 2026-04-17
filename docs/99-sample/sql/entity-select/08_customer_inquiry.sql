-- =============================================================================
-- 1:1 문의·고객지원 (member-service / inquiry 도메인)
-- Inquiry, InquiryImage, InquiryReply
-- 대상 DB: PostgreSQL
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Entity: Inquiry → inquiry
-- -----------------------------------------------------------------------------
SELECT id,
       member_id,
       inquiry_type,
       title,
       content,
       created_at,
       updated_at
FROM inquiry;

-- -----------------------------------------------------------------------------
-- Entity: InquiryImage → inquiry_image
-- -----------------------------------------------------------------------------
SELECT id,
       inquiry_id,
       image_url,
       sort_order
FROM inquiry_image;

-- -----------------------------------------------------------------------------
-- Entity: InquiryReply → inquiry_reply
-- -----------------------------------------------------------------------------
SELECT id,
       inquiry_id,
       content,
       reply_by,
       replied_at
FROM inquiry_reply;
