<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import type { Recipe } from '@/types/recipe';

interface Props {
    recipe: Recipe;
    showStats?: boolean;
    showAuthor?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
    showStats: true,
    showAuthor: true
});

const router = useRouter();

// 레시피 상세 페이지로 이동
const goToDetail = () => {
    router.push(`/recipe/${props.recipe.id}`);
};

// 썸네일 이미지 URL (없으면 기본 이미지)
const thumbnailUrl = computed(() => {
    return props.recipe.thumbnail || '/images/default-recipe.png';
});

// 카테고리 문자열 (첫 번째 카테고리만)
const categoryName = computed(() => {
    if (props.recipe.categories && props.recipe.categories.length > 0) {
        return props.recipe.categories[0].codeName || props.recipe.categories[0].detailName || '';
    }
    return '';
});

// 날짜 포맷팅
const formatDate = (dateString?: string) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
};
</script>

<template>
    <div class="recipe-list-item card cursor-pointer hover:shadow-lg transition-shadow" @click="goToDetail">
        <!-- 썸네일 이미지 -->
        <div class="recipe-thumbnail">
            <img :src="thumbnailUrl" :alt="recipe.title" class="w-full h-48 object-cover rounded-t-lg" />
        </div>

        <!-- 레시피 정보 -->
        <div class="p-4">
            <!-- 카테고리 -->
            <div v-if="categoryName" class="recipe-list-item__category">
                {{ categoryName }}
            </div>

            <!-- 제목 (모바일: 대시보드 섹션 타이틀보다 한 단계 작게) -->
            <h3 class="recipe-list-item__title line-clamp-2">
                {{ recipe.title }}
            </h3>

            <!-- 설명 -->
            <p v-if="recipe.description" class="recipe-list-item__desc line-clamp-2">
                {{ recipe.description }}
            </p>

            <!-- 통계 정보 -->
            <div v-if="showStats" class="recipe-list-item__stats">
                <span class="recipe-list-item__stat">
                    <i class="pi pi-eye"></i>
                    {{ recipe.hits || 0 }}
                </span>
                <span class="recipe-list-item__stat">
                    <i class="pi pi-heart"></i>
                    {{ recipe.favoriteCount || 0 }}
                </span>
                <span class="recipe-list-item__stat">
                    <i class="pi pi-comment"></i>
                    {{ recipe.commentCount || 0 }}
                </span>
            </div>

            <!-- 작성자 정보 -->
            <div v-if="showAuthor" class="recipe-list-item__author-row">
                <div class="recipe-list-item__author">
                    <img v-if="recipe.memberProfileImage" :src="recipe.memberProfileImage" :alt="recipe.memberNickname || recipe.memberName" class="w-8 h-8 rounded-full" />
                    <div v-else class="w-8 h-8 rounded-full bg-gray-300 flex items-center justify-center">
                        <i class="pi pi-user text-gray-500"></i>
                    </div>
                    <span class="recipe-list-item__author-name">
                        {{ recipe.memberNickname || recipe.memberName }}
                    </span>
                </div>
                <span class="recipe-list-item__date">
                    {{ formatDate(recipe.createdAt) }}
                </span>
            </div>
        </div>
    </div>
</template>

<style scoped lang="scss">
.recipe-list-item {
    transition: all 0.3s ease;
}

.recipe-list-item:hover {
    transform: translateY(-4px);
}

.recipe-thumbnail {
    overflow: hidden;
    border-radius: 0.5rem 0.5rem 0 0;
}

.recipe-thumbnail img {
    transition: transform 0.3s ease;
}

.recipe-list-item:hover .recipe-thumbnail img {
    transform: scale(1.05);
}

.line-clamp-2 {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.recipe-list-item__category {
    font-size: 0.6875rem;
    font-weight: 600;
    color: var(--primary-color, #f97316);
    margin-bottom: 0.375rem;
    text-transform: none;
    letter-spacing: -0.01em;
}

.recipe-list-item__title {
    font-size: 1rem;
    font-weight: 600;
    line-height: 1.35;
    margin: 0 0 0.5rem;
    color: var(--text-color);
}

.recipe-list-item__desc {
    font-size: 0.8125rem;
    line-height: 1.45;
    color: var(--text-color-secondary);
    margin: 0 0 1rem;
}

.recipe-list-item__stats {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 0.75rem 1rem;
    font-size: 0.8125rem;
    color: var(--text-color-secondary);
    margin-bottom: 0.75rem;
}

.recipe-list-item__stat {
    display: inline-flex;
    align-items: center;
    gap: 0.25rem;
}

.recipe-list-item__author-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 0.5rem;
}

.recipe-list-item__author {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    min-width: 0;
}

.recipe-list-item__author-name {
    font-size: 0.8125rem;
    font-weight: 500;
    color: var(--text-color);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.recipe-list-item__date {
    font-size: 0.6875rem;
    color: var(--text-color-secondary);
    flex-shrink: 0;
}

@media (max-width: 767px) {
    .recipe-list-item__category {
        font-size: 0.625rem;
        margin-bottom: 0.25rem;
    }

    .recipe-list-item__title {
        font-size: 0.9375rem;
        font-weight: 600;
        margin-bottom: 0.375rem;
    }

    .recipe-list-item__desc {
        font-size: 0.75rem;
        margin-bottom: 0.75rem;
    }

    .recipe-list-item__stats {
        font-size: 0.75rem;
        gap: 0.5rem 0.75rem;
        margin-bottom: 0.625rem;
    }

    .recipe-list-item__author-name {
        font-size: 0.75rem;
    }
}

@media (min-width: 768px) {
    .recipe-list-item__category {
        font-size: 0.8125rem;
        margin-bottom: 0.5rem;
    }

    .recipe-list-item__title {
        font-size: 1.25rem;
        font-weight: 700;
    }

    .recipe-list-item__desc {
        font-size: 0.875rem;
    }
}
</style>
