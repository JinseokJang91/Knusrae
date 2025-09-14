<template>
    <div class="card">
        <div class="flex justify-content-between align-items-center mb-4">
            <h1 class="text-3xl font-bold text-900">질문과 답변</h1>
            <div class="flex gap-2">
                <Button icon="pi pi-refresh" label="새로고침" severity="secondary" @click="refreshQnA" />
                <Button icon="pi pi-plus" label="질문하기" severity="success" @click="showAskDialog = true" />
            </div>
        </div>

        <!-- 질문하기 다이얼로그 -->
        <Dialog v-model:visible="showAskDialog" header="질문하기" :style="{ width: '600px' }">
            <div class="flex flex-column gap-3">
                <div>
                    <label class="block text-900 font-medium mb-2">제목 *</label>
                    <InputText v-model="newQuestion.title" placeholder="질문 제목을 입력하세요" class="w-full" />
                </div>
                <div>
                    <label class="block text-900 font-medium mb-2">카테고리 *</label>
                    <Dropdown v-model="newQuestion.category" :options="categories" optionLabel="name" optionValue="value" placeholder="카테고리 선택" class="w-full" />
                </div>
                <div>
                    <label class="block text-900 font-medium mb-2">질문 내용 *</label>
                    <Textarea v-model="newQuestion.content" placeholder="질문 내용을 자세히 입력하세요" rows="6" class="w-full" />
                </div>
                <div>
                    <label class="block text-900 font-medium mb-2">태그</label>
                    <InputText v-model="newQuestion.tags" placeholder="태그를 쉼표로 구분하여 입력하세요" class="w-full" />
                </div>
            </div>
            <template #footer>
                <Button label="취소" severity="secondary" @click="cancelAsk" />
                <Button label="질문하기" @click="askQuestion" />
            </template>
        </Dialog>

        <!-- 통계 카드 -->
        <div class="grid mb-4">
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ totalQuestions }}</h3>
                                <p class="text-600 m-0">총 질문</p>
                            </div>
                            <i class="pi pi-question-circle text-4xl text-blue-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ answeredQuestions }}</h3>
                                <p class="text-600 m-0">답변 완료</p>
                            </div>
                            <i class="pi pi-check-circle text-4xl text-green-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ pendingQuestions }}</h3>
                                <p class="text-600 m-0">답변 대기</p>
                            </div>
                            <i class="pi pi-clock text-4xl text-orange-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ expertCount }}</h3>
                                <p class="text-600 m-0">전문가</p>
                            </div>
                            <i class="pi pi-user text-4xl text-purple-500"></i>
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
            <Dropdown v-model="selectedStatus" :options="statusOptions" optionLabel="label" optionValue="value" placeholder="상태" class="w-auto" />
            <Dropdown v-model="selectedSort" :options="sortOptions" optionLabel="label" optionValue="value" placeholder="정렬" class="w-auto" />
        </div>

        <!-- 질문 목록 -->
        <div class="grid">
            <div v-for="question in filteredQuestions" :key="question.id" class="col-12">
                <Card class="question-card">
                    <template #content>
                        <div class="flex gap-3">
                            <!-- 사용자 아바타 -->
                            <Avatar :image="question.userAvatar" :label="question.userName.charAt(0)" size="large" shape="circle" />

                            <!-- 질문 내용 -->
                            <div class="flex-1">
                                <div class="flex justify-content-between align-items-start mb-2">
                                    <div>
                                        <h4 class="text-lg font-semibold text-900 m-0 mb-1 cursor-pointer" @click="viewQuestion(question.id)">
                                            {{ question.title }}
                                        </h4>
                                        <div class="flex align-items-center gap-2 text-sm text-500">
                                            <Tag :value="question.category" :severity="getCategorySeverity(question.category)" />
                                            <Tag :value="question.status" :severity="getStatusSeverity(question.status)" />
                                            <span>{{ question.userName }}</span>
                                            <span>•</span>
                                            <span>{{ formatDate(question.createdAt) }}</span>
                                        </div>
                                    </div>
                                    <div class="flex align-items-center gap-2">
                                        <div class="flex align-items-center gap-1 text-sm text-500">
                                            <i class="pi pi-eye"></i>
                                            <span>{{ question.views }}</span>
                                        </div>
                                        <Button icon="pi pi-ellipsis-v" size="small" severity="secondary" text rounded />
                                    </div>
                                </div>

                                <p class="text-600 mb-3">{{ question.content.substring(0, 200) }}{{ question.content.length > 200 ? '...' : '' }}</p>

                                <!-- 태그 -->
                                <div v-if="question.tags && question.tags.length > 0" class="flex flex-wrap gap-1 mb-3">
                                    <Tag v-for="tag in question.tags" :key="tag" :value="tag" severity="secondary" />
                                </div>

                                <!-- 답변 미리보기 -->
                                <div v-if="question.answers && question.answers.length > 0" class="mb-3">
                                    <div class="answer-preview">
                                        <div class="flex align-items-center gap-2 mb-2">
                                            <i class="pi pi-check-circle text-green-500"></i>
                                            <span class="text-sm font-medium text-900">답변 {{ question.answers.length }}개</span>
                                        </div>
                                        <p class="text-600 text-sm m-0">{{ question.answers[0].content.substring(0, 100) }}{{ question.answers[0].content.length > 100 ? '...' : '' }}</p>
                                    </div>
                                </div>

                                <!-- 액션 버튼 -->
                                <div class="flex justify-content-between align-items-center">
                                    <div class="flex gap-3">
                                        <Button
                                            :icon="question.isLiked ? 'pi pi-heart-fill' : 'pi pi-heart'"
                                            :class="question.isLiked ? 'p-button-danger' : 'p-button-secondary'"
                                            size="small"
                                            text
                                            :label="question.likes.toString()"
                                            @click="toggleLike(question.id)"
                                        />
                                        <Button icon="pi pi-comment" size="small" severity="secondary" text :label="question.answers.length.toString()" />
                                        <Button icon="pi pi-share-alt" size="small" severity="secondary" text @click="shareQuestion(question)" />
                                    </div>
                                    <div class="flex gap-2">
                                        <Button icon="pi pi-bookmark" size="small" severity="secondary" text @click="bookmarkQuestion(question.id)" />
                                        <Button v-if="question.isMine" icon="pi pi-pencil" size="small" severity="secondary" text @click="editQuestion(question)" />
                                        <Button v-if="question.isMine" icon="pi pi-trash" size="small" severity="danger" text @click="deleteQuestion(question.id)" />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </template>
                </Card>
            </div>
        </div>

        <!-- 빈 상태 -->
        <div v-if="filteredQuestions.length === 0" class="text-center py-8">
            <i class="pi pi-question-circle text-6xl text-300 mb-4"></i>
            <h3 class="text-2xl font-semibold text-600 mb-2">질문이 없습니다</h3>
            <p class="text-600 mb-4">궁금한 것이 있으시면 언제든 질문해주세요!</p>
            <Button label="질문하기" @click="showAskDialog = true" />
        </div>

        <!-- 페이지네이션 -->
        <div v-if="filteredQuestions.length > 0" class="flex justify-content-center mt-4">
            <Paginator v-model:first="first" :rows="rows" :totalRecords="totalFilteredQuestions" @page="onPageChange" template="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink" />
        </div>

        <!-- 질문 상세 다이얼로그 -->
        <Dialog v-model:visible="showQuestionDetail" :header="selectedQuestion?.title" :style="{ width: '800px' }">
            <div v-if="selectedQuestion" class="flex flex-column gap-4">
                <!-- 질문 내용 -->
                <div class="question-detail">
                    <div class="flex align-items-center gap-3 mb-3">
                        <Avatar :image="selectedQuestion.userAvatar" :label="selectedQuestion.userName.charAt(0)" size="large" shape="circle" />
                        <div>
                            <div class="font-semibold">{{ selectedQuestion.userName }}</div>
                            <div class="text-sm text-500">{{ formatDate(selectedQuestion.createdAt) }}</div>
                        </div>
                        <div class="flex gap-2 ml-auto">
                            <Tag :value="selectedQuestion.category" :severity="getCategorySeverity(selectedQuestion.category)" />
                            <Tag :value="selectedQuestion.status" :severity="getStatusSeverity(selectedQuestion.status)" />
                        </div>
                    </div>
                    <p class="text-600 mb-3">{{ selectedQuestion.content }}</p>
                    <div v-if="selectedQuestion.tags && selectedQuestion.tags.length > 0" class="flex flex-wrap gap-1 mb-3">
                        <Tag v-for="tag in selectedQuestion.tags" :key="tag" :value="tag" severity="secondary" />
                    </div>
                </div>

                <!-- 답변 목록 -->
                <div v-if="selectedQuestion.answers && selectedQuestion.answers.length > 0">
                    <h4 class="text-900 font-medium mb-3">답변 ({{ selectedQuestion.answers.length }}개)</h4>
                    <div v-for="answer in selectedQuestion.answers" :key="answer.id" class="answer-item mb-3">
                        <div class="flex align-items-center gap-3 mb-2">
                            <Avatar :image="answer.userAvatar" :label="answer.userName.charAt(0)" size="normal" shape="circle" />
                            <div>
                                <div class="font-semibold">{{ answer.userName }}</div>
                                <div class="text-sm text-500">{{ formatDate(answer.createdAt) }}</div>
                            </div>
                            <div v-if="answer.isExpert" class="ml-auto">
                                <Tag value="전문가" severity="success" />
                            </div>
                        </div>
                        <p class="text-600 m-0">{{ answer.content }}</p>
                    </div>
                </div>

                <!-- 답변 작성 -->
                <div v-if="selectedQuestion.status === 'pending'">
                    <h4 class="text-900 font-medium mb-3">답변하기</h4>
                    <Textarea v-model="newAnswer" placeholder="답변을 입력하세요" rows="4" class="w-full mb-3" />
                    <Button label="답변 등록" @click="submitAnswer" />
                </div>
            </div>
            <template #footer>
                <Button label="닫기" @click="showQuestionDetail = false" />
            </template>
        </Dialog>
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
const questions = ref([]);
const searchQuery = ref('');
const selectedCategory = ref(null);
const selectedStatus = ref(null);
const selectedSort = ref('latest');
const showAskDialog = ref(false);
const showQuestionDetail = ref(false);
const selectedQuestion = ref(null);
const newAnswer = ref('');
const first = ref(0);
const rows = ref(10);

