<template>
    <div class="user-profile-container">
        <!-- 로딩 상태 -->
        <PageStateBlock
            v-if="loading"
            state="loading"
            loading-message="프로필 정보를 불러오는 중..."
        />

        <!-- 에러 상태 -->
        <PageStateBlock
            v-else-if="error"
            state="error"
            error-title="프로필을 불러올 수 없습니다"
            :error-message="error"
            retry-label="다시 시도"
            @retry="loadUserProfile"
        />

        <!-- 프로필 콘텐츠 -->
        <div v-else-if="user" class="profile-content">
            <!-- 프로필 헤더 -->
            <div class="card mb-4">
                <div class="profile-header">
                    <!-- 프로필 이미지 -->
                    <div class="profile-avatar">
                        <img 
                            v-if="user.profileImage" 
                            :src="user.profileImage" 
                            alt="프로필 이미지"
                            class="avatar-image"
                        />
                        <span v-else class="avatar-placeholder">
                            {{ user.nickname?.substring(0, 1) || user.name?.substring(0, 1) || '?' }}
                        </span>
                    </div>

                    <!-- 프로필 정보 -->
                    <div class="profile-info">
                        <h1 class="profile-name">{{ user.nickname || user.name }}</h1>
                        <p v-if="user.bio" class="profile-bio">{{ user.bio }}</p>
                        
                        <!-- 통계 정보 -->
                        <div class="profile-stats">
                            <div class="stat-item" @click="showFollowersDialog = true">
                                <span class="stat-value">{{ user.followerCount || 0 }}</span>
                                <span class="stat-label">팔로워</span>
                            </div>
                            <div class="stat-item" @click="showFollowingsDialog = true">
                                <span class="stat-value">{{ user.followingCount || 0 }}</span>
                                <span class="stat-label">팔로잉</span>
                            </div>
                            <div class="stat-item">
                                <span class="stat-value">{{ recipes.length }}</span>
                                <span class="stat-label">레시피</span>
                            </div>
                        </div>

                        <!-- 팔로우 버튼 -->
                        <div v-if="!isOwnProfile" class="profile-actions">
                            <Button
                                :label="isFollowing ? '팔로잉' : '팔로우'"
                                :icon="isFollowing ? 'pi pi-check' : 'pi pi-plus'"
                                :severity="isFollowing ? 'secondary' : 'primary'"
                                :outlined="isFollowing"
                                @click="toggleFollow"
                                :loading="followLoading"
                                :disabled="!isLoggedIn"
                            />
                        </div>
                    </div>
                </div>
            </div>

            <!-- 레시피 목록 -->
            <div class="card">
                <h2 class="text-2xl font-bold mb-4">레시피</h2>
                
                <PageStateBlock
                    v-if="recipesLoading"
                    state="loading"
                    loading-message="레시피를 불러오는 중..."
                />
                
                <PageStateBlock
                    v-else-if="recipes.length === 0"
                    state="empty"
                    empty-icon="pi pi-book"
                    empty-title="등록된 레시피가 없습니다"
                    empty-message="아직 레시피를 등록하지 않았습니다."
                />
                
                <div v-else class="recipe-grid">
                    <RecipeCard
                        v-for="recipe in recipes"
                        :key="recipe.id"
                        :recipe="recipe"
                        @click="goToRecipeDetail(recipe.id)"
                    />
                </div>
            </div>
        </div>

        <!-- 팔로워/팔로잉 목록 Dialog -->
        <FollowListDialog
            v-model:visible="showFollowersDialog"
            :memberId="userId"
            type="followers"
        />
        
        <FollowListDialog
            v-model:visible="showFollowingsDialog"
            :memberId="userId"
            type="followings"
        />
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { useAppToast } from '@/utils/toast';
import { getUserProfile, checkFollowing, followUser, unfollowUser } from '@/api/followApi';
import { getRecipeListByMember } from '@/api/recipeApi';
import PageStateBlock from '@/components/common/PageStateBlock.vue';
import Button from 'primevue/button';
import RecipeCard from '@/components/recipe/RecipeCard.vue';
import FollowListDialog from '@/components/follow/FollowListDialog.vue';
import type { Recipe } from '@/types/recipe';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const { showSuccess, showError, showWarn } = useAppToast();

const userId = ref<number>(Number(route.params.id));
const user = ref<{
    id: number;
    name: string;
    nickname: string;
    profileImage?: string;
    bio?: string;
    followerCount: number;
    followingCount: number;
} | null>(null);

