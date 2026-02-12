<template>
    <div class="following-feed-container">
        <div class="card">
            <div class="feed-header">
                <h1 class="text-3xl font-bold m-0">팔로잉 피드</h1>
                <p class="text-secondary mt-2">팔로우한 크리에이터들의 최신 레시피를 확인하세요</p>
            </div>

            <!-- 로그인하지 않은 경우 -->
            <PageStateBlock
                v-if="!isLoggedIn"
                state="empty"
                empty-icon="pi pi-lock"
                empty-title="로그인이 필요합니다"
                empty-message="팔로잉 피드를 보려면 로그인해 주세요."
                empty-button-label="로그인하기"
                @empty-action="goToLogin"
            />

            <!-- 로딩 상태 -->
            <PageStateBlock
                v-else-if="loading"
                state="loading"
                loading-message="레시피를 불러오는 중..."
            />

            <!-- 에러 상태 -->
            <PageStateBlock
                v-else-if="error"
                state="error"
                error-title="레시피를 불러올 수 없습니다"
                :error-message="error"
                retry-label="다시 시도"
                @retry="loadFeed"
            />

            <!-- 팔로우한 사람이 없거나 레시피가 없는 경우 -->
            <PageStateBlock
                v-else-if="recipes.length === 0"
                state="empty"
                empty-icon="pi pi-users"
                empty-title="팔로잉 피드가 비어있습니다"
                empty-message="팔로우한 크리에이터가 없거나, 아직 레시피를 등록하지 않았습니다."
                empty-button-label="크리에이터 탐색하기"
                @empty-action="goToRecipeList"
            />

            <!-- 레시피 목록 -->
            <div v-else class="feed-content">
                <div class="recipe-grid">
                    <RecipeCard
                        v-for="recipe in recipes"
                        :key="recipe.id"
                        :recipe="recipe"
                        @click="goToRecipeDetail(recipe.id)"
                    />
                </div>

                <!-- 페이지네이션 -->
                <div v-if="totalPages > 1" class="flex justify-center mt-6">
                    <Paginator
                        v-model:first="first"
                        :rows="pageSize"
                        :totalRecords="totalRecipes"
                        @page="onPageChange"
                    />
                </div>

                <!-- 더 불러오기 버튼 (무한 스크롤 대신) -->
                <div v-if="hasMore" class="flex justify-center mt-4">
                    <Button
                        label="더 보기"
                        icon="pi pi-refresh"
                        outlined
                        @click="loadMore"
                        :loading="loadingMore"
                    />
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { useAppToast } from '@/utils/toast';
import { getFollowingFeed } from '@/api/recipeApi';
import PageStateBlock from '@/components/common/PageStateBlock.vue';
import RecipeCard from '@/components/recipe/RecipeCard.vue';
import Button from 'primevue/button';
import Paginator from 'primevue/paginator';
import type { Recipe } from '@/types/recipe';
import type { PageState } from 'primevue/paginator';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const { showError } = useAppToast();

const recipes = ref<Recipe[]>([]);
const loading = ref(true);
const loadingMore = ref(false);
const error = ref<string | null>(null);
const currentPage = ref(0);
const pageSize = 20;
const totalRecipes = ref(0);
const totalPages = ref(0);
const first = ref(0);

const isLoggedIn = computed(() => authStore.isLoggedIn);
const hasMore = computed(() => recipes.value.length > 0 && currentPage.value < totalPages.value - 1);

const loadFeed = async (page: number = 0, append: boolean = false) => {
    if (!isLoggedIn.value) {
        loading.value = false;
        return;
    }

    if (append) {
        loadingMore.value = true;
    } else {
        loading.value = true;
    }
    
    error.value = null;

    try {
        const feedRecipes = await getFollowingFeed(page, pageSize);
        
        if (append) {
            recipes.value = [...recipes.value, ...feedRecipes];
        } else {
            recipes.value = feedRecipes;
        }
        
        currentPage.value = page;
        // API가 페이지 정보를 반환하지 않는 경우 대략적으로 계산
        totalRecipes.value = feedRecipes.length < pageSize ? (page * pageSize + feedRecipes.length) : (page + 2) * pageSize;
        totalPages.value = Math.ceil(totalRecipes.value / pageSize);
    } catch (err) {
        console.error('팔로잉 피드 로드 실패:', err);
        error.value = (err instanceof Error ? err.message : null) || '레시피를 불러오는데 실패했습니다.';
        showError(error.value);
    } finally {
        loading.value = false;
        loadingMore.value = false;
    }
};

const loadMore = () => {
    loadFeed(currentPage.value + 1, true);
};

const goToRecipeDetail = (recipeId: number) => {
    router.push(`/recipe/${recipeId}`);
};

const goToLogin = () => {
    router.push({ path: '/auth/login', query: { redirect: route.fullPath } });
};

const goToRecipeList = () => {
    router.push('/recipe/category');
};

const onPageChange = (event: PageState) => {
    first.value = event.first;
    loadFeed(event.page);
    window.scrollTo({ top: 0, behavior: 'smooth' });
};

onMounted(() => {
    loadFeed();
});
</script>

<style scoped lang="scss">
.following-feed-container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 2rem;

    .feed-header {
        margin-bottom: 2rem;
        padding-bottom: 1.5rem;
        border-bottom: 2px solid var(--surface-border);

        .text-secondary {
            color: var(--text-color-secondary);
        }
    }

    .feed-content {
        .recipe-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
            gap: 1.5rem;
        }
    }
}

@media (max-width: 768px) {
    .following-feed-container {
        padding: 1rem;

        .feed-content {
            .recipe-grid {
                grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
                gap: 1rem;
            }
        }
    }
}
</style>
