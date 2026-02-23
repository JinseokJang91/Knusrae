<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import SelectButton from 'primevue/selectbutton';
import Accordion from 'primevue/accordion';
import AccordionPanel from 'primevue/accordionpanel';
import AccordionHeader from 'primevue/accordionheader';
import AccordionContent from 'primevue/accordioncontent';
import Button from 'primevue/button';
import PageStateBlock from '@/components/common/PageStateBlock.vue';
import { FAQ_CATEGORIES, FAQ_ITEMS } from '@/data/faqData';
import type { FaqCategoryId, FaqItem } from '@/types/faq';

const router = useRouter();

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

const goToInquiries = () => {
    router.push('/my?tab=inquiries');
};
</script>

<template>
    <div class="page-container page-container--card">
        <div class="faq-container">
            <!-- Header -->
            <div class="faq-header mb-6">
                <h1 class="text-3xl font-bold m-0 mb-2">FAQ - 자주 묻는 질문</h1>
                <p class="text-gray-600 m-0">앱 사용 중 궁금한 점을 확인해보세요.</p>
            </div>

            <!-- Category Filter -->
            <div class="faq-filter mb-5">
                <SelectButton v-model="selectedCategory" :options="categoryOptions" option-label="label" option-value="value" class="faq-category-buttons" :allow-empty="false" />
            </div>

            <!-- Body: Accordion or Empty State -->
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

            <!-- Footer CTA -->
            <div class="faq-cta mt-8 p-4 bg-orange-50 border-l-4 border-orange-500 rounded-r">
                <p class="text-gray-700 mb-3 m-0">궁금한 점이 해결되지 않았나요?</p>
                <Button label="1:1 문의하기" icon="pi pi-inbox" severity="secondary" @click="goToInquiries" />
            </div>
        </div>
    </div>
</template>

<style lang="scss" scoped>
.faq-container {
    max-width: 900px;
    margin-left: auto;
    margin-right: auto;
}

.faq-header {
    h1 {
        color: var(--primary-color);
    }
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
    :deep(.p-accordion-panel) {
        .p-accordion-header {
            .p-accordion-header-link {
                font-weight: 500;
                padding: 1rem 1.25rem;
            }
        }
        .p-accordion-content {
            padding: 0 1.25rem 1.25rem;
        }
    }
}

@media (max-width: 768px) {
    .faq-container {
        max-width: 100%;
    }

    .faq-header {
        h1 {
            font-size: 1.5rem;
        }
    }

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
