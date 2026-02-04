<template>
    <div class="min-h-screen">
        <!-- 로딩 상태 -->
        <div v-if="loading" class="flex items-center justify-center min-h-screen">
            <div class="animate-spin rounded-full h-32 w-32 border-b-2 border-gray-500"></div>
        </div>

        <!-- 에러 상태 -->
        <div v-else-if="error" class="flex items-center justify-center min-h-screen">
            <div class="text-center">
                <div class="text-6xl mb-4">😞</div>
                <h2 class="text-2xl font-bold text-gray-800 mb-2">레시피를 찾을 수 없습니다</h2>
                <p class="text-gray-600 mb-4">{{ error }}</p>
                <button @click="goBack" class="px-6 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600">
                    돌아가기
                </button>
            </div>
        </div>

        <!-- 레시피 상세 내용 -->
        <div v-else-if="recipe" class="max-w-6xl mx-auto px-4 py-8">
            <RecipeDetailHeader
                :recipe="recipe"
                :main-image="mainImage"
                :cooking-tips-data="cookingTipsData"
                :is-liked="isLiked"
                :format-number="formatNumber"
                @go-back="goBack"
                @toggle-like="toggleLike"
                @share="shareRecipe"
            />

            <RecipeDetailIngredients :ingredient-groups="recipe.ingredientGroups || []" />

            <RecipeDetailSteps :steps="recipe.steps || []" />

            <RecipeDetailGallery :images="recipe.images || []" />

            <!-- 댓글 섹션 -->
            <RecipeComments
                :comments="comments"
                :is-logged-in="isLoggedIn"
                :is-recipe-author="isRecipeAuthor"
                :member-profile-image="authStore.memberProfileImage"
                v-model:new-comment="newComment"
                :new-comment-image-preview="newCommentImagePreview"
                v-model:reply-content="replyContent"
                :reply-image-preview="replyImagePreview"
                :replying-to-comment-id="replyingToCommentId"
                :replying-to-comment="replyingToComment"
                :editing-comment-id="editingCommentId"
                v-model:editing-content="editingContent"
                :editing-image-preview="editingImagePreview"
                :expanded-comments="expandedComments"
                :total-pages="totalPages"
                :current-page="currentPage"
                :current-member-id="currentMemberId"
                :format-date="formatDate"
                @submit-comment="submitComment"
                @focus-comment-textarea="focusCommentTextarea"
                @comment-image-select="handleCommentImageSelect"
                @remove-comment-image="removeCommentImage"
                @go-login="router.push({ path: '/auth/login', query: { redirect: route.fullPath } })"
                @submit-reply="submitReply"
                @cancel-reply="cancelReply"
                @reply-image-select="handleReplyImageSelect"
                @remove-reply-image="removeReplyImage"
                @toggle-reply-form="toggleReplyForm"
                @start-edit-comment="startEditComment"
                @cancel-edit-comment="cancelEditComment"
                @edit-image-select="handleEditImageSelect"
                @remove-edit-image="removeEditImage"
                @update-comment="updateComment"
                @delete-comment="deleteComment"
                @toggle-replies-visibility="toggleRepliesVisibility"
                @load-page="loadPage"
                @open-image="(payload) => openImageModalFromPayload(payload, 0)"
            />

        </div>

        <!-- 이미지 모달 -->
        <Teleport to="body">
            <div 
                v-if="showImageModal" 
                @click="closeImageModal"
                class="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center z-[1000]"
                data-modal="image-modal"
            >
            <div 
                class="relative max-w-4xl max-h-full p-4"
                @click.stop
            >
                <button 
                    @click="closeImageModal"
                    class="absolute top-4 right-4 text-white text-2xl hover:text-gray-300 z-10"
                >
                    <i class="pi pi-times"></i>
                </button>
                <img 
                    :src="selectedImage?.url" 
                    :alt="`이미지 ${selectedImageIndex + 1}`"
                    class="max-w-full max-h-full rounded-lg"
                />
            </div>
        </div>
        </Teleport>
    </div>
</template>

