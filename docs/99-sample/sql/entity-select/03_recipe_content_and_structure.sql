-- =============================================================================
-- 레시피 본문·구성 (cook-service / recipe 도메인)
-- Recipe, RecipeDetail, RecipeImage, RecipeCategory,
-- RecipeIngredientGroup, RecipeIngredientItem
-- 대상 DB: PostgreSQL
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Entity: Recipe → recipe
-- -----------------------------------------------------------------------------
SELECT id,
       title,
       introduction,
       status,
       visibility,
       thumbnail,
       hits,
       member_id,
       created_at,
       updated_at
FROM recipe;

-- -----------------------------------------------------------------------------
-- Entity: RecipeDetail → recipe_detail
-- -----------------------------------------------------------------------------
SELECT id,
       step,
       image,
       description,
       created_at,
       updated_at,
       recipe_id
FROM recipe_detail;

-- -----------------------------------------------------------------------------
-- Entity: RecipeImage → recipe_image
-- -----------------------------------------------------------------------------
SELECT id,
       url,
       storage_key,
       file_name,
       content_type,
       size,
       sort_order,
       is_main_image,
       created_at,
       updated_at,
       recipe_id
FROM recipe_image;

-- -----------------------------------------------------------------------------
-- Entity: RecipeCategory → recipe_category
-- -----------------------------------------------------------------------------
SELECT recipe_id,
       code_id,
       detail_code_id,
       code_group,
       created_at,
       updated_at
FROM recipe_category;

-- -----------------------------------------------------------------------------
-- Entity: RecipeIngredientGroup → recipe_ingredient_group
-- -----------------------------------------------------------------------------
SELECT id,
       group_order,
       created_at,
       updated_at,
       recipe_id,
       type_code_id,
       type_detail_code_id,
       custom_type_name
FROM recipe_ingredient_group;

-- -----------------------------------------------------------------------------
-- Entity: RecipeIngredientItem → recipe_ingredient_item
-- -----------------------------------------------------------------------------
SELECT id,
       name,
       quantity,
       item_order,
       created_at,
       updated_at,
       recipe_ingredient_group_id,
       unit_code_id,
       unit_detail_code_id,
       custom_unit_name
FROM recipe_ingredient_item;
