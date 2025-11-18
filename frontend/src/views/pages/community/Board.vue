<template>
    <div class="card">
        <div class="flex justify-between items-center mb-4">
            <h1 class="text-3xl font-bold text-gray-900">자유 게시판</h1>
            <div class="flex gap-2">
                <Button icon="pi pi-refresh" label="새로고침" severity="secondary" @click="refreshBoard" />
                <Button icon="pi pi-plus" label="글 작성" severity="success" @click="showWriteDialog = true" />
            </div>
        </div>

        <!-- 글 작성 다이얼로그 -->
        <Dialog v-model:visible="showWriteDialog" header="게시글 작성" :style="{ width: '600px' }">
            <div class="flex flex-col gap-3">
                <div>
                    <label class="block text-gray-900 font-medium mb-2">제목 *</label>
                    <InputText v-model="newPost.title" placeholder="제목을 입력하세요" class="w-full" />
                </div>
                <div>
                    <label class="block text-gray-900 font-medium mb-2">카테고리 *</label>
                    <Dropdown v-model="newPost.category" :options="categories" optionLabel="name" optionValue="value" placeholder="카테고리 선택" class="w-full" />
                </div>
                <div>
                    <label class="block text-gray-900 font-medium mb-2">내용 *</label>
                    <Textarea v-model="newPost.content" placeholder="내용을 입력하세요" rows="8" class="w-full" />
                </div>
                <div>
                    <label class="block text-gray-900 font-medium mb-2">태그</label>
                    <InputText v-model="newPost.tags" placeholder="태그를 쉼표로 구분하여 입력하세요" class="w-full" />
                </div>
            </div>
            <template #footer>
                <Button label="취소" severity="secondary" @click="cancelWrite" />
                <Button label="작성" @click="writePost" />
            </template>
        </Dialog>

        <!-- 통계 카드 -->
        <div class="grid mb-4">
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-gray-900 m-0">{{ totalPosts }}</h3>
                                <p class="text-gray-600 m-0">총 게시글</p>
                            </div>
                            <i class="pi pi-file text-4xl text-blue-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-gray-900 m-0">{{ todayPosts }}</h3>
                                <p class="text-gray-600 m-0">오늘의 글</p>
                            </div>
                            <i class="pi pi-calendar text-4xl text-green-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-gray-900 m-0">{{ totalComments }}</h3>
                                <p class="text-gray-600 m-0">총 댓글</p>
                            </div>
                            <i class="pi pi-comment text-4xl text-orange-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-gray-900 m-0">{{ activeMembers }}</h3>
                                <p class="text-gray-600 m-0">활성 사용자</p>
                            </div>
                            <i class="pi pi-users text-4xl text-purple-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
        </div>

        <!-- 검색 및 필터 -->
        <div class="flex flex-wrap gap-2 mb-4">
            <div class="flex-1">
                <InputText v-model="searchQuery" placeholder="제목, 내용, 작성자로 검색..." class="w-full" @keyup.enter="performSearch" />
            </div>
            <Button icon="pi pi-search" label="검색" @click="performSearch" />
            <Dropdown v-model="selectedCategory" :options="categoryOptions" optionLabel="name" optionValue="value" placeholder="카테고리" class="w-auto" />
            <Dropdown v-model="selectedSort" :options="sortOptions" optionLabel="label" optionValue="value" placeholder="정렬" class="w-auto" />
        </div>

        <!-- 게시글 목록 -->
        <div class="grid">
            <div v-for="post in filteredPosts" :key="post.id" class="col-12">
                <Card class="post-card">
                    <template #content>
                        <div class="flex gap-3">
                            <!-- 사용자 아바타 -->
                            <Avatar :image="post.memberAvatar" :label="post.memberName.charAt(0)" size="large" shape="circle" />

                            <!-- 게시글 내용 -->
                            <div class="flex-1">
                                <div class="flex justify-between items-start mb-2">
                                    <div>
                                        <h4 class="text-lg font-semibold text-gray-900 m-0 mb-1 cursor-pointer" @click="viewPost(post.id)">
                                            {{ post.title }}
                                        </h4>
                                        <div class="flex items-center gap-2 text-sm text-gray-500">
                                            <Tag :value="post.category" :severity="getCategorySeverity(post.category)" />
                                            <span>{{ post.memberName }}</span>
                                            <span>•</span>
                                            <span>{{ formatDate(post.createdAt) }}</span>
                                            <span v-if="post.updatedAt !== post.createdAt" class="text-orange-500">(수정됨)</span>
                                        </div>
                                    </div>
                                    <div class="flex items-center gap-2">
                                        <div class="flex items-center gap-1 text-sm text-gray-500">
                                            <i class="pi pi-eye"></i>
                                            <span>{{ post.views }}</span>
                                        </div>
                                        <Button icon="pi pi-ellipsis-v" size="small" severity="secondary" text rounded />
                                    </div>
                                </div>

                                <p class="text-gray-600 mb-3">{{ post.content.substring(0, 150) }}{{ post.content.length > 150 ? '...' : '' }}</p>

                                <!-- 태그 -->
                                <div v-if="post.tags && post.tags.length > 0" class="flex flex-wrap gap-1 mb-3">
                                    <Tag v-for="tag in post.tags" :key="tag" :value="tag" severity="secondary" />
                                </div>

                                <!-- 액션 버튼 -->
                                <div class="flex justify-between items-center">
                                    <div class="flex gap-3">
                                        <Button
                                            :icon="post.isLiked ? 'pi pi-heart-fill' : 'pi pi-heart'"
                                            :class="post.isLiked ? 'p-button-danger' : 'p-button-secondary'"
                                            size="small"
                                            text
                                            :label="post.likes.toString()"
                                            @click="toggleLike(post.id)"
                                        />
                                        <Button icon="pi pi-comment" size="small" severity="secondary" text :label="post.comments.toString()" />
                                        <Button icon="pi pi-share-alt" size="small" severity="secondary" text @click="sharePost(post)" />
                                    </div>
                                    <div class="flex gap-2">
                                        <Button icon="pi pi-bookmark" size="small" severity="secondary" text @click="bookmarkPost(post.id)" />
                                        <Button v-if="post.isMine" icon="pi pi-pencil" size="small" severity="secondary" text @click="editPost(post)" />
                                        <Button v-if="post.isMine" icon="pi pi-trash" size="small" severity="danger" text @click="deletePost(post.id)" />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </template>
                </Card>
            </div>
        </div>

        <!-- 빈 상태 -->
        <div v-if="filteredPosts.length === 0" class="text-center py-8">
            <i class="pi pi-file text-6xl text-300 mb-4"></i>
            <h3 class="text-2xl font-semibold text-gray-600 mb-2">게시글이 없습니다</h3>
            <p class="text-gray-600 mb-4">첫 번째 게시글을 작성해보세요!</p>
            <Button label="글 작성" @click="showWriteDialog = true" />
        </div>

        <!-- 페이지네이션 -->
        <div v-if="filteredPosts.length > 0" class="flex justify-center mt-4">
            <Paginator v-model:first="first" :rows="rows" :totalRecords="totalFilteredPosts" @page="onPageChange" template="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink" />
        </div>
    </div>
