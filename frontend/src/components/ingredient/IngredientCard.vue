<script setup lang="ts">
import type { Ingredient, IngredientType } from '@/types/ingredient';

const props = defineProps<{
    ingredient: Ingredient;
    type: IngredientType;
}>();

const emit = defineEmits<{
    (e: 'click', ingredient: Ingredient): void;
}>();

const handleClick = () => {
    emit('click', props.ingredient);
};
</script>

<template>
    <div class="ingredient-card" @click="handleClick">
        <div class="ingredient-image-container">
            <img v-if="ingredient.imageUrl" :src="ingredient.imageUrl" :alt="ingredient.name" class="ingredient-image" />
            <div v-else class="ingredient-placeholder">
                <i class="pi pi-image text-4xl text-gray-400"></i>
            </div>
        </div>
        <div class="ingredient-label">
            {{ ingredient.name }}
        </div>
    </div>
</template>

<style scoped>
.ingredient-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 16px;
    border: 1px solid var(--surface-border);
    border-radius: 12px;
    background: var(--surface-card);
    cursor: pointer;
    transition: all 0.2s;
}

.ingredient-card:hover {
    border-color: var(--primary-color);
    transform: translateY(-4px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.ingredient-image-container {
    width: 100%;
    aspect-ratio: 1;
    border-radius: 50%;
    overflow: hidden;
    background: var(--surface-ground);
    display: flex;
    align-items: center;
    justify-content: center;
}

.ingredient-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.ingredient-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
}

.ingredient-label {
    font-size: 14px;
    font-weight: 600;
    text-align: center;
    color: var(--text-color);
    word-break: keep-all;
}

@media (max-width: 768px) {
    .ingredient-card {
        padding: 12px;
    }

    .ingredient-label {
        font-size: 12px;
    }
}
</style>
