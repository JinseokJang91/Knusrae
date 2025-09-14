<template>
    <div class="card">
        <div class="flex justify-content-between align-items-center mb-4">
            <h1 class="text-3xl font-bold text-900">요리 후기</h1>
            <div class="flex gap-2">
                <Button icon="pi pi-refresh" label="새로고침" severity="secondary" @click="refreshReviews" />
                <Button icon="pi pi-plus" label="후기 작성" severity="success" @click="showWriteDialog = true" />
            </div>
        </div>

        <!-- 후기 작성 다이얼로그 -->
        <Dialog v-model:visible="showWriteDialog" header="요리 후기 작성" :style="{ width: '600px' }">
            <div class="flex flex-column gap-3">
                <div>
                    <label class="block text-900 font-medium mb-2">레시피 선택 *</label>
                    <Dropdown v-model="newReview.recipeId" :options="recipes" optionLabel="title" optionValue="id" placeholder="레시피를 선택하세요" class="w-full" />
                </div>
                <div>
                    <label class="block text-900 font-medium mb-2">평점 *</label>
                    <Rating v-model="newReview.rating" :cancel="false" />
                </div>
                <div>
                    <label class="block text-900 font-medium mb-2">후기 제목 *</label>
                    <InputText v-model="newReview.title" placeholder="후기 제목을 입력하세요" class="w-full" />
                </div>
                <div>
                    <label class="block text-900 font-medium mb-2">후기 내용 *</label>
                    <Textarea v-model="newReview.content" placeholder="요리 경험을 자세히 적어주세요" rows="5" class="w-full" />
                </div>
                <div>
                    <label class="block text-900 font-medium mb-2">사진 업로드</label>
                    <FileUpload mode="basic" accept="image/*" :maxFileSize="1000000" chooseLabel="사진 선택" class="w-full" />
                </div>
                <div>
                    <label class="block text-900 font-medium mb-2">태그</label>
                    <InputText v-model="newReview.tags" placeholder="태그를 쉼표로 구분하여 입력하세요" class="w-full" />
                </div>
            </div>
            <template #footer>
                <Button label="취소" severity="secondary" @click="cancelWrite" />
                <Button label="작성" @click="writeReview" />
            </template>
        </Dialog>

        <!-- 통계 카드 -->
        <div class="grid mb-4">
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ totalReviews }}</h3>
                                <p class="text-600 m-0">총 후기</p>
                            </div>
                            <i class="pi pi-comment text-4xl text-blue-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ averageRating }}</h3>
                                <p class="text-600 m-0">평균 평점</p>
                            </div>
                            <i class="pi pi-star text-4xl text-yellow-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ todayReviews }}</h3>
                                <p class="text-600 m-0">오늘의 후기</p>
                            </div>
                            <i class="pi pi-calendar text-4xl text-green-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ topReviewer }}</h3>
                                <p class="text-600 m-0">최다 후기 작성자</p>
                            </div>
                            <i class="pi pi-user text-4xl text-purple-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
        </div>

        <!-- 필터 및 정렬 -->
        <div class="flex flex-wrap gap-2 mb-4">
            <Button v-for="filter in filters" :key="filter.value" :label="filter.label" :severity="selectedFilter === filter.value ? 'primary' : 'secondary'" size="small" @click="filterReviews(filter.value)" />
            <Dropdown v-model="selectedSort" :options="sortOptions" optionLabel="label" optionValue="value" placeholder="정렬" class="w-auto" />
        </div>

        <!-- 후기 목록 -->
        <div class="grid">
            <div v-for="review in filteredReviews" :key="review.id" class="col-12">
                <Card class="review-card">
                    <template #content>
                        <div class="flex gap-3">
                            <!-- 사용자 아바타 -->
                            <Avatar :image="review.userAvatar" :label="review.userName.charAt(0)" size="large" shape="circle" />

                            <!-- 후기 내용 -->
                            <div class="flex-1">
                                <div class="flex justify-content-between align-items-start mb-2">
                                    <div>
                                        <h4 class="text-lg font-semibold text-900 m-0 mb-1">{{ review.title }}</h4>
                                        <div class="flex align-items-center gap-2 text-sm text-500">
                                            <span>{{ review.userName }}</span>
                                            <span>•</span>
                                            <span>{{ formatDate(review.createdAt) }}</span>
                                            <span>•</span>
                                            <span>{{ review.recipeTitle }}</span>
                                        </div>
                                    </div>
                                    <div class="flex align-items-center gap-2">
                                        <Rating v-model="review.rating" readonly :cancel="false" />
                                        <Button icon="pi pi-ellipsis-v" size="small" severity="secondary" text rounded />
                                    </div>
                                </div>

                                <p class="text-600 mb-3">{{ review.content }}</p>

                                <!-- 후기 이미지 -->
                                <div v-if="review.images && review.images.length > 0" class="flex gap-2 mb-3">
                                    <img v-for="(image, index) in review.images.slice(0, 3)" :key="index" :src="image" :alt="`후기 이미지 ${index + 1}`" class="review-image cursor-pointer" @click="viewImage(image)" />
                                    <div v-if="review.images.length > 3" class="more-images">+{{ review.images.length - 3 }}</div>
                                </div>

                                <!-- 태그 -->
                                <div v-if="review.tags && review.tags.length > 0" class="flex flex-wrap gap-1 mb-3">
                                    <Tag v-for="tag in review.tags" :key="tag" :value="tag" severity="secondary" />
                                </div>

                                <!-- 액션 버튼 -->
                                <div class="flex justify-content-between align-items-center">
                                    <div class="flex gap-3">
                                        <Button
                                            :icon="review.isLiked ? 'pi pi-heart-fill' : 'pi pi-heart'"
                                            :class="review.isLiked ? 'p-button-danger' : 'p-button-secondary'"
                                            size="small"
                                            text
                                            :label="review.likes.toString()"
                                            @click="toggleLike(review.id)"
                                        />
                                        <Button icon="pi pi-comment" size="small" severity="secondary" text :label="review.comments.toString()" />
                                        <Button icon="pi pi-share-alt" size="small" severity="secondary" text @click="shareReview(review)" />
                                    </div>
                                    <div class="flex gap-2">
                                        <Button icon="pi pi-bookmark" size="small" severity="secondary" text @click="bookmarkReview(review.id)" />
                                        <Button v-if="review.isMine" icon="pi pi-pencil" size="small" severity="secondary" text @click="editReview(review)" />
                                        <Button v-if="review.isMine" icon="pi pi-trash" size="small" severity="danger" text @click="deleteReview(review.id)" />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </template>
                </Card>
            </div>
        </div>

        <!-- 빈 상태 -->
        <div v-if="filteredReviews.length === 0" class="text-center py-8">
            <i class="pi pi-comment text-6xl text-300 mb-4"></i>
            <h3 class="text-2xl font-semibold text-600 mb-2">후기가 없습니다</h3>
            <p class="text-600 mb-4">첫 번째 후기를 작성해보세요!</p>
            <Button label="후기 작성" @click="showWriteDialog = true" />
        </div>

        <!-- 페이지네이션 -->
        <div v-if="filteredReviews.length > 0" class="flex justify-content-center mt-4">
            <Paginator v-model:first="first" :rows="rows" :totalRecords="totalFilteredReviews" @page="onPageChange" template="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink" />
        </div>

        <!-- 이미지 뷰어 다이얼로그 -->
        <Dialog v-model:visible="showImageViewer" header="후기 이미지" :style="{ width: '80vw' }">
            <div class="text-center">
                <img :src="selectedImage" alt="후기 이미지" class="w-full max-h-70vh object-contain" />
            </div>
        </Dialog>
    </div>
