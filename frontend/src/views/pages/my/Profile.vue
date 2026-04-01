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

/** 글자 수 제한 */
const MAX_NAME = 10;
const MAX_NICKNAME = 50;
const MAX_BIO = 1000;

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

const initials = computed(() => {
    const base = form.name?.trim() || '사용자';
    return base.substring(0, 1);
});

/** 현재 글자 수 (제한 초과 시 입력 차단용) */
const nameLength = computed(() => (form.name ?? '').length);
const nicknameLength = computed(() => (form.nickname ?? '').length);
const bioLength = computed(() => (form.bio ?? '').length);

/** 저장 전 유효성 검사 */
function validateBeforeSave(): { valid: boolean; message?: string } {
    const name = form.name?.trim() ?? '';
    const nickname = form.nickname?.trim() ?? '';
    if (!name) return { valid: false, message: '이름을 입력해주세요.' };
    if (name.length > MAX_NAME) return { valid: false, message: `이름은 ${MAX_NAME}자 이내로 입력해주세요.` };
    if (!nickname) return { valid: false, message: '닉네임을 입력해주세요.' };
    if (nickname.length > MAX_NICKNAME) return { valid: false, message: `닉네임은 ${MAX_NICKNAME}자 이내로 입력해주세요.` };
    if (bioLength.value > MAX_BIO) return { valid: false, message: `자기소개는 ${MAX_BIO}자 이내로 입력해주세요.` };
    return { valid: true };
}

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
        }
    } catch (error) {
        console.error('회원 정보 조회 실패:', error);
        initialError.value = (error instanceof Error ? error.message : null) || '회원 정보를 불러오지 못했습니다.';
    } finally {
        initialLoading.value = false;
    }
};

const retryLoadMemberInfo = async () => {
    initialLoading.value = true;
    await loadMemberInfo();
};

