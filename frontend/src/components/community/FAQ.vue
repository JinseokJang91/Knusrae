<script setup lang="ts">
import { computed, ref } from 'vue';
import SelectButton from 'primevue/selectbutton';
import Accordion from 'primevue/accordion';
import AccordionPanel from 'primevue/accordionpanel';
import AccordionHeader from 'primevue/accordionheader';
import AccordionContent from 'primevue/accordioncontent';
import PageStateBlock from '@/components/common/PageStateBlock.vue';
import { FAQ_CATEGORIES, FAQ_ITEMS } from '@/data/faqData';
import type { FaqCategoryId, FaqItem } from '@/types/faq';

type CategoryFilterValue = FaqCategoryId | 'all';

const categoryOptions: { value: CategoryFilterValue; label: string }[] = [
    { value: 'all', label: '전체' },
    ...FAQ_CATEGORIES.sort((a, b) => a.sortOrder - b.sortOrder).map((c) => ({
        value: c.id as CategoryFilterValue,
        label: c.label
    }))
];

const selectedCategory = ref<CategoryFilterValue>('all');

const filteredFaqList = computed<FaqItem[]>(() => {
    if (selectedCategory.value === 'all') {
        return FAQ_ITEMS;
    }
    return FAQ_ITEMS.filter((item) => item.categoryId === selectedCategory.value);
});
</script>

<template>
    <div class="faq-section">
        <div class="faq-filter mb-5">
            <SelectButton v-model="selectedCategory" :options="categoryOptions" option-label="label" option-value="value" class="faq-category-buttons" :allow-empty="false" />
        </div>
        <div class="faq-body">
            <PageStateBlock v-if="filteredFaqList.length === 0" state="empty" empty-icon="pi pi-search" empty-title="해당 카테고리에 FAQ가 없습니다" empty-message="다른 카테고리를 선택해보세요." />
            <Accordion v-else :value="[]" multiple class="faq-accordion">
                <AccordionPanel v-for="(item, idx) in filteredFaqList" :key="item.id" :value="String(idx)">
                    <AccordionHeader>{{ item.question }}</AccordionHeader>
                    <AccordionContent>
                        <p class="m-0 whitespace-pre-line text-gray-700">{{ item.answer }}</p>
                    </AccordionContent>
                </AccordionPanel>
            </Accordion>
        </div>
    </div>
</template>

<style lang="scss" scoped>
.faq-section {
    width: 100%;
}

.faq-filter {
    :deep(.faq-category-buttons) {
        flex-wrap: wrap;
        gap: 0.5rem;

        .p-button {
            font-size: 0.9rem;
            padding: 0.5rem 1rem;
        }
    }
}

.faq-accordion {
    /* Accordion 색상: 디자인 토큰으로 헤더/콘텐츠 색 적용 */
    --p-accordion-header-color: var(--text-color);
    --p-accordion-header-hover-color: var(--primary-color);
    --p-accordion-header-active-color: var(--primary-color);
    /* 헤더 배경: 열림/닫힘/호버 관계없이 동일하게 유지 (연한 회색) */
    --p-accordion-header-background: var(--surface-ground, #f5f5f5);
    --p-accordion-header-hover-background: var(--surface-ground, #f5f5f5);
    --p-accordion-header-active-background: var(--surface-ground, #f5f5f5);
    --p-accordion-header-active-hover-background: var(--surface-ground, #f5f5f5);
    --p-accordion-content-color: var(--text-color);
    --p-accordion-content-background: var(--surface-card);
    /* 콘텐츠 패딩: 상단 여백으로 헤더와 간격 확보 */
    --p-accordion-content-padding: 1.25rem 1.25rem 1.25rem;
    --p-accordion-header-toggle-icon-color: var(--primary-color);
    --p-accordion-header-toggle-icon-active-color: var(--primary-color);

    :deep(.p-accordion-panel) {
        .p-accordion-header {
            .p-accordion-header-link {
                font-weight: 500;
                padding: 1rem 1.25rem;
            }
        }
        /* PrimeVue 실제 콘텐츠 래퍼 클래스 */
        .p-accordioncontent-content {
            padding: 1.25rem 1.25rem 1.25rem !important;
        }
    }
}

@media (max-width: 768px) {
    .faq-filter :deep(.faq-category-buttons .p-button) {
        font-size: 0.85rem;
        padding: 0.4rem 0.75rem;
    }

    .faq-accordion :deep(.p-accordion-panel .p-accordion-header .p-accordion-header-link) {
        padding: 0.75rem 1rem;
        font-size: 0.95rem;
    }
}
</style>
