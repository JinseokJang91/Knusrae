<script setup lang="ts">
import { useRouter } from 'vue-router';
import type { Recipe } from '@/types/recipe';

const props = defineProps<{
    recipe: Recipe;
    compact?: boolean;
    showStats?: boolean;
}>();

const router = useRouter();

const formatNumber = (num?: number) => {
    if (!num) return '0';
    if (num >= 1000) {
        return (num / 1000).toFixed(1) + 'k';
    }
    return num.toString();
};

const goToRecipe = () => {
    router.push(`/recipes/${props.recipe.id}`);
};
</script>

<template>
    <div class="recipe-card" :class="{ compact }" @click="goToRecipe">
        <div class="recipe-image-container">
            <img :src="recipe.thumbnail || '/images/default-recipe.jpg'" :alt="recipe.title" class="recipe-image" />
        </div>

        <div class="recipe-content">
            <h3 class="recipe-title">{{ recipe.title }}</h3>
            <p v-if="!compact && recipe.description" class="recipe-description">
                {{ recipe.description }}
            </p>

            <div class="recipe-meta">
                <div class="recipe-author">
                    <i class="pi pi-user"></i>
                    <span>{{ recipe.memberNickname || recipe.memberName }}</span>
                </div>

                <div v-if="showStats" class="recipe-stats">
                    <span><i class="pi pi-eye"></i> {{ formatNumber(recipe.hits) }}</span>
                    <span><i class="pi pi-heart"></i> {{ formatNumber(recipe.favoriteCount) }}</span>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.recipe-card {
    background: var(--surface-card);
    border-radius: 12px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s;
    border: 1px solid var(--surface-border);
    display: flex;
    flex-direction: column;
    height: 100%;
}

.recipe-card:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    transform: translateY(-4px);
}

.recipe-card.compact {
    width: 280px;
    flex-shrink: 0;
}

.recipe-image-container {
    position: relative;
    width: 100%;
    padding-top: 66.67%; /* 3:2 aspect ratio */
    overflow: hidden;
    background: var(--surface-100);
}

.recipe-image {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.recipe-content {
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 8px;
    flex-grow: 1;
}

.recipe-title {
    font-size: 16px;
    font-weight: 600;
    margin: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    line-height: 1.4;
}

.recipe-description {
    font-size: 14px;
    color: var(--text-color-secondary);
    margin: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    line-height: 1.5;
}

.recipe-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: auto;
}

.recipe-author {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    color: var(--text-color-secondary);
}

.recipe-author i {
    font-size: 12px;
}

.recipe-stats {
    display: flex;
    gap: 12px;
    font-size: 13px;
    color: var(--text-color-secondary);
}

.recipe-stats span {
    display: flex;
    align-items: center;
    gap: 4px;
}

.recipe-stats i {
    font-size: 12px;
}

@media (max-width: 768px) {
    .recipe-card.compact {
        width: 240px;
    }

    .recipe-content {
        padding: 12px;
    }

    .recipe-title {
        font-size: 15px;
    }
}
</style>
