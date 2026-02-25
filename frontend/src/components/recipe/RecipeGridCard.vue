<script setup lang="ts">
import Button from 'primevue/button';
import Card from 'primevue/card';
import Tag from 'primevue/tag';
import { computed } from 'vue';
import type { RecipeGridItem } from '@/types/recipe';

const props = withDefaults(
    defineProps<{
        recipe: RecipeGridItem;
        /** 카테고리 태그에 표시할 문자열 */
        categoryLabel?: string | null;
        /** 검색어 하이라이트 (지정 시 제목에 하이라이트 적용) */
        highlightKeyword?: string | null;
        /** 찜 버튼 표시 여부 (예: 북마크 페이지에서는 숨김) */
        showFavorite?: boolean;
        /** 북마크 버튼 표시 여부 */
        showBookmark?: boolean;
        /** 북마크 선택 여부 (하나라도 레시피북에 저장된 경우 true) */
        isBookmarked?: boolean;
        /** 댓글 개수 표시 여부 */
        showCommentCount?: boolean;
        /** 찜 목록 모드: 항상 찜 채움 아이콘, 클릭 시 제거 */
        favoritesMode?: boolean;
        /** 작성자 영역 표시 (false면 날짜 등 다른 메타만 표시) */
        showAuthor?: boolean;
        /** 작성자 대신 표시할 날짜 텍스트 (예: 찜한 날짜) */
        dateText?: string | null;
    }>(),
    {
        showFavorite: true,
        showBookmark: true,
        isBookmarked: false,
        showCommentCount: true,
        favoritesMode: false,
        showAuthor: true,
        categoryLabel: null,
        highlightKeyword: null,
        dateText: null
    }
);

defineEmits<{
    click: [recipeId: number];
    favorite: [recipeId: number];
    bookmark: [recipeId: number];
    'scroll-to-comments': [recipeId: number];
}>();

// 조회수 포맷 (만/억)
const hitsText = computed(() => formatCount(props.recipe.hits));

// 댓글 수 포맷
const commentCountText = computed(() => formatCount(props.recipe.commentCount));

