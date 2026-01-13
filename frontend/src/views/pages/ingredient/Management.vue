<template>
    <div class="card">
        <div class="flex justify-content-between align-items-center mb-4">
            <h1 class="text-3xl font-bold text-900">재료 관리</h1>
            <div class="flex gap-2">
                <Button icon="pi pi-refresh" label="새로고침" severity="secondary" @click="refreshData" />
            </div>
        </div>

        <!-- 탭 메뉴 -->
        <TabView v-model:activeIndex="activeTab">
            <TabPanel header="재료 보관법">
                <Storage />
            </TabPanel>
            <TabPanel header="재료 손질법">
                <Preparation />
            </TabPanel>
        </TabView>
    </div>
</template>

<script setup>
import Button from 'primevue/button';
import TabView from 'primevue/tabview';
import TabPanel from 'primevue/tabpanel';
import { ref } from 'vue';
import Storage from '@/views/pages/recipe/Storage.vue';
import Preparation from '@/views/pages/recipe/Preparation.vue';

const activeTab = ref(0);

const refreshData = () => {
    // 새로고침 로직은 각 컴포넌트에서 처리
    window.location.reload();
};
</script>

<style scoped></style>

