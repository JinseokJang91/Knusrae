<template>
    <Dialog
        :visible="visible"
        @update:visible="$emit('update:visible', $event)"
        modal
        :header="type === 'followers' ? '팔로워' : '팔로잉'"
        :style="{ width: '600px' }"
        :breakpoints="{ '960px': '75vw', '640px': '90vw' }"
    >
        <Tabs v-model:value="activeTab">
            <TabList>
                <Tab value="followers">팔로워</Tab>
                <Tab value="followings">팔로잉</Tab>
            </TabList>
            
            <TabPanels>
                <TabPanel value="followers">
                    <!-- 팔로워 목록 -->
                    <div class="follow-list-content">
                        <PageStateBlock
                            v-if="followersLoading"
                            state="loading"
                            loading-message="팔로워 목록을 불러오는 중..."
                        />
                        
                        <PageStateBlock
                            v-else-if="followers.length === 0"
                            state="empty"
                            empty-icon="pi pi-users"
                            empty-title="팔로워가 없습니다"
                        />
                        
                        <div v-else class="follow-items">
                            <div
                                v-for="follower in followers"
                                :key="follower.followId"
                                class="follow-item"
                            >
                                <div class="follow-member-info" @click="goToMemberProfile(follower.followerId)">
                                    <div class="follow-avatar">
                                        <img
                                            v-if="follower.followerProfileImage"
                                            :src="follower.followerProfileImage"
                                            alt="프로필"
                                        />
                                        <span v-else>{{ follower.followerNickname?.substring(0, 1) || '?' }}</span>
                                    </div>
                                    <span class="follow-nickname">{{ follower.followerNickname }}</span>
                                </div>
                                <Button
                                    label="프로필 보기"
                                    icon="pi pi-user"
                                    text
                                    @click="goToMemberProfile(follower.followerId)"
                                />
                            </div>
                        </div>
                        
                        <Paginator
                            v-if="followersTotalPages > 1"
                            v-model:first="followersFirst"
                            :rows="pageSize"
                            :totalRecords="followersTotalElements"
                            @page="onFollowersPageChange"
                        />
                    </div>
                </TabPanel>
                
                <TabPanel value="followings">
                    <!-- 팔로잉 목록 -->
                    <div class="follow-list-content">
                        <PageStateBlock
                            v-if="followingsLoading"
                            state="loading"
                            loading-message="팔로잉 목록을 불러오는 중..."
                        />
                        
                        <PageStateBlock
                            v-else-if="followings.length === 0"
                            state="empty"
                            empty-icon="pi pi-users"
                            empty-title="팔로잉이 없습니다"
                        />
                        
                        <div v-else class="follow-items">
                            <div
                                v-for="following in followings"
                                :key="following.followId"
                                class="follow-item"
                            >
                                <div class="follow-member-info" @click="goToMemberProfile(following.followingId)">
                                    <div class="follow-avatar">
                                        <img
                                            v-if="following.followingProfileImage"
                                            :src="following.followingProfileImage"
                                            alt="프로필"
                                        />
                                        <span v-else>{{ following.followingNickname?.substring(0, 1) || '?' }}</span>
                                    </div>
                                    <span class="follow-nickname">{{ following.followingNickname }}</span>
                                </div>
                                <div class="follow-actions">
                                    <Button
                                        label="프로필 보기"
                                        icon="pi pi-user"
                                        text
                                        @click="goToMemberProfile(following.followingId)"
                                    />
                                    <Button
                                        v-if="isOwnList"
                                        label="언팔로우"
                                        icon="pi pi-times"
                                        severity="danger"
                                        text
                                        @click="handleUnfollow(following.followingId)"
                                    />
                                </div>
                            </div>
                        </div>
                        
                        <Paginator
                            v-if="followingsTotalPages > 1"
                            v-model:first="followingsFirst"
                            :rows="pageSize"
                            :totalRecords="followingsTotalElements"
                            @page="onFollowingsPageChange"
                        />
                    </div>
                </TabPanel>
            </TabPanels>
        </Tabs>
    </Dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { useAppToast } from '@/utils/toast';
import { getFollowers, getFollowings, unfollowUser } from '@/api/followApi';
import Dialog from 'primevue/dialog';
import Tabs from 'primevue/tabs';
import TabList from 'primevue/tablist';
import Tab from 'primevue/tab';
import TabPanels from 'primevue/tabpanels';
import TabPanel from 'primevue/tabpanel';
import Button from 'primevue/button';
import Paginator from 'primevue/paginator';
import PageStateBlock from '@/components/common/PageStateBlock.vue';
import type { FollowerItem, FollowingItem } from '@/types/follow';
import type { PageState } from 'primevue/paginator';

