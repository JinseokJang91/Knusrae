<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getActiveThemes, getThemeRecipes } from '@/api/themeApi';
import { useAppToast } from '@/utils/toast';
import type { ThemeCollection, ThemeRecipeItem } from '@/types/theme';

const router = useRouter();
const { showError } = useAppToast();

const themes = ref<ThemeCollection[]>([]);
const selectedTheme = ref<ThemeCollection | null>(null);
const themeRecipes = ref<ThemeRecipeItem[]>([]);
const loadingThemes = ref(false);
const loadingRecipes = ref(false);
const loadThemes = async () => {
    loadingThemes.value = true;
    try {
        const data = await getActiveThemes();
        themes.value = data;
        if (data.length > 0) {
            selectTheme(data[0]);
        }
    } catch (error) {
        console.error('테마 목록 로딩 실패:', error);
        showError('테마 컬렉션을 불러오는데 실패했습니다.');
    } finally {
        loadingThemes.value = false;
    }
};

const selectTheme = async (theme: ThemeCollection) => {
    selectedTheme.value = theme;
    loadingRecipes.value = true;
    try {
        const data = await getThemeRecipes(theme.id, 10);
        themeRecipes.value = data.recipes;
    } catch (error) {
        console.error('테마 레시피 로딩 실패:', error);
        showError('테마 레시피를 불러오는데 실패했습니다.');
    } finally {
        loadingRecipes.value = false;
    }
};

const goToRecipe = (recipeId: number) => {
    router.push(`/recipes/${recipeId}`);
};

const scrollLeft = () => {
    const container = document.querySelector('.recipes-scroll') as HTMLElement;
    container?.scrollBy({ left: -320, behavior: 'smooth' });
};

const scrollRight = () => {
    const container = document.querySelector('.recipes-scroll') as HTMLElement;
    container?.scrollBy({ left: 320, behavior: 'smooth' });
};

const formatNumber = (num: number): string => {
    if (num >= 10000) return `${(num / 10000).toFixed(1)}만`;
    if (num >= 1000) return `${(num / 1000).toFixed(1)}천`;
    return num.toString();
};

onMounted(() => {
    loadThemes();
});
</script>

<template>
    <div class="theme-collections">
        <!-- 섹션 헤더 -->
        <div class="section-header">
            <div class="header-left">
                <h2 class="section-title">
                    <i class="pi pi-bookmark-fill" style="color: var(--primary-color)"></i>
                    테마 컬렉션
                </h2>
                <p class="section-subtitle">큐레이션된 레시피 모음을 만나보세요</p>
            </div>
        </div>

        <!-- 로딩 상태 -->
        <div v-if="loadingThemes" class="loading-container">
            <i class="pi pi-spinner pi-spin" style="font-size: 2rem; color: var(--primary-color)"></i>
        </div>

        <!-- 테마 없음 -->
        <div v-else-if="themes.length === 0" class="empty-state">
            <i class="pi pi-bookmark" style="font-size: 3rem; color: var(--text-color-secondary)"></i>
            <p>현재 활성화된 테마가 없습니다.</p>
        </div>

        <!-- 테마 탭 + 레시피 -->
        <div v-else class="theme-content">
            <!-- 테마 탭 -->
            <div class="theme-tabs">
                <button v-for="theme in themes" :key="theme.id" class="theme-tab" :class="{ active: selectedTheme?.id === theme.id }" @click="selectTheme(theme)">
                    <img v-if="theme.thumbnailImage" :src="theme.thumbnailImage" :alt="theme.name" class="theme-tab-icon" />
                    <i v-else class="pi pi-bookmark theme-tab-icon-default"></i>
                    <div class="theme-tab-info">
                        <span class="theme-tab-name">{{ theme.name }}</span>
                        <span class="theme-tab-count">레시피 {{ theme.recipeCount }}개</span>
                    </div>
                </button>
            </div>

            <!-- 선택된 테마 상세 -->
            <div v-if="selectedTheme" class="theme-detail">
                <div class="theme-description" v-if="selectedTheme.description">
                    <p>{{ selectedTheme.description }}</p>
                    <span v-if="selectedTheme.endDate" class="theme-period">
                        <i class="pi pi-calendar"></i>
                        {{ selectedTheme.endDate }}까지
                    </span>
                </div>

                <!-- 레시피 스크롤 -->
                <div class="recipes-scroll-wrapper">
                    <button class="scroll-btn scroll-btn-left" @click="scrollLeft" aria-label="이전">
                        <i class="pi pi-chevron-left"></i>
                    </button>

                    <div v-if="loadingRecipes" class="recipes-loading">
                        <i class="pi pi-spinner pi-spin" style="font-size: 1.5rem"></i>
                    </div>

                    <div v-else-if="themeRecipes.length === 0" class="recipes-empty">
                        <p>등록된 레시피가 없습니다.</p>
                    </div>

                    <div v-else class="recipes-scroll">
                        <div v-for="recipe in themeRecipes" :key="recipe.recipeId" class="recipe-card" @click="goToRecipe(recipe.recipeId)">
                            <div class="recipe-thumbnail">
                                <img v-if="recipe.thumbnail" :src="recipe.thumbnail" :alt="recipe.title" loading="lazy" />
                                <div v-else class="recipe-thumbnail-placeholder">
                                    <i class="pi pi-image"></i>
                                </div>
                            </div>
                            <div class="recipe-info">
                                <h4 class="recipe-title">{{ recipe.title }}</h4>
                                <p class="recipe-author">{{ recipe.memberNickname }}</p>
                                <div class="recipe-stats">
                                    <span class="stat">
                                        <i class="pi pi-eye"></i>
                                        {{ formatNumber(recipe.hits) }}
                                    </span>
                                    <span class="stat">
                                        <i class="pi pi-heart"></i>
                                        {{ formatNumber(recipe.favoriteCount) }}
                                    </span>
                                    <span class="stat">
                                        <i class="pi pi-comment"></i>
                                        {{ recipe.commentCount }}
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <button class="scroll-btn scroll-btn-right" @click="scrollRight" aria-label="다음">
                        <i class="pi pi-chevron-right"></i>
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped lang="scss">
.theme-collections {
    width: 100%;
}

