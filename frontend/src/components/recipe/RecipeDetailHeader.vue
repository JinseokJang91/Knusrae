<script setup lang="ts">
import { computed } from 'vue';
import Button from 'primevue/button';
import type { RecipeDetail, RecipeImage } from '@/types/recipe';

const props = defineProps<{
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

const hasCookingTipsMeta = computed(() => !!(props.cookingTipsData.servings || props.cookingTipsData.cookingTime || props.cookingTipsData.difficulty));

defineEmits<{
    'go-back': [];
    'toggle-like': [];
    'toggle-bookmark': [];
    'toggle-follow': [];
    'go-to-author-profile': [];
}>();
</script>

<template>
    <div class="recipe-detail-header bg-white rounded-xl shadow-lg overflow-hidden mb-6 md:rounded-2xl md:mb-8">
        <!-- 메인 이미지 -->
        <div class="recipe-detail-header__image relative w-full h-96 bg-white">
            <img v-if="mainImage" :src="mainImage.url" :alt="recipe.title" class="w-full mx-auto h-full object-cover" />
            <div v-else class="flex items-center justify-center h-full text-white text-6xl">🍳</div>

            <!-- 뒤로가기 버튼 -->
            <div class="recipe-detail-header__fab recipe-detail-header__fab--back absolute z-10">
                <Button class="recipe-hero-btn recipe-hero-btn--back" @click="$emit('go-back')" icon="pi pi-arrow-left" size="large" rounded aria-label="뒤로가기" />
            </div>

            <!-- 액션 버튼 (좋아요, 북마크) -->
            <div class="recipe-detail-header__fab recipe-detail-header__fab--actions absolute z-10 flex gap-2">
                <Button
                    class="recipe-hero-btn recipe-hero-btn--like"
                    :class="{ 'recipe-hero-btn--liked': isLiked }"
                    @click="$emit('toggle-like')"
                    :icon="isLiked ? 'pi pi-heart-fill' : 'pi pi-heart'"
                    size="large"
                    rounded
                    :aria-pressed="isLiked"
                    aria-label="찜"
                />
                <Button
                    class="recipe-hero-btn recipe-hero-btn--bookmark"
                    :class="{ 'recipe-hero-btn--bookmarked': isBookmarked }"
                    @click="$emit('toggle-bookmark')"
                    :icon="isBookmarked ? 'pi pi-bookmark-fill' : 'pi pi-bookmark'"
                    size="large"
                    rounded
                    :aria-pressed="!!isBookmarked"
                    aria-label="북마크"
                />
            </div>
        </div>

        <!-- 레시피 기본 정보 -->
        <div class="recipe-detail-header__body p-4 sm:p-6 md:p-8">
            <div class="recipe-detail-header__main flex items-start justify-between mb-4 md:mb-6">
                <div class="flex-1 min-w-0">
                    <h1 class="recipe-detail-header__title font-bold mb-3 md:mb-4">{{ recipe.title }}</h1>
                    <div class="recipe-intro-bubble" v-if="recipe.introduction">
                        <p class="recipe-intro-bubble__text">{{ recipe.introduction }}</p>
                    </div>

                    <!-- 태그 -->
                    <div class="flex flex-wrap gap-1.5 sm:gap-2 mb-3 md:mb-4">
                        <span v-for="category in recipe.categories" :key="`${category.codeId}-${category.detailCodeId}`" class="px-2.5 py-0.5 sm:px-3 sm:py-1 bg-amber-100 text-amber-800 rounded-full text-xs sm:text-sm font-medium">
                            {{ category.detailName || category.codeName }}
                        </span>
                    </div>

                    <!-- 요리 팁(인분·시간·난이도) + 통계: 팁이 없어도 조회/댓글/찜은 항상 표시 -->
                    <div class="recipe-detail-header__tips flex items-center justify-between gap-4 md:gap-6 mb-4 p-3 sm:p-4 bg-gray-50 rounded-lg md:rounded-lg" :class="{ 'recipe-detail-header__tips--stats-only': !hasCookingTipsMeta }">
                        <div v-if="hasCookingTipsMeta" class="recipe-detail-header__tips-meta flex flex-nowrap items-center gap-1.5 min-w-0 sm:gap-3 md:gap-6">
                            <div v-if="cookingTipsData.servings" class="recipe-detail-header__tip-item flex items-center gap-1 min-w-0 sm:gap-2">
                                <i class="pi pi-users recipe-detail-header__tip-icon text-gray-600 shrink-0"></i>
                                <span class="recipe-detail-header__tip-text text-gray-700 font-medium truncate">{{ cookingTipsData.servings }}</span>
                            </div>
                            <div v-if="cookingTipsData.cookingTime" class="recipe-detail-header__tip-item flex items-center gap-1 min-w-0 sm:gap-2">
                                <i class="pi pi-clock recipe-detail-header__tip-icon text-gray-600 shrink-0"></i>
                                <span class="recipe-detail-header__tip-text text-gray-700 font-medium truncate">{{ cookingTipsData.cookingTime }}</span>
                            </div>
                            <div v-if="cookingTipsData.difficulty" class="recipe-detail-header__tip-item flex items-center gap-1 min-w-0 sm:gap-2">
                                <i class="pi pi-star recipe-detail-header__tip-icon text-yellow-600 shrink-0"></i>
                                <span class="recipe-detail-header__tip-text text-gray-700 font-medium truncate">{{ cookingTipsData.difficulty }}</span>
                            </div>
                        </div>
                        <div class="recipe-detail-header__stats flex items-center text-gray-600 shrink-0" :class="{ 'recipe-detail-header__stats--below-tips': hasCookingTipsMeta }">
                            <div class="recipe-detail-header__stat-cell text-center px-2 sm:px-4">
                                <div class="recipe-detail-header__stat-value text-gray-600">{{ formatNumber(recipe.hits) }}</div>
                                <div class="recipe-detail-header__stat-label">조회수</div>
                            </div>
                            <div class="recipe-detail-header__stat-cell text-center px-2 sm:px-4">
                                <div class="recipe-detail-header__stat-value text-gray-600">{{ formatNumber(recipe.stats?.totalComments) }}</div>
                                <div class="recipe-detail-header__stat-label">댓글</div>
                            </div>
                            <div class="recipe-detail-header__stat-cell text-center px-2 sm:px-4">
                                <div class="recipe-detail-header__stat-value text-red-600">{{ formatNumber(recipe.stats?.favoriteCount) }}</div>
                                <div class="recipe-detail-header__stat-label">찜</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 작성자 정보: 닉네임과 팔로우 버튼 동일 행(모바일 포함) -->
            <div class="recipe-detail-header__author flex flex-row items-center justify-between gap-2 py-3 sm:py-4 border-t border-gray-200 min-w-0">
                <div class="flex items-center gap-2 sm:gap-3 min-w-0 flex-1">
                    <div class="w-11 h-11 sm:w-12 sm:h-12 bg-gray-300 rounded-full flex items-center justify-center overflow-hidden cursor-pointer shrink-0 hover:opacity-80 transition-opacity" @click="$emit('go-to-author-profile')">
                        <img v-if="recipe.memberProfileImage" :src="recipe.memberProfileImage" alt="작성자 프로필" class="w-full h-full object-cover" />
                        <i v-else class="pi pi-user text-gray-600"></i>
                    </div>
                    <div class="min-w-0 flex-1">
                        <div class="text-base sm:text-lg font-medium text-gray-800 cursor-pointer hover:text-primary-600 transition-colors truncate" @click="$emit('go-to-author-profile')">
                            {{ recipe.memberNickname || recipe.memberName }}
                        </div>
                    </div>
                </div>
                <div v-if="!isRecipeAuthor" class="shrink-0">
                    <Button
                        class="recipe-detail-header__follow-btn"
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

/* 제목: 요리 앱에서 흔히 쓰는 딥 블루 톤 (가독성) */
.recipe-detail-header__title {
    color: #1e3a5f;
    line-height: 1.25;
    word-break: keep-all;
    font-size: 2.25rem;
}

@media (max-width: 1024px) {
    .recipe-detail-header__title {
        font-size: 1.875rem;
    }
}

/* 히어로 플로팅 버튼 — 노치·라운드 코너 대응 */
.recipe-detail-header__fab--back {
    top: calc(0.75rem + env(safe-area-inset-top, 0px));
    left: calc(0.75rem + env(safe-area-inset-left, 0px));
}

.recipe-detail-header__fab--actions {
    top: calc(0.75rem + env(safe-area-inset-top, 0px));
    right: calc(0.75rem + env(safe-area-inset-right, 0px));
}

@media (min-width: 769px) {
    .recipe-detail-header__fab--back {
        top: 1rem;
        left: 1rem;
    }

    .recipe-detail-header__fab--actions {
        top: 1rem;
        right: 1rem;
    }
}

/* 터치 최소 영역 확보 (모바일). class는 루트 .p-button에 합쳐지므로 자손 선택자 금지 */
:deep(.recipe-hero-btn.p-button) {
    width: max(2.75rem, 44px);
    height: max(2.75rem, 44px);
    min-width: max(2.75rem, 44px);
    min-height: max(2.75rem, 44px);
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.12);
}

