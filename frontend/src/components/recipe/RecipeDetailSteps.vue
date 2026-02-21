<script setup lang="ts">
import type { RecipeStep } from '@/types/recipe';

defineProps<{
    steps: RecipeStep[];
}>();
</script>

<template>
    <div class="bg-white rounded-2xl shadow-lg p-8 mb-8">
        <h2 class="text-3xl font-bold text-gray-800 mb-8 flex items-center">
            <i class="pi pi-list mr-3 text-orange-600"></i>
            조리 순서
        </h2>

        <div class="space-y-8">
            <div v-for="(step, index) in steps" :key="`step-${index}-${step.order}`" class="bg-orange-50 rounded-xl p-6">
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6 items-start">
                    <div>
                        <div class="relative w-full overflow-hidden rounded-lg shadow-md bg-white border border-orange-100">
                            <img v-if="step.imageUrl" :src="step.imageUrl" :alt="`단계 ${index + 1} 이미지`" class="w-full h-72 object-cover" />
                            <div v-else class="w-full h-72 flex items-center justify-center text-5xl text-orange-200 bg-orange-50">🖼️</div>
                        </div>
                    </div>
                    <div class="flex items-start gap-4">
                        <div class="w-10 h-10 bg-orange-500 text-white rounded-full flex items-center justify-center text-lg font-bold flex-shrink-0">
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
    font-size: 1.125rem;
    line-height: 1.65;
    color: #92400e;
    white-space: pre-line;
}
</style>