<script setup lang="ts">
import RecipeComments from '@/components/recipe/RecipeComments.vue';
import RecipeDetailGallery from '@/components/recipe/RecipeDetailGallery.vue';
import RecipeDetailHeader from '@/components/recipe/RecipeDetailHeader.vue';
import RecipeDetailIngredients from '@/components/recipe/RecipeDetailIngredients.vue';
import RecipeDetailSteps from '@/components/recipe/RecipeDetailSteps.vue';
import { ref, onMounted, computed, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { httpJson, httpForm } from '@/utils/http';
import { useConfirm } from 'primevue/useconfirm';
import { useAuthStore } from '@/stores/authStore';
import { useErrorHandler } from '@/utils/errorHandler';
import { getApiBaseUrl } from '@/utils/constants';
import { useAppToast } from '@/utils/toast';
import { createRecipeView } from '@/api/recipeViewApi';
import type { RecipeDetail, RecipeComment, RecipeImage, RecipeStep } from '@/types/recipe';

const route = useRoute();
const router = useRouter();
const confirm = useConfirm();
const authStore = useAuthStore();
const { handleApiCall, handleApiCallVoid } = useErrorHandler();
const { showSuccess, showWarn, showError } = useAppToast();

// 반응형 데이터
const loading = ref(true);
const error = ref<string | null>(null);
const recipe = ref<RecipeDetail | null>(null);
const comments = ref<RecipeComment[]>([]);
const newComment = ref('');
const newCommentImage = ref<File | null>(null);
const newCommentImagePreview = ref<string | null>(null);
const replyContent = ref('');
const replyImage = ref<File | null>(null);
const replyImagePreview = ref<string | null>(null);
const replyingToCommentId = ref<number | null>(null);
const replyingToComment = ref<RecipeComment | null>(null); // 답글 대상 댓글 정보
const editingCommentId = ref<number | null>(null);
const editingContent = ref('');
const editingImage = ref<File | null>(null);
const editingImagePreview = ref<string | null>(null);
const editingRemoveImage = ref(false);
const isLiked = ref(false);
const showImageModal = ref(false);
const selectedImage = ref<RecipeImage | null>(null);
const selectedImageIndex = ref(0);

// Pagination 관련
const currentPage = ref(0);
const totalPages = ref(0);
const totalComments = ref(0);
const pageSize = 10;

// 답글 펼치기/접기 상태
const expandedComments = ref<Set<number>>(new Set());

// 난이도 공통코드
const difficultyCodes = ref<Map<string, string>>(new Map());

// 현재 로그인한 사용자 정보 (authStore에서 가져옴)
const isLoggedIn = computed(() => authStore.isLoggedIn);
const currentMemberId = computed(() => authStore.memberInfo?.id || null);

// 현재 사용자가 레시피 작성자인지 확인
const isRecipeAuthor = computed(() => {
    return !!(recipe.value && currentMemberId.value && recipe.value.memberId === currentMemberId.value);
});

// 계산된 속성
const mainImage = computed(() => {
    if (!recipe.value?.images) return null;
    return recipe.value.images.find((img) => img.isMainImage) || recipe.value.images[0];
});

// cookingTips에서 각 항목 추출
const cookingTipsData = computed(() => {
    if (!recipe.value?.cookingTips || !Array.isArray(recipe.value.cookingTips)) {
        return { servings: null, cookingTime: null, difficulty: null };
    }
    
    const servingsTip = recipe.value.cookingTips.find((tip) => tip.codeId === 'SERVINGS');
    const cookingTimeTip = recipe.value.cookingTips.find((tip) => tip.codeId === 'COOKING_TIME');
    const difficultyTip = recipe.value.cookingTips.find((tip) => tip.codeId === 'DIFFICULTY');
    
    // 난이도는 detailCodeId를 사용하여 공통코드에서 codeName을 찾음
    let difficultyText = null;
    if (difficultyTip) {
        const detailCodeId = difficultyTip.detailCodeId || difficultyTip.detailName;
        if (detailCodeId) {
            difficultyText = difficultyCodes.value.get(detailCodeId) || detailCodeId;
        }
    }
    
    return {
        servings: servingsTip?.detailName || null,
        cookingTime: cookingTimeTip?.detailName || null,
        difficulty: difficultyText
    };
});

// 난이도 공통코드 로드
const loadDifficultyCodes = async () => {
    try {
        const response = await httpJson(
            getApiBaseUrl('cook'),
            `/api/common-codes?codeGroup=COOKINGTIP`,
            { method: 'GET' }
        );
        
        const codes = Array.isArray(response) ? response : [];
        const difficultyCode = codes.find((code: { codeId: string; details?: Array<{ detailCodeId: string; codeName: string }> }) => code.codeId === 'DIFFICULTY');
        
        if (difficultyCode && difficultyCode.details) {
            difficultyCodes.value.clear();
            difficultyCode.details.forEach((detail: { detailCodeId: string; codeName: string }) => {
                difficultyCodes.value.set(detail.detailCodeId, detail.codeName);
            });
        }
    } catch (err) {
        console.error('난이도 공통코드 로드 실패:', err);
    }
};

// 메서드
const fetchRecipeDetail = async () => {
    try {
        loading.value = true;
        error.value = null;
        
        const recipeId = route.params.id;
        const response = await httpJson<RecipeDetail>(
            getApiBaseUrl('cook'),
            `/api/recipe/${recipeId}`,
            { method: 'GET' }
        );
        
        // 백엔드 응답의 steps 필드명을 프론트엔드 타입에 맞게 변환
        if (response.steps && Array.isArray(response.steps)) {
            type StepFromApi = RecipeStep & { step?: number; description?: string; image?: string };
            response.steps = (response.steps as StepFromApi[]).map((step: StepFromApi) => ({
                order: step.step ?? step.order ?? 0,
                text: step.description ?? step.text ?? '',
                imageUrl: step.image ?? step.imageUrl ?? undefined
            }));
        }
        
        recipe.value = response;
        
        // 찜 여부 확인
        if (currentMemberId.value) {
            await checkFavoriteStatus();
        }
        
        // 조회 기록 생성 (로그인 사용자만)
        if (authStore.isLoggedIn) {
            await recordRecipeView(Number(recipeId));
        }
        
        // 댓글 목록 불러오기
        await fetchComments();
    } catch (err) {
        error.value = '레시피를 불러오는 중 오류가 발생했습니다.';
        console.error('Recipe detail fetch error:', err);
    } finally {
        loading.value = false;
    }
};

/**
 * 레시피 조회 기록 생성
 */
const recordRecipeView = async (recipeId: number) => {
    try {
        await createRecipeView(recipeId);
    } catch (err) {
        // 조회 기록 생성 실패는 사용자에게 노출하지 않음
        console.error('Failed to record recipe view:', err);
    }
};

const checkFavoriteStatus = async () => {
    try {
        const recipeId = route.params.id;
        const response = await httpJson<{ isFavorite: boolean }>(
            getApiBaseUrl('cook'),
            `/api/recipe/favorites/check?memberId=${currentMemberId.value}&recipeId=${recipeId}`,
            { method: 'GET' }
        );
        
        isLiked.value = response.isFavorite;
    } catch (err) {
        console.error('찜 여부 확인 실패:', err);
    }
};

const fetchComments = async (page: number = 0) => {
    try {
        const recipeId = route.params.id;
        const response = await httpJson<{ comments?: RecipeComment[]; currentPage: number; totalPages: number; totalComments: number }>(
            getApiBaseUrl('cook'),
            `/api/recipe/comments/${recipeId}/page?page=${page}&size=${pageSize}`,
            { method: 'GET' }
        );
        // API는 답글 목록을 replies로 반환하므로, 템플릿에서 사용하는 children으로 정규화
        const rawComments = response.comments ?? [];
        comments.value = rawComments.map((c: RecipeComment & { replies?: RecipeComment[] }) => ({
            ...c,
            children: c.replies ?? c.children ?? []
        }));
        currentPage.value = response.currentPage;
        totalPages.value = response.totalPages;
        totalComments.value = response.totalComments;
    } catch (err) {
        console.error('Comments fetch error:', err);
    }
};

const loadPage = async (page: number) => {
    await fetchComments(page);
};

const toggleRepliesVisibility = (commentId: number) => {
    const next = new Set(expandedComments.value);
    if (next.has(commentId)) {
        next.delete(commentId);
    } else {
        next.add(commentId);
    }
    expandedComments.value = next;
};

const goBack = () => {
    router.back();
};

const toggleLike = async () => {
    // 로그인 확인
    if (!isLoggedIn.value || !currentMemberId.value) {
        return;
    }
    
    const recipeId = route.params.id;
    const response = await handleApiCall(
        () => httpJson<{ isFavorite: boolean }>(
            getApiBaseUrl('cook'),
            `/api/recipe/favorites/toggle?memberId=${currentMemberId.value}&recipeId=${recipeId}`,
            { method: 'PUT' }
        ),
        '찜 기능을 사용할 수 없습니다.',
        '찜 토글 실패'
    );
    
    if (response) {
        isLiked.value = response.isFavorite;
    }
};

const shareRecipe = () => {
    if (!recipe.value) return;
    if (navigator.share) {
        navigator.share({
            title: recipe.value.title,
            text: recipe.value.description || recipe.value.introduction || '',
            url: window.location.href
        });
    } else {
        // 클립보드에 URL 복사
        navigator.clipboard.writeText(window.location.href);
        showSuccess('링크가 클립보드에 복사되었습니다.');
    }
};

const handleCommentImageSelect = (event: Event) => {
    const target = event.target as HTMLInputElement;
    const file = target.files?.[0];
    if (file) {
        if (!file.type.startsWith('image/')) {
            showWarn('이미지 파일만 업로드할 수 있습니다.');
            return;
        }
        if (file.size > 5 * 1024 * 1024) {
            showWarn('이미지 크기는 5MB 이하여야 합니다.');
            return;
        }
        
        newCommentImage.value = file;
        const reader = new FileReader();
        reader.onload = (e) => {
            newCommentImagePreview.value = e.target?.result as string;
        };
        reader.readAsDataURL(file);
    }
};

const removeCommentImage = () => {
    newCommentImage.value = null;
    newCommentImagePreview.value = null;
};

const submitComment = async () => {
    if (!newComment.value.trim()) return;
    
    // 로그인 확인
    if (!isLoggedIn.value || !currentMemberId.value) {
        return;
    }
    
    // 레시피 작성자는 댓글 작성 불가
    if (isRecipeAuthor.value) {
        showWarn('작성자는 답글만 작성이 가능합니다');
        return;
    }
    
    const recipeId = route.params.id;
    
    // 이미지가 있으면 multipart/form-data로 전송
    if (newCommentImage.value) {
        const formData = new FormData();
        formData.append('memberId', currentMemberId.value.toString());
        formData.append('content', newComment.value);
        formData.append('image', newCommentImage.value);
        
        const success = await handleApiCallVoid(
            () => httpForm(
                getApiBaseUrl('cook'),
                `/api/recipe/comments/${recipeId}/with-image`,
                formData,
                { method: 'POST' }
            ),
            '댓글 작성 중 오류가 발생했습니다.',
            '댓글 작성 실패'
        );
        
        if (success) {
            newComment.value = '';
            newCommentImage.value = null;
            newCommentImagePreview.value = null;
            await fetchComments(0);
        }
    } else {
        // 이미지가 없으면 JSON으로 전송
        const success = await handleApiCallVoid(
            () => httpJson(
                getApiBaseUrl('cook'),
                `/api/recipe/comments/${recipeId}`,
                {
                    method: 'POST',
                    body: JSON.stringify({
                        memberId: currentMemberId.value,
                        content: newComment.value,
                        parentId: null
                    })
                }
            ),
            '댓글 작성 중 오류가 발생했습니다.',
            '댓글 작성 실패'
        );
        
        if (success) {
            newComment.value = '';
            newCommentImage.value = null;
            newCommentImagePreview.value = null;
            await fetchComments(0);
        }
    }
};

const focusCommentTextarea = () => {
    // 레시피 작성자는 댓글 작성 불가
    if (isRecipeAuthor.value) {
        showWarn('작성자는 답글만 작성이 가능합니다');
    }
};

const handleReplyImageSelect = (event: Event) => {
    const target = event.target as HTMLInputElement;
    const file = target.files?.[0];
    if (file) {
        if (!file.type.startsWith('image/')) {
            showWarn('이미지 파일만 업로드할 수 있습니다.');
            return;
        }
        if (file.size > 5 * 1024 * 1024) {
            showWarn('이미지 크기는 5MB 이하여야 합니다.');
            return;
        }
        
        replyImage.value = file;
        const reader = new FileReader();
        reader.onload = (e) => {
            replyImagePreview.value = e.target?.result as string;
        };
        reader.readAsDataURL(file);
    }
};

const removeReplyImage = () => {
    replyImage.value = null;
    replyImagePreview.value = null;
};

const submitReply = async () => {
    if (!replyContent.value.trim()) return;
    if (!replyingToComment.value) return;

    // 로그인 확인
    if (!isLoggedIn.value || !currentMemberId.value) {
        return;
    }

    // 답글의 부모는 항상 최상위 댓글 ID (백엔드가 동일 depth로 저장)
    const parentId = replyingToComment.value.parentId ?? replyingToComment.value.id;
    const recipeId = route.params.id;

    // 부모 댓글 닉네임 prefix 추가
    const parentNickname = replyingToComment.value.memberNickname || replyingToComment.value.memberName;
    const contentWithPrefix = `@${parentNickname} ${replyContent.value}`;

    // 이미지가 있으면 multipart/form-data로 전송
    if (replyImage.value) {
        const formData = new FormData();
        formData.append('memberId', currentMemberId.value.toString());
        formData.append('content', contentWithPrefix);
        formData.append('parentId', parentId.toString());
        formData.append('image', replyImage.value);
        
        const success = await handleApiCallVoid(
            () => httpForm(
                getApiBaseUrl('cook'),
                `/api/recipe/comments/${recipeId}/with-image`,
                formData,
                { method: 'POST' }
            ),
            '답글 작성 중 오류가 발생했습니다.',
            '답글 작성 실패'
        );
        
        if (success) {
            replyContent.value = '';
            replyImage.value = null;
            replyImagePreview.value = null;
            replyingToCommentId.value = null;
            replyingToComment.value = null;
            await fetchComments(currentPage.value);
        }
    } else {
        // 이미지가 없으면 JSON으로 전송
        const success = await handleApiCallVoid(
            () => httpJson(
                getApiBaseUrl('cook'),
                `/api/recipe/comments/${recipeId}`,
                {
                    method: 'POST',
                    body: JSON.stringify({
                        memberId: currentMemberId.value,
                        content: contentWithPrefix,
                        parentId: parentId
                    })
                }
            ),
            '답글 작성 중 오류가 발생했습니다.',
            '답글 작성 실패'
        );
        
        if (success) {
            replyContent.value = '';
            replyImage.value = null;
            replyImagePreview.value = null;
            replyingToCommentId.value = null;
            replyingToComment.value = null;
            await fetchComments(currentPage.value);
        }
    }
};

const toggleReplyForm = (comment: RecipeComment) => {
    // 로그인 확인
    if (!isLoggedIn.value) {
        return;
    }
    
    if (replyingToCommentId.value === comment.id) {
        replyingToCommentId.value = null;
        replyingToComment.value = null;
        replyContent.value = '';
    } else {
        replyingToCommentId.value = comment.id;
        replyingToComment.value = comment;
        replyContent.value = '';
        
        // 답글 목록을 펼침
        const rootCommentId = comment.parentId || comment.id;
        if (!expandedComments.value.has(rootCommentId)) {
            expandedComments.value = new Set([...expandedComments.value, rootCommentId]);
        }
    }
};

const cancelReply = () => {
    replyingToCommentId.value = null;
    replyingToComment.value = null;
    replyContent.value = '';
    replyImage.value = null;
    replyImagePreview.value = null;
};

const startEditComment = (comment: RecipeComment) => {
    editingCommentId.value = comment.id;
    editingContent.value = comment.content;
    editingImagePreview.value = comment.imageUrl || null;
    editingRemoveImage.value = false;
};

const cancelEditComment = () => {
    editingCommentId.value = null;
    editingContent.value = '';
    editingImage.value = null;
    editingImagePreview.value = null;
    editingRemoveImage.value = false;
};

const handleEditImageSelect = (event: Event) => {
    const target = event.target as HTMLInputElement;
    const file = target.files?.[0];
    if (file) {
        if (!file.type.startsWith('image/')) {
            showWarn('이미지 파일만 업로드할 수 있습니다.');
            return;
        }
        if (file.size > 5 * 1024 * 1024) {
            showWarn('이미지 크기는 5MB 이하여야 합니다.');
            return;
        }
        
        editingImage.value = file;
        const reader = new FileReader();
        reader.onload = (e) => {
            editingImagePreview.value = e.target?.result as string;
        };
        reader.readAsDataURL(file);
        editingRemoveImage.value = false;
    }
};

const removeEditImage = () => {
    editingImage.value = null;
    editingImagePreview.value = null;
    editingRemoveImage.value = true;
};

const updateComment = async (commentId: number) => {
    if (!editingContent.value.trim()) return;
    
    try {
        // 이미지가 변경되었거나 제거된 경우
        if (editingImage.value || editingRemoveImage.value) {
            const formData = new FormData();
            formData.append('memberId', currentMemberId.value!.toString());
            formData.append('content', editingContent.value);
            formData.append('removeImage', editingRemoveImage.value.toString());
            if (editingImage.value) {
                formData.append('image', editingImage.value);
            }
            
            await httpForm(
                getApiBaseUrl('cook'),
                `/api/recipe/comments/${commentId}/with-image`,
                formData,
                { method: 'PUT' }
            );
        } else {
            // 이미지가 변경되지 않은 경우
            await httpJson(
                getApiBaseUrl('cook'),
                `/api/recipe/comments/${commentId}`,
                {
                    method: 'PUT',
                    body: JSON.stringify({
                        memberId: currentMemberId.value,
                        content: editingContent.value
                    })
                }
            );
        }
        
        editingCommentId.value = null;
        editingContent.value = '';
        editingImage.value = null;
        editingImagePreview.value = null;
        editingRemoveImage.value = false;
        
        // 댓글 목록 다시 불러오기
        await fetchComments(currentPage.value);
    } catch (err) {
        console.error('Comment update error:', err);
        showError('댓글 수정 중 오류가 발생했습니다.');
    }
};

const deleteComment = async (commentId: number) => {
    confirm.require({
        message: '정말 삭제하시겠습니까?',
        header: '안내',
        icon: 'pi pi-info-circle',
        rejectProps: {
            label: '취소',
            severity: 'secondary',
            outlined: true
        },
        acceptProps: {
            label: '확인'
        },
        accept: async () => {
            loading.value = true;
            error.value = null;
            
            const success = await handleApiCallVoid(
                () => httpJson(
                    getApiBaseUrl('cook'),
                    `/api/recipe/comments/${commentId}?memberId=${currentMemberId.value}`,
                    { method: 'DELETE' }
                ),
                '댓글 삭제 중 오류가 발생했습니다.',
                '댓글 삭제 실패'
            );
            
            if (success) {
                // 댓글 목록 다시 불러오기
                await fetchComments(currentPage.value);
            }
            
            loading.value = false;
        },
        reject: () => {
            // 취소 시 아무것도 하지 않음
        }
    });
};

const openImageModalFromPayload = (payload: RecipeImage | { url: string }, index: number) => {
    const image: RecipeImage = 'isMainImage' in payload ? payload : { ...payload, isMainImage: false };
    openImageModal(image, index);
};

const openImageModal = (image: RecipeImage, index: number, event?: Event) => {
    if (event) {
        event.stopPropagation();
        event.preventDefault();
    }
    selectedImage.value = image;
    selectedImageIndex.value = index;
    showImageModal.value = true;
    
    // 모달이 열릴 때 body 스크롤 방지
    document.body.style.overflow = 'hidden';
};

const closeImageModal = () => {
    showImageModal.value = false;
    selectedImage.value = null;
    selectedImageIndex.value = 0;
    
    // 모달이 닫힐 때 body 스크롤 복원
    document.body.style.overflow = '';
};

const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};

