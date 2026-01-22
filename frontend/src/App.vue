<script setup>
import { onMounted } from 'vue';
import ConfirmDialog from 'primevue/confirmdialog';
import Toast from 'primevue/toast';
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
    <Toast position="top-right" :pt="{
        root: { class: 'w-96' },
        message: { class: 'shadow-lg' }
    }">
        <template #message="slotProps">
            <div class="flex items-start gap-3 p-3">
                <i 
                    :class="[
                        'pi text-xl',
                        slotProps.message.severity === 'success' ? 'pi-check-circle text-green-500' :
                        slotProps.message.severity === 'info' ? 'pi-info-circle text-blue-500' :
                        slotProps.message.severity === 'warn' ? 'pi-exclamation-triangle text-yellow-500' :
                        'pi-times-circle text-red-500'
                    ]"
                ></i>
                <div class="flex-1">
                    <div class="font-semibold text-sm mb-1">
                        {{ slotProps.message.summary }}
                    </div>
                    <div class="text-sm text-gray-600">
                        {{ slotProps.message.detail }}
                    </div>
                </div>
                <button 
                    @click="slotProps.closeCallback"
                    class="text-gray-400 hover:text-gray-600 transition-colors"
                >
                    <i class="pi pi-times"></i>
                </button>
            </div>
        </template>
    </Toast>
    <router-view />
</template>

<style scoped></style>
