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
    { breakpoint: '576px', numVisible: 2 },
    { breakpoint: '420px', numVisible: 1 }
];
</script>

<template>
    <div v-if="images && images.length > 0" class="recipe-section-card bg-white rounded-xl shadow-lg p-4 sm:p-6 md:p-8 mb-6 md:rounded-2xl md:mb-8">
        <h2 class="recipe-section-card__title text-xl font-bold text-gray-800 mb-4 flex items-center sm:text-2xl md:text-3xl md:mb-8">
            <i class="pi pi-images mr-2 sm:mr-3 text-orange-600 shrink-0"></i>
            이미지 갤러리
        </h2>
        <Galleria :value="images" :num-visible="5" :responsive-options="galleriaResponsiveOptions" thumbnails-position="bottom" container-class="galleria-thumbnail-container" show-item-navigators show-thumbnail-navigators>
            <template #item="slotProps">
                <img :src="slotProps.item.url" :alt="slotProps.item.fileName || '갤러리 이미지'" class="galleria-main-img w-full block object-contain rounded-lg" />
            </template>
            <template #thumbnail="slotProps">
                <img :src="slotProps.item.url" :alt="slotProps.item.fileName || '썸네일'" class="w-full block object-cover rounded cursor-pointer" />
            </template>
        </Galleria>
    </div>
</template>

<style scoped>
.galleria-main-img {
    max-height: min(50vh, 280px);
    width: 100%;
}

@media (min-width: 480px) {
    .galleria-main-img {
        max-height: min(55vh, 380px);
    }
}

@media (min-width: 768px) {
    .galleria-main-img {
        max-height: 480px;
    }
}

:deep(.galleria-thumbnail-container) {
    max-width: 100%;
}

/* 모바일: 슬라이드 좌우 버튼 터치 영역 확대 */
:deep([data-pc-group-section='itemnavigator']) {
    width: 2.75rem;
    height: 2.75rem;
    min-width: 44px;
    min-height: 44px;
}

@media (min-width: 768px) {
    :deep([data-pc-group-section='itemnavigator']) {
        width: 2.5rem;
        height: 2.5rem;
        min-width: unset;
        min-height: unset;
    }
}

:deep([data-pc-group-section='thumbnailnavigator']) {
    min-width: 2.75rem;
    min-height: 2.75rem;
}

@media (min-width: 768px) {
    :deep([data-pc-group-section='thumbnailnavigator']) {
        min-width: unset;
        min-height: unset;
    }
}
</style>