.section-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    margin-bottom: 20px;

    .header-left {
        .section-title {
            font-size: 1.25rem;
            font-weight: 700;
            color: var(--text-color);
            margin: 0 0 4px 0;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .section-subtitle {
            font-size: 0.875rem;
            color: var(--text-color-secondary);
            margin: 0;
        }
    }
}

.loading-container,
.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px;
    gap: 12px;
    color: var(--text-color-secondary);
}

.theme-content {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

// 테마 탭
.theme-tabs {
    display: flex;
    gap: 12px;
    overflow-x: auto;
    padding-bottom: 4px;

    &::-webkit-scrollbar {
        height: 4px;
    }

    &::-webkit-scrollbar-thumb {
        background: var(--surface-border);
        border-radius: 2px;
    }
}

.theme-tab {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 16px;
    border-radius: 12px;
    border: 2px solid var(--surface-border);
    background: var(--surface-card);
    cursor: pointer;
    transition: all 0.2s ease;
    white-space: nowrap;
    min-width: 160px;

    &:hover {
        border-color: var(--primary-color);
        background: rgba(255, 107, 53, 0.04);
    }

    &.active {
        border-color: var(--primary-color);
        background: rgba(255, 107, 53, 0.08);
    }

    .theme-tab-icon {
        width: 36px;
        height: 36px;
        border-radius: 8px;
        object-fit: cover;
        flex-shrink: 0;
    }

    .theme-tab-icon-default {
        font-size: 1.5rem;
        color: var(--primary-color);
        width: 36px;
        text-align: center;
    }

    .theme-tab-info {
        display: flex;
        flex-direction: column;
        align-items: flex-start;

        .theme-tab-name {
            font-size: 0.875rem;
            font-weight: 600;
            color: var(--text-color);
        }

        .theme-tab-count {
            font-size: 0.75rem;
            color: var(--text-color-secondary);
        }
    }
}

// 테마 상세
.theme-detail {
    .theme-description {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 16px;

        p {
            font-size: 0.875rem;
            color: var(--text-color-secondary);
            margin: 0;
        }

        .theme-period {
            font-size: 0.8rem;
            color: var(--primary-color);
            display: flex;
            align-items: center;
            gap: 4px;
            white-space: nowrap;
        }
    }
}

// 레시피 스크롤
.recipes-scroll-wrapper {
    position: relative;
    padding: 0 8px;
}

.recipes-loading,
.recipes-empty {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 200px;
    color: var(--text-color-secondary);
}

.recipes-scroll {
    display: flex;
    gap: 16px;
    overflow-x: auto;
    padding: 8px 4px 12px;
    scroll-behavior: smooth;

    &::-webkit-scrollbar {
        height: 6px;
    }

    &::-webkit-scrollbar-track {
        background: var(--surface-ground);
        border-radius: 3px;
    }

    &::-webkit-scrollbar-thumb {
        background: var(--primary-color);
        border-radius: 3px;
        opacity: 0.6;
    }
}

// 레시피 카드
.recipe-card {
    flex-shrink: 0;
    width: 180px;
    border-radius: 12px;
    overflow: hidden;
    background: var(--surface-card);
    border: 1px solid var(--surface-border);
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
        border-color: var(--primary-color);
    }

    .recipe-thumbnail {
        width: 100%;
        height: 120px;
        overflow: hidden;
        background: var(--surface-ground);

        img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            transition: transform 0.3s ease;
        }

        &:hover img {
            transform: scale(1.05);
        }

        .recipe-thumbnail-placeholder {
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 2rem;
            color: var(--text-color-secondary);
        }
    }

    .recipe-info {
        padding: 10px 12px;

        .recipe-title {
            font-size: 0.8rem;
            font-weight: 600;
            color: var(--text-color);
            margin: 0 0 4px 0;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            line-height: 1.3;
        }

        .recipe-author {
            font-size: 0.75rem;
            color: var(--text-color-secondary);
            margin: 0 0 6px 0;
        }

        .recipe-stats {
            display: flex;
            gap: 8px;

            .stat {
                display: flex;
                align-items: center;
                gap: 3px;
                font-size: 0.7rem;
                color: var(--text-color-secondary);

                i {
                    font-size: 0.65rem;
                }
            }
        }
    }
}

// 스크롤 버튼
.scroll-btn {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: var(--surface-card);
    border: 1px solid var(--surface-border);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
    cursor: pointer;
    z-index: 10;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;

    &:hover {
        background: var(--primary-color);
        color: white;
        border-color: var(--primary-color);
        transform: translateY(-50%) scale(1.1);
    }

    &.scroll-btn-left {
        left: -18px;
    }

    &.scroll-btn-right {
        right: -18px;
    }
}

// 반응형
@media (max-width: 768px) {
    .scroll-btn {
        display: none;
    }

    .theme-tabs {
        gap: 8px;
    }

    .theme-tab {
        min-width: 130px;
        padding: 10px 12px;
    }

    .recipe-card {
        width: 150px;
    }
}
</style>