</template>

<script setup>
import Avatar from 'primevue/avatar';
import Button from 'primevue/button';
import Card from 'primevue/card';
import Dialog from 'primevue/dialog';
import Dropdown from 'primevue/dropdown';
import InputText from 'primevue/inputtext';
import Paginator from 'primevue/paginator';
import Tag from 'primevue/tag';
import Textarea from 'primevue/textarea';
import { useToast } from 'primevue/usetoast';
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const toast = useToast();

// 반응형 데이터
const posts = ref([]);
const searchQuery = ref('');
const selectedCategory = ref(null);
const selectedSort = ref('latest');
const showWriteDialog = ref(false);
const first = ref(0);
const rows = ref(10);

const newPost = ref({
    title: '',
    category: '',
    content: '',
    tags: ''
});

const categories = ref([
    { name: '일반', value: 'general' },
    { name: '질문', value: 'question' },
    { name: '정보', value: 'info' },
    { name: '후기', value: 'review' },
    { name: '자랑', value: 'showoff' },
    { name: '기타', value: 'etc' }
]);

const categoryOptions = ref([{ name: '전체', value: null }, ...categories.value]);

const sortOptions = ref([
    { label: '최신순', value: 'latest' },
    { label: '인기순', value: 'popular' },
    { label: '댓글순', value: 'comments' },
    { label: '조회순', value: 'views' }
]);

// 계산된 속성
const totalPosts = computed(() => posts.value.length);

const todayPosts = computed(() => {
    const today = new Date().toDateString();
    return posts.value.filter((post) => new Date(post.createdAt).toDateString() === today).length;
});

const totalComments = computed(() => {
    return posts.value.reduce((sum, post) => sum + post.comments, 0);
});

const activeMembers = computed(() => {
    const uniqueMembers = new Set(posts.value.map((post) => post.memberName));
    return uniqueMembers.size;
});