:deep(.recipe-hero-btn.recipe-hero-btn--back.p-button) {
    background: var(--primary-color, #f97316) !important;
    border-color: var(--primary-color, #f97316) !important;
    color: #fff !important;
}

:deep(.recipe-hero-btn.recipe-hero-btn--back.p-button:hover) {
    filter: brightness(0.95);
}

/* 찜·북마크: 비선택 시 흰 배경 + 주황 아이콘(브랜드 primary) */
:deep(.recipe-hero-btn.recipe-hero-btn--like.p-button),
:deep(.recipe-hero-btn.recipe-hero-btn--bookmark.p-button) {
    background: #fff !important;
    border: 1px solid #e5e7eb !important;
    color: var(--primary-color, #f97316) !important;
}

:deep(.recipe-hero-btn.recipe-hero-btn--like.p-button .p-button-icon),
:deep(.recipe-hero-btn.recipe-hero-btn--bookmark.p-button .p-button-icon) {
    color: inherit;
}

/* 선택됨: 원형 배경만 주황, 아이콘은 fill 글리프 + 흰색으로 테두리·내부까지 동일 톤 */
:deep(.recipe-hero-btn.recipe-hero-btn--like.p-button.recipe-hero-btn--liked),
:deep(.recipe-hero-btn.recipe-hero-btn--bookmark.p-button.recipe-hero-btn--bookmarked) {
    background: var(--primary-color, #f97316) !important;
    border-color: var(--primary-color, #f97316) !important;
    color: #fff !important;
}

:deep(.recipe-hero-btn.recipe-hero-btn--like.p-button.recipe-hero-btn--liked .p-button-icon),
:deep(.recipe-hero-btn.recipe-hero-btn--bookmark.p-button.recipe-hero-btn--bookmarked .p-button-icon) {
    color: #fff !important;
    -webkit-text-fill-color: #fff;
}

:deep(.recipe-hero-btn.recipe-hero-btn--like.p-button.recipe-hero-btn--liked:hover),
:deep(.recipe-hero-btn.recipe-hero-btn--bookmark.p-button.recipe-hero-btn--bookmarked:hover) {
    filter: brightness(0.95);
}

.recipe-detail-header__stat-value {
    font-size: clamp(1.125rem, 4vw, 1.5rem);
    font-weight: 700;
    line-height: 1.2;
}

.recipe-detail-header__stat-label {
    font-size: 0.7rem;
    margin-top: 0.125rem;
    color: #6b7280;
}

@media (min-width: 640px) {
    .recipe-detail-header__stat-label {
        font-size: 0.875rem;
    }
}

.recipe-detail-header__stat-cell + .recipe-detail-header__stat-cell {
    border-left: 1px solid rgba(0, 0, 0, 0.06);
}

.recipe-detail-header__follow-btn :deep(.p-button) {
    justify-content: center;
    padding: 0.35rem 0.55rem;
    font-size: 0.75rem;
    gap: 0.25rem;
    white-space: nowrap;
}

.recipe-detail-header__follow-btn :deep(.p-button .p-button-icon) {
    font-size: 0.7rem;
}

@media (min-width: 640px) {
    .recipe-detail-header__follow-btn :deep(.p-button) {
        padding: 0.5rem 0.75rem;
        font-size: 0.875rem;
    }

    .recipe-detail-header__follow-btn :deep(.p-button .p-button-icon) {
        font-size: 0.875rem;
    }
}

/* 요리 팁이 없을 때: 통계만 회색 카드 안에 균형 있게 배치 */
.recipe-detail-header__tips--stats-only {
    justify-content: center;
}

.recipe-detail-header__tips--stats-only .recipe-detail-header__stats {
    width: 100%;
    justify-content: space-around;
}

.recipe-detail-header__tips-meta {
    flex: 1 1 auto;
    min-width: 0;
}

.recipe-detail-header__tip-icon {
    font-size: 0.8125rem;
}

.recipe-detail-header__tip-text {
    font-size: 0.6875rem;
    line-height: 1.2;
}

@media (min-width: 400px) {
    .recipe-detail-header__tip-icon {
        font-size: 0.875rem;
    }

    .recipe-detail-header__tip-text {
        font-size: 0.75rem;
    }
}

@media (min-width: 640px) {
    .recipe-detail-header__tip-icon {
        font-size: 1rem;
    }

    .recipe-detail-header__tip-text {
        font-size: 0.875rem;
    }
}

@media (min-width: 768px) {
    .recipe-detail-header__tip-icon {
        font-size: 1.125rem;
    }

    .recipe-detail-header__tip-text {
        font-size: 1rem;
    }
}

@media (min-width: 1024px) {
    .recipe-detail-header__tip-icon {
        font-size: 1.25rem;
    }

    .recipe-detail-header__tip-text {
        font-size: 1.125rem;
    }
}

.recipe-detail-header__tip-item {
    flex: 1 1 0;
    min-width: 0;
}

@media (min-width: 768px) {
    .recipe-detail-header__tip-item {
        flex: 0 1 auto;
    }
}
/* 반응형: 태블릿 */
@media (max-width: 768px) {
    .recipe-detail-header__image {
        height: 16rem;
    }
    .recipe-detail-header__title {
        font-size: 1.5rem;
    }
    .recipe-detail-header__main {
        flex-direction: column;
        gap: 0.75rem;
    }
    .recipe-detail-header__tips {
        flex-direction: column;
        align-items: stretch;
        gap: 0.75rem;
    }
    .recipe-detail-header__stats {
        width: 100%;
        justify-content: space-between;
    }
    .recipe-detail-header__stats--below-tips {
        padding-top: 0.75rem;
        border-top: 1px solid rgba(0, 0, 0, 0.06);
    }
}

/* 반응형: 모바일 */
@media (max-width: 480px) {
    .recipe-detail-header__image {
        height: 13rem;
    }
    .recipe-detail-header__title {
        font-size: 1.25rem;
    }
    .recipe-intro-bubble {
        padding: 0.75rem 1rem;
    }
    .recipe-intro-bubble__text {
        font-size: 0.95rem;
    }
}
</style>