const formatNumber = (num: number | null | undefined): string => {
    if (num === null || num === undefined) return '0';
    return num.toLocaleString('ko-KR');
};

// 생명주기
onMounted(() => {
    const initializePage = async () => {
        // 페이지 진입 즉시 맨 위로 스크롤 (데이터 로딩 전)
        window.scrollTo({ top: 0, behavior: 'instant' });
        
        // 난이도 공통코드 로드
        await loadDifficultyCodes();
        
        // 로그인 여부와 관계없이 레시피 상세 조회
        await fetchRecipeDetail();
        
        // 레시피 로딩 완료 후 해시가 있으면 해당 위치로 스크롤
        if (route.hash) {
            // DOM 렌더링 완료 대기 (이미지 포함)
            await nextTick();
            
            // 이미지와 레이아웃이 완전히 로드될 때까지 추가 대기
            // requestAnimationFrame을 두 번 호출해서 브라우저의 레이아웃 계산 완료 보장
            await new Promise(resolve => {
                requestAnimationFrame(() => {
                    requestAnimationFrame(() => {
                        setTimeout(resolve, 100); // 추가 100ms 여유
                    });
                });
            });
            
            const element = document.querySelector(route.hash);
            if (element) {
                // 요소의 절대 위치를 구해서 고정된 offset만큼 빼고 스크롤
                // 이렇게 하면 댓글 개수와 상관없이 항상 같은 위치에서 보임
                const elementPosition = element.getBoundingClientRect().top + window.pageYOffset;
                const offsetPosition = elementPosition - 80; // 상단 여백 80px
                
                window.scrollTo({
                    top: offsetPosition,
                    behavior: 'smooth'
                });
            }
        }
    };
    initializePage();
});
</script>

<style scoped>
.animate-spin {
    animation: spin 1s linear infinite;
}

@keyframes spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
}
</style>
