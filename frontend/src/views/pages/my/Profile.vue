<template>
    <div class="card">
        <h2 class="text-2xl font-bold mb-4">내 정보 수정</h2>
        <div class="grid grid-cols-12 gap-6">
            <div class="col-span-12 md:col-span-4">
                <div class="p-4 border rounded-md flex flex-col items-center gap-4">
                    <div class="w-28 h-28 rounded-full bg-gray-200 flex items-center justify-center text-3xl text-gray-500">{{ initials }}</div>
                    <div class="text-center">
                        <div class="font-semibold">{{ form.name || '사용자' }}</div>
                        <div class="text-sm text-gray-500">{{ form.email || 'email@example.com' }}</div>
                    </div>
                    <button type="button" class="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600">
                        <span class="pi pi-upload mr-2"></span>
                        <span>프로필 이미지 변경</span>
                    </button>
                </div>
            </div>
            <div class="col-span-12 md:col-span-8">
                <div class="grid grid-cols-12 gap-4">
                    <div class="col-span-12 md:col-span-6">
                        <label class="block text-sm mb-2">이름</label>
                        <input v-model="form.name" type="text" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" placeholder="이름" />
                    </div>
                    <div class="col-span-12 md:col-span-6">
                        <label class="block text-sm mb-2">닉네임</label>
                        <input v-model="form.nickname" type="text" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" placeholder="닉네임" />
                    </div>
                    <div class="col-span-12">
                        <label class="block text-sm mb-2">이메일</label>
                        <input v-model="form.email" type="email" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 opacity-80" disabled />
                    </div>
                </div>

                <div class="mt-6 p-4 border rounded-md">
                    <h3 class="font-semibold mb-4">비밀번호 변경</h3>
                    <div class="grid grid-cols-12 gap-4">
                        <div class="col-span-12 md:col-span-6">
                            <label class="block text-sm mb-2">새 비밀번호</label>
                            <input v-model="form.newPassword" type="password" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" placeholder="새 비밀번호" />
                        </div>
                        <div class="col-span-12 md:col-span-6">
                            <label class="block text-sm mb-2">새 비밀번호 확인</label>
                            <input v-model="form.newPasswordConfirm" type="password" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" placeholder="새 비밀번호 확인" />
                        </div>
                    </div>
                </div>

                <div class="mt-6 flex gap-2 justify-end">
                    <button type="button" class="px-4 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50" @click="onReset">
                        <span class="pi pi-refresh mr-2"></span>
                        <span>초기화</span>
                    </button>
                    <button type="button" class="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600" @click="onSave">
                        <span class="pi pi-save mr-2"></span>
                        <span>저장</span>
                    </button>
                </div>
            </div>
        </div>
    </div>
    <div v-if="toast" class="mt-3 text-green-600 text-sm">{{ toast }}</div>
    <div v-if="error" class="mt-3 text-red-600 text-sm">{{ error }}</div>
    <div class="mt-6 text-xs text-gray-500">UI 스켈레톤 - 실제 API 연동 시 상태/검증을 연결합니다.</div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';

interface ProfileFormState {
    name: string;
    nickname: string;
    email: string;
    newPassword: string;
    newPasswordConfirm: string;
}

const form = reactive<ProfileFormState>({
    name: '홍길동',
    nickname: '길동이',
    email: 'gildong@example.com',
    newPassword: '',
    newPasswordConfirm: ''
});

const toast = ref<string>('');
const error = ref<string>('');

const initials = computed(() => {
    const base = form.name?.trim() || '사용자';
    return base.substring(0, 1);
});

const onReset = () => {
    form.name = '홍길동';
    form.nickname = '길동이';
    form.newPassword = '';
    form.newPasswordConfirm = '';
    toast.value = '';
    error.value = '';
};

const onSave = () => {
    toast.value = '';
    error.value = '';
    if (form.newPassword || form.newPasswordConfirm) {
        if (form.newPassword.length < 8) {
            error.value = '비밀번호는 8자 이상이어야 합니다.';
            return;
        }
        if (form.newPassword !== form.newPasswordConfirm) {
            error.value = '비밀번호가 일치하지 않습니다.';
            return;
        }
    }
    toast.value = '프로필 정보가 저장되었습니다. (모의)';
};
</script>

<style scoped></style>
