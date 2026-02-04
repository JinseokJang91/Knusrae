<template>
    <div class="profile-content">
        <div class="grid grid-cols-12 gap-6">
            <div class="col-span-12 md:col-span-4">
                <div class="p-4 border rounded-md flex flex-col items-center justify-center h-5/6">
                    <input 
                        ref="profileImageInputRef" 
                        type="file" 
                        accept="image/*" 
                        @change="onProfileImageChange" 
                        class="hidden" 
                    />
                    <div 
                        class="w-48 h-48 rounded-full bg-gray-200 flex items-center justify-center overflow-hidden cursor-pointer hover:opacity-80 transition-opacity"
                        @click="() => profileImageInputRef?.click()"
                    >
                        <img 
                            v-if="form.profileImage" 
                            :src="form.profileImage" 
                            alt="프로필 이미지" 
                            class="w-full h-full object-cover"
                        />
                        <span v-else class="text-5xl text-gray-500">{{ initials }}</span>
                    </div>
                </div>
            </div>
            <div class="col-span-12 md:col-span-8">
                <div class="grid grid-cols-12 gap-4">
                    <div class="col-span-12 md:col-span-6">
                        <label class="block text-sm mb-2">이름</label>
                        <input v-model="form.name" type="text" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500" placeholder="이름" />
                    </div>
                    <div class="col-span-12 md:col-span-6">
                        <label class="block text-sm mb-2">닉네임</label>
                        <input v-model="form.nickname" type="text" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500" placeholder="닉네임" />
                    </div>
                    <div class="col-span-12">
                        <label class="block text-sm mb-2">이메일</label>
                        <input v-model="form.email" type="email" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500 opacity-80" disabled />
                    </div>
                    <div class="col-span-12">
                        <label class="block text-sm mb-2">자기소개</label>
                        <textarea 
                            v-model="form.bio" 
                            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500 resize-none" 
                            placeholder="자기소개를 입력해주세요"
                            rows="4"
                        ></textarea>
                    </div>
                    <div class="col-span-12 md:col-span-6">
                        <label class="block text-sm mb-2">팔로워</label>
                        <div class="px-3 py-2 border border-gray-300 rounded-md bg-gray-50">
                            {{ form.followerCount || 0 }}
                        </div>
                    </div>
                    <div class="col-span-12 md:col-span-6">
                        <label class="block text-sm mb-2">팔로잉</label>
                        <div class="px-3 py-2 border border-gray-300 rounded-md bg-gray-50">
                            {{ form.followingCount || 0 }}
                        </div>
                    </div>
                </div>

                <div class="mt-6 flex gap-2 justify-end">
                    <button type="button" class="px-4 py-2 bg-gray-500 text-white rounded-md hover:bg-gray-600" @click="onSave">
                        <span class="pi pi-save mr-2"></span>
                        <span>저장</span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { updateProfile } from '@/api/memberApi';
import { computed, onMounted, reactive, ref } from 'vue';
import { fetchMemberInfo } from '@/utils/auth';
import { useAuthStore } from '@/stores/authStore';
import type { ProfileFormState } from '@/types/profile';

const authStore = useAuthStore();
const form = reactive<ProfileFormState>({
    name: '',
    nickname: '',
    email: '',
    bio: '',
    profileImage: '',
    followerCount: 0,
    followingCount: 0
});

const profileImageInputRef = ref<HTMLInputElement | null>(null);
const profileImageFile = ref<File | null>(null);
const loading = ref(false);

const initials = computed(() => {
    const base = form.name?.trim() || '사용자';
    return base.substring(0, 1);
});

const onProfileImageChange = (event: Event) => {
    const target = event.target as HTMLInputElement;
    if (target.files && target.files[0]) {
        profileImageFile.value = target.files[0];
        const reader = new FileReader();
        reader.onload = (e) => {
            form.profileImage = e.target?.result as string;
        };
        reader.readAsDataURL(target.files[0]);
    }
};

const onSave = async () => {
    try {
        loading.value = true;

        const formData = new FormData();
        if (form.name) formData.append('name', form.name);
        if (form.nickname) formData.append('nickname', form.nickname);
        if (form.bio !== undefined) formData.append('bio', form.bio);
        if (profileImageFile.value) {
            formData.append('profileImage', profileImageFile.value);
        }

        await updateProfile(formData);

        // 프로필 정보 다시 불러오기
        await loadMemberInfo();
        // authStore의 회원 정보도 업데이트하여 AppTopbar에 즉시 반영
        await authStore.loadMemberInfo();
        profileImageFile.value = null;
    } catch (error) {
        console.error('프로필 저장 실패:', error);
    } finally {
        loading.value = false;
    }
};

const loadMemberInfo = async () => {
    try {
        const memberInfo = await fetchMemberInfo();
        if (memberInfo) {
            form.name = memberInfo.name || '';
            form.nickname = memberInfo.nickname || '';
            form.email = memberInfo.email || '';
            form.bio = memberInfo.bio || '';
            form.profileImage = memberInfo.profileImage || '';
            form.followerCount = memberInfo.followerCount || 0;
            form.followingCount = memberInfo.followingCount || 0;
        }
    } catch (error) {
        console.error('회원 정보 조회 실패:', error);
    }
};

onMounted(() => {
    loadMemberInfo();
});
</script>

<style scoped></style>
