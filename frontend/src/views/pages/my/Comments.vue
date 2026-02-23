<script setup lang="ts">
import PageStateBlock from '@/components/common/PageStateBlock.vue';
import Paginator from 'primevue/paginator';
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { getMyComments } from '@/api/recipeApi';
import type { MyCommentItem } from '@/types/recipe';
import type { PageState } from 'primevue/paginator';

const router = useRouter();
const authStore = useAuthStore();

const commentItems = ref<MyCommentItem[]>([]);
const totalComments = ref(0);
const first = ref(0);
const rows = ref(10);
const loading = ref(false);
const error = ref<string | null>(null);

const currentMemberId = computed(() => authStore.memberInfo?.id ?? null);

const loadMyComments = async () => {
    const memberId = currentMemberId.value;
    if (!memberId) {
        error.value = '로그인이 필요합니다.';
        return;
    }

    try {
        loading.value = true;
        error.value = null;
        const page = Math.floor(first.value / rows.value);
        const response = await getMyComments(memberId, page, rows.value);
        commentItems.value = response.content ?? [];
        totalComments.value = response.totalElements ?? 0;
    } catch (err: unknown) {
        console.error('내 댓글 로드 실패:', err);
        error.value = (err instanceof Error ? err.message : null) || '내 댓글을 불러오는데 실패했습니다.';
        commentItems.value = [];
    } finally {
        loading.value = false;
    }
};

const goToRecipeComments = (recipeId: number): void => {
    router.push(`/recipe/${recipeId}#comments`);
};

const goToLogin = (): void => {
    router.push({ path: '/auth/login', query: { redirect: '/my?tab=comments' } });
};

const browseRecipes = (): void => {
    router.push('/recipe/category');
};

const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
};

const onPageChange = (event: PageState): void => {
    first.value = event.first;
    rows.value = event.rows;
};

onMounted(() => {
    if (currentMemberId.value) {
        loadMyComments();
    } else {
        error.value = null;
    }
});

watch(currentMemberId, (id) => {
    if (id) {
        first.value = 0;
        loadMyComments();
    } else {
        commentItems.value = [];
        totalComments.value = 0;
        error.value = '로그인이 필요합니다.';
    }
});
</script>

<template>
    <div class="page-container page-container--card">
        <div class="comments-content">
            <div class="mb-6 p-4 bg-orange-50 border-l-4 border-orange-500 rounded-r">
                <p class="text-gray-700 italic">카드를 클릭하면 해당 레시피 상세 페이지의 댓글 영역으로 이동해요.</p>
            </div>

            <div class="comments-section">
                <div class="flex justify-between items-center mb-3">
                    <h2 class="text-2xl font-semibold text-gray-900 m-0">내가 작성한 댓글 ({{ totalComments }})</h2>
                </div>

                <!-- 로딩 / 에러 / 비로그인 / 빈 상태 -->
                <PageStateBlock v-if="loading" state="loading" loading-message="내 댓글을 불러오는 중..." />
                <PageStateBlock v-else-if="error" state="error" error-title="내 댓글을 불러올 수 없습니다" :error-message="error" retry-label="다시 시도" @retry="loadMyComments" />
                <PageStateBlock v-else-if="!currentMemberId" state="empty" empty-icon="pi pi-lock" empty-title="로그인이 필요합니다" empty-message="내 댓글을 보려면 로그인해 주세요." empty-button-label="로그인하기" @empty-action="goToLogin" />
                <PageStateBlock
                    v-else-if="commentItems.length === 0"
                    state="empty"
                    empty-icon="pi pi-comments"
                    empty-title="작성한 댓글이 없습니다"
                    empty-message="레시피에 댓글을 남겨보세요!"
                    empty-button-label="레시피 둘러보기"
                    @empty-action="browseRecipes"
                />

                <!-- 댓글 카드 목록 -->
                <template v-else>
                    <div class="comment-cards">
                        <div
                            v-for="item in commentItems"
                            :key="item.comment.id"
                            class="comment-card"
                            role="button"
                            tabindex="0"
                            @click="goToRecipeComments(item.recipeId)"
                            @keydown.enter="goToRecipeComments(item.recipeId)"
                            @keydown.space.prevent="goToRecipeComments(item.recipeId)"
                        >
                            <!-- 좌측: 레시피 정보 -->
                            <div class="comment-card__recipe">
                                <div class="comment-card__recipe-thumb">
                                    <img :src="item.recipeThumbnail || '/placeholder-recipe.jpg'" :alt="item.recipeTitle" class="comment-card__recipe-img" />
                                </div>
                                <h3 class="comment-card__recipe-title">{{ item.recipeTitle }}</h3>
                            </div>
                            <!-- 우측: 댓글 정보 -->
                            <div class="comment-card__comment">
                                <p class="comment-card__comment-content">{{ item.comment.content }}</p>
                                <div v-if="item.comment.imageUrl" class="comment-card__comment-image-wrap">
                                    <img :src="item.comment.imageUrl" alt="댓글 이미지" class="comment-card__comment-image" />
                                </div>
                                <div class="comment-card__comment-meta">
                                    <span class="comment-card__comment-date">{{ formatDate(item.comment.createdAt) }}</span>
                                    <span v-if="item.comment.updatedAt && item.comment.updatedAt !== item.comment.createdAt" class="comment-card__comment-edited">(수정됨)</span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="flex justify-center mt-4">
                        <Paginator v-model:first="first" :rows="rows" :total-records="totalComments" template="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink" @page="onPageChange" />
                    </div>
                </template>
            </div>
        </div>
    </div>
