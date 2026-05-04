-- 2026-01-02 기준 8개

INSERT INTO common_code (code_id,code_group,code_name,created_at,updated_at,use_yn) VALUES ('INGREDIENTS_GROUP', 'INGREDIENTS', '재료그룹',now(), now(), 'Y');
INSERT INTO common_code (code_id,code_group,code_name,created_at,updated_at,use_yn) VALUES ('INGREDIENTS_UNIT', 'INGREDIENTS', '재료단위',now(), now(), 'Y');

INSERT INTO common_code (code_id,code_group,code_name,created_at,updated_at,use_yn) VALUES ('SERVINGS', 'COOKINGTIP', '인분수', now(), now(), 'Y');
INSERT INTO common_code (code_id,code_group,code_name,created_at,updated_at,use_yn) VALUES ('COOKING_TIME', 'COOKINGTIP', '요리시간',now(), now(), 'Y');
INSERT INTO common_code (code_id,code_group,code_name,created_at,updated_at,use_yn) VALUES ('DIFFICULTY', 'COOKINGTIP', '난이도',now(), now(), 'Y');

INSERT INTO common_code (code_id,code_group,code_name,created_at,updated_at,use_yn) VALUES ('COOKING_STYLE', 'CATEGORY', '국적/스타일', now(), now(), 'Y');
INSERT INTO common_code (code_id,code_group,code_name,created_at,updated_at,use_yn) VALUES ('COOKING_MAIN_INGREDIENT', 'CATEGORY', '메인 재료', now(), now(), 'Y');
INSERT INTO common_code (code_id,code_group,code_name,created_at,updated_at,use_yn) VALUES ('COOKING_MENU_FORM', 'CATEGORY', '메뉴 형태/코스', now(), now(), 'Y');
INSERT INTO common_code (code_id,code_group,code_name,created_at,updated_at,use_yn) VALUES ('COOKING_TECHNIQUE', 'CATEGORY', '조리법/기술', now(), now(), 'Y');
INSERT INTO common_code (code_id,code_group,code_name,created_at,updated_at,use_yn) VALUES ('COOKING_TARGET', 'CATEGORY', '상황/목적', now(), now(), 'Y');
INSERT INTO common_code (code_id,code_group,code_name,created_at,updated_at,use_yn) VALUES ('COOKING_LEVEL', 'CATEGORY', '난이도/시간', now(), now(), 'Y');
INSERT INTO common_code (code_id,code_group,code_name,created_at,updated_at,use_yn) VALUES ('COOKING_DESSERT', 'CATEGORY', '디저트/음료', now(), now(), 'Y');