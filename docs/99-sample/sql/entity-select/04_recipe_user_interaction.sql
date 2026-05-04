-- =============================================================================
-- 레시피 사용자 상호작용·북마크 (cook-service)
-- RecipeComment, RecipeFavorite, RecipeView, RecipeBook, RecipeBookmark
-- 대상 DB: PostgreSQL
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Entity: RecipeComment → recipe_comment
-- -----------------------------------------------------------------------------
SELECT id,
       parent_id,
       content,
       image_url,
       image_storage_key,
       member_id,
       created_at,
       updated_at,
       recipe_id
FROM recipe_comment;

-- -----------------------------------------------------------------------------
-- Entity: RecipeFavorite → recipe_favorite
-- -----------------------------------------------------------------------------
SELECT id,
       member_id,
       recipe_id,
       created_at
FROM recipe_favorite;

-- -----------------------------------------------------------------------------
-- Entity: RecipeView → recipe_view
-- -----------------------------------------------------------------------------
SELECT id,
       member_id,
       recipe_id,
       viewed_at,
       created_at,
       updated_at
FROM recipe_view;

-- -----------------------------------------------------------------------------
-- Entity: RecipeBook → recipebook
-- -----------------------------------------------------------------------------
SELECT id,
       member_id,
       name,
       color,
       sort_order,
       created_at,
       updated_at
FROM recipebook;

-- -----------------------------------------------------------------------------
-- Entity: RecipeBookmark → recipe_bookmark
-- -----------------------------------------------------------------------------
SELECT id,
       recipebook_id,
       recipe_id,
       member_id,
       memo,
       created_at
FROM recipe_bookmark;
