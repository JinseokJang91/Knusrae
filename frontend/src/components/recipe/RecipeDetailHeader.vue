<script setup lang="ts">
import Button from 'primevue/button';
import type { RecipeDetail, RecipeImage } from '@/types/recipe';

defineProps<{
    recipe: RecipeDetail;
    mainImage: RecipeImage | null;
    cookingTipsData: { servings: string | null; cookingTime: string | null; difficulty: string | null };
    isLiked: boolean;
    /** 북마크 선택 여부 (하나라도 레시피북에 저장된 경우 true) */
    isBookmarked?: boolean;
    formatNumber: (num: number | null | undefined) => string;
    /** 현재 사용자가 레시피 작성자인지 여부 */
    isRecipeAuthor?: boolean;
    /** 팔로우 여부 */
    isFollowing?: boolean;
    /** 팔로우 버튼 비활성화 여부 (로그인하지 않은 경우) */
    followDisabled?: boolean;
}>();

defineEmits<{
    'go-back': [];
    'toggle-like': [];
    'toggle-bookmark': [];
    'toggle-follow': [];
    'go-to-author-profile': [];
}>();
</script>

<template>
    <div class="recipe-detail-header bg-white rounded-2xl shadow-lg overflow-hidden mb-8">
        <!-- 메인 이미지 -->
        <div class="recipe-detail-header__image relative w-full h-96 bg-white">
            <img v-if="mainImage" :src="mainImage.url" :alt="recipe.title" class="w-full mx-auto h-full object-cover" />
            <div v-else class="flex items-center justify-center h-full text-white text-6xl">🍳</div>

            <!-- 뒤로가기 버튼 -->
            <div class="absolute top-4 left-4 z-10">
                <Button @click="$emit('go-back')" icon="pi pi-arrow-left" size="large" rounded />
            </div>

            <!-- 액션 버튼 (좋아요, 북마크) -->
            <div class="absolute top-4 right-4 z-10 flex gap-2">
                <Button @click="$emit('toggle-like')" :icon="isLiked ? 'pi pi-heart-fill' : 'pi pi-heart'" :class="isLiked ? 'p-button-danger' : 'p-button-secondary'" size="large" rounded />
                <Button @click="$emit('toggle-bookmark')" :icon="isBookmarked ? 'pi pi-bookmark-fill' : 'pi pi-bookmark'" :class="isBookmarked ? 'p-button-primary' : 'p-button-secondary'" size="large" rounded />
            </div>
        </div>

        <!-- 레시피 기본 정보 -->
        <div class="recipe-detail-header__body p-8">
            <div class="recipe-detail-header__main flex items-start justify-between mb-6">
                <div class="flex-1">
                    <h1 class="recipe-detail-header__title text-4xl font-bold text-gray-800 mb-4">{{ recipe.title }}</h1>
                    <div class="recipe-intro-bubble" v-if="recipe.introduction">
                        <p class="recipe-intro-bubble__text">{{ recipe.introduction }}</p>
                    </div>

                    <!-- 태그 -->
                    <div class="flex flex-wrap gap-2 mb-4">
                        <span v-for="category in recipe.categories" :key="`${category.codeId}-${category.detailCodeId}`" class="px-3 py-1 bg-amber-100 text-amber-800 rounded-full text-sm font-medium">
                            {{ category.detailName || category.codeName }}
                        </span>
                    </div>

                    <!-- 레시피 상세 정보 (요리팁 + 통계) -->
                    <div v-if="cookingTipsData.servings || cookingTipsData.cookingTime || cookingTipsData.difficulty" class="recipe-detail-header__tips flex items-center justify-between gap-6 mb-4 p-4 bg-gray-50 rounded-lg">
                        <div class="flex flex-wrap gap-6">
                            <div v-if="cookingTipsData.servings" class="flex items-center space-x-2">
                                <i class="pi pi-users text-gray-600 text-xl"></i>
                                <span class="text-gray-700 font-medium">{{ cookingTipsData.servings }}</span>
                            </div>
                            <div v-if="cookingTipsData.cookingTime" class="flex items-center space-x-2">
                                <i class="pi pi-clock text-gray-600 text-xl"></i>
                                <span class="text-gray-700 font-medium">{{ cookingTipsData.cookingTime }}</span>
                            </div>
                            <div v-if="cookingTipsData.difficulty" class="flex items-center space-x-2">
                                <i class="pi pi-star text-yellow-600 text-xl"></i>
                                <span class="text-gray-700 font-medium">{{ cookingTipsData.difficulty }}</span>
                            </div>
                        </div>
                        <div class="recipe-detail-header__stats flex items-center space-x-6 text-gray-600">
                            <div class="text-center">
                                <div class="text-2xl font-bold text-gray-600">{{ formatNumber(recipe.hits) }}</div>
                                <div class="text-sm">조회수</div>
                            </div>
                            <div class="text-center">
                                <div class="text-2xl font-bold text-gray-600">{{ formatNumber(recipe.stats?.totalComments) }}</div>
                                <div class="text-sm">댓글</div>
                            </div>
                            <div class="text-center">
                                <div class="text-2xl font-bold text-red-600">{{ formatNumber(recipe.stats?.favoriteCount) }}</div>
                                <div class="text-sm">찜</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 작성자 정보 -->
            <div class="flex items-center justify-between py-4 border-t border-gray-200">
                <div class="flex items-center space-x-3">
                    <div class="w-12 h-12 bg-gray-300 rounded-full flex items-center justify-center overflow-hidden cursor-pointer hover:opacity-80 transition-opacity" @click="$emit('go-to-author-profile')">
                        <img v-if="recipe.memberProfileImage" :src="recipe.memberProfileImage" alt="작성자 프로필" class="w-full h-full object-cover" />
                        <i v-else class="pi pi-user text-gray-600"></i>
                    </div>
                    <div>
                        <div class="text-lg font-medium text-gray-800 cursor-pointer hover:text-primary-600 transition-colors" @click="$emit('go-to-author-profile')">
                            {{ recipe.memberNickname || recipe.memberName }}
                        </div>
                    </div>
                </div>
                <div class="flex items-center gap-2">
                    <!-- 팔로우 버튼 (본인이 아닌 경우만) -->
                    <Button
                        v-if="!isRecipeAuthor"
                        :label="isFollowing ? '팔로잉' : '팔로우'"
                        :icon="isFollowing ? 'pi pi-check' : 'pi pi-plus'"
                        :severity="isFollowing ? 'secondary' : 'primary'"
                        :outlined="isFollowing"
                        size="small"
                        @click="$emit('toggle-follow')"
                        :disabled="followDisabled"
                    />
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.recipe-intro-bubble {
    position: relative;
    max-width: 100%;
    margin-bottom: 1rem;
    padding: 1rem 1.25rem;
    background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
    border-radius: 20px;
    border: 2px solid #fcd34d;
    box-shadow: 0 2px 8px rgba(252, 211, 77, 0.2);
}