</template>

<script setup>
import Avatar from 'primevue/avatar';
import Button from 'primevue/button';
import Card from 'primevue/card';
import Dialog from 'primevue/dialog';
import Dropdown from 'primevue/dropdown';
import FileUpload from 'primevue/fileupload';
import InputText from 'primevue/inputtext';
import Paginator from 'primevue/paginator';
import Rating from 'primevue/rating';
import Tag from 'primevue/tag';
import Textarea from 'primevue/textarea';
import { useToast } from 'primevue/usetoast';
import { computed, onMounted, ref } from 'vue';

const toast = useToast();

// 반응형 데이터
const reviews = ref([]);
const selectedFilter = ref('all');
const selectedSort = ref('latest');
const showWriteDialog = ref(false);
const showImageViewer = ref(false);
const selectedImage = ref('');
const first = ref(0);
const rows = ref(10);

const newReview = ref({
    recipeId: null,
    rating: 0,
    title: '',
    content: '',
    tags: ''
});

const recipes = ref([
    { id: 1, title: '김치찌개' },
    { id: 2, title: '파스타' },
    { id: 3, title: '초밥' },
    { id: 4, title: '짜장면' },
    { id: 5, title: '치즈케이크' }
]);

const filters = ref([
    { label: '전체', value: 'all' },
    { label: '5점', value: '5' },
    { label: '4점', value: '4' },
    { label: '3점', value: '3' },
    { label: '2점', value: '2' },
    { label: '1점', value: '1' },
    { label: '내 후기', value: 'mine' }
]);

