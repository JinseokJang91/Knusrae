<script setup>
import { onMounted } from 'vue';
import ConfirmDialog from 'primevue/confirmdialog';
import { useAuthStore } from '@/stores/authStore';

const authStore = useAuthStore();

onMounted(async () => {
    // 앱 초기화 시 Token Refresh를 통해 로그인 상태 확인
    await authStore.checkAuth();
});
</script>

<template>
    <ConfirmDialog />
    <router-view />
</template>

<style scoped></style>
