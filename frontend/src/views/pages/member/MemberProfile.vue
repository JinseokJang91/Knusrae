<template>
    <div class="member-profile-container">
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
            @retry="loadMemberProfile"
        />

        <!-- 프로필 콘텐츠 (프로필 헤더 + 레시피 통합 카드) -->
        <div v-else-if="member" class="profile-content">
            <div class="card profile-card">
                <!-- 프로필 헤더 -->
                <div class="profile-header">
                    <!-- 프로필 이미지 -->
                    <div class="profile-avatar">
                        <img 
                            v-if="member.profileImage" 
                            :src="member.profileImage" 
                            alt="프로필 이미지"
                            class="avatar-image"
                        />
                        <span v-else class="avatar-placeholder">
                            {{ member.nickname?.substring(0, 1) || member.name?.substring(0, 1) || '?' }}
                        </span>
                    </div>

                    <!-- 프로필 정보 -->
                    <div class="profile-info">
                        <h1 class="profile-name">{{ member.nickname || member.name }}</h1>
                        <p v-if="member.bio" class="profile-bio">{{ member.bio }}</p>
                        
                        <!-- 통계 정보 블록 -->
                        <div class="profile-stats">
                            <div class="stat-item" @click="showFollowersDialog = true">
                                <span class="stat-value">{{ member.followerCount || 0 }}</span>
                                <span class="stat-label">팔로워</span>
                            </div>
                            <div class="stat-item" @click="showFollowingsDialog = true">
                                <span class="stat-value">{{ member.followingCount || 0 }}</span>
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

                <!-- 레시피 목록 (Favorites/Category와 동일한 RecipeGridCard + _recipe-card-list 스타일) -->
                <div class="recipe-section">
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
                        <RecipeGridCard
                            v-for="recipe in displayRecipes"
                            :key="recipe.id"
                            :recipe="getRecipeGridItem(recipe)"
                            :category-label="getCategoryName(recipe)"
                            :is-bookmarked="bookmarkedRecipeIds.has(recipe.id)"
                            show-bookmark
                            :show-comment-count="false"
                            :show-author="true"
                            @click="goToRecipeDetail"
                            @favorite="toggleFavorite"
                            @bookmark="bookmarkRecipe"
                            @scroll-to-comments="scrollToComments"
                        />
                    </div>
                </div>
            </div>
        </div>

        <!-- 팔로워/팔로잉 목록 Dialog -->
        <FollowListDialog
            v-model:visible="showFollowersDialog"
            :memberId="memberId"
            type="followers"
        />
        
        <FollowListDialog
            v-model:visible="showFollowingsDialog"
            :memberId="memberId"
            type="followings"
        />

        <!-- 북마크 Dialog -->
        <BookmarkDialog
            v-model:visible="bookmarkDialogVisible"
            :recipe-id="bookmarkRecipeId"
            @bookmarked="onBookmarked"
        />
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { useAppToast } from '@/utils/toast';
import { getMemberProfile, checkFollowing, followUser, unfollowUser } from '@/api/followApi';
import { getRecipeListByMember, getFavorites, toggleFavorite as toggleFavoriteApi } from '@/api/recipeApi';
import { getRecipeBooks, getBookmarksByRecipeBook } from '@/api/bookmarkApi';
import PageStateBlock from '@/components/common/PageStateBlock.vue';
import Button from 'primevue/button';
import RecipeGridCard from '@/components/recipe/RecipeGridCard.vue';
import FollowListDialog from '@/components/follow/FollowListDialog.vue';
import BookmarkDialog from '@/components/bookmark/BookmarkDialog.vue';
import type { Recipe, RecipeGridItem } from '@/types/recipe';
import type { RecipeCookingTip } from '@/types/recipe';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const { showError, showWarn } = useAppToast();

