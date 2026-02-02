<script setup lang="ts">
import { onMounted } from 'vue';
import ConfirmDialog from 'primevue/confirmdialog';
import AppToast from '@/components/common/AppToast.vue';
import { useAuthStore } from '@/stores/authStore';

const authStore = useAuthStore();

onMounted(() => {
    // 앱 초기화 시 Token Refresh를 통해 로그인 상태 확인
    const initializeAuth = async () => {
        await authStore.checkAuth();
    };
    initializeAuth();
});
</script>

<template>
    <ConfirmDialog />
    <AppToast />
    <router-view />
</template>
