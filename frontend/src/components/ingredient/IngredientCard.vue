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
    gap: 10px;
    padding: 20px;
    border: 1px solid rgba(180, 150, 110, 0.25);
    border-radius: 16px;
    background: linear-gradient(165deg, #fefcf9 0%, #faf6f1 50%, #f5f0e8 100%);
    box-shadow:
        0 1px 2px rgba(0, 0, 0, 0.04),
        0 4px 12px rgba(0, 0, 0, 0.04),
        inset 0 1px 0 rgba(255, 255, 255, 0.8);
    cursor: pointer;
    transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;
}

.ingredient-card:hover {
    border-color: rgba(180, 150, 110, 0.45);
    transform: translateY(-6px);
    box-shadow:
        0 4px 8px rgba(0, 0, 0, 0.06),
        0 12px 28px rgba(0, 0, 0, 0.08),
        inset 0 1px 0 rgba(255, 255, 255, 0.9);
}

.ingredient-image-container {
    width: 100%;
    aspect-ratio: 1;
    border-radius: 50%;
    overflow: hidden;
    background: linear-gradient(145deg, #fffefc 0%, #f8f4ee 100%);
    border: 1px solid rgba(180, 150, 110, 0.2);
    box-shadow:
        0 2px 8px rgba(0, 0, 0, 0.06),
        inset 0 1px 0 rgba(255, 255, 255, 0.9);
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
    letter-spacing: 0.02em;
    text-align: center;
    color: #2d2a26;
    word-break: keep-all;
}

@media (max-width: 768px) {
    .ingredient-card {
        padding: 14px;
    }

    .ingredient-label {
        font-size: 12px;
    }
}
</style>