.recipe-intro-bubble::before {
    content: '';
    position: absolute;
    top: -8px;
    left: 1.5rem;
    width: 0;
    height: 0;
    border-left: 10px solid transparent;
    border-right: 10px solid transparent;
    border-bottom: 10px solid #fcd34d;
}

.recipe-intro-bubble::after {
    content: '';
    position: absolute;
    top: -5px;
    left: 1.6rem;
    width: 0;
    height: 0;
    border-left: 8px solid transparent;
    border-right: 8px solid transparent;
    border-bottom: 8px solid #fffbeb;
}

.recipe-intro-bubble__text {
    margin: 0;
    font-size: 1.125rem;
    line-height: 1.65;
    color: #92400e;
}

/* 반응형: 태블릿 */
@media (max-width: 768px) {
    .recipe-detail-header__image {
        height: 16rem;
    }
    .recipe-detail-header__body {
        padding: 1.5rem;
    }
    .recipe-detail-header__title {
        font-size: 1.75rem;
    }
    .recipe-detail-header__main {
        flex-direction: column;
        gap: 1rem;
    }
    .recipe-detail-header__tips {
        flex-direction: column;
        align-items: flex-start;
        gap: 1rem;
        padding: 1rem;
    }
    .recipe-detail-header__stats {
        width: 100%;
        justify-content: space-between;
        padding-top: 0.75rem;
        border-top: 1px solid rgba(0, 0, 0, 0.06);
    }
}

/* 반응형: 모바일 */
@media (max-width: 480px) {
    .recipe-detail-header__image {
        height: 12rem;
    }
    .recipe-detail-header__body {
        padding: 1rem;
    }
    .recipe-detail-header__title {
        font-size: 1.35rem;
    }
    .recipe-intro-bubble {
        padding: 0.75rem 1rem;
    }
    .recipe-intro-bubble__text {
        font-size: 0.95rem;
    }
}
</style>
