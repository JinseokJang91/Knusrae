<script setup>
import Toast from 'primevue/toast';
import Avatar from 'primevue/avatar';
import Button from 'primevue/button';
import logoIcon from '@/assets/images/logo/logo-icon.png';
</script>

<template>
    <Toast position="top-center">
        <template #container="{ message, closeCallback }">
            <section class="flex flex-col p-2 gap-2 w-full rounded-xl">
                <div class="flex items-center gap-2">
                    <Avatar :image="logoIcon" shape="circle" size="large" />
                    <span class="font-bold">
                        <i 
                            :class="[
                                'pi text-xl',
                                message.severity === 'success' ? 'pi-check-circle text-orange-500' :
                                message.severity === 'info' ? 'pi-info-circle text-blue-500' :
                                message.severity === 'warn' ? 'pi-exclamation-triangle text-yellow-500' :
                                'pi-times-circle text-red-500'
                            ]"
                        ></i>
                        {{ message.summary }} 메시지
                    </span>
                </div>
                <div class="flex flex-col p-2 gap-2">
                    <div class="text-sm text-gray-600">
                        {{ message.detail }}
                    </div>
                </div>
                <div class="flex gap-4 p-2 justify-end">
                    <Button label="확인" size="small" @click="closeCallback"></Button>
                </div>
            </section>
        </template>
    </Toast>
</template>

