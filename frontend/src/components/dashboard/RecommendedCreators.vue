<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { getRecommendedCreators } from '@/api/creatorApi';
import { followUser } from '@/api/followApi';
import { useAuthStore } from '@/stores/authStore';
import { useAppToast } from '@/utils/toast';
import type { Creator } from '@/types/creator';

const router = useRouter();
const authStore = useAuthStore();
const { showSuccess, showError } = useAppToast();

const creators = ref<Creator[]>([]);
const loading = ref(false);
const followLoadingMap = ref<Map<number, boolean>>(new Map());
// 팔로우 직후 카드 페이드아웃 처리를 위한 Set
const removingSet = ref<Set<number>>(new Set());

const isLoggedIn = computed(() => authStore.isLoggedIn);

const loadCreators = async () => {
    loading.value = true;
    try {
        const data = await getRecommendedCreators(6);
        creators.value = data;
    } catch (error) {
        console.error('추천 크리에이터 로딩 실패:', error);
        showError('추천 크리에이터를 불러오는데 실패했습니다.');
    } finally {
        loading.value = false;
    }
};

const goToProfile = (memberId: number) => {
    router.push(`/member/${memberId}`);
};

const handleFollow = async (creator: Creator) => {
    if (!isLoggedIn.value) {
        router.push('/auth/login');
        return;
    }

    followLoadingMap.value.set(creator.memberId, true);
    try {
        await followUser(creator.memberId);
        showSuccess(`${creator.nickname}님을 팔로우했습니다.`);

        // 팔로우 성공 시 카드를 목록에서 제거 (페이드아웃 후 삭제)
        removingSet.value = new Set([...removingSet.value, creator.memberId]);
        setTimeout(() => {
            creators.value = creators.value.filter(c => c.memberId !== creator.memberId);
            const next = new Set(removingSet.value);
            next.delete(creator.memberId);
            removingSet.value = next;
        }, 400);
    } catch (error: any) {
        if (error?.status === 409) {
            showError('이미 팔로우 중입니다.');
        } else {
            showError('팔로우에 실패했습니다.');
        }
    } finally {
        followLoadingMap.value.set(creator.memberId, false);
    }
};

const isFollowLoading = (memberId: number): boolean => {
    return followLoadingMap.value.get(memberId) ?? false;
};

const isRemoving = (memberId: number): boolean => {
    return removingSet.value.has(memberId);
};

const formatNumber = (num: number): string => {
    if (num >= 10000) return `${(num / 10000).toFixed(1)}만`;
    if (num >= 1000) return `${(num / 1000).toFixed(1)}천`;
    return num.toString();
};

const truncateBio = (bio?: string): string => {
    if (!bio) return '소개가 없습니다.';
    return bio.length > 45 ? bio.substring(0, 45) + '...' : bio;
};

onMounted(() => {
    loadCreators();
});
</script>

<template>
    <div class="recommended-creators">
        <!-- 섹션 헤더 -->
        <div class="section-header">
            <div class="header-left">
                <h2 class="section-title">
                    <i class="pi pi-users" style="color: var(--primary-color);"></i>
                    추천 크리에이터
                </h2>
                <p class="section-subtitle">맛있는 레시피를 공유하는 크리에이터를 팔로우해보세요</p>
            </div>
        </div>

        <!-- 로딩 상태 -->
        <div v-if="loading" class="loading-container">
            <i class="pi pi-spinner pi-spin" style="font-size: 2rem; color: var(--primary-color);"></i>
        </div>

        <!-- 크리에이터 없음 -->
        <div v-else-if="creators.length === 0" class="empty-state">
            <i class="pi pi-users" style="font-size: 3rem; color: var(--text-color-secondary);"></i>
            <p>추천 크리에이터가 없습니다.</p>
        </div>

        <!-- 크리에이터 그리드 -->
        <div v-else class="creators-grid">
            <div
                v-for="creator in creators"
                :key="creator.memberId"
                class="creator-card"
                :class="{ 'removing': isRemoving(creator.memberId) }"
                @click="goToProfile(creator.memberId)"
            >
                <!-- 추천 이유 배지 -->
                <div class="recommend-badge">
                    <span class="badge-text">{{ creator.recommendReason }}</span>
                </div>

                <!-- 프로필 이미지 -->
                <div class="creator-avatar">
                    <img
                        v-if="creator.profileImage"
                        :src="creator.profileImage"
                        :alt="creator.nickname"
                        class="avatar-img"
                    />
                    <div v-else class="avatar-placeholder">
                        {{ creator.nickname?.charAt(0) ?? '?' }}
                    </div>
                </div>

                <!-- 크리에이터 정보 -->
                <div class="creator-info">
                    <h3 class="creator-nickname">{{ creator.nickname }}</h3>
                    <p class="creator-bio">{{ truncateBio(creator.bio) }}</p>
                </div>

                <!-- 통계 -->
                <div class="creator-stats">
                    <div class="stat-item">
                        <i class="pi pi-users"></i>
                        <span>{{ formatNumber(creator.followerCount) }}</span>
                        <span class="stat-label">팔로워</span>
                    </div>
                    <div class="stat-divider"></div>
                    <div class="stat-item">
                        <i class="pi pi-book"></i>
                        <span>{{ creator.recipeCount }}</span>
                        <span class="stat-label">레시피</span>
                    </div>
                    <div class="stat-divider"></div>
                    <div class="stat-item">
                        <i class="pi pi-eye"></i>
                        <span>{{ formatNumber(creator.totalHits) }}</span>
                        <span class="stat-label">조회</span>
                    </div>
                </div>

                <!-- 팔로우 버튼 -->
                <button
                    class="follow-btn"
                    :class="{ 'loading': isFollowLoading(creator.memberId) }"
                    @click.stop="handleFollow(creator)"
                    :disabled="isFollowLoading(creator.memberId)"
                >
                    <i v-if="isFollowLoading(creator.memberId)" class="pi pi-spinner pi-spin"></i>
                    <i v-else class="pi pi-plus"></i>
                    <span>팔로우</span>
                </button>
            </div>
        </div>
    </div>