const filteredPosts = computed(() => {
    let filtered = posts.value;

    // 검색 필터
    if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase();
        filtered = filtered.filter((post) => post.title.toLowerCase().includes(query) || post.content.toLowerCase().includes(query) || post.memberName.toLowerCase().includes(query));
    }

    // 카테고리 필터
    if (selectedCategory.value) {
        filtered = filtered.filter((post) => post.category === selectedCategory.value);
    }

    // 정렬
    switch (selectedSort.value) {
        case 'popular':
            filtered = filtered.sort((a, b) => b.likes - a.likes);
            break;
        case 'comments':
            filtered = filtered.sort((a, b) => b.comments - a.comments);
            break;
        case 'views':
            filtered = filtered.sort((a, b) => b.views - a.views);
            break;
        case 'latest':
        default:
            filtered = filtered.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
            break;
    }

    return filtered.slice(first.value, first.value + rows.value);
});

const totalFilteredPosts = computed(() => {
    let filtered = posts.value;

    if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase();
        filtered = filtered.filter((post) => post.title.toLowerCase().includes(query) || post.content.toLowerCase().includes(query) || post.memberName.toLowerCase().includes(query));
    }

    if (selectedCategory.value) {
        filtered = filtered.filter((post) => post.category === selectedCategory.value);
    }

    return filtered.length;
});

// 메서드
const loadPosts = () => {
    posts.value = [
        {
            id: 1,
            title: '요리 초보를 위한 팁 모음',
            content: '요리를 처음 시작하는 분들을 위한 기본적인 팁들을 정리해봤습니다. 칼 사용법부터 기본 양념까지 차근차근 설명드릴게요. 특히 안전사고 예방에 대한 내용도 포함되어 있으니 꼭 읽어보세요.',
            category: 'info',
            memberName: '김요리',
            memberAvatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=50',
            createdAt: '2024-01-15T10:30:00Z',
            updatedAt: '2024-01-15T10:30:00Z',
            views: 156,
            likes: 23,
            comments: 8,
            isLiked: false,
            isMine: false,
            tags: ['초보', '팁', '안전']
        },
        {
            id: 2,
            title: '김치찌개가 계속 시어져요 ㅠㅠ',
            content: '김치찌개를 만들 때마다 김치가 너무 시어져서 맛이 없어요. 김치를 어떻게 보관해야 할까요? 그리고 김치찌개를 만들 때 김치를 어떻게 처리해야 맛있게 나올까요? 도움 부탁드려요!',
            category: 'question',
            memberName: '이맛집',
            memberAvatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=50',
            createdAt: '2024-01-14T15:20:00Z',
            updatedAt: '2024-01-14T15:20:00Z',
            views: 89,
            likes: 5,
            comments: 12,
            isLiked: true,
            isMine: true,
            tags: ['김치찌개', '질문', '도움']
        },
        {
            id: 3,
            title: '오늘 만든 파스타 자랑해요!',
            content: '처음으로 크림파스타를 만들어봤는데 정말 맛있게 나왔어요! 특히 크림소스가 진하지도 않고 묽지도 않게 딱 좋게 나왔습니다. 다음에는 다른 소스로도 도전해보고 싶어요.',
            category: 'showoff',
            memberName: '박요리사',
            memberAvatar: 'https://images.unsplash.com/photo-1494790108755-2616b612b786?w=50',
            createdAt: '2024-01-13T20:15:00Z',
            updatedAt: '2024-01-13T20:15:00Z',
            views: 234,
            likes: 45,
            comments: 15,
            isLiked: false,
            isMine: false,
            tags: ['파스타', '자랑', '성공']
        },
        {
            id: 4,
            title: '새로운 레시피 사이트 발견!',
            content: '요즘 새로운 레시피 사이트를 발견했는데 정말 좋더라고요. 특히 영상으로 설명이 잘 되어있어서 따라하기 쉬웠습니다. 여러분도 한번 확인해보세요!',
            category: 'info',
            memberName: '최맛있',
            memberAvatar: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=50',
            createdAt: '2024-01-12T14:45:00Z',
            updatedAt: '2024-01-12T14:45:00Z',
            views: 167,
            likes: 18,
            comments: 6,
            isLiked: false,
            isMine: false,
            tags: ['사이트', '추천', '정보']
        },
        {
            id: 5,
            title: '요리 도구 추천 부탁드려요',
            content: '요리를 시작하려고 하는데 기본적으로 필요한 도구들이 뭔지 모르겠어요. 칼, 도마, 팬 정도는 알고 있는데 그 외에 꼭 필요한 도구들이 있을까요? 예산은 10만원 정도로 생각하고 있어요.',
            category: 'question',
            memberName: '정요리왕',
            memberAvatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=50',
            createdAt: '2024-01-11T09:30:00Z',
            updatedAt: '2024-01-11T09:30:00Z',
            views: 98,
            likes: 7,
            comments: 9,
            isLiked: false,
            isMine: false,
            tags: ['도구', '추천', '초보']
        }
    ];
};