const onSave = async () => {
    const validation = validateBeforeSave();
    if (!validation.valid) {
        showError(validation.message ?? '입력값을 확인해주세요.');
        return;
    }

    try {
        saving.value = true;

        const formData = new FormData();
        formData.append('name', form.name!.trim());
        formData.append('nickname', form.nickname!.trim());
        formData.append('bio', (form.bio ?? '').trim());
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

<template>
    <div class="page-container page-container--card page-container--wide profile-card">
        <div class="profile-content">
            <!-- 초기 로딩 -->
            <PageStateBlock v-if="initialLoading" state="loading" loading-message="프로필 정보를 불러오는 중..." />

            <!-- 초기 로드 실패 -->
            <PageStateBlock v-else-if="initialError" state="error" error-title="프로필을 불러올 수 없습니다" :error-message="initialError" retry-label="다시 시도" @retry="retryLoadMemberInfo" />

            <template v-else>
                <!-- 1. 상단: 프로필 요약 (미리보기) -->
                <section class="profile-summary">
                    <input ref="profileImageInputRef" type="file" accept="image/*" @change="onProfileImageChange" class="hidden" />
                    <div class="profile-summary-avatar" @click="() => profileImageInputRef?.click()">
                        <img v-if="form.profileImage" :src="form.profileImage" alt="프로필 이미지" class="w-full h-full object-cover" />
                        <span v-else class="profile-summary-initials">{{ initials }}</span>
                        <span class="profile-summary-avatar-hint">클릭하여 변경</span>
                    </div>
                    <div class="profile-summary-info">
                        <h2 class="profile-summary-name">{{ form.name || '이름' }}</h2>
                        <p class="profile-summary-nickname">@{{ form.nickname || '닉네임' }}</p>
                        <div class="profile-summary-social">
                            <button type="button" class="profile-summary-link" @click="showFollowersDialog = true">
                                <span class="profile-summary-count">{{ form.followerCount ?? 0 }}</span>
                                <span class="profile-summary-label">팔로워</span>
                            </button>
                            <span class="profile-summary-divider" aria-hidden="true">·</span>
                            <button type="button" class="profile-summary-link" @click="showFollowingsDialog = true">
                                <span class="profile-summary-count">{{ form.followingCount ?? 0 }}</span>
                                <span class="profile-summary-label">팔로잉</span>
                            </button>
                        </div>
                    </div>
                </section>

                <!-- 2. 아래: 기본 정보 수정 카드 -->
                <section class="profile-edit-card">
                    <h3 class="profile-edit-title">기본 정보</h3>
                    <div class="profile-edit-fields">
                        <div class="grid grid-cols-12 gap-4">
                            <div class="col-span-12 md:col-span-6">
                                <label class="block text-sm font-medium mb-2">
                                    이름
                                    <span class="profile-edit-count" :class="{ 'text-red-500': nameLength > MAX_NAME }">({{ nameLength }}/{{ MAX_NAME }})</span>
                                </label>
                                <InputText v-model="form.name" class="w-full" placeholder="이름" :maxlength="MAX_NAME" :disabled="saving" />
                            </div>
                            <div class="col-span-12 md:col-span-6">
                                <label class="block text-sm font-medium mb-2">
                                    닉네임
                                    <span class="profile-edit-count" :class="{ 'text-red-500': nicknameLength > MAX_NICKNAME }">({{ nicknameLength }}/{{ MAX_NICKNAME }})</span>
                                </label>
                                <InputText v-model="form.nickname" class="w-full" placeholder="닉네임" :maxlength="MAX_NICKNAME" :disabled="saving" />
                            </div>
                            <div class="col-span-12">
                                <label class="block text-sm font-medium mb-2">이메일</label>
                                <InputText v-model="form.email" type="email" class="w-full" disabled />
                            </div>
                            <div class="col-span-12">
                                <label class="block text-sm font-medium mb-2">
                                    자기소개
                                    <span class="profile-edit-count" :class="{ 'text-red-500': bioLength > MAX_BIO }">({{ bioLength }}/{{ MAX_BIO }})</span>
                                </label>
                                <Textarea v-model="form.bio" class="w-full profile-edit-bio" placeholder="자기소개를 입력해주세요" :rows="4" :maxlength="MAX_BIO" :disabled="saving" />
                            </div>
                        </div>
                        <div class="profile-edit-actions">
                            <Button type="button" label="저장" icon="pi pi-save" :loading="saving" :disabled="saving" @click="onSave" />
                        </div>
                    </div>
                </section>
            </template>

            <!-- 팔로워/팔로잉 목록 Dialog -->
            <FollowListDialog
                v-if="authStore.memberInfo?.id"
                v-model:visible="showFollowersDialog"
                :memberId="authStore.memberInfo.id"
                type="followers"
                :followerCount="form.followerCount"
                :followingCount="form.followingCount"
                @update:visible="handleDialogClose"
            />
            <FollowListDialog
                v-if="authStore.memberInfo?.id"
                v-model:visible="showFollowingsDialog"
                :memberId="authStore.memberInfo.id"
                type="followings"
                :followerCount="form.followerCount"
                :followingCount="form.followingCount"
                @update:visible="handleDialogClose"
            />
        </div>
    </div>
</template>

<style scoped>
/* 프로필 카드: 은은한 오렌지톤 배경 */
/* 프로필 카드: 라벤더 톤 (내부 카드보다 진하게 계층감) */
.profile-card {
    background: linear-gradient(165deg, #f3eff8 0%, #ebe4f2 50%, #e5dcee 100%);
}

.profile-content {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
    min-height: 500px;
}

/* 1. 상단 프로필 요약 */
.profile-summary {
    display: flex;
    align-items: center;
    gap: 1.5rem;
    flex-wrap: wrap;
    padding: 1.25rem 1.5rem;
    background: linear-gradient(180deg, #fdfcfe 0%, #f8f5fb 100%);
    border: 1px solid var(--surface-border);
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.profile-summary-avatar {
    position: relative;
    width: 5.5rem;
    height: 5.5rem;
    min-width: 5.5rem;
    min-height: 5.5rem;
    border-radius: 50%;
    background: var(--surface-100);
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    cursor: pointer;
    transition: opacity 0.2s;
    border: 2px solid var(--surface-border);
}

.profile-summary-avatar:hover {
    opacity: 0.9;
}

.profile-summary-avatar:hover .profile-summary-avatar-hint {
    opacity: 1;
}

.profile-summary-avatar img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.profile-summary-initials {
    font-size: 1.75rem;
    font-weight: 600;
    color: var(--text-color-secondary);
}

.profile-summary-avatar-hint {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.4);
    color: #fff;
    font-size: 0.7rem;
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.2s;
}

.profile-summary-info {
    flex: 1;
    min-width: 0;
}

.profile-summary-name {
    margin: 0 0 0.25rem 0;
    font-size: 1.35rem;
    font-weight: 700;
    color: var(--text-color);
}

.profile-summary-nickname {
    margin: 0 0 0.5rem 0;
    font-size: 0.95rem;
    color: var(--text-color-secondary);
}

.profile-summary-social {
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.profile-summary-link {
    position: relative;
    display: inline-flex;
    align-items: baseline;
    gap: 0.35rem;
    padding: 0.25rem 0;
    background: none;
    border: none;
    font: inherit;
    color: var(--primary-color);
    cursor: pointer;
    transition: color 0.2s;
}

.profile-summary-link::after {
    content: '';
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0.1em;
    height: 1px;
    background: currentColor;
    opacity: 0;
    transition: opacity 0.2s;
}

.profile-summary-link:hover {
    color: var(--primary-color);
}

.profile-summary-link:hover::after {
    opacity: 0.7;
}

.profile-summary-count {
    font-weight: 600;
}

.profile-summary-label {
    font-size: 0.9rem;
    color: var(--text-color-secondary);
}

.profile-summary-divider {
    color: var(--text-color-secondary);
    font-weight: 300;
}

/* 2. 아래 기본 정보 수정 카드 */
.profile-edit-card {
    padding: 1.5rem;
    background: linear-gradient(180deg, #fdfcfe 0%, #f8f5fb 100%);
    border: 1px solid var(--surface-border);
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.profile-edit-title {
    margin: 0 0 1.25rem 0;
    font-size: 1.1rem;
    font-weight: 600;
    color: var(--text-color);
}

.profile-edit-count {
    font-weight: 400;
    color: var(--text-color-secondary);
    font-size: 0.875rem;
}

.profile-edit-fields {
    display: flex;
    flex-direction: column;
    gap: 1.25rem;
}

.profile-edit-actions {
    display: flex;
    gap: 0.5rem;
    justify-content: flex-end;
    padding-top: 0.5rem;
}

/* 자기소개: 수동 리사이즈 비활성화, 넘치면 스크롤 (PrimeVue Textarea 루트가 textarea 요소) */
:deep(.profile-edit-bio) {
    resize: none !important;
    overflow-y: auto;
}

@media (max-width: 768px) {
    .profile-summary {
        padding: 1rem;
    }

    .profile-summary-name {
        font-size: 1.2rem;
    }

    .profile-edit-card {
        padding: 1.25rem;
    }
}
</style>
