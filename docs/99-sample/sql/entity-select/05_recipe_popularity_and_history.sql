-- =============================================================================
-- 레시피 인기도·랭킹 스냅샷 (cook-service)
-- RecipePopularity, RecipePopularityHistory
-- 대상 DB: PostgreSQL
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Entity: RecipePopularity → recipe_popularity
-- -----------------------------------------------------------------------------
SELECT recipe_id,
       popularity_score,
       hits_24h,
       hits_7d,
       hits_30d,
       favorite_count,
       comment_count,
       favorite_increase_24h,
       calculated_at,
       updated_at
FROM recipe_popularity;

-- -----------------------------------------------------------------------------
-- Entity: RecipePopularityHistory → recipe_popularity_history
-- (컬럼명 rank는 예약어이므로 PostgreSQL에서는 따옴표 필요)
-- -----------------------------------------------------------------------------
SELECT id,
       recipe_id,
       "rank",
       popularity_score,
       recorded_at
FROM recipe_popularity_history;
