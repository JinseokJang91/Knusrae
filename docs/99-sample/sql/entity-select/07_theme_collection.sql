-- =============================================================================
-- 테마 큐레이션 (cook-service / theme 도메인)
-- ThemeCollection, ThemeCollectionRecipe
-- 대상 DB: PostgreSQL
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Entity: ThemeCollection → theme_collection
-- -----------------------------------------------------------------------------
SELECT id,
       name,
       description,
       thumbnail_image,
       start_date,
       end_date,
       status,
       sort_order,
       created_at,
       updated_at
FROM theme_collection;

-- -----------------------------------------------------------------------------
-- Entity: ThemeCollectionRecipe → theme_collection_recipe
-- -----------------------------------------------------------------------------
SELECT id,
       collection_id,
       recipe_id,
       sort_order,
       created_at
FROM theme_collection_recipe;