const refreshBoard = () => {
    loadPosts();
    toast.add({ severity: 'success', summary: '새로고침', detail: '게시판이 업데이트되었습니다.', life: 3000 });
};

const performSearch = () => {
    first.value = 0;
    toast.add({ severity: 'info', summary: '검색', detail: `"${searchQuery.value}" 검색 결과를 표시합니다.`, life: 3000 });
};

const writePost = () => {
    if (!newPost.value.title || !newPost.value.category || !newPost.value.content) {
        toast.add({ severity: 'error', summary: '입력 오류', detail: '필수 항목을 모두 입력해주세요.', life: 3000 });
        return;
    }

    const newId = Math.max(...posts.value.map((p) => p.id)) + 1;
    const post = {
        id: newId,
        title: newPost.value.title,
        content: newPost.value.content,
        category: newPost.value.category,
        memberName: '나',
        memberAvatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=50',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        views: 0,
        likes: 0,
        comments: 0,
        isLiked: false,
        isMine: true,
        tags: newPost.value.tags ? newPost.value.tags.split(',').map((tag) => tag.trim()) : []
    };

    posts.value.unshift(post);
    cancelWrite();
    toast.add({ severity: 'success', summary: '작성 완료', detail: '게시글이 작성되었습니다.', life: 3000 });
};

const cancelWrite = () => {
    newPost.value = {
        title: '',
        category: '',
        content: '',
        tags: ''
    };
    showWriteDialog.value = false;
};

const viewPost = (postId) => {
    router.push(`/community/board/${postId}`);
};

const toggleLike = (postId) => {
    const post = posts.value.find((p) => p.id === postId);
    if (post) {
        post.isLiked = !post.isLiked;
        post.likes += post.isLiked ? 1 : -1;
        toast.add({
            severity: 'success',
            summary: post.isLiked ? '좋아요' : '좋아요 취소',
            detail: post.isLiked ? '게시글에 좋아요를 눌렀습니다.' : '좋아요를 취소했습니다.',
            life: 3000
        });
    }
};

const sharePost = (post) => {
    if (navigator.share) {
        navigator.share({
            title: post.title,
            text: post.content,
            url: window.location.origin + `/community/board/${post.id}`
        });
    } else {
        navigator.clipboard.writeText(window.location.origin + `/community/board/${post.id}`);
        toast.add({ severity: 'info', summary: '링크 복사', detail: '게시글 링크가 클립보드에 복사되었습니다.', life: 3000 });
    }
};

const bookmarkPost = (postId) => {
    toast.add({ severity: 'info', summary: '북마크', detail: '게시글이 북마크되었습니다.', life: 3000 });
};

const editPost = (post) => {
    toast.add({ severity: 'info', summary: '편집', detail: '편집 기능은 준비 중입니다.', life: 3000 });
};

const deletePost = (postId) => {
    const index = posts.value.findIndex((post) => post.id === postId);
    if (index > -1) {
        posts.value.splice(index, 1);
        toast.add({ severity: 'success', summary: '삭제 완료', detail: '게시글이 삭제되었습니다.', life: 3000 });
    }
};

const getCategorySeverity = (category) => {
    const severityMap = {
        general: 'info',
        question: 'warning',
        info: 'success',
        review: 'help',
        showoff: 'contrast',
        etc: 'secondary'
    };
    return severityMap[category] || 'primary';
};

const formatDate = (dateString) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffTime = Math.abs(now - date);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays === 1) return '어제';
    if (diffDays < 7) return `${diffDays}일 전`;
    if (diffDays < 30) return `${Math.ceil(diffDays / 7)}주 전`;
    return date.toLocaleDateString('ko-KR');
};

const onPageChange = (event) => {
    first.value = event.first;
    rows.value = event.rows;
};

// 생명주기
onMounted(() => {
    loadPosts();
});
</script>

<style scoped>
.stat-card {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
}

.stat-card .text-900 {
    color: white !important;
}

.stat-card .text-600 {
    color: rgba(255, 255, 255, 0.8) !important;
}

.post-card {
    transition:
        transform 0.2s,
        box-shadow 0.2s;
}

.post-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}
</style>
