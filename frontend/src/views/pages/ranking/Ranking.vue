<template>
    <div class="card">
        <div class="flex justify-content-between align-items-center mb-4">
            <h1 class="text-3xl font-bold text-900">랭킹</h1>
            <div class="flex gap-2">
                <Button icon="pi pi-refresh" label="새로고침" severity="secondary" @click="refreshRanking" />
            </div>
        </div>

        <!-- 탭 메뉴 -->
        <TabView v-model:activeIndex="activeTab">
            <TabPanel header="주간 랭킹">
                <WeeklyRanking />
            </TabPanel>
            <TabPanel header="월간 랭킹">
                <MonthlyRanking />
            </TabPanel>
        </TabView>
    </div>
</template>

<script setup>
import Button from 'primevue/button';
import TabView from 'primevue/tabview';
import TabPanel from 'primevue/tabpanel';
import { ref } from 'vue';
import WeeklyRanking from '@/views/pages/recommend/WeeklyRanking.vue';
import MonthlyRanking from '@/views/pages/recommend/MonthlyRanking.vue';

const activeTab = ref(0);

const refreshRanking = () => {
    // 새로고침 로직은 각 컴포넌트에서 처리
    window.location.reload();
};
</script>

<style scoped></style>

