<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import Tabs from 'primevue/tabs';
import TabList from 'primevue/tablist';
import Tab from 'primevue/tab';
import TabPanels from 'primevue/tabpanels';
import TabPanel from 'primevue/tabpanel';
import Button from 'primevue/button';
import FAQ from '@/components/community/FAQ.vue';

const router = useRouter();

/** 고객지원 탭 ID (확장 시 여기에 추가) */
const SUPPORT_TAB_ID = {
    FAQ: 'faq'
} as const;

type SupportTabId = (typeof SUPPORT_TAB_ID)[keyof typeof SUPPORT_TAB_ID];

/** 고객지원 탭 목록 - 메뉴 추가 시 이 배열에 항목만 추가하면 됨 */
const SUPPORT_TABS: { value: SupportTabId; label: string; icon: string }[] = [{ value: SUPPORT_TAB_ID.FAQ, label: '자주 묻는 질문', icon: 'pi pi-question-circle' }];

const activeTab = ref<SupportTabId>(SUPPORT_TAB_ID.FAQ);

const goToInquiries = () => {
    router.push('/my?tab=inquiries');
};
</script>

<template>
    <div class="page-container page-container--card">
        <div class="support-page">
            <header class="support-header">
                <h1 class="support-title">고객지원</h1>
                <p class="support-desc">궁금한 점을 찾아보시거나 1:1 문의를 이용해 주세요.</p>
            </header>

            <Tabs v-model:value="activeTab" class="support-tabs" :lazy="true">
                <TabList>
                    <Tab v-for="tab in SUPPORT_TABS" :key="tab.value" :value="tab.value">
                        <i :class="tab.icon" class="tab-icon" aria-hidden="true"></i>
                        <span>{{ tab.label }}</span>
                    </Tab>
                </TabList>
                <TabPanels>
                    <TabPanel :value="SUPPORT_TAB_ID.FAQ" class="support-tab-panel">
                        <p class="section-desc">앱 사용 중 자주 묻는 질문과 답변입니다.</p>
                        <FAQ />
                    </TabPanel>
                    <!-- 추후 탭 추가 시 여기에 TabPanel 추가 (예: 공지사항, 이용 안내 등) -->
                </TabPanels>
            </Tabs>

            <div class="support-cta">
                <p class="support-cta__text">궁금한 점이 해결되지 않았나요?</p>
                <Button label="1:1 문의하기" icon="pi pi-inbox" severity="secondary" @click="goToInquiries" />
            </div>
        </div>
    </div>
</template>

<style lang="scss" scoped>
.support-page {
    max-width: 1200px;
    margin: 0 auto;
    padding: 24px 16px;
}

.support-header {
    margin-bottom: 32px;
}

.support-title {
    font-size: 1.875rem;
    font-weight: 700;
    margin: 0 0 0.5rem 0;
    color: var(--primary-color);
}

.support-desc {
    color: var(--text-color-secondary);
    margin: 0;
}

.support-tabs {
    border: none;
    border-radius: 0;
    background: transparent;
}

.support-tabs :deep(.p-tablist) {
    margin-bottom: 12px;
}

.support-tabs :deep(.p-tabpanels) {
    padding: 0;
}

.tab-icon {
    margin-right: 0.5rem;
}

.support-tab-panel {
    min-height: 200px;
}

.section-desc {
    color: var(--text-color-secondary);
    margin: 0 0 1rem 0;
}

.support-cta {
    margin-top: 2rem;
    padding: 1rem 1.25rem;
    background: var(--orange-50, #fff7ed);
    border-left: 4px solid var(--orange-500, #f97316);
    border-radius: 0 8px 8px 0;
}

.support-cta__text {
    margin: 0 0 0.75rem 0;
    color: var(--text-color);
}

@media (max-width: 768px) {
    .support-page {
        padding: 1rem;
    }

    .support-title {
        font-size: 1.5rem;
    }
}
</style>
