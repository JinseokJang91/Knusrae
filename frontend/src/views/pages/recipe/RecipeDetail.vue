<template>
    <div class="min-h-screen bg-gray-50">
        <!-- 로딩 상태 -->
        <div v-if="loading" class="flex items-center justify-center min-h-screen">
            <div class="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-500"></div>
        </div>

        <!-- 에러 상태 -->
        <div v-else-if="error" class="flex items-center justify-center min-h-screen">
            <div class="text-center">
                <div class="text-6xl mb-4">😞</div>
                <h2 class="text-2xl font-bold text-gray-800 mb-2">레시피를 찾을 수 없습니다</h2>
                <p class="text-gray-600 mb-4">{{ error }}</p>
                <button @click="goBack" class="px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600">
                    돌아가기
                </button>
            </div>
        </div>

        <!-- 레시피 상세 내용 -->
        <div v-else-if="recipe" class="max-w-6xl mx-auto px-4 py-8">
            <!-- 헤더 섹션 -->
            <div class="bg-white rounded-2xl shadow-lg overflow-hidden mb-8">
                <!-- 메인 이미지 -->
                <div class="relative w-full bg-gray-500">
                    <img 
                        v-if="mainImage" 
                        :src="mainImage.url" 
                        :alt="recipe.title"
                        class="w-3/4 mx-auto h-full object-cover py-4"
                    />
                    <div v-else class="flex items-center justify-center h-full text-white text-6xl">
                        🍳
                    </div>
                    
                    <!-- 뒤로가기 버튼 -->
                    <div class="absolute top-4 left-4 z-10">
                        <Button 
                            @click="goBack"
                            icon="pi pi-arrow-left"
                            size="large"
                            rounded />
                    </div>

                    <!-- 좋아요 버튼 -->
                    <div class="absolute top-4 right-4 z-10">
                        <Button 
                            @click="toggleLike" 
                            :icon="isLiked ? 'pi pi-heart-fill' : 'pi pi-heart'" 
                            :class="isLiked ? 'p-button-danger' : 'p-button-secondary'" 
                            size="large" 
                            rounded />
                    </div>
                </div>

                <!-- 레시피 기본 정보 -->
                <div class="p-8">
                    <div class="flex items-start justify-between mb-6">
                        <div class="flex-1">
                            <h1 class="text-4xl font-bold text-gray-800 mb-2">{{ recipe.title }}</h1>
                            <p class="text-lg text-gray-600 mb-4">{{ recipe.introduction }}</p>
                            
                            <!-- 태그 -->
                            <div class="flex flex-wrap gap-2 mb-4">
                                <span
                                    v-for="category in recipe.categories"
                                    :key="`${category.codeId}-${category.detailCodeId}`"
                                    class="px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-sm font-medium"
                                >
                                    {{ category.detailName || category.codeName }}
                                </span>
                                <span class="px-3 py-1 bg-green-100 text-green-800 rounded-full text-sm font-medium">
                                    {{ recipe.visibility === 'PUBLIC' ? '공개' : '비공개' }}
                                </span>
                            </div>

                            <!-- 요리 정보 (cookingTips) -->
                            <div v-if="cookingTipsData.servings || cookingTipsData.cookingTime || cookingTipsData.difficulty" class="flex flex-wrap gap-6 mb-4 p-4 bg-gray-50 rounded-lg">
                                <!-- 인분 수 -->
                                <div v-if="cookingTipsData.servings" class="flex items-center space-x-2">
                                    <i class="pi pi-users text-blue-600 text-xl"></i>
                                    <span class="text-gray-700 font-medium">{{ cookingTipsData.servings }}</span>
                                </div>
                                
                                <!-- 요리 시간 -->
                                <div v-if="cookingTipsData.cookingTime" class="flex items-center space-x-2">
                                    <i class="pi pi-clock text-green-600 text-xl"></i>
                                    <span class="text-gray-700 font-medium">{{ cookingTipsData.cookingTime }}</span>
                                </div>
                                
                                <!-- 난이도 -->
                                <div v-if="cookingTipsData.difficulty" class="flex items-center space-x-2">
                                    <i class="pi pi-star text-yellow-600 text-xl"></i>
                                    <div class="flex items-center space-x-1">
                                        <i 
                                            v-for="star in 5" 
                                            :key="star"
                                            :class="star <= (cookingTipsData.difficulty || 0) ? 'pi pi-star-fill text-yellow-400' : 'pi pi-star text-gray-300'"
                                        ></i>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- 통계 정보 -->
                        <div class="flex items-center space-x-6 text-gray-600">
                            <div class="text-center">
                                <div class="text-2xl font-bold text-blue-600">{{ recipe.hits }}</div>
                                <div class="text-sm">조회수</div>
                            </div>
                            <div class="text-center">
                                <div class="text-2xl font-bold text-green-600">{{ recipe.stats?.totalComments || 0 }}</div>
                                <div class="text-sm">댓글</div>
                            </div>
                            <div class="text-center">
                                <div class="text-2xl font-bold text-yellow-600">{{ recipe.stats?.averageRating?.toFixed(1) || '0.0' }}</div>
                                <div class="text-sm">평점</div>
                            </div>
                        </div>
                    </div>

                    <!-- 작성자 정보 -->
                    <div class="flex items-center justify-between py-4 border-t border-gray-200">
                        <div class="flex items-center space-x-3">
                            <div class="w-10 h-10 bg-gray-300 rounded-full flex items-center justify-center">
                                <i class="pi pi-user text-gray-600"></i>
                            </div>
                            <div>
                                <div class="font-medium text-gray-800">{{ recipe.memberName }}</div>
                                <div class="text-sm text-gray-500">{{ formatDate(recipe.createdAt) }}</div>
                            </div>
                        </div>
                        
                        <!-- 공유 버튼 -->
                        <button 
                            @click="shareRecipe"
                            class="flex items-center space-x-2 px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors"
                        >
                            <i class="pi pi-share-alt"></i>
                            <span>공유</span>
                        </button>
                    </div>
                </div>
            </div>

            <!-- 조리 단계 섹션 -->
            <div class="bg-white rounded-2xl shadow-lg p-8 mb-8">
                <h2 class="text-3xl font-bold text-gray-800 mb-8 flex items-center">
                    <i class="pi pi-list mr-3 text-blue-500"></i>
                    조리 순서
                </h2>
                
                <div class="space-y-8">
                    <div 
                        v-for="(step, index) in recipe.steps" 
                        :key="step.id"
                        class="bg-gray-50 rounded-xl p-6"
                    >
                        <div class="mb-4 flex items-center gap-3">
                            <div class="w-10 h-10 bg-blue-500 text-white rounded-full flex items-center justify-center text-lg font-bold">
                                {{ index + 1 }}
                            </div>
                        </div>

                        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 items-start">
                            <!-- 좌측: 이미지 -->
                            <div>
                                <div class="relative w-full overflow-hidden rounded-lg shadow-md bg-white">
                                    <img 
                                        v-if="step.image"
                                        :src="step.image"
                                        :alt="`단계 ${index + 1} 이미지`"
                                        class="w-full h-72 object-cover"
                                    />
                                    <div v-else class="w-full h-72 flex items-center justify-center text-5xl text-gray-300 bg-gray-100">
                                        🖼️
                                    </div>
                                </div>
                            </div>

                            <!-- 우측: 설명 -->
                            <div>
                                <p class="text-gray-800 text-lg leading-relaxed whitespace-pre-line">
                                    {{ step.description }}
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 이미지 갤러리 섹션 -->
            <div v-if="recipe.images && recipe.images.length > 0" class="bg-white rounded-2xl shadow-lg p-8 mb-8">
                <h2 class="text-3xl font-bold text-gray-800 mb-8 flex items-center">
                    <i class="pi pi-images mr-3 text-green-500"></i>
                    이미지 갤러리
                </h2>
                
                <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                    <div 
                        v-for="(image, index) in recipe.images" 
                        :key="image.id"
                        @click="openImageModal(image, index)"
                        class="relative group cursor-pointer overflow-hidden rounded-lg shadow-md hover:shadow-lg transition-all duration-300 transform hover:scale-105"
                    >
                        <img 
                            :src="image.url" 
                            :alt="`이미지 ${index + 1}`"
                            class="w-full h-48 object-cover"
                        />
                        <div class="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-30 transition-all duration-300 flex items-center justify-center">
                            <i class="pi pi-search-plus text-white text-2xl opacity-0 group-hover:opacity-100 transition-opacity"></i>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 댓글 섹션 -->
            <div class="bg-white rounded-2xl shadow-lg p-8 mb-8">
                <h2 class="text-3xl font-bold text-gray-800 mb-8 flex items-center">
                    <i class="pi pi-comments mr-3 text-purple-500"></i>
                    댓글 ({{ comments.length + comments.reduce((sum, c) => sum + (c.replies?.length || 0), 0) }})
                </h2>
                
                <!-- 댓글 작성 -->
                <div class="mb-6">
                    <div class="flex space-x-4">
                        <div class="w-10 h-10 bg-gray-300 rounded-full flex items-center justify-center flex-shrink-0">
                            <i class="pi pi-user text-gray-600"></i>
                        </div>
                        <div class="flex-1">
                            <textarea 
                                v-model="newComment"
                                placeholder="댓글을 작성해주세요..."
                                class="w-full p-4 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
                                rows="3"
                            ></textarea>
                            <div class="flex justify-end mt-2">
                                <button 
                                    @click="submitComment"
                                    :disabled="!newComment.trim()"
                                    class="px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                >
                                    댓글 작성
                                </button>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 댓글 목록 -->
                <div class="space-y-4">
                    <div 
                        v-for="comment in comments" 
                        :key="comment.id"
                        class="space-y-4"
                    >
                        <!-- 최상위 댓글 -->
                        <div class="flex space-x-4 p-4 bg-gray-50 rounded-lg">
                            <div class="w-10 h-10 bg-gray-300 rounded-full flex items-center justify-center flex-shrink-0">
                                <i class="pi pi-user text-gray-600"></i>
                            </div>
                            <div class="flex-1">
                                <div class="flex items-center justify-between mb-2">
                                    <div class="flex items-center space-x-2">
                                        <span class="font-medium text-gray-800">
                                            {{ comment.memberNickname || comment.memberName }}
                                        </span>
                                        <span class="text-sm text-gray-500">{{ formatDate(comment.createdAt) }}</span>
                                        <span v-if="comment.updatedAt && comment.updatedAt !== comment.createdAt" class="text-xs text-gray-400">(수정됨)</span>
                                    </div>
                                    <div v-if="isMyComment(comment)" class="flex space-x-2">
                                        <button 
                                            @click="startEditComment(comment)"
                                            class="text-sm text-blue-500 hover:text-blue-700"
                                        >
                                            수정
                                        </button>
                                        <button 
                                            @click="deleteComment(comment.id)"
                                            class="text-sm text-red-500 hover:text-red-700"
                                        >
                                            삭제
                                        </button>
                                    </div>
                                </div>
                                
                                <!-- 댓글 내용 (수정 모드) -->
                                <div v-if="editingCommentId === comment.id">
                                    <textarea 
                                        v-model="editingContent"
                                        class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none mb-2"
                                        rows="2"
                                    ></textarea>
                                    <div class="flex justify-end space-x-2">
                                        <button 
                                            @click="cancelEditComment"
                                            class="px-4 py-1 text-sm bg-gray-200 text-gray-700 rounded hover:bg-gray-300 transition-colors"
                                        >
                                            취소
                                        </button>
                                        <button 
                                            @click="updateComment(comment.id)"
                                            :disabled="!editingContent.trim()"
                                            class="px-4 py-1 text-sm bg-blue-500 text-white rounded hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                        >
                                            수정 완료
                                        </button>
                                    </div>
                                </div>
                                <!-- 댓글 내용 (일반 모드) -->
                                <p v-else class="text-gray-700 mb-2 whitespace-pre-wrap">{{ comment.content }}</p>
                                
                                <!-- 답글 버튼 -->
                                <button 
                                    v-if="editingCommentId !== comment.id"
                                    @click="toggleReplyForm(comment.id)"
                                    class="text-sm text-blue-500 hover:text-blue-700 font-medium"
                                >
                                    <i class="pi pi-reply mr-1"></i>
                                    답글 {{ comment.replies?.length > 0 ? `(${comment.replies.length})` : '' }}
                                </button>
                            </div>
                        </div>

                        <!-- 답글 작성 폼 -->
                        <div v-if="replyingToCommentId === comment.id" class="ml-14 flex space-x-4 p-4 bg-blue-50 rounded-lg">
                            <div class="w-8 h-8 bg-gray-300 rounded-full flex items-center justify-center flex-shrink-0">
                                <i class="pi pi-user text-gray-600 text-sm"></i>
                            </div>
                            <div class="flex-1">
                                <textarea 
                                    v-model="replyContent"
                                    placeholder="답글을 작성해주세요..."
                                    class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
                                    rows="2"
                                ></textarea>
                                <div class="flex justify-end space-x-2 mt-2">
                                    <button 
                                        @click="cancelReply"
                                        class="px-4 py-1 text-sm bg-gray-200 text-gray-700 rounded hover:bg-gray-300 transition-colors"
                                    >
                                        취소
                                    </button>
                                    <button 
                                        @click="submitReply(comment.id)"
                                        :disabled="!replyContent.trim()"
                                        class="px-4 py-1 text-sm bg-blue-500 text-white rounded hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                    >
                                        답글 작성
                                    </button>
                                </div>
                            </div>
                        </div>

                        <!-- 답글 목록 -->
                        <div v-if="comment.replies && comment.replies.length > 0" class="ml-14 space-y-4">
                            <div 
                                v-for="reply in comment.replies" 
                                :key="reply.id"
                                class="flex space-x-4 p-4 bg-gray-100 rounded-lg"
                            >
                                <div class="w-8 h-8 bg-gray-300 rounded-full flex items-center justify-center flex-shrink-0">
                                    <i class="pi pi-user text-gray-600 text-sm"></i>
                                </div>
                                <div class="flex-1">
                                    <div class="flex items-center justify-between mb-2">
                                        <div class="flex items-center space-x-2">
                                            <span class="font-medium text-gray-800">
                                                {{ reply.memberNickname || reply.memberName }}
                                            </span>
                                            <span class="text-sm text-gray-500">{{ formatDate(reply.createdAt) }}</span>
                                            <span v-if="reply.updatedAt && reply.updatedAt !== reply.createdAt" class="text-xs text-gray-400">(수정됨)</span>
                                        </div>
                                        <div v-if="isMyComment(reply)" class="flex space-x-2">
                                            <button 
                                                @click="startEditComment(reply)"
                                                class="text-sm text-blue-500 hover:text-blue-700"
                                            >
                                                수정
                                            </button>
                                            <button 
                                                @click="deleteComment(reply.id)"
                                                class="text-sm text-red-500 hover:text-red-700"
                                            >
                                                삭제
                                            </button>
                                        </div>
                                    </div>
                                    
                                    <!-- 답글 내용 (수정 모드) -->
                                    <div v-if="editingCommentId === reply.id">
                                        <textarea 
                                            v-model="editingContent"
                                            class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none mb-2"
                                            rows="2"
                                        ></textarea>
                                        <div class="flex justify-end space-x-2">
                                            <button 
                                                @click="cancelEditComment"
                                                class="px-4 py-1 text-sm bg-gray-200 text-gray-700 rounded hover:bg-gray-300 transition-colors"
                                            >
                                                취소
                                            </button>
                                            <button 
                                                @click="updateComment(reply.id)"
                                                :disabled="!editingContent.trim()"
                                                class="px-4 py-1 text-sm bg-blue-500 text-white rounded hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                            >
                                                수정 완료
                                            </button>
                                        </div>
                                    </div>
                                    <!-- 답글 내용 (일반 모드) -->
                                    <p v-else class="text-gray-700 whitespace-pre-wrap">{{ reply.content }}</p>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 댓글이 없는 경우 -->
                    <div v-if="comments.length === 0" class="text-center py-12">
                        <i class="pi pi-comments text-gray-300 text-5xl mb-4"></i>
                        <p class="text-gray-500">첫 번째 댓글을 작성해보세요!</p>
                    </div>
                </div>
            </div>

            <!-- 리뷰 섹션 -->
            <div v-if="recipe.reviews && recipe.reviews.length > 0" class="bg-white rounded-2xl shadow-lg p-8">
                <h2 class="text-3xl font-bold text-gray-800 mb-8 flex items-center">
                    <i class="pi pi-star mr-3 text-yellow-500"></i>
                    리뷰 ({{ recipe.stats?.totalReviews || 0 }})
                </h2>
                
                <div class="space-y-6">
                    <div 
                        v-for="review in recipe.reviews" 
                        :key="review.id"
                        class="border border-gray-200 rounded-lg p-6"
                    >
                        <div class="flex items-start justify-between mb-4">
                            <div class="flex items-center space-x-3">
                                <div class="w-10 h-10 bg-gray-300 rounded-full flex items-center justify-center">
                                    <i class="pi pi-user text-gray-600"></i>
                                </div>
                                <div>
                                    <div class="font-medium text-gray-800">{{ review.memberName }}</div>
                                    <div class="text-sm text-gray-500">{{ formatDate(review.createdAt) }}</div>
                                </div>
                            </div>
                            
                            <!-- 별점 -->
                            <div class="flex items-center space-x-1">
                                <i 
                                    v-for="star in 5" 
                                    :key="star"
                                    :class="star <= review.rating ? 'pi pi-star-fill text-yellow-400' : 'pi pi-star text-gray-300'"
                                ></i>
                                <span class="ml-2 text-gray-600">{{ review.rating }}/5</span>
                            </div>
                        </div>
                        
                        <p class="text-gray-700">{{ review.content }}</p>
                    </div>
                </div>
            </div>
        </div>

        <!-- 이미지 모달 -->
        <div 
            v-if="showImageModal" 
            @click="closeImageModal"
            class="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center z-50"
        >
            <div class="relative max-w-4xl max-h-full p-4">
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
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { httpJson } from '@/utils/http';
import { fetchMemberInfo } from '@/utils/auth';

