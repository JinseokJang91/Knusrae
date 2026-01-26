<template>
  <section class="today-recommendations-section">
    <div class="section-header">
      <div>
        <h2 class="section-title">
          <i class="pi pi-sparkles"></i>
          오늘의 레시피 추천
        </h2>
        <p class="section-subtitle" v-if="recommendations">
          {{ recommendations.recommendationType === 'PERSONALIZED' ? '당신을 위한 맞춤 추천' : '오늘의 인기 레시피' }}
        </p>
      </div>
      <div class="header-actions" v-if="recommendations?.refreshable">
        <Button 
          label="새로고침" 
          icon="pi pi-refresh"
          @click="refreshRecommendations"
          :loading="isLoading"
          text
          size="small"
        />
      </div>
    </div>
    
    <!-- 로딩 상태 -->
    <div v-if="isLoading" class="loading-container">
      <div class="skeleton-cards">
        <Skeleton v-for="i in 3" :key="i" height="350px" />
      </div>
    </div>
    
    <!-- 추천 레시피 -->
    <div v-else-if="recommendations && recommendations.recipes.length > 0" class="recommendations-grid">
      <div
        v-for="recipe in recommendations.recipes"
        :key="recipe.id"
        class="recommendation-card"
        @click="goToRecipe(recipe.id)"
      >
        <!-- 추천 이유 배지 -->
        <div class="recommend-badge" v-if="recipe.recommendReason">
          <i class="pi pi-star-fill"></i>
          {{ recipe.recommendReason }}
        </div>
        
        <!-- 썸네일 -->
        <div class="card-thumbnail">
          <img 
            :src="recipe.thumbnail || '/placeholder.jpg'" 
            :alt="recipe.title"
          />
        </div>
        
        <!-- 정보 -->
        <div class="card-info">
          <h3 class="recipe-title">{{ recipe.title }}</h3>
          <p class="recipe-description" v-if="recipe.description">
            {{ recipe.description }}
          </p>
          
          <!-- 카테고리 -->
          <div class="recipe-categories" v-if="recipe.categories && recipe.categories.length > 0">
            <span
              v-for="category in recipe.categories.slice(0, 2)"
              :key="`${category.codeId}-${category.detailCodeId}`"
              class="category-tag"
            >
              {{ category.detailName }}
            </span>
          </div>
          
          <!-- 작성자 -->
          <div class="recipe-author">
            <i class="pi pi-user"></i>
            {{ recipe.memberNickname || '익명' }}
          </div>
          
          <!-- 통계 -->
          <div class="recipe-stats">
            <span>
              <i class="pi pi-eye"></i>
              {{ formatNumber(recipe.hits || 0) }}
            </span>
            <span>
              <i class="pi pi-heart"></i>
              {{ formatNumber(recipe.favoriteCount || 0) }}
            </span>
            <span v-if="recipe.commentCount">
              <i class="pi pi-comment"></i>
              {{ formatNumber(recipe.commentCount) }}
            </span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 데이터 없음 -->
    <div v-else class="empty-container">
      <i class="pi pi-sparkles" style="font-size: 3rem; color: var(--text-color-secondary)"></i>
      <p>추천할 레시피가 없습니다</p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useToast } from 'primevue/usetoast';
import Button from 'primevue/button';
import Skeleton from 'primevue/skeleton';
import { getTodayRecommendations } from '@/api/recommendationApi';
import type { TodayRecommendationsResponse } from '@/types/recipe';

const router = useRouter();
const toast = useToast();

const recommendations = ref<TodayRecommendationsResponse | null>(null);
const isLoading = ref(false);

/**
 * 오늘의 추천 레시피 로딩
 */
const loadRecommendations = async (refresh: boolean = false) => {
  console.log('[TodayRecommendations] 추천 레시피 로딩 시작, refresh:', refresh);
  isLoading.value = true;
  
  try {
    const data = await getTodayRecommendations(3, refresh);
    console.log('[TodayRecommendations] 데이터 로딩 성공:', data);
    recommendations.value = data;
    console.log('[TodayRecommendations] 추천 타입:', data.recommendationType);
  } catch (error) {
    console.error('[TodayRecommendations] 추천 레시피 로딩 실패:', error);
    toast.add({
      severity: 'error',
      summary: '오류',
      detail: '추천 레시피를 불러오는데 실패했습니다.',
      life: 3000
    });
  } finally {
    isLoading.value = false;
  }
};

/**
 * 새로고침
 */
const refreshRecommendations = async () => {
  await loadRecommendations(true);
  toast.add({
    severity: 'success',
    summary: '새로고침',
    detail: '새로운 추천 레시피를 불러왔습니다.',
    life: 2000
  });
};

/**
 * 레시피 상세 페이지로 이동
 */
const goToRecipe = (recipeId: number) => {
  router.push(`/recipe/${recipeId}`);
};

/**
 * 숫자 포맷팅 (천 단위 구분)
 */
const formatNumber = (num: number): string => {
  if (num >= 10000) {
    return `${(num / 10000).toFixed(1)}만`;
  }
  if (num >= 1000) {
    return `${(num / 1000).toFixed(1)}천`;
  }
  return num.toString();
};

// 컴포넌트 마운트 시 데이터 로드
onMounted(() => {
  console.log('[TodayRecommendations] 컴포넌트 마운트됨');
  loadRecommendations();
});

// 외부에서 사용할 수 있도록 노출
defineExpose({
  loadRecommendations
});
</script>

<style scoped lang="scss">
.today-recommendations-section {
  margin-bottom: 48px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  
  .section-title {
    font-size: 24px;
    font-weight: 700;
    color: var(--text-color);
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 4px;
    
    i {
      color: #FFD700;
    }
  }
  
  .section-subtitle {
    font-size: 14px;
    color: var(--text-color-secondary);
  }
  
  .header-actions {
    display: flex;
    gap: 8px;
  }
}

.recommendations-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.recommendation-card {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  background: var(--surface-card);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  }
}

.recommend-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 4px;
  
  i {
    font-size: 10px;
  }
}

.card-thumbnail {
  width: 100%;
  height: 200px;
  overflow: hidden;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
  }
  
  .recommendation-card:hover & img {
    transform: scale(1.05);
  }
}

.card-info {
  padding: 16px;
}

.recipe-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-color);
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.recipe-description {
  font-size: 13px;
  color: var(--text-color-secondary);
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.recipe-categories {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.category-tag {
  padding: 4px 10px;
  background: var(--surface-ground);
  color: var(--text-color);
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
}

.recipe-author {
  font-size: 12px;
  color: var(--text-color-secondary);
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  
  i {
    font-size: 11px;
  }
}

.recipe-stats {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--text-color-secondary);
  padding-top: 12px;
  border-top: 1px solid var(--surface-border);
  
  span {
    display: flex;
    align-items: center;
    gap: 4px;
    
    i {
      font-size: 11px;
    }
  }
}

.loading-container {
  .skeleton-cards {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 24px;
  }
}

.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64px 24px;
  text-align: center;
  color: var(--text-color-secondary);
  
  p {
    margin-top: 16px;
    font-size: 14px;
  }
}

@media (max-width: 1024px) {
  .recommendations-grid,
  .loading-container .skeleton-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .recommendations-grid,
  .loading-container .skeleton-cards {
    grid-template-columns: 1fr;
  }
  
  .recommendation-card {
    max-width: 500px;
    margin: 0 auto;
  }
}
</style>