interface Props {
    visible: boolean;
    memberId: number;
    type?: 'followers' | 'followings';
}

const props = withDefaults(defineProps<Props>(), {
    type: 'followers'
});

const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void;
}>();

const router = useRouter();
const authStore = useAuthStore();
const { showError } = useAppToast();

const activeTab = ref<'followers' | 'followings'>(props.type);
const pageSize = 20;

// 팔로워 상태
const followers = ref<FollowerItem[]>([]);
const followersLoading = ref(false);
const followersFirst = ref(0);
const followersTotalElements = ref(0);
const followersTotalPages = ref(0);

// 팔로잉 상태
const followings = ref<FollowingItem[]>([]);
const followingsLoading = ref(false);
const followingsFirst = ref(0);
const followingsTotalElements = ref(0);
const followingsTotalPages = ref(0);

const isOwnList = computed(() => authStore.memberInfo?.id === props.memberId);

const loadFollowers = async (page: number = 0) => {
    followersLoading.value = true;
    try {
        const response = await getFollowers(props.memberId, page, pageSize);
        followers.value = response.content as FollowerItem[];
        followersTotalElements.value = response.totalElements;
        followersTotalPages.value = response.totalPages;
    } catch (err) {
        console.error('팔로워 목록 로드 실패:', err);
        showError('팔로워 목록을 불러오는데 실패했습니다.');
    } finally {
        followersLoading.value = false;
    }
};

const loadFollowings = async (page: number = 0) => {
    followingsLoading.value = true;
    try {
        const response = await getFollowings(props.memberId, page, pageSize);
        followings.value = response.content as FollowingItem[];
        followingsTotalElements.value = response.totalElements;
        followingsTotalPages.value = response.totalPages;
    } catch (err) {
        console.error('팔로잉 목록 로드 실패:', err);
        showError('팔로잉 목록을 불러오는데 실패했습니다.');
    } finally {
        followingsLoading.value = false;
    }
};

const handleUnfollow = async (followingId: number) => {
    try {
        await unfollowUser(followingId);
        // 목록 새로고침
        await loadFollowings(Math.floor(followingsFirst.value / pageSize));
    } catch (err) {
        console.error('언팔로우 실패:', err);
        showError('언팔로우에 실패했습니다.');
    }
};

const goToMemberProfile = (memberId: number) => {
    emit('update:visible', false);
    router.push(`/member/${memberId}`);
};

const onFollowersPageChange = (event: PageState) => {
    followersFirst.value = event.first;
    loadFollowers(event.page);
};

const onFollowingsPageChange = (event: PageState) => {
    followingsFirst.value = event.first;
    loadFollowings(event.page);
};

// Dialog가 열릴 때 데이터 로드
watch(() => props.visible, (newValue) => {
    if (newValue) {
        activeTab.value = props.type;
        if (activeTab.value === 'followers') {
            loadFollowers();
        } else {
            loadFollowings();
        }
    }
});

// 탭 변경 시 데이터 로드
watch(activeTab, (newTab) => {
    if (newTab === 'followers' && followers.value.length === 0) {
        loadFollowers();
    } else if (newTab === 'followings' && followings.value.length === 0) {
        loadFollowings();
    }
});
</script>

<style scoped lang="scss">
.follow-list-content {
    min-height: 300px;
    
    .follow-items {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
        
        .follow-item {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0.75rem;
            border-radius: 8px;
            transition: background 0.2s;
            
            &:hover {
                background: var(--surface-100);
            }
            
            .follow-member-info {
                display: flex;
                align-items: center;
                gap: 1rem;
                cursor: pointer;
                flex: 1;
                
                .follow-avatar {
                    width: 40px;
                    height: 40px;
                    border-radius: 50%;
                    overflow: hidden;
                    background: var(--surface-200);
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-weight: 600;
                    color: var(--text-color-secondary);
                    
                    img {
                        width: 100%;
                        height: 100%;
                        object-fit: cover;
                    }
                }
                
                .follow-nickname {
                    font-weight: 500;
                    color: var(--text-color);
                }
            }
            
            .follow-actions {
                display: flex;
                gap: 0.5rem;
            }
        }
    }
}
</style>