const newQuestion = ref({
    title: '',
    category: '',
    content: '',
    tags: ''
});

const categories = ref([
    { name: '요리법', value: 'cooking' },
    { name: '재료', value: 'ingredient' },
    { name: '도구', value: 'tool' },
    { name: '보관', value: 'storage' },
    { name: '기타', value: 'etc' }
]);

const categoryOptions = ref([{ name: '전체', value: null }, ...categories.value]);

const statusOptions = ref([
    { label: '전체', value: null },
    { label: '답변 대기', value: 'pending' },
    { label: '답변 완료', value: 'answered' }
]);

const sortOptions = ref([
    { label: '최신순', value: 'latest' },
    { label: '인기순', value: 'popular' },
    { label: '답변순', value: 'answers' },
    { label: '조회순', value: 'views' }
]);

// 계산된 속성
const totalQuestions = computed(() => questions.value.length);

const answeredQuestions = computed(() => {
    return questions.value.filter((question) => question.status === 'answered').length;
});

const pendingQuestions = computed(() => {
    return questions.value.filter((question) => question.status === 'pending').length;
});

const expertCount = computed(() => {
    const experts = new Set();
    questions.value.forEach((question) => {
        if (question.answers) {
            question.answers.forEach((answer) => {
                if (answer.isExpert) {
                    experts.add(answer.userName);
                }
            });
        }
    });
    return experts.size;
});

