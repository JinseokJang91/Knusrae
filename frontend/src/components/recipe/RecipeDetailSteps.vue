<script setup lang="ts">
import type { RecipeStep } from '@/types/recipe';

defineProps<{
    steps: RecipeStep[];
}>();
</script>

<template>
    <div class="recipe-section-card bg-white rounded-xl shadow-lg p-4 sm:p-6 md:p-8 mb-6 md:rounded-2xl md:mb-8">
        <h2 class="recipe-section-card__title text-xl font-bold text-gray-800 mb-4 flex items-center sm:text-2xl md:text-3xl md:mb-8">
            <i class="pi pi-list mr-2 sm:mr-3 text-orange-600 shrink-0"></i>
            조리 순서
        </h2>

        <div class="space-y-6 md:space-y-8">
            <div v-for="(step, index) in steps" :key="`step-${index}-${step.order}`" class="bg-orange-50 rounded-lg md:rounded-xl p-4 md:p-6">
                <div class="flex flex-col md:grid md:grid-cols-2 gap-4 md:gap-6 md:items-start">
                    <div class="order-1 md:order-none min-w-0">
                        <div class="relative w-full overflow-hidden rounded-lg shadow-md bg-white border border-orange-100">
                            <img v-if="step.imageUrl" :src="step.imageUrl" :alt="`단계 ${index + 1} 이미지`" class="recipe-step__img w-full object-cover" />
                            <div v-else class="recipe-step__img w-full flex items-center justify-center text-5xl text-orange-200 bg-orange-50">🖼️</div>
                        </div>
                    </div>
                    <div class="order-2 flex items-start gap-3 sm:gap-4 min-w-0">
                        <div class="w-9 h-9 sm:w-10 sm:h-10 bg-orange-500 text-white rounded-full flex items-center justify-center text-base sm:text-lg font-bold shrink-0">
                            {{ index + 1 }}
                        </div>
                        <div class="recipe-step-bubble flex-1 min-w-0">
                            <p class="recipe-step-bubble__text">
                                {{ step.text }}
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.recipe-step__img {
    height: 12rem;
    max-height: 50vh;
}

@media (min-width: 480px) {
    .recipe-step__img {
        height: 15rem;
    }
}

@media (min-width: 768px) {
    .recipe-step__img {
        height: 18rem;
    }
}
.recipe-step-bubble {
    position: relative;
    padding: 1rem 1.25rem;
    background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
    border-radius: 20px;
    border: 2px solid #fcd34d;
    box-shadow: 0 2px 8px rgba(252, 211, 77, 0.2);
}

.recipe-step-bubble::before {
    content: '';
    position: absolute;
    top: 1.5rem;
    left: -10px;
    width: 0;
    height: 0;
    border-top: 10px solid transparent;
    border-bottom: 10px solid transparent;
    border-right: 10px solid #fcd34d;
}

.recipe-step-bubble::after {
    content: '';
    position: absolute;
    top: calc(1.5rem + 2px);
    left: -6px;
    width: 0;
    height: 0;
    border-top: 8px solid transparent;
    border-bottom: 8px solid transparent;
    border-right: 8px solid #fffbeb;
}

.recipe-step-bubble__text {
    margin: 0;
    font-size: 0.9375rem;
    line-height: 1.65;
    color: #92400e;
    white-space: pre-line;
}

@media (min-width: 640px) {
    .recipe-step-bubble__text {
        font-size: 1.0625rem;
    }
}

@media (min-width: 768px) {
    .recipe-step-bubble__text {
        font-size: 1.125rem;
    }
}
</style>
