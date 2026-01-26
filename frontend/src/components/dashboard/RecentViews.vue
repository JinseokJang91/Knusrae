<template>
  <section v-if="isLoggedIn" class="recent-views-section">
    <div class="section-header">
      <div>
        <h2 class="section-title">
          <i class="pi pi-clock"></i>
          최근 본 레시피
        </h2>
        <p class="section-subtitle" v-if="recentViews.length > 0">{{ recentViews.length }}개</p>
        <p class="section-subtitle" v-else>아직 본 레시피가 없습니다</p>
      </div>
      <div class="header-actions" v-if="recentViews.length > 0">
        <Button 
          label="기록 삭제" 
          icon="pi pi-trash"
          @click="confirmDelete"
          text
          severity="danger"
          size="small"
        />
      </div>
    </div>
    
    <!-- 로딩 상태 -->
    <div v-if="isLoading" class="loading-container">
      <i class="pi pi-spin pi-spinner" style="font-size: 2rem"></i>
      <p>최근 본 레시피를 불러오는 중...</p>
    </div>
    
    <!-- 데이터 없음 -->
    <div v-else-if="recentViews.length === 0" class="empty-container">
      <i class="pi pi-clock" style="font-size: 3rem; color: var(--text-color-secondary)"></i>
      <p>아직 본 레시피가 없습니다</p>
      <p class="empty-hint">레시피를 보시면 여기에 표시됩니다</p>
    </div>
    
    <!-- 레시피 목록 -->
    <div v-else class="recent-views-scroll">
      <div class="recent-views-container">
        <div
          v-for="view in recentViews"
          :key="view.id"
          class="recent-view-card"
          @click="goToRecipe(view.recipeId)"
        >
          <!-- 썸네일 -->
          <div class="card-thumbnail">
            <img 
              :src="view.recipe?.thumbnail || '/placeholder.jpg'" 
              :alt="view.recipe?.title"
            />
            <div class="view-time">
              {{ formatViewTime(view.viewedAt) }}
            </div>
          </div>
          
          <!-- 정보 -->
          <div class="card-info">
            <h3 class="recipe-title">{{ view.recipe?.title }}</h3>
            <p class="recipe-author">{{ view.recipe?.memberNickname }}</p>
            
            <div class="recipe-stats">
              <span>
                <i class="pi pi-eye"></i>
                {{ formatNumber(view.recipe?.hits || 0) }}
              </span>
              <span>
                <i class="pi pi-heart"></i>
                {{ formatNumber(view.recipe?.favoriteCount || 0) }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useConfirm } from 'primevue/useconfirm';
import { useToast } from 'primevue/usetoast';
import Button from 'primevue/button';
import { useAuthStore } from '@/stores/authStore';
import { getRecentViews, deleteAllRecentViews } from '@/api/recipeViewApi';
import type { RecipeView } from '@/types/recipe';

const router = useRouter();
const authStore = useAuthStore();
const confirm = useConfirm();
const toast = useToast();

const recentViews = ref<RecipeView[]>([]);
const isLoading = ref(false);

const isLoggedIn = computed(() => authStore.isLoggedIn);
const memberId = computed(() => authStore.memberInfo?.id);

/**
 * 최근 본 레시피 목록 로딩
 */
const loadRecentViews = async () => {
  if (!memberId.value) {
    console.log('[RecentViews] memberId가 없어서 로딩을 건너뜁니다.');
    return;
  }
  
  console.log('[RecentViews] 최근 본 레시피 로딩 시작, memberId:', memberId.value);
  isLoading.value = true;
  try {
    const data = await getRecentViews(memberId.value, 10, 0);
    console.log('[RecentViews] 데이터 로딩 성공:', data);
    recentViews.value = data.views || [];
    console.log('[RecentViews] 로딩된 레시피 개수:', recentViews.value.length);
  } catch (error) {
    console.error('[RecentViews] 최근 본 레시피 로딩 실패:', error);
    toast.add({
      severity: 'error',
      summary: '오류',
      detail: '최근 본 레시피를 불러오는데 실패했습니다.',
      life: 3000
    });
    recentViews.value = [];
  } finally {
    isLoading.value = false;
  }
};

/**
 * 레시피 상세 페이지로 이동
 */
const goToRecipe = (recipeId: number) => {
  router.push(`/recipe/${recipeId}`);
};

/**
 * 기록 삭제 확인
 */
