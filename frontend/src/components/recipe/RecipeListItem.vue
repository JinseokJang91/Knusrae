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
            <div v-if="categoryName" class="text-sm text-primary-500 font-semibold mb-2">
                {{ categoryName }}
            </div>

            <!-- 제목 -->
            <h3 class="text-xl font-bold mb-2 line-clamp-2">
                {{ recipe.title }}
            </h3>

            <!-- 설명 -->
            <p v-if="recipe.description" class="text-gray-600 text-sm mb-4 line-clamp-2">
                {{ recipe.description }}
            </p>

            <!-- 통계 정보 -->
            <div v-if="showStats" class="flex items-center gap-4 text-sm text-gray-500 mb-3">
                <span class="flex items-center gap-1">
                    <i class="pi pi-eye"></i>
                    {{ recipe.hits || 0 }}
                </span>
                <span class="flex items-center gap-1">
                    <i class="pi pi-heart"></i>
                    {{ recipe.favoriteCount || 0 }}
                </span>
                <span class="flex items-center gap-1">
                    <i class="pi pi-comment"></i>
                    {{ recipe.commentCount || 0 }}
                </span>
            </div>

            <!-- 작성자 정보 -->
            <div v-if="showAuthor" class="flex items-center justify-between">
                <div class="flex items-center gap-2">
                    <img v-if="recipe.memberProfileImage" :src="recipe.memberProfileImage" :alt="recipe.memberNickname || recipe.memberName" class="w-8 h-8 rounded-full" />
                    <div v-else class="w-8 h-8 rounded-full bg-gray-300 flex items-center justify-center">
                        <i class="pi pi-user text-gray-500"></i>
                    </div>
                    <span class="text-sm font-medium">
                        {{ recipe.memberNickname || recipe.memberName }}
                    </span>
                </div>
                <span class="text-xs text-gray-400">
                    {{ formatDate(recipe.createdAt) }}
                </span>
            </div>
        </div>
    </div>
</template>

<style scoped>
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
</style>
