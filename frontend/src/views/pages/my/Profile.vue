<template>
    <div class="profile-content">
        <!-- 초기 로딩 -->
        <PageStateBlock
            v-if="initialLoading"
            state="loading"
            loading-message="프로필 정보를 불러오는 중..."
        />

        <!-- 초기 로드 실패 -->
        <PageStateBlock
            v-else-if="initialError"
            state="error"
            error-title="프로필을 불러올 수 없습니다"
            :error-message="initialError"
            retry-label="다시 시도"
            @retry="retryLoadMemberInfo"
        />

        <!-- 폼 -->
        <div v-else class="grid grid-cols-12 gap-6">
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
                        <label class="block text-sm font-medium mb-2">이름</label>
                        <InputText
                            v-model="form.name"
                            class="w-full"
                            placeholder="이름"
                            :disabled="saving"
                        />
                    </div>
                    <div class="col-span-12 md:col-span-6">
                        <label class="block text-sm font-medium mb-2">닉네임</label>
                        <InputText
                            v-model="form.nickname"
                            class="w-full"
                            placeholder="닉네임"
                            :disabled="saving"
                        />
                    </div>
                    <div class="col-span-12">
                        <label class="block text-sm font-medium mb-2">이메일</label>
                        <InputText
                            v-model="form.email"
                            type="email"
                            class="w-full"
                            disabled
                        />
                    </div>
                    <div class="col-span-12">
                        <label class="block text-sm font-medium mb-2">자기소개</label>
                        <Textarea
                            v-model="form.bio"
                            class="w-full"
                            placeholder="자기소개를 입력해주세요"
                            :rows="4"
                            :disabled="saving"
                        />
                    </div>
                    <div class="col-span-12 md:col-span-6">
                        <label class="block text-sm font-medium mb-2">팔로워</label>
                        <div 
                            class="w-full px-3 py-2 border border-gray-300 rounded bg-white text-gray-700 cursor-pointer hover:bg-gray-50 transition-colors flex items-center justify-between"
                            @click="showFollowersDialog = true"
                        >
                            <span>{{ form.followerCount || 0 }}</span>
                            <i class="pi pi-angle-right text-gray-400"></i>
                        </div>
                        <p class="text-sm text-gray-500 mt-1">클릭하여 목록 보기</p>
                    </div>
                    <div class="col-span-12 md:col-span-6">
                        <label class="block text-sm font-medium mb-2">팔로잉</label>
                        <div 
                            class="w-full px-3 py-2 border border-gray-300 rounded bg-white text-gray-700 cursor-pointer hover:bg-gray-50 transition-colors flex items-center justify-between"
                            @click="showFollowingsDialog = true"
                        >
                            <span>{{ form.followingCount || 0 }}</span>
                            <i class="pi pi-angle-right text-gray-400"></i>
                        </div>
                        <p class="text-sm text-gray-500 mt-1">클릭하여 목록 보기</p>
                    </div>
                </div>

                <div class="mt-6 flex gap-2 justify-end">
                    <Button
                        type="button"
                        label="취소"
                        icon="pi pi-times"
                        severity="secondary"
                        outlined
                        :disabled="saving"
                        @click="onCancel"
                    />
                    <Button
                        type="button"
                        label="저장"
                        icon="pi pi-save"
                        :loading="saving"
                        :disabled="saving"
                        @click="onSave"
                    />
                </div>
            </div>
        </div>

        <!-- 팔로워/팔로잉 목록 Dialog -->
        <FollowListDialog
            v-if="authStore.memberInfo?.id"
            v-model:visible="showFollowersDialog"
            :memberId="authStore.memberInfo.id"
            type="followers"
            @update:visible="handleDialogClose"
        />
        
        <FollowListDialog
            v-if="authStore.memberInfo?.id"
            v-model:visible="showFollowingsDialog"
            :memberId="authStore.memberInfo.id"
            type="followings"
            @update:visible="handleDialogClose"
        />
    </div>
</template>

<script setup lang="ts">
import { updateProfile } from '@/api/memberApi';
import { computed, onMounted, reactive, ref } from 'vue';
import { fetchMemberInfo } from '@/utils/auth';
import { useAuthStore } from '@/stores/authStore';
import { useAppToast } from '@/utils/toast';
import type { ProfileFormState } from '@/types/profile';
import PageStateBlock from '@/components/common/PageStateBlock.vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import Textarea from 'primevue/textarea';
import FollowListDialog from '@/components/follow/FollowListDialog.vue';

const authStore = useAuthStore();
const { showSuccess, showError } = useAppToast();

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
const initialLoading = ref(true);
const initialError = ref<string | null>(null);
const saving = ref(false);
const showFollowersDialog = ref(false);
const showFollowingsDialog = ref(false);

/** 저장된 스냅샷(취소 시 복원용) */
const savedSnapshot = ref<ProfileFormState | null>(null);

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

const loadMemberInfo = async () => {
    initialError.value = null;
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
            savedSnapshot.value = { ...form };
        }
    } catch (error) {
        console.error('회원 정보 조회 실패:', error);
        initialError.value = (error instanceof Error ? error.message : null) || '회원 정보를 불러오지 못했습니다.';
    } finally {
        initialLoading.value = false;
    }
};

const onCancel = () => {
    if (savedSnapshot.value) {
        form.name = savedSnapshot.value.name;
        form.nickname = savedSnapshot.value.nickname;
        form.bio = savedSnapshot.value.bio;
        form.profileImage = savedSnapshot.value.profileImage;
        profileImageFile.value = null;
    }
};

const retryLoadMemberInfo = async () => {
    initialLoading.value = true;
    await loadMemberInfo();
};

const onSave = async () => {
    try {
        saving.value = true;

        const formData = new FormData();
        if (form.name) formData.append('name', form.name);
        if (form.nickname) formData.append('nickname', form.nickname);
        if (form.bio !== undefined) formData.append('bio', form.bio);
        if (profileImageFile.value) {
            formData.append('profileImage', profileImageFile.value);
        }

        await updateProfile(formData);

        await loadMemberInfo();
        await authStore.loadMemberInfo();
        profileImageFile.value = null;

        showSuccess('프로필이 저장되었습니다.');
    } catch (error) {
        console.error('프로필 저장 실패:', error);
        const message = (error instanceof Error ? error.message : null) || '프로필 저장에 실패했습니다.';
        showError(message);
    } finally {
        saving.value = false;
    }
};

const handleDialogClose = async () => {
    // Dialog가 닫힐 때 프로필 정보를 다시 로드하여 카운트 업데이트
    await loadMemberInfo();
};

onMounted(() => {
    loadMemberInfo();
});
</script>

<style scoped></style>