const confirmDelete = () => {
  confirm.require({
    message: '최근 본 레시피 기록을 모두 삭제하시겠습니까?',
    header: '기록 삭제',
    icon: 'pi pi-exclamation-triangle',
    acceptLabel: '삭제',
    rejectLabel: '취소',
    acceptClass: 'p-button-danger',
    accept: async () => {
      await deleteAllViews();
    }
  });
};

/**
 * 모든 조회 기록 삭제
 */
const deleteAllViews = async () => {
  if (!memberId.value) return;
  
  try {
    await deleteAllRecentViews(memberId.value);
    recentViews.value = [];
    
    toast.add({
      severity: 'success',
      summary: '삭제 완료',
      detail: '최근 본 레시피 기록이 삭제되었습니다.',
      life: 3000
    });
  } catch (error) {
    console.error('기록 삭제 실패:', error);
    toast.add({
      severity: 'error',
      summary: '오류',
      detail: '기록 삭제에 실패했습니다.',
      life: 3000
    });
  }
};

/**
 * 조회 시간 포맷팅
 */
const formatViewTime = (viewedAt: string): string => {
  const now = new Date();
  const viewTime = new Date(viewedAt);
  const diffMs = now.getTime() - viewTime.getTime();
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMs / 3600000);
  const diffDays = Math.floor(diffMs / 86400000);
  
  if (diffMins < 1) return '방금 전';
  if (diffMins < 60) return `${diffMins}분 전`;
  if (diffHours < 24) return `${diffHours}시간 전`;
  if (diffDays < 7) return `${diffDays}일 전`;
  
  return viewTime.toLocaleDateString('ko-KR', {
    month: 'long',
    day: 'numeric'
  });
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

// 로그인 상태 변경 감지
watch([isLoggedIn, memberId], ([newIsLoggedIn, newMemberId]) => {
  console.log('[RecentViews] 로그인 상태 변경:', { isLoggedIn: newIsLoggedIn, memberId: newMemberId });
  if (newIsLoggedIn && newMemberId) {
    loadRecentViews();
  } else {
    recentViews.value = [];
  }
}, { immediate: true });

// 컴포넌트 마운트 시 데이터 로드
onMounted(() => {
  console.log('[RecentViews] 컴포넌트 마운트됨', { isLoggedIn: isLoggedIn.value, memberId: memberId.value });
  if (isLoggedIn.value && memberId.value) {
    loadRecentViews();
  }
});

// 외부에서 사용할 수 있도록 노출
defineExpose({
  loadRecentViews
});
</script>

<style scoped lang="scss">
.recent-views-section {
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
      color: var(--primary-color);
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

.recent-views-scroll {
  overflow-x: auto;
  padding-bottom: 8px;
  
  &::-webkit-scrollbar {
    height: 6px;
  }
  
  &::-webkit-scrollbar-track {
    background: var(--surface-ground);
    border-radius: 3px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: var(--surface-border);
    border-radius: 3px;
    
    &:hover {
      background: var(--surface-hover);
    }
  }
}

.recent-views-container {
  display: flex;
  gap: 16px;
}

.recent-view-card {
  flex: 0 0 200px;
  width: 200px;
  border-radius: 12px;
  overflow: hidden;
  background: var(--surface-card);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  }
}

.card-thumbnail {
  position: relative;
  width: 100%;
  height: 150px;
  overflow: hidden;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .view-time {
    position: absolute;
    bottom: 8px;
    right: 8px;
    background: rgba(0, 0, 0, 0.7);
    color: white;
    padding: 4px 8px;
    border-radius: 4px;
    font-size: 11px;
    font-weight: 500;
  }
}

.card-info {
  padding: 12px;
}

.recipe-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-color);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recipe-author {
  font-size: 12px;
  color: var(--text-color-secondary);
  margin-bottom: 8px;
}

.recipe-stats {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--text-color-secondary);
  
  span {
    display: flex;
    align-items: center;
    gap: 4px;
    
    i {
      font-size: 11px;
    }
  }
}

.loading-container,
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  text-align: center;
  color: var(--text-color-secondary);
  
  p {
    margin-top: 16px;
    font-size: 14px;
  }
}

.empty-container {
  .empty-hint {
    font-size: 12px;
    color: var(--text-color-secondary);
    opacity: 0.7;
    margin-top: 8px;
  }
}

@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .recent-view-card {
    flex: 0 0 180px;
    width: 180px;
  }
  
  .card-thumbnail {
    height: 130px;
  }
}
</style>
