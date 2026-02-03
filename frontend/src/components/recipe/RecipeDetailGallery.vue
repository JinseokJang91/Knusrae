<template>
    <div v-if="images && images.length > 0" class="bg-white rounded-2xl shadow-lg p-8 mb-8">
        <h2 class="text-3xl font-bold text-gray-800 mb-8 flex items-center">
            <i class="pi pi-images mr-3 text-gray-500"></i>
            이미지 갤러리
        </h2>
        <Galleria
            :value="images"
            :num-visible="5"
            :responsive-options="galleriaResponsiveOptions"
            thumbnails-position="bottom"
            container-class="galleria-thumbnail-container"
            show-item-navigators
            show-thumbnail-navigators
        >
            <template #item="slotProps">
                <img
                    :src="slotProps.item.url"
                    :alt="slotProps.item.fileName || '갤러리 이미지'"
                    class="w-full block object-contain max-h-[480px] rounded-lg"
                />
            </template>
            <template #thumbnail="slotProps">
                <img
                    :src="slotProps.item.url"
                    :alt="slotProps.item.fileName || '썸네일'"
                    class="w-full block object-cover rounded cursor-pointer"
                />
            </template>
        </Galleria>
    </div>
</template>

<script setup lang="ts">
import Galleria from 'primevue/galleria';
import type { RecipeImage } from '@/types/recipe';

defineProps<{
    images: RecipeImage[];
}>();

const galleriaResponsiveOptions = [
    { breakpoint: '1400px', numVisible: 5 },
    { breakpoint: '992px', numVisible: 4 },
    { breakpoint: '768px', numVisible: 3 },
    { breakpoint: '576px', numVisible: 2 }
];
</script>

<style scoped>
:deep(.galleria-thumbnail-container) {
    max-width: 100%;
}
</style>
