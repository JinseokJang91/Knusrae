<script setup lang="ts">
import type { Ingredient, IngredientType } from '@/types/ingredient';
import IngredientCard from './IngredientCard.vue';

defineProps<{
    ingredients: Ingredient[];
    type: IngredientType;
}>();

const emit = defineEmits<{
    (e: 'ingredient-click', ingredient: Ingredient): void;
}>();

const handleClick = (ingredient: Ingredient) => {
    emit('ingredient-click', ingredient);
};
</script>

<template>
    <div class="ingredient-grid">
        <IngredientCard v-for="ingredient in ingredients" :key="ingredient.id" :ingredient="ingredient" :type="type" @click="handleClick(ingredient)" />
    </div>
</template>

<style scoped>
.ingredient-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 20px;
}

@media (max-width: 1024px) {
    .ingredient-grid {
        grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
        gap: 16px;
    }
}

@media (max-width: 768px) {
    .ingredient-grid {
        grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
        gap: 12px;
    }
}

@media (max-width: 480px) {
    .ingredient-grid {
        grid-template-columns: repeat(2, 1fr);
        gap: 12px;
    }
}
</style>