</template>

<style scoped>
.comments-content {
    min-height: 240px;
}

.comments-section {
    width: 100%;
}

.comment-cards {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 1rem;
}

.comment-card {
    display: flex;
    align-items: stretch;
    gap: 1.25rem;
    padding: 1rem;
    background: var(--p-card-background);
    border: 1px solid var(--p-card-border-color);
    border-radius: var(--p-card-border-radius);
    box-shadow: var(--p-card-shadow);
    cursor: pointer;
    transition:
        box-shadow 0.2s,
        border-color 0.2s;
    text-align: left;
}

.comment-card:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    border-color: var(--p-primary-color);
}

.comment-card:focus-visible {
    outline: 2px solid var(--p-primary-color);
    outline-offset: 2px;
}

.comment-card__recipe {
    flex-shrink: 0;
    width: 160px;
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.comment-card__recipe-thumb {
    width: 100%;
    aspect-ratio: 1;
    border-radius: 8px;
    overflow: hidden;
    background: var(--p-surface-100);
}

.comment-card__recipe-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.comment-card__recipe-title {
    font-size: 0.95rem;
    font-weight: 600;
    color: var(--p-text-color);
    margin: 0;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.comment-card__comment {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.comment-card__comment-content {
    margin: 0;
    font-size: 0.95rem;
    color: var(--p-text-color);
    display: -webkit-box;
    -webkit-line-clamp: 3;
    line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
    white-space: pre-wrap;
}

.comment-card__comment-image-wrap {
    width: 64px;
    height: 64px;
    flex-shrink: 0;
    border-radius: 6px;
    overflow: hidden;
    border: 1px solid var(--p-surface-200);
}

.comment-card__comment-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.comment-card__comment-meta {
    font-size: 0.8rem;
    color: var(--p-text-muted-color);
}

.comment-card__comment-edited {
    margin-left: 0.25rem;
}

@media (max-width: 900px) {
    .comment-cards {
        grid-template-columns: 1fr;
    }
}

@media (max-width: 640px) {
    .comment-card {
        flex-direction: column;
        gap: 0.75rem;
    }

    .comment-card__recipe {
        width: 100%;
        flex-direction: row;
        align-items: center;
        gap: 0.75rem;
    }

    .comment-card__recipe-thumb {
        width: 72px;
        aspect-ratio: 1;
    }

    .comment-card__recipe-title {
        -webkit-line-clamp: 2;
        line-clamp: 2;
    }
}
</style>