const sortOptions = ref([
    { label: '최신순', value: 'latest' },
    { label: '인기순', value: 'popular' },
    { label: '평점순', value: 'rating' },
    { label: '댓글순', value: 'comments' }
]);

// 계산된 속성
const totalReviews = computed(() => reviews.value.length);

const averageRating = computed(() => {
    if (reviews.value.length === 0) return 0;
    const sum = reviews.value.reduce((acc, review) => acc + review.rating, 0);
    return (sum / reviews.value.length).toFixed(1);
});

const todayReviews = computed(() => {
    const today = new Date().toDateString();
    return reviews.value.filter((review) => new Date(review.createdAt).toDateString() === today).length;
});

const topReviewer = computed(() => {
    const userCounts = {};
    reviews.value.forEach((review) => {
        userCounts[review.userName] = (userCounts[review.userName] || 0) + 1;
    });
    const topUser = Object.keys(userCounts).reduce((a, b) => (userCounts[a] > userCounts[b] ? a : b), '');
    return topUser || '없음';
});

const filteredReviews = computed(() => {
    let filtered = reviews.value;

    // 필터 적용
    if (selectedFilter.value !== 'all') {
        if (selectedFilter.value === 'mine') {
            filtered = filtered.filter((review) => review.isMine);
        } else {
            filtered = filtered.filter((review) => review.rating === parseInt(selectedFilter.value));
        }
    }

    // 정렬 적용
    switch (selectedSort.value) {
        case 'popular':
            filtered = filtered.sort((a, b) => b.likes - a.likes);
            break;
        case 'rating':
            filtered = filtered.sort((a, b) => b.rating - a.rating);
            break;
        case 'comments':
            filtered = filtered.sort((a, b) => b.comments - a.comments);
            break;
        case 'latest':
        default:
            filtered = filtered.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
            break;
    }

    return filtered.slice(first.value, first.value + rows.value);
});

const totalFilteredReviews = computed(() => {
    let filtered = reviews.value;
    if (selectedFilter.value !== 'all') {
        if (selectedFilter.value === 'mine') {
            filtered = filtered.filter((review) => review.isMine);
        } else {
            filtered = filtered.filter((review) => review.rating === parseInt(selectedFilter.value));
        }
    }
    return filtered.length;
});

// 메서드
const loadReviews = () => {
    reviews.value = [
        {
            id: 1,
            title: '김치찌개 완벽하게 성공!',
            content: '처음 만들어본 김치찌개인데 정말 맛있게 나왔어요. 김치가 잘 익어서 시원하고 매콤한 맛이 일품이었습니다. 다음에는 돼지고기를 더 넣어서 만들어보고 싶어요.',
            rating: 5,
            userName: '김요리',
            userAvatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=50',
            recipeTitle: '김치찌개',
            createdAt: '2024-01-15T10:30:00Z',
            likes: 24,
            comments: 8,
            isLiked: false,
            isMine: false,
            tags: ['맛있음', '성공', '추천'],
            images: ['https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=200', 'https://images.unsplash.com/photo-1529042410759-befb1204b468?w=200']
        },
        {
            id: 2,
            title: '파스타가 좀 아쉬웠어요',
            content: '크림소스가 너무 진해서 좀 아쉬웠습니다. 다음에는 우유를 조금 더 넣어서 만들어보겠어요. 그래도 기본적인 맛은 있었습니다.',
            rating: 3,
            userName: '이맛집',
            userAvatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=50',
            recipeTitle: '파스타',
            createdAt: '2024-01-14T15:20:00Z',
            likes: 12,
            comments: 5,
            isLiked: true,
            isMine: true,
            tags: ['아쉬움', '개선필요'],
            images: ['https://images.unsplash.com/photo-1621996346565-e3dbc353d2e5?w=200']
        },
        {
            id: 3,
            title: '초밥 만들기 도전!',
            content: '생선이 너무 신선해서 초밥이 정말 맛있게 나왔어요. 밥 양념도 딱 맞게 해서 완벽했습니다. 다음에는 더 다양한 생선으로 도전해보겠어요.',
            rating: 5,
            userName: '박요리사',
            userAvatar: 'https://images.unsplash.com/photo-1494790108755-2616b612b786?w=50',
            recipeTitle: '초밥',
            createdAt: '2024-01-13T20:15:00Z',
            likes: 35,
            comments: 12,
            isLiked: false,
            isMine: false,
            tags: ['성공', '신선함', '완벽'],
            images: ['https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?w=200', 'https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=200', 'https://images.unsplash.com/photo-1553909489-cd47e0ef937f?w=200']
        },
        {
            id: 4,
            title: '치즈케이크 첫 도전',
            content: '디저트 만들기는 처음이었는데 생각보다 어려웠어요. 하지만 결과적으로는 괜찮게 나왔습니다. 다음에는 더 부드럽게 만들어보고 싶어요.',
            rating: 4,
            userName: '최맛있',
            userAvatar: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=50',
            recipeTitle: '치즈케이크',
            createdAt: '2024-01-12T14:45:00Z',
            likes: 18,
            comments: 6,
            isLiked: false,
            isMine: false,
            tags: ['첫도전', '괜찮음', '개선필요'],
            images: ['https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=200']
        }
    ];
};

