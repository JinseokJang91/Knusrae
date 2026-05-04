-- =============================================================================
-- 식재료 마스터·저장·요청 (cook-service / ingredient 도메인)
-- IngredientGroup, Ingredient, IngredientStorage, IngredientPreparation,
-- IngredientRequest
-- 대상 DB: PostgreSQL
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Entity: IngredientGroup → ingredient_group
-- -----------------------------------------------------------------------------
SELECT id,
       name,
       image_url,
       sort_order,
       created_at,
       updated_at
FROM ingredient_group;

-- -----------------------------------------------------------------------------
-- Entity: Ingredient → ingredient
-- -----------------------------------------------------------------------------
SELECT id,
       name,
       group_id,
       image_url,
       sort_order,
       created_at,
       updated_at
FROM ingredient;

-- -----------------------------------------------------------------------------
-- Entity: IngredientStorage → ingredient_storage
-- -----------------------------------------------------------------------------
SELECT id,
       ingredient_id,
       content,
       summary,
       created_by,
       created_at,
       updated_at
FROM ingredient_storage;

-- -----------------------------------------------------------------------------
-- Entity: IngredientPreparation → ingredient_preparation
-- -----------------------------------------------------------------------------
SELECT id,
       ingredient_id,
       content,
       summary,
       created_by,
       created_at,
       updated_at
FROM ingredient_preparation;

-- -----------------------------------------------------------------------------
-- Entity: IngredientRequest → ingredient_request
-- -----------------------------------------------------------------------------
SELECT id,
       ingredient_name,
       request_type,
       message,
       member_id,
       status,
       created_at,
       updated_at
FROM ingredient_request;