// 검색어 하이라이트 분할
const highlightParts = computed(() => {
    if (!props.highlightKeyword || !props.recipe.title) {
        return [];
    }
    const text = props.recipe.title;
    const keyword = props.highlightKeyword;
    const regex = new RegExp(`(${keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi');
    const parts: Array<{ text: string; isHighlight: boolean }> = [];
    let lastIndex = 0;
    let match;
    while ((match = regex.exec(text)) !== null) {
        if (match.index > lastIndex) {
            parts.push({ text: text.substring(lastIndex, match.index), isHighlight: false });
        }
        parts.push({ text: match[0], isHighlight: true });
        lastIndex = regex.lastIndex;
    }
    if (lastIndex < text.length) {
        parts.push({ text: text.substring(lastIndex), isHighlight: false });
    }
    return parts.length > 0 ? parts : [{ text, isHighlight: false }];
});

function formatCount(count: number | undefined | null): string | null {
    if (!count || count === 0) return null;
    if (count >= 100000000) {
        const eok = count / 100000000;
        const rounded = Math.round(eok * 10) / 10;
        return rounded % 1 === 0 ? `${Math.round(rounded)}억` : `${rounded}억`;
    }
    if (count >= 10000) {
        const man = count / 10000;
        const rounded = Math.round(man * 10) / 10;
        return rounded % 1 === 0 ? `${Math.round(rounded)}만` : `${rounded}만`;
    }
    return count.toLocaleString();
}
</script>

<template>
    <div class="recipe-card-wrapper" @click="$emit('click', recipe.id)">
        <Card class="recipe-card h-full">
            <template #header>
                <div class="recipe-image-container">
                    <img :src="recipe.thumbnail || '/placeholder-recipe.jpg'" :alt="recipe.title" class="recipe-image" />
                    <div class="recipe-overlay">
                        <div class="recipe-actions">
                            <Button
                                v-if="showFavorite"
                                :icon="favoritesMode ? 'pi pi-heart-fill' : recipe.isFavorite ? 'pi pi-heart-fill' : 'pi pi-heart'"
                                :class="recipe.isFavorite || favoritesMode ? 'p-button-danger' : 'p-button-secondary'"
                                size="large"
                                rounded
                                @click.stop="$emit('favorite', recipe.id)"
                            />
                            <Button
                                v-if="showBookmark"
                                :icon="isBookmarked ? 'pi pi-bookmark-fill' : 'pi pi-bookmark'"
                                :class="isBookmarked ? 'p-button-primary' : 'p-button-secondary'"
                                size="large"
                                rounded
                                @click.stop="$emit('bookmark', recipe.id)"
                            />
                        </div>
                        <Tag v-if="categoryLabel" :value="categoryLabel" severity="info" class="recipe-category-tag" />
                    </div>
                    <div v-if="hitsText" class="recipe-hits-overlay">조회수 {{ hitsText }}</div>
                </div>
            </template>
            <template #content>
                <div class="recipe-content">
                    <!-- 제목 영역: 최대 2줄 고정 높이 (기존 margin-bottom 0.5rem 유지) -->
                    <div class="recipe-title-zone">
                        <h3 class="recipe-title">
                            <template v-if="highlightKeyword && highlightParts.length">
                                <template v-for="(part, index) in highlightParts" :key="index">
                                    <mark v-if="part.isHighlight" class="bg-yellow-200">{{ part.text }}</mark>
                                    <span v-else>{{ part.text }}</span>
                                </template>
                            </template>
                            <span v-else>{{ recipe.title }}</span>
                        </h3>
                    </div>
                    <div class="recipe-meta">
                        <div class="recipe-info">
                            <div v-if="recipe.cookingTime" class="info-item">
                                <i class="pi pi-clock"></i>
                                <span>{{ recipe.cookingTime }}</span>
                            </div>
                            <div v-if="recipe.servings" class="info-item">
                                <i class="pi pi-users"></i>
                                <span>{{ recipe.servings }}</span>
                            </div>
                        </div>
                        <!-- 작성자/날짜 영역: 표시할 내용이 있을 때만 영역 노출 -->
                        <div v-if="(showAuthor && (recipe.memberNickname || recipe.memberName)) || dateText" class="recipe-author-zone">
                            <div v-if="showAuthor && (recipe.memberNickname || recipe.memberName)" class="recipe-author mt-2 flex items-center gap-2">
                                <div class="w-6 h-6 rounded-full bg-gray-300 flex items-center justify-center overflow-hidden flex-shrink-0">
                                    <img v-if="recipe.memberProfileImage" :src="recipe.memberProfileImage" alt="작성자 프로필" class="w-full h-full object-cover" />
                                    <i v-else class="pi pi-user text-gray-600 text-xs"></i>
                                </div>
                                <span class="text-sm text-gray-600 truncate">{{ recipe.memberNickname || recipe.memberName }}</span>
                            </div>
                            <div v-else-if="dateText" class="text-sm text-gray-500 mt-1 flex items-center gap-1">
                                <i class="pi pi-calendar"></i>
                                <span class="truncate">{{ dateText }}</span>
                            </div>
                        </div>
                        <!-- 댓글 영역: showCommentCount이고 댓글 수가 있을 때만 영역 노출 (0이면 숨김) -->
                        <div v-if="showCommentCount && commentCountText" class="recipe-comment-zone">
                            <div class="recipe-comment-count mt-1">
                                <span class="text-sm text-gray-600 cursor-pointer hover:text-primary truncate" @click.stop="$emit('scroll-to-comments', recipe.id)"> 댓글 {{ commentCountText }} </span>
                            </div>
                        </div>
                    </div>
                </div>
            </template>
        </Card>
    </div>
</template>

<style scoped>
/* 레시피 그리드 카드 스타일은 layout _recipe-card-list.scss 공통 사용 */
</style>