const filteredQuestions = computed(() => {
    let filtered = questions.value;

    // 검색 필터
    if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase();
        filtered = filtered.filter((question) => question.title.toLowerCase().includes(query) || question.content.toLowerCase().includes(query) || question.userName.toLowerCase().includes(query));
    }

    // 카테고리 필터
    if (selectedCategory.value) {
        filtered = filtered.filter((question) => question.category === selectedCategory.value);
    }

    // 상태 필터
    if (selectedStatus.value) {
        filtered = filtered.filter((question) => question.status === selectedStatus.value);
    }

    // 정렬
    switch (selectedSort.value) {
        case 'popular':
            filtered = filtered.sort((a, b) => b.likes - a.likes);
            break;
        case 'answers':
            filtered = filtered.sort((a, b) => (b.answers?.length || 0) - (a.answers?.length || 0));
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

const totalFilteredQuestions = computed(() => {
    let filtered = questions.value;

    if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase();
        filtered = filtered.filter((question) => question.title.toLowerCase().includes(query) || question.content.toLowerCase().includes(query) || question.userName.toLowerCase().includes(query));
    }

    if (selectedCategory.value) {
        filtered = filtered.filter((question) => question.category === selectedCategory.value);
    }

    if (selectedStatus.value) {
        filtered = filtered.filter((question) => question.status === selectedStatus.value);
    }

    return filtered.length;
});

// 메서드
const loadQuestions = () => {
    questions.value = [
        {
            id: 1,
            title: '김치찌개가 너무 시어져요',
            content: '김치찌개를 만들 때마다 김치가 너무 시어져서 맛이 없어요. 김치를 어떻게 보관해야 할까요? 그리고 김치찌개를 만들 때 김치를 어떻게 처리해야 맛있게 나올까요?',
            category: 'cooking',
            status: 'answered',
            userName: '김요리',
            userAvatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=50',
            createdAt: '2024-01-15T10:30:00Z',
            views: 89,
            likes: 5,
            isLiked: false,
            isMine: false,
            tags: ['김치찌개', '김치', '보관'],
            answers: [
                {
                    id: 1,
                    content: '김치가 시어지는 이유는 보관 방법 때문일 가능성이 높습니다. 김치를 냉장고에 보관할 때는 밀폐용기에 담아서 보관하시고, 김치찌개를 만들 때는 김치를 먼저 볶아서 신맛을 줄여보세요.',
                    userName: '요리전문가',
                    userAvatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=50',
                    createdAt: '2024-01-15T11:00:00Z',
                    isExpert: true
                }
            ]
        },
        {
            id: 2,
            title: '파스타 면이 계속 끈적거려요',
            content: '파스타를 삶을 때 면이 계속 끈적거리고 뭉쳐요. 어떻게 해야 할까요? 물에 소금을 넣고 끓여도 같은 문제가 발생합니다.',
            category: 'cooking',
            status: 'answered',
            userName: '이맛집',
            userAvatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=50',
            createdAt: '2024-01-14T15:20:00Z',
            views: 67,
            likes: 3,
            isLiked: true,
            isMine: true,
            tags: ['파스타', '면', '끈적거림'],
            answers: [
                {
                    id: 2,
                    content: '파스타 면이 끈적거리는 이유는 물이 충분히 끓지 않았거나 면을 넣은 후 물이 끓지 않았기 때문입니다. 물이 완전히 끓을 때까지 기다린 후 면을 넣고, 면을 넣은 후에도 물이 계속 끓도록 해주세요.',
                    userName: '파스타마스터',
                    userAvatar: 'https://images.unsplash.com/photo-1494790108755-2616b612b786?w=50',
                    createdAt: '2024-01-14T16:00:00Z',
                    isExpert: true
                }
            ]
        },
        {
            id: 3,
            title: '초밥 만들 때 밥이 너무 끈적해요',
            content: '초밥을 만들 때 밥이 너무 끈적해서 초밥이 제대로 뭉쳐지지 않아요. 밥을 어떻게 지어야 할까요?',
            category: 'cooking',
            status: 'pending',
            userName: '박요리사',
            userAvatar: 'https://images.unsplash.com/photo-1494790108755-2616b612b786?w=50',
            createdAt: '2024-01-13T20:15:00Z',
            views: 45,
            likes: 2,
            isLiked: false,
            isMine: false,
            tags: ['초밥', '밥', '끈적거림'],
            answers: []
        },
        {
            id: 4,
            title: '요리용 칼 추천해주세요',
            content: '요리를 시작하려고 하는데 좋은 칼을 추천해주세요. 예산은 5만원 정도로 생각하고 있어요.',
            category: 'tool',
            status: 'answered',
            userName: '최맛있',
            userAvatar: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=50',
            createdAt: '2024-01-12T14:45:00Z',
            views: 123,
            likes: 8,
            isLiked: false,
            isMine: false,
            tags: ['칼', '도구', '추천'],
            answers: [
                {
                    id: 3,
                    content: '5만원 예산으로는 일본산 칼이나 독일산 칼을 추천드립니다. 특히 셰프나이프는 다용도로 사용할 수 있어서 초보자에게 좋습니다. 온라인에서 구매하시기 전에 직접 잡아보시는 것을 추천드려요.',
                    userName: '도구전문가',
                    userAvatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=50',
                    createdAt: '2024-01-12T15:30:00Z',
                    isExpert: true
                }
            ]
        },
        {
            id: 5,
            title: '생선 보관 방법이 궁금해요',
            content: '생선을 사서 집에 가져왔는데 어떻게 보관해야 할까요? 냉장고에 그냥 넣어두면 될까요?',
            category: 'storage',
            status: 'pending',
            userName: '정요리왕',
            userAvatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=50',
            createdAt: '2024-01-11T09:30:00Z',
            views: 78,
            likes: 4,
            isLiked: false,
            isMine: false,
            tags: ['생선', '보관', '냉장고'],
            answers: []
        }
    ];
};

const refreshQnA = () => {
    loadQuestions();
    toast.add({ severity: 'success', summary: '새로고침', detail: 'Q&A가 업데이트되었습니다.', life: 3000 });
};

const performSearch = () => {
    first.value = 0;
    toast.add({ severity: 'info', summary: '검색', detail: `"${searchQuery.value}" 검색 결과를 표시합니다.`, life: 3000 });
};

const askQuestion = () => {
    if (!newQuestion.value.title || !newQuestion.value.category || !newQuestion.value.content) {
        toast.add({ severity: 'error', summary: '입력 오류', detail: '필수 항목을 모두 입력해주세요.', life: 3000 });
        return;
    }

    const newId = Math.max(...questions.value.map((q) => q.id)) + 1;
    const question = {
        id: newId,
        title: newQuestion.value.title,
        content: newQuestion.value.content,
        category: newQuestion.value.category,
        status: 'pending',
        userName: '나',
        userAvatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=50',
        createdAt: new Date().toISOString(),
        views: 0,
        likes: 0,
        isLiked: false,
        isMine: true,
        tags: newQuestion.value.tags ? newQuestion.value.tags.split(',').map((tag) => tag.trim()) : [],
        answers: []
    };

    questions.value.unshift(question);
    cancelAsk();
    toast.add({ severity: 'success', summary: '질문 등록', detail: '질문이 등록되었습니다.', life: 3000 });
};

const cancelAsk = () => {
    newQuestion.value = {
        title: '',
        category: '',
        content: '',
        tags: ''
    };
    showAskDialog.value = false;
};

const viewQuestion = (questionId) => {
    const question = questions.value.find((q) => q.id === questionId);
    if (question) {
        question.views += 1;
        selectedQuestion.value = question;
        showQuestionDetail.value = true;
    }
};

const submitAnswer = () => {
    if (!newAnswer.value.trim()) {
        toast.add({ severity: 'error', summary: '입력 오류', detail: '답변 내용을 입력해주세요.', life: 3000 });
        return;
    }

    if (selectedQuestion.value) {
        const answerId = Math.max(...selectedQuestion.value.answers.map((a) => a.id)) + 1;
        const answer = {
            id: answerId,
            content: newAnswer.value,
            userName: '나',
            userAvatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=50',
            createdAt: new Date().toISOString(),
            isExpert: false
        };

        selectedQuestion.value.answers.push(answer);
        selectedQuestion.value.status = 'answered';
        newAnswer.value = '';
        toast.add({ severity: 'success', summary: '답변 등록', detail: '답변이 등록되었습니다.', life: 3000 });
    }
};

const toggleLike = (questionId) => {
    const question = questions.value.find((q) => q.id === questionId);
    if (question) {
        question.isLiked = !question.isLiked;
        question.likes += question.isLiked ? 1 : -1;
        toast.add({
            severity: 'success',
            summary: question.isLiked ? '좋아요' : '좋아요 취소',
            detail: question.isLiked ? '질문에 좋아요를 눌렀습니다.' : '좋아요를 취소했습니다.',
            life: 3000
        });
    }
};

const shareQuestion = (question) => {
    if (navigator.share) {
        navigator.share({
            title: question.title,
            text: question.content,
            url: window.location.origin + `/community/qna/${question.id}`
        });
    } else {
        navigator.clipboard.writeText(window.location.origin + `/community/qna/${question.id}`);
        toast.add({ severity: 'info', summary: '링크 복사', detail: '질문 링크가 클립보드에 복사되었습니다.', life: 3000 });
    }
};

const bookmarkQuestion = (questionId) => {
    toast.add({ severity: 'info', summary: '북마크', detail: '질문이 북마크되었습니다.', life: 3000 });
};

const editQuestion = (question) => {
    toast.add({ severity: 'info', summary: '편집', detail: '편집 기능은 준비 중입니다.', life: 3000 });
};

const deleteQuestion = (questionId) => {
    const index = questions.value.findIndex((question) => question.id === questionId);
    if (index > -1) {
        questions.value.splice(index, 1);
        toast.add({ severity: 'success', summary: '삭제 완료', detail: '질문이 삭제되었습니다.', life: 3000 });
    }
};

const getCategorySeverity = (category) => {
    const severityMap = {
        cooking: 'info',
        ingredient: 'success',
        tool: 'warning',
        storage: 'help',
        etc: 'secondary'
    };
    return severityMap[category] || 'primary';
};

const getStatusSeverity = (status) => {
    const severityMap = {
        pending: 'warning',
        answered: 'success'
    };
    return severityMap[status] || 'primary';
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
    loadQuestions();
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

.question-card {
    transition:
        transform 0.2s,
        box-shadow 0.2s;
}

.question-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.answer-preview {
    background-color: var(--surface-50);
    padding: 1rem;
    border-radius: 8px;
    border-left: 4px solid var(--green-500);
}

.answer-item {
    background-color: var(--surface-50);
    padding: 1rem;
    border-radius: 8px;
    border-left: 4px solid var(--blue-500);
}
</style>
