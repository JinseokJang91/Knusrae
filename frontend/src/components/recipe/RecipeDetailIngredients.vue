<script setup lang="ts">
import type { RecipeIngredientGroup } from '@/types/recipe';

defineProps<{
    ingredientGroups: RecipeIngredientGroup[];
}>();
</script>

<template>
    <div v-if="ingredientGroups && ingredientGroups.length > 0" class="recipe-section-card bg-white rounded-xl shadow-lg p-4 sm:p-6 md:p-8 mb-6 md:rounded-2xl md:mb-8">
        <h2 class="recipe-section-card__title text-xl font-bold text-gray-800 mb-4 flex items-center sm:text-2xl md:text-3xl md:mb-8">
            <i class="pi pi-shopping-cart mr-2 sm:mr-3 text-orange-600 shrink-0"></i>
            준비물
        </h2>

        <div class="space-y-4 md:space-y-6">
            <div v-for="(group, groupIndex) in ingredientGroups" :key="`group-${groupIndex}-${group.order}`" class="bg-orange-50 rounded-lg md:rounded-xl p-4 md:p-6">
                <div class="flex items-center mb-3 md:mb-4">
                    <div class="w-7 h-7 sm:w-8 sm:h-8 bg-orange-500 text-white rounded-full flex items-center justify-center text-xs sm:text-sm font-bold shrink-0 mr-2 sm:mr-3">
                        {{ groupIndex + 1 }}
                    </div>
                    <h3 class="text-base font-semibold text-gray-800 sm:text-lg md:text-xl min-w-0 break-words">
                        {{ group.customTypeName || group.detailName || '재료' }}
                    </h3>
                </div>

                <div v-if="group.items && group.items.length > 0" class="grid grid-cols-1 md:grid-cols-2 gap-2 sm:gap-3">
                    <div v-for="(item, itemIndex) in group.items" :key="`item-${groupIndex}-${itemIndex}-${item.name}`" class="flex items-center gap-2 p-2.5 sm:p-3 bg-white rounded-lg border border-orange-200 min-w-0">
                        <i class="pi pi-circle-fill text-orange-400 text-xs shrink-0"></i>
                        <span class="text-gray-800 text-sm sm:text-base font-medium flex-1 min-w-0 break-words">{{ item.name }}</span>
                        <span class="text-gray-600 text-sm sm:text-base shrink-0 text-right tabular-nums">
                            <template v-if="item.quantity">{{ item.quantity }}{{ item.customUnitName || item.detailName }}</template>
                            <template v-else-if="item.customUnitName || item.detailName">{{ item.customUnitName || item.detailName }}</template>
                        </span>
                    </div>
                </div>

                <div v-else class="text-orange-700/70 text-center py-4">항목이 없습니다.</div>
            </div>
        </div>
    </div>
</template>