</template>

<style scoped lang="scss">
.recommended-creators {
    width: 100%;
}

.section-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    margin-bottom: 20px;

    .header-left {
        .section-title {
            font-size: 1.25rem;
            font-weight: 700;
            color: var(--text-color);
            margin: 0 0 4px 0;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .section-subtitle {
            font-size: 0.875rem;
            color: var(--text-color-secondary);
            margin: 0;
        }
    }
}

.loading-container,
.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px;
    gap: 12px;
    color: var(--text-color-secondary);
}

// 크리에이터 그리드
.creators-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;

    @media (max-width: 1024px) {
        grid-template-columns: repeat(2, 1fr);
    }

    @media (max-width: 600px) {
        grid-template-columns: 1fr;
    }
}

// 크리에이터 카드
.creator-card {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 24px 20px 20px;
    border-radius: 16px;
    border: 1px solid var(--surface-border);
    background: var(--surface-card);
    cursor: pointer;
    transition: all 0.25s ease, opacity 0.4s ease, transform 0.4s ease;

    &:hover {
        transform: translateY(-6px);
        box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
        border-color: var(--primary-color);
    }

    &.removing {
        opacity: 0;
        transform: scale(0.92);
        pointer-events: none;
    }
}

// 추천 배지
.recommend-badge {
    position: absolute;
    top: 12px;
    right: 12px;

    .badge-text {
        font-size: 0.7rem;
        font-weight: 600;
        color: var(--primary-color);
        background: rgba(255, 107, 53, 0.1);
        padding: 3px 8px;
        border-radius: 20px;
        white-space: nowrap;
    }
}

// 아바타
.creator-avatar {
    margin-bottom: 12px;

    .avatar-img {
        width: 72px;
        height: 72px;
        border-radius: 50%;
        object-fit: cover;
        border: 3px solid var(--surface-border);
    }

    .avatar-placeholder {
        width: 72px;
        height: 72px;
        border-radius: 50%;
        background: linear-gradient(135deg, var(--primary-color), rgba(255, 107, 53, 0.6));
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.8rem;
        font-weight: 700;
        color: white;
    }
}

// 크리에이터 정보
.creator-info {
    text-align: center;
    margin-bottom: 16px;
    width: 100%;

    .creator-nickname {
        font-size: 1rem;
        font-weight: 700;
        color: var(--text-color);
        margin: 0 0 6px 0;
    }

    .creator-bio {
        font-size: 0.8rem;
        color: var(--text-color-secondary);
        margin: 0;
        min-height: 36px;
        line-height: 1.4;
    }
}

// 통계
.creator-stats {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0;
    width: 100%;
    margin-bottom: 16px;
    padding: 12px 0;
    border-top: 1px solid var(--surface-border);
    border-bottom: 1px solid var(--surface-border);

    .stat-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 2px;
        flex: 1;

        i {
            font-size: 0.85rem;
            color: var(--primary-color);
            margin-bottom: 2px;
        }

        span:not(.stat-label) {
            font-size: 0.9rem;
            font-weight: 700;
            color: var(--text-color);
        }

        .stat-label {
            font-size: 0.7rem;
            color: var(--text-color-secondary);
        }
    }

    .stat-divider {
        width: 1px;
        height: 30px;
        background: var(--surface-border);
    }
}

// 팔로우 버튼
.follow-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    width: 100%;
    padding: 9px 0;
    border-radius: 10px;
    border: 2px solid var(--primary-color);
    background: transparent;
    color: var(--primary-color);
    font-size: 0.875rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover:not(:disabled) {
        background: var(--primary-color);
        color: white;
    }

    &.loading,
    &:disabled {
        opacity: 0.6;
        cursor: not-allowed;
    }
}
</style>