const refreshReviews = () => {
    loadReviews();
    toast.add({ severity: 'success', summary: '새로고침', detail: '후기 목록이 업데이트되었습니다.', life: 3000 });
};

const filterReviews = (filter) => {
    selectedFilter.value = filter;
    first.value = 0;
};

const writeReview = () => {
    if (!newReview.value.recipeId || !newReview.value.rating || !newReview.value.title || !newReview.value.content) {
        toast.add({ severity: 'error', summary: '입력 오류', detail: '필수 항목을 모두 입력해주세요.', life: 3000 });
        return;
    }

    const newId = Math.max(...reviews.value.map((r) => r.id)) + 1;
    const recipe = recipes.value.find((r) => r.id === newReview.value.recipeId);

    const review = {
        id: newId,
        title: newReview.value.title,
        content: newReview.value.content,
        rating: newReview.value.rating,
        userName: '나',
        userAvatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=50',
        recipeTitle: recipe.title,
        createdAt: new Date().toISOString(),
        likes: 0,
        comments: 0,
        isLiked: false,
        isMine: true,
        tags: newReview.value.tags ? newReview.value.tags.split(',').map((tag) => tag.trim()) : [],
        images: []
    };

    reviews.value.unshift(review);
    cancelWrite();
    toast.add({ severity: 'success', summary: '작성 완료', detail: '후기가 작성되었습니다.', life: 3000 });
};

const cancelWrite = () => {
    newReview.value = {
        recipeId: null,
        rating: 0,
        title: '',
        content: '',
        tags: ''
    };
    showWriteDialog.value = false;
};

const toggleLike = (reviewId) => {
    const review = reviews.value.find((r) => r.id === reviewId);
    if (review) {
        review.isLiked = !review.isLiked;
        review.likes += review.isLiked ? 1 : -1;
        toast.add({
            severity: 'success',
            summary: review.isLiked ? '좋아요' : '좋아요 취소',
            detail: review.isLiked ? '후기에 좋아요를 눌렀습니다.' : '좋아요를 취소했습니다.',
            life: 3000
        });
    }
};

const shareReview = (review) => {
    if (navigator.share) {
        navigator.share({
            title: review.title,
            text: review.content,
            url: window.location.origin + `/community/reviews/${review.id}`
        });
    } else {
        navigator.clipboard.writeText(window.location.origin + `/community/reviews/${review.id}`);
        toast.add({ severity: 'info', summary: '링크 복사', detail: '후기 링크가 클립보드에 복사되었습니다.', life: 3000 });
    }
};

const bookmarkReview = (reviewId) => {
    toast.add({ severity: 'info', summary: '북마크', detail: '후기가 북마크되었습니다.', life: 3000 });
};

const editReview = (review) => {
    toast.add({ severity: 'info', summary: '편집', detail: '편집 기능은 준비 중입니다.', life: 3000 });
};

const deleteReview = (reviewId) => {
    const index = reviews.value.findIndex((review) => review.id === reviewId);
    if (index > -1) {
        reviews.value.splice(index, 1);
        toast.add({ severity: 'success', summary: '삭제 완료', detail: '후기가 삭제되었습니다.', life: 3000 });
    }
};

const viewImage = (image) => {
    selectedImage.value = image;
    showImageViewer.value = true;
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
    loadReviews();
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

.review-card {
    transition:
        transform 0.2s,
        box-shadow 0.2s;
}

.review-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.review-image {
    width: 80px;
    height: 80px;
    object-fit: cover;
    border-radius: 8px;
    transition: transform 0.2s;
}

.review-image:hover {
    transform: scale(1.05);
}

.more-images {
    width: 80px;
    height: 80px;
    background-color: var(--surface-200);
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
    color: var(--text-color-secondary);
}
</style>