const memberId = ref<number>(Number(route.params.id));
const member = ref<{
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

// 북마크 Dialog
const bookmarkDialogVisible = ref(false);
const bookmarkRecipeId = ref<number | null>(null);
const bookmarkedRecipeIds = ref<Set<number>>(new Set());

const isLoggedIn = computed(() => authStore.isLoggedIn);
const currentMemberId = computed(() => authStore.memberInfo?.id);
const isOwnProfile = computed(() => currentMemberId.value === memberId.value);

const displayRecipes = computed(() => recipes.value);

const loadMemberProfile = async () => {
    loading.value = true;
    error.value = null;
    
    try {
        member.value = await getMemberProfile(memberId.value);
        
        // 팔로우 여부 확인 (로그인한 경우만)
        if (isLoggedIn.value && !isOwnProfile.value) {
            const followStatus = await checkFollowing(memberId.value);
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
        const list = await getRecipeListByMember(memberId.value);
        let favoriteRecipeIds: number[] = [];
        if (currentMemberId.value) {
            try {
                const favoritesList = await getFavorites(currentMemberId.value);
                favoriteRecipeIds = favoritesList.map((fav) => fav.recipeId);
            } catch {
                // 무시
            }
        }
        recipes.value = list.map((r) => ({
            ...r,
            isFavorite: favoriteRecipeIds.includes(r.id)
        }));
        await loadBookmarkedRecipeIds();
    } catch (err) {
        console.error('레시피 로드 실패:', err);
    } finally {
        recipesLoading.value = false;
    }
};

const loadBookmarkedRecipeIds = async () => {
    if (!currentMemberId.value) {
        bookmarkedRecipeIds.value = new Set();
        return;
    }
    try {
        const recipeBooks = await getRecipeBooks();
        const ids = new Set<number>();
        for (const recipeBook of recipeBooks) {
            const bookmarks = await getBookmarksByRecipeBook(recipeBook.id);
            bookmarks.forEach((b) => ids.add(b.recipeId));
        }
        bookmarkedRecipeIds.value = ids;
    } catch {
        bookmarkedRecipeIds.value = new Set();
    }
};

const extractCookingTime = (cookingTips: RecipeCookingTip[] | undefined): string | null => {
    if (!cookingTips || !Array.isArray(cookingTips)) return null;
    const tip = cookingTips.find((t) => t.codeId === 'COOKING_TIME');
    return tip?.detailName ?? null;
};

const extractServings = (cookingTips: RecipeCookingTip[] | undefined): string | null => {
    if (!cookingTips || !Array.isArray(cookingTips)) return null;
    const tip = cookingTips.find((t) => t.codeId === 'SERVINGS');
    return tip?.detailName ?? null;
};

const getRecipeGridItem = (recipe: Recipe): RecipeGridItem => ({
    id: recipe.id,
    title: recipe.title,
    thumbnail: recipe.thumbnail,
    cookingTime: recipe.cookingTime ?? extractCookingTime(recipe.cookingTips) ?? undefined,
    servings: recipe.servings ?? extractServings(recipe.cookingTips) ?? undefined,
    hits: recipe.hits,
    commentCount: recipe.commentCount,
    isFavorite: recipe.isFavorite,
    memberNickname: recipe.memberNickname,
    memberName: recipe.memberName,
    memberProfileImage: recipe.memberProfileImage
});

const getCategoryName = (recipe: Recipe): string | null => {
    if (!recipe.categories?.length) return null;
    const keywordCategory = recipe.categories.find((cat) => cat.codeId === 'COOKING_KEYWORD');
    const target = keywordCategory || recipe.categories[0];
    return target?.detailName || target?.codeName || null;
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
            await unfollowUser(memberId.value);
            isFollowing.value = false;
            if (member.value) {
                member.value.followerCount--;
            }
        } else {
            await followUser(memberId.value);
            isFollowing.value = true;
            if (member.value) {
                member.value.followerCount++;
            }
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

const scrollToComments = (recipeId: number) => {
    router.push(`/recipe/${recipeId}#comments`);
};

const toggleFavorite = async (recipeId: number) => {
    if (!currentMemberId.value) return;
    const recipe = recipes.value.find((r) => r.id === recipeId);
    if (!recipe) return;
    try {
        const response = await toggleFavoriteApi(currentMemberId.value, recipeId);
        recipe.isFavorite = response.isFavorite;
    } catch (err) {
        console.error('찜 토글 실패:', err);
    }
};

const bookmarkRecipe = (recipeId: number) => {
    if (!isLoggedIn.value) {
        showWarn('로그인이 필요한 기능입니다.');
        router.push({ path: '/auth/login', query: { redirect: route.fullPath } });
        return;
    }
    bookmarkRecipeId.value = recipeId;
    bookmarkDialogVisible.value = true;
};

const onBookmarked = async () => {
    await loadBookmarkedRecipeIds();
};

onMounted(() => {
    loadMemberProfile();
});
</script>

<style scoped lang="scss">
.member-profile-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 2rem;
}

.profile-content {
    /* 1. 프로필 카드 시각적 강화: 배경, 테두리, 그림자 */
    .profile-card {
        background: var(--surface-card);
        border: 1px solid var(--surface-border);
        border-radius: 12px;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
        overflow: hidden;
        padding: 1.5rem 2rem 2rem;
    }

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
            border: 3px solid #fdba74;
            box-shadow: 0 2px 8px rgba(251, 146, 60, 0.25);
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
            
            /* 5. 수직 리듬 정리: margin 일관화 (1rem 기준) */
            .profile-name {
                margin: 0 0 1rem 0;
                font-size: 2rem;
                font-weight: 700;
                color: var(--text-color);
            }
            
            .profile-bio {
                margin: 0 0 1.25rem 0;
                color: var(--text-color-secondary);
                line-height: 1.6;
            }
            
            /* 3. 통계 영역 블록화: 시선이 모이는 박스 */
            .profile-stats {
                display: flex;
                gap: 2rem;
                margin-bottom: 1.25rem;
                padding: 1rem 1.25rem;
                background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%);
                border: 1px solid #fdba74;
                border-radius: 10px;
                box-shadow: 0 1px 4px rgba(251, 146, 60, 0.1);
                
                .stat-item {
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    cursor: pointer;
                    transition: all 0.2s;
                    
                    &:hover {
                        opacity: 0.8;
                    }
                    
                    /* 2. 통계 숫자 대비 강화: 진한 오렌지 톤 */
                    .stat-value {
                        font-size: 1.5rem;
                        font-weight: 700;
                        color: #c2410c;
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
                margin-top: 0.25rem;
            }
        }
    }
    
    /* 레시피 그리드/카드 스타일은 layout _recipe-card-list.scss 공통 사용 */
}

@media (max-width: 768px) {
    .member-profile-container {
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
}
</style>
