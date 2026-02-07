<template>
    <div class="admin-page">
        <div class="page-header mb-6">
            <h1 class="text-3xl font-bold text-gray-900">관리자페이지</h1>
            <p class="text-gray-600 mt-2">관리 기능을 선택하세요</p>
        </div>

        <div class="admin-buttons">
            <Button
                label="재료·재료 그룹 관리"
                icon="pi pi-folder-plus"
                class="admin-btn"
                @click="router.push('/admin/ingredient-groups')"
            />
            <Button
                label="재료 관리 등록"
                icon="pi pi-box"
                class="admin-btn"
                @click="router.push('/admin/ingredient-management/register')"
            />
            <Button
                label="재료 정보 요청 목록"
                icon="pi pi-list"
                class="admin-btn"
                @click="router.push('/admin/ingredient-requests')"
            />
            <Button
                label="공통코드 관리"
                icon="pi pi-book"
                class="admin-btn"
                @click="router.push('/admin/common-codes')"
            />
        </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import Button from 'primevue/button';

const router = useRouter();
const authStore = useAuthStore();

onMounted(() => {
    if (!authStore.isAdmin) {
        router.replace('/');
    }
});
</script>

<style scoped>
.admin-page {
    padding: 24px;
    max-width: 800px;
    margin: 0 auto;
}

.page-header {
    margin-bottom: 24px;
}

.admin-buttons {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.admin-btn {
    justify-content: flex-start;
    padding: 1rem 1.25rem;
    font-size: 1rem;
}
</style>