const route = useRoute();
const router = useRouter();

// 반응형 데이터
const loading = ref(true);
const error = ref<string | null>(null);
const recipe = ref<any>(null);
const comments = ref<any[]>([]);
const newComment = ref('');
const replyContent = ref('');
const replyingToCommentId = ref<number | null>(null);
const editingCommentId = ref<number | null>(null);
const editingContent = ref('');
const isLiked = ref(false);
const showImageModal = ref(false);
const selectedImage = ref<any>(null);
const selectedImageIndex = ref(0);

// 현재 로그인한 사용자 ID (TODO: 실제로는 auth store에서 가져와야 함)
const currentMemberInfo = ref<any | null>(null);
const currentMemberId = ref<number | null>(null);

// 계산된 속성
const mainImage = computed(() => {
    if (!recipe.value?.images) return null;
    return recipe.value.images.find((img: any) => img.isMainImage) || recipe.value.images[0];
});

// cookingTips에서 각 항목 추출
const cookingTipsData = computed(() => {
    if (!recipe.value?.cookingTips || !Array.isArray(recipe.value.cookingTips)) {
        return { servings: null, cookingTime: null, difficulty: null };
    }
    
    const servingsTip = recipe.value.cookingTips.find((tip: any) => tip.codeId === 'SERVINGS');
    const cookingTimeTip = recipe.value.cookingTips.find((tip: any) => tip.codeId === 'COOKING_TIME');
    const difficultyTip = recipe.value.cookingTips.find((tip: any) => tip.codeId === 'DIFFICULTY');
    
    return {
        servings: servingsTip?.detailName || null,
        cookingTime: cookingTimeTip?.detailName || null,
        difficulty: difficultyTip ? parseInt(difficultyTip.detailName || '0', 10) : null
    };
});