const recipes = ref<Recipe[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);
const recipesLoading = ref(false);
const isFollowing = ref(false);
const followLoading = ref(false);
const showFollowersDialog = ref(false);
const showFollowingsDialog = ref(false);

const isLoggedIn = computed(() => authStore.isLoggedIn);
const currentMemberId = computed(() => authStore.memberInfo?.id);
const isOwnProfile = computed(() => currentMemberId.value === userId.value);

const loadUserProfile = async () => {
    loading.value = true;
    error.value = null;
    
    try {
        user.value = await getUserProfile(userId.value);
        
        // 팔로우 여부 확인 (로그인한 경우만)
        if (isLoggedIn.value && !isOwnProfile.value) {
            const followStatus = await checkFollowing(userId.value);
            isFollowing.value = followStatus.isFollowing;
        }
        
        // 레시피 목록 로드
        await loadRecipes();
    } catch (err) {
        console.error('프로필 로드 실패:', err);
        error.value = (err instanceof Error ? err.message : null) || '프로필을 불러오는데 실패했습니다.';
    } finally {
        loading.value = false;
    }
};

const loadRecipes = async () => {
    recipesLoading.value = true;
    try {
        recipes.value = await getRecipeListByMember(userId.value);
    } catch (err) {
        console.error('레시피 로드 실패:', err);
    } finally {
        recipesLoading.value = false;
    }
};

const toggleFollow = async () => {
    if (!isLoggedIn.value) {
        showWarn('로그인이 필요한 기능입니다.');
        router.push({ path: '/auth/login', query: { redirect: route.fullPath } });
        return;
    }
    
    followLoading.value = true;
    try {
        if (isFollowing.value) {
            await unfollowUser(userId.value);
            isFollowing.value = false;
            if (user.value) {
                user.value.followerCount--;
            }
            showSuccess('언팔로우했습니다.');
        } else {
            await followUser(userId.value);
            isFollowing.value = true;
            if (user.value) {
                user.value.followerCount++;
            }
            showSuccess('팔로우했습니다.');
        }
    } catch (err) {
        console.error('팔로우 토글 실패:', err);
        showError((err instanceof Error ? err.message : null) || '팔로우 처리에 실패했습니다.');
    } finally {
        followLoading.value = false;
    }
};

const goToRecipeDetail = (recipeId: number) => {
    router.push(`/recipe/${recipeId}`);
};

onMounted(() => {
    loadUserProfile();
});
</script>

<style scoped lang="scss">
.user-profile-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 2rem;
}

.profile-content {
    .profile-header {
        display: flex;
        gap: 2rem;
        align-items: flex-start;
        
        .profile-avatar {
            flex-shrink: 0;
            width: 150px;
            height: 150px;
            border-radius: 50%;
            overflow: hidden;
            background: var(--surface-100);
            display: flex;
            align-items: center;
            justify-content: center;
            
            .avatar-image {
                width: 100%;
                height: 100%;
                object-fit: cover;
            }
            
            .avatar-placeholder {
                font-size: 3rem;
                font-weight: 600;
                color: var(--text-color-secondary);
            }
        }
        
        .profile-info {
            flex: 1;
            
            .profile-name {
                margin: 0 0 0.5rem 0;
                font-size: 2rem;
                font-weight: 700;
                color: var(--text-color);
            }
            
            .profile-bio {
                margin: 0 0 1.5rem 0;
                color: var(--text-color-secondary);
                line-height: 1.6;
            }
            
            .profile-stats {
                display: flex;
                gap: 2rem;
                margin-bottom: 1.5rem;
                
                .stat-item {
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    cursor: pointer;
                    transition: all 0.2s;
                    
                    &:hover {
                        opacity: 0.8;
                    }
                    
                    .stat-value {
                        font-size: 1.5rem;
                        font-weight: 700;
                        color: var(--primary-color);
                    }
                    
                    .stat-label {
                        font-size: 0.9rem;
                        color: var(--text-color-secondary);
                    }
                }
            }
            
            .profile-actions {
                display: flex;
                gap: 1rem;
            }
        }
    }
    
    .recipe-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
        gap: 1.5rem;
    }
}

@media (max-width: 768px) {
    .user-profile-container {
        padding: 1rem;
    }
    
    .profile-header {
        flex-direction: column;
        align-items: center;
        text-align: center;
        
        .profile-avatar {
            width: 120px;
            height: 120px;
        }
        
        .profile-info {
            .profile-stats {
                justify-content: center;
            }
            
            .profile-actions {
                justify-content: center;
            }
        }
    }
    
    .recipe-grid {
        grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
        gap: 1rem;
    }
}
</style>