// 메서드
const fetchRecipeDetail = async () => {
    try {
        loading.value = true;
        error.value = null;
        
        const recipeId = route.params.id;
        const response = await httpJson(
            import.meta.env.VITE_API_BASE_URL_COOK,
            `/api/recipe/${recipeId}`,
            { method: 'GET' }
        );
        
        recipe.value = response;
        
        // 찜 여부 확인
        if (currentMemberId.value) {
            await checkFavoriteStatus();
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

const checkFavoriteStatus = async () => {
    try {
        const recipeId = route.params.id;
        const response = await httpJson(
            import.meta.env.VITE_API_BASE_URL_COOK,
            `/api/recipe/favorites/check?memberId=${currentMemberId.value}&recipeId=${recipeId}`,
            { method: 'GET' }
        );
        
        isLiked.value = response.isFavorite;
    } catch (err) {
        console.error('찜 여부 확인 실패:', err);
    }
};

const fetchComments = async () => {
    try {
        const recipeId = route.params.id;
        const response = await httpJson(
            import.meta.env.VITE_API_BASE_URL_COOK,
            `/api/recipe/comments/${recipeId}`,
            { method: 'GET' }
        );
        
        comments.value = response;
    } catch (err) {
        console.error('Comments fetch error:', err);
    }
};

const goBack = () => {
    router.back();
};

const toggleLike = async () => {
    if (!currentMemberId.value) {
        alert('로그인이 필요합니다.');
        return;
    }
    
    try {
        const recipeId = route.params.id;
        const response = await httpJson(
            import.meta.env.VITE_API_BASE_URL_COOK,
            `/api/recipe/favorites/toggle?memberId=${currentMemberId.value}&recipeId=${recipeId}`,
            { method: 'PUT' }
        );
        
        isLiked.value = response.isFavorite;
        
        if (response.isFavorite) {
            // 찜 추가됨
        } else {
            // 찜 해제됨
        }
    } catch (err) {
        console.error('찜 토글 실패:', err);
        alert('찜 기능을 사용할 수 없습니다.');
    }
};

const shareRecipe = () => {
    if (navigator.share) {
        navigator.share({
            title: recipe.value.title,
            text: recipe.value.description,
            url: window.location.href
        });
    } else {
        // 클립보드에 URL 복사
        navigator.clipboard.writeText(window.location.href);
        alert('링크가 클립보드에 복사되었습니다.');
    }
};

const submitComment = async () => {
    if (!newComment.value.trim()) return;
    
    try {
        const recipeId = route.params.id;
        await httpJson(
            import.meta.env.VITE_API_BASE_URL_COOK,
            `/api/recipe/comments/${recipeId}`,
            {
                method: 'POST',
                body: JSON.stringify({
                    memberId: currentMemberId.value,
                    content: newComment.value,
                    parentId: null
                })
            }
        );
        
        newComment.value = '';
        
        // 댓글 목록 다시 불러오기
        await fetchComments();
    } catch (err) {
        console.error('Comment submission error:', err);
        alert('댓글 작성 중 오류가 발생했습니다.');
    }
};

const submitReply = async (parentId: number) => {
    if (!replyContent.value.trim()) return;
    
    try {
        const recipeId = route.params.id;
        await httpJson(
            import.meta.env.VITE_API_BASE_URL_COOK,
            `/api/recipe/comments/${recipeId}`,
            {
                method: 'POST',
                body: JSON.stringify({
                    memberId: currentMemberId.value,
                    content: replyContent.value,
                    parentId: parentId
                })
            }
        );
        
        replyContent.value = '';
        replyingToCommentId.value = null;
        
        // 댓글 목록 다시 불러오기
        await fetchComments();
    } catch (err) {
        console.error('Reply submission error:', err);
        alert('답글 작성 중 오류가 발생했습니다.');
    }
};

const toggleReplyForm = (commentId: number) => {
    if (replyingToCommentId.value === commentId) {
        replyingToCommentId.value = null;
        replyContent.value = '';
    } else {
        replyingToCommentId.value = commentId;
        replyContent.value = '';
    }
};

const cancelReply = () => {
    replyingToCommentId.value = null;
    replyContent.value = '';
};

const startEditComment = (comment: any) => {
    editingCommentId.value = comment.id;
    editingContent.value = comment.content;
};

const cancelEditComment = () => {
    editingCommentId.value = null;
    editingContent.value = '';
};

const updateComment = async (commentId: number) => {
    if (!editingContent.value.trim()) return;
    
    try {
        await httpJson(
            import.meta.env.VITE_API_BASE_URL_COOK,
            `/api/recipe/comments/${commentId}`,
            {
                method: 'PUT',
                body: JSON.stringify({
                    memberId: currentMemberId.value,
                    content: editingContent.value
                })
            }
        );
        
        editingCommentId.value = null;
        editingContent.value = '';
        
        // 댓글 목록 다시 불러오기
        await fetchComments();
    } catch (err) {
        console.error('Comment update error:', err);
        alert('댓글 수정 중 오류가 발생했습니다.');
    }
};

const deleteComment = async (commentId: number) => {
    if (!confirm('정말 삭제하시겠습니까?')) return;
    
    try {
        await httpJson(
            import.meta.env.VITE_API_BASE_URL_COOK,
            `/api/recipe/comments/${commentId}?memberId=${currentMemberId.value}`,
            { method: 'DELETE' }
        );
        
        // 댓글 목록 다시 불러오기
        await fetchComments();
    } catch (err) {
        console.error('Comment deletion error:', err);
        alert('댓글 삭제 중 오류가 발생했습니다.');
    }
};

const isMyComment = (comment: any) => {
    return comment.memberId === currentMemberId.value;
};

const openImageModal = (image: any, index: number) => {
    selectedImage.value = image;
    selectedImageIndex.value = index;
    showImageModal.value = true;
};

const closeImageModal = () => {
    showImageModal.value = false;
    selectedImage.value = null;
    selectedImageIndex.value = 0;
};

const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
};

// 생명주기
onMounted(() => {
    fetchRecipeDetail();
    fetchMemberInfo().then((memberInfo) => {
        if (memberInfo) {
            currentMemberInfo.value = memberInfo;
            currentMemberId.value = memberInfo.id;
        }
    });
});
</script>

<style scoped>
/* 커스텀 스타일 */
.animate-spin {
    animation: spin 1s linear infinite;
}

@keyframes spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
}
</style>
