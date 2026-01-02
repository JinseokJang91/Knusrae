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
                <div class="relative w-full h-96 bg-white">
                    <img 
                        v-if="mainImage" 
                        :src="mainImage.url" 
                        :alt="recipe.title"
                        class="w-full mx-auto h-full object-cover"
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

                            <!-- 레시피 상세 정보-->
                            <div v-if="cookingTipsData.servings || cookingTipsData.cookingTime || cookingTipsData.difficulty" class="flex items-center justify-between gap-6 mb-4 p-4 bg-gray-50 rounded-lg">
                                <!-- 요리 정보 (cookingTips) -->
                                <div class="flex flex-wrap gap-6">
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
                                        <span class="text-gray-700 font-medium">{{ cookingTipsData.difficulty }}</span>
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
                                        <div class="text-2xl font-bold text-red-600">{{ recipe.stats?.favoriteCount || 0 }}</div>
                                        <div class="text-sm">찜</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 작성자 정보 -->
                    <div class="flex items-center justify-between py-4 border-t border-gray-200">
                        <div class="flex items-center space-x-3">
                            <div class="w-12 h-12 bg-gray-300 rounded-full flex items-center justify-center overflow-hidden">
                                <img 
                                    v-if="recipe.memberProfileImage" 
                                    :src="recipe.memberProfileImage" 
                                    alt="작성자 프로필" 
                                    class="w-full h-full object-cover"
                                />
                                <i v-else class="pi pi-user text-gray-600"></i>
                            </div>
                            <div>
                                <div class="text-lg font-medium text-gray-800">{{ recipe.memberNickname || recipe.memberName }}</div>
                                <!-- <div class="text-sm text-gray-500">{{ formatDate(recipe.createdAt) }}</div> -->
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

            <!-- 준비물 섹션 -->
            <div v-if="recipe.ingredientGroups && recipe.ingredientGroups.length > 0" class="bg-white rounded-2xl shadow-lg p-8 mb-8">
                <h2 class="text-3xl font-bold text-gray-800 mb-8 flex items-center">
                    <i class="pi pi-shopping-cart mr-3 text-orange-500"></i>
                    준비물
                </h2>
                
                <div class="space-y-6">
                    <div 
                        v-for="(group, groupIndex) in recipe.ingredientGroups" 
                        :key="group.id"
                        class="bg-gray-50 rounded-xl p-6"
                    >
                        <!-- 그룹 제목 -->
                        <div class="flex items-center mb-4">
                            <div class="w-8 h-8 bg-orange-500 text-white rounded-full flex items-center justify-center text-sm font-bold flex-shrink-0 mr-3">
                                {{ groupIndex + 1 }}
                            </div>
                            <h3 class="text-xl font-semibold text-gray-800">
                                {{ group.customTypeName || group.detailName || '재료' }}
                            </h3>
                        </div>

                        <!-- 항목 목록 -->
                        <div v-if="group.items && group.items.length > 0" class="grid grid-cols-1 md:grid-cols-2 gap-3">
                            <div 
                                v-for="item in group.items" 
                                :key="item.id"
                                class="flex items-center p-3 bg-white rounded-lg border border-gray-200"
                            >
                                <i class="pi pi-circle-fill text-orange-400 text-xs mr-3"></i>
                                <span class="text-gray-800 text-lg font-medium flex-1">{{ item.name }}</span>
                                <span class="text-gray-600 text-lg ml-2">
                                    {{ item.quantity }}{{ item.customUnitName || item.detailName }}
                                </span>
                            </div>
                        </div>

                        <!-- 항목이 없는 경우 -->
                        <div v-else class="text-gray-500 text-center py-4">
                            항목이 없습니다.
                        </div>
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
                                <div class="flex items-start gap-3">
                                    <div class="w-10 h-10 bg-blue-500 text-white rounded-full flex items-center justify-center text-lg font-bold flex-shrink-0">
                                        {{ index + 1 }}
                                    </div>
                                    <p class="text-gray-800 text-lg leading-relaxed whitespace-pre-line">
                                        {{ step.description }}
                                    </p>
                                </div>
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
                        @click.stop="openImageModal(image, index)"
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
            <div id="comments" class="bg-white rounded-2xl shadow-lg p-8 mb-8">
                <h2 class="text-3xl font-bold text-gray-800 mb-8 flex items-center">
                    <i class="pi pi-comments mr-3 text-purple-500"></i>
                    댓글 ({{ comments.length }})
                </h2>
                
                <!-- 댓글 작성 -->
                <div class="mb-6">
                    <!-- 로그인 상태: 댓글 작성 폼 -->
                    <div v-if="isLoggedIn" class="flex space-x-4">
                        <div class="w-10 h-10 bg-gray-300 rounded-full flex items-center justify-center flex-shrink-0 overflow-hidden">
                            <img 
                                v-if="authStore.memberProfileImage" 
                                :src="authStore.memberProfileImage" 
                                alt="프로필" 
                                class="w-full h-full object-cover"
                            />
                            <i v-else class="pi pi-user text-gray-600"></i>
                        </div>
                        <div class="flex-1">
                            <textarea 
                                v-model="newComment"
                                @focus="focusCommentTextarea"
                                :disabled="isRecipeAuthor"
                                :placeholder="isRecipeAuthor ? '작성자는 답글만 작성이 가능합니다' : '댓글을 작성해주세요...'"
                                class="w-full p-4 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
                                :class="{ 'bg-gray-100 cursor-not-allowed': isRecipeAuthor }"
                                rows="3"
                            ></textarea>
                            
                            <!-- 이미지 미리보기 -->
                            <div v-if="newCommentImagePreview" class="mt-2 relative inline-block">
                                <img 
                                    :src="newCommentImagePreview" 
                                    alt="미리보기" 
                                    class="w-24 h-24 object-cover rounded-lg border border-gray-300"
                                />
                                <button 
                                    @click="removeCommentImage"
                                    class="absolute -top-2 -right-2 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center hover:bg-red-600"
                                >
                                    <i class="pi pi-times text-xs"></i>
                                </button>
                            </div>
                            
                            <div class="flex justify-between items-center mt-2">
                                <label class="cursor-pointer" :class="{ 'opacity-50 cursor-not-allowed': isRecipeAuthor }">
                                    <input 
                                        type="file" 
                                        accept="image/*" 
                                        @change="handleCommentImageSelect"
                                        :disabled="isRecipeAuthor"
                                        class="hidden"
                                    />
                                    <div class="flex items-center space-x-2 px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors">
                                        <i class="pi pi-image"></i>
                                        <span>이미지 첨부</span>
                                    </div>
                                </label>
                                <button 
                                    @click="submitComment"
                                    :disabled="!newComment.trim() || isRecipeAuthor"
                                    class="px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                >
                                    댓글 작성
                                </button>
                            </div>
                        </div>
                    </div>
                    
                    <!-- 비로그인 상태: 안내 메시지 -->
                    <div v-else class="p-6 bg-gray-50 rounded-lg border border-gray-200 text-center">
                        <i class="pi pi-lock text-gray-400 text-3xl mb-2"></i>
                        <p class="text-gray-600 mb-3">댓글을 작성하려면 로그인이 필요합니다.</p>
                        <button 
                            @click="router.push({ path: '/auth/login', query: { redirect: route.fullPath } })"
                            class="px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
                        >
                            로그인하기
                        </button>
                    </div>
                </div>

                <!-- 댓글 목록 -->
                <div class="space-y-6">
                    <div 
                        v-for="comment in comments" 
                        :key="comment.id"
                        class="space-y-4"
                    >
                        <!-- 최상위 댓글 -->
                        <div class="flex space-x-4 p-4 bg-gray-50 rounded-lg border-l-4 border-blue-500">
                            <div class="w-10 h-10 bg-gray-300 rounded-full flex items-center justify-center flex-shrink-0 overflow-hidden">
                                <img 
                                    v-if="comment.memberProfileImage" 
                                    :src="comment.memberProfileImage" 
                                    alt="프로필" 
                                    class="w-full h-full object-cover"
                                />
                                <i v-else class="pi pi-user text-gray-600"></i>
                            </div>
                            <div class="flex-1 min-w-0">
                                <div class="flex items-center justify-between mb-2">
                                    <div class="flex items-center space-x-2">
                                        <span class="font-medium text-gray-800">
                                            {{ comment.memberNickname || comment.memberName }}
                                        </span>
                                        <span class="text-sm text-gray-500">{{ formatDate(comment.createdAt) }}</span>
                                        <span v-if="comment.updatedAt && comment.updatedAt !== comment.createdAt" class="text-xs text-gray-400">(수정됨)</span>
                                    </div>
                                    <div class="flex items-center space-x-2">
                                        <template v-if="isMyComment(comment)">
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
                                        </template>
                                        <button 
                                            v-if="!isMyComment(comment)"
                                            @click="toggleReplyForm(comment)"
                                            class="text-sm text-gray-800 hover:text-gray-600 font-medium"
                                        >
                                            답글
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
                                    
                                    <!-- 이미지 미리보기 (수정 모드) -->
                                    <div v-if="editingImagePreview" class="mb-2 relative inline-block">
                                        <img 
                                            :src="editingImagePreview" 
                                            alt="미리보기" 
                                            class="w-24 h-24 object-cover rounded-lg border border-gray-300"
                                        />
                                        <button 
                                            @click="removeEditImage"
                                            class="absolute -top-2 -right-2 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center hover:bg-red-600"
                                        >
                                            <i class="pi pi-times text-xs"></i>
                                        </button>
                                    </div>
                                    
                                    <div class="flex justify-between items-center">
                                        <label class="cursor-pointer">
                                            <input 
                                                type="file" 
                                                accept="image/*" 
                                                @change="handleEditImageSelect"
                                                class="hidden"
                                            />
                                            <div class="flex items-center space-x-2 px-3 py-1 text-sm bg-gray-100 text-gray-700 rounded hover:bg-gray-200 transition-colors">
                                                <i class="pi pi-image text-sm"></i>
                                                <span>이미지 변경</span>
                                            </div>
                                        </label>
                                        <div class="flex space-x-2">
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
                                </div>
                                <!-- 댓글 내용 (일반 모드) -->
                                <div v-else>
                                    <div class="flex items-start gap-4">
                                        <p class="text-gray-700 mb-2 whitespace-pre-wrap flex-1">{{ comment.content }}</p>
                                        <!-- 댓글 이미지 -->
                                        <div 
                                            v-if="comment.imageUrl" 
                                            @click="openImageModal({url: comment.imageUrl}, 0)"
                                            class="w-20 h-20 flex-shrink-0 cursor-pointer rounded-lg overflow-hidden border border-gray-200 hover:opacity-80 transition-opacity"
                                        >
                                            <img 
                                                :src="comment.imageUrl" 
                                                alt="댓글 이미지" 
                                                class="w-full h-full object-cover"
                                            />
                                        </div>
                                    </div>
                                    
                                    <!-- 답글 펼치기/접기 버튼 -->
                                    <button 
                                        v-if="comment.replies && comment.replies.length > 0"
                                        @click="toggleRepliesVisibility(comment.id)"
                                        class="text-sm text-gray-600 hover:text-gray-800 font-medium mt-2"
                                    >
                                        <i :class="expandedComments.has(comment.id) ? 'pi pi-chevron-up' : 'pi pi-chevron-down'" class="mr-1"></i>
                                        {{ expandedComments.has(comment.id) ? '접기' : `답글 (${comment.replies.length})` }}
                                    </button>
                                </div>
                            </div>
                        </div>

                        <!-- 최상위 댓글에 대한 답글 작성 폼 (답글 목록 위) -->
                        <div v-if="replyingToCommentId === comment.id && !comment.parentId" class="ml-14 flex space-x-4 p-4 bg-blue-50 rounded-lg border-2 border-blue-300">
                            <div class="w-8 h-8 bg-gray-300 rounded-full flex items-center justify-center flex-shrink-0 overflow-hidden">
                                <img 
                                    v-if="authStore.memberProfileImage" 
                                    :src="authStore.memberProfileImage" 
                                    alt="프로필" 
                                    class="w-full h-full object-cover"
                                />
                                <i v-else class="pi pi-user text-gray-600 text-sm"></i>
                            </div>
                            <div class="flex-1">
                                <div class="text-xs text-blue-600 font-medium mb-2">
                                    <i class="pi pi-at mr-1"></i>{{ replyingToComment?.memberNickname || replyingToComment?.memberName }}님에게 답글 작성
                                </div>
                                <textarea 
                                    v-model="replyContent"
                                    placeholder="답글을 작성해주세요..."
                                    class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
                                    rows="2"
                                ></textarea>
                                
                                <!-- 이미지 미리보기 (답글) -->
                                <div v-if="replyImagePreview" class="mt-2 relative inline-block">
                                    <img 
                                        :src="replyImagePreview" 
                                        alt="미리보기" 
                                        class="w-24 h-24 object-cover rounded-lg border border-gray-300"
                                    />
                                    <button 
                                        @click="removeReplyImage"
                                        class="absolute -top-2 -right-2 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center hover:bg-red-600"
                                    >
                                        <i class="pi pi-times text-xs"></i>
                                    </button>
                                </div>
                                
                                <div class="flex justify-between items-center mt-2">
                                    <label class="cursor-pointer">
                                        <input 
                                            type="file" 
                                            accept="image/*" 
                                            @change="handleReplyImageSelect"
                                            class="hidden"
                                        />
                                        <div class="flex items-center space-x-2 px-3 py-1 text-sm bg-white text-gray-700 rounded hover:bg-gray-100 transition-colors border border-gray-300">
                                            <i class="pi pi-image text-sm"></i>
                                            <span>이미지 첨부</span>
                                        </div>
                                    </label>
                                    <div class="flex space-x-2">
                                        <button 
                                            @click="cancelReply"
                                            class="px-4 py-1 text-sm bg-gray-200 text-gray-700 rounded hover:bg-gray-300 transition-colors"
                                        >
                                            취소
                                        </button>
                                        <button 
                                            @click="submitReply(replyingToComment.parentId || replyingToComment.id)"
                                            :disabled="!replyContent.trim()"
                                            class="px-4 py-1 text-sm bg-blue-500 text-white rounded hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                        >
                                            답글 작성
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- 답글 목록 (펼쳐진 경우에만 표시) -->
                        <div v-if="comment.replies && comment.replies.length > 0 && expandedComments.has(comment.id)" class="ml-14 space-y-4">
                            <!-- 각 답글을 감싸는 컨테이너 -->
                            <div 
                                v-for="reply in comment.replies" 
                                :key="reply.id"
                                class="space-y-4"
                            >
                                <!-- 답글 내용 -->
                                <div class="flex space-x-4 p-4 bg-gray-100 rounded-lg">
                                    <div class="w-8 h-8 bg-gray-300 rounded-full flex items-center justify-center flex-shrink-0 overflow-hidden">
                                        <img 
                                            v-if="reply.memberProfileImage" 
                                            :src="reply.memberProfileImage" 
                                            alt="프로필" 
                                            class="w-full h-full object-cover"
                                        />
                                        <i v-else class="pi pi-user text-gray-600 text-sm"></i>
                                    </div>
                                    <div class="flex-1 min-w-0">
                                        <div class="flex items-center justify-between mb-2">
                                            <div class="flex items-center space-x-2">
                                                <span class="font-medium text-gray-800">
                                                    {{ reply.memberNickname || reply.memberName }}
                                                </span>
                                                <span class="text-sm text-gray-500">{{ formatDate(reply.createdAt) }}</span>
                                                <span v-if="reply.updatedAt && reply.updatedAt !== reply.createdAt" class="text-xs text-gray-400">(수정됨)</span>
                                            </div>
                                            <div class="flex items-center space-x-2">
                                                <template v-if="isMyComment(reply)">
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
                                                </template>
                                                <button 
                                                    v-if="!isMyComment(reply)"
                                                    @click="toggleReplyForm(reply)"
                                                    class="text-sm text-gray-800 hover:text-gray-600 font-medium"
                                                >
                                                    답글
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
                                            
                                            <!-- 이미지 미리보기 (수정 모드) -->
                                            <div v-if="editingImagePreview" class="mb-2 relative inline-block">
                                                <img 
                                                    :src="editingImagePreview" 
                                                    alt="미리보기" 
                                                    class="w-24 h-24 object-cover rounded-lg border border-gray-300"
                                                />
                                                <button 
                                                    @click="removeEditImage"
                                                    class="absolute -top-2 -right-2 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center hover:bg-red-600"
                                                >
                                                    <i class="pi pi-times text-xs"></i>
                                                </button>
                                            </div>
                                            
                                            <div class="flex justify-between items-center">
                                                <label class="cursor-pointer">
                                                    <input 
                                                        type="file" 
                                                        accept="image/*" 
                                                        @change="handleEditImageSelect"
                                                        class="hidden"
                                                    />
                                                    <div class="flex items-center space-x-2 px-3 py-1 text-sm bg-gray-100 text-gray-700 rounded hover:bg-gray-200 transition-colors">
                                                        <i class="pi pi-image text-sm"></i>
                                                        <span>이미지 변경</span>
                                                    </div>
                                                </label>
                                                <div class="flex space-x-2">
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
                                        </div>
                                        <!-- 답글 내용 (일반 모드) -->
                                        <div v-else>
                                            <div class="flex items-start gap-4">
                                                <!-- 답글 내용 (닉네임 prefix 강조) -->
                                                <p class="text-gray-700 whitespace-pre-wrap flex-1">
                                                    <template v-if="reply.content.startsWith('@')">
                                                        <span class="font-bold text-blue-600">{{ reply.content.split(' ')[0] }}</span>
                                                        {{ reply.content.substring(reply.content.indexOf(' ')) }}
                                                    </template>
                                                    <template v-else>
                                                        {{ reply.content }}
                                                    </template>
                                                </p>
                                                <!-- 답글 이미지 -->
                                                <div 
                                                    v-if="reply.imageUrl" 
                                                    @click="openImageModal({url: reply.imageUrl}, 0)"
                                                    class="w-20 h-20 flex-shrink-0 cursor-pointer rounded-lg overflow-hidden border border-gray-200 hover:opacity-80 transition-opacity"
                                                >
                                                    <img 
                                                        :src="reply.imageUrl" 
                                                        alt="답글 이미지" 
                                                        class="w-full h-full object-cover"
                                                    />
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            
                                <!-- 답글에 대한 답글 작성 폼 -->
                                <div v-if="replyingToCommentId === reply.id" class="flex space-x-4 p-4 bg-blue-50 rounded-lg border-2 border-blue-300">
                                    <div class="w-8 h-8 bg-gray-300 rounded-full flex items-center justify-center flex-shrink-0 overflow-hidden">
                                        <img 
                                            v-if="authStore.memberProfileImage" 
                                            :src="authStore.memberProfileImage" 
                                            alt="프로필" 
                                            class="w-full h-full object-cover"
                                        />
                                        <i v-else class="pi pi-user text-gray-600 text-sm"></i>
                                    </div>
                                    <div class="flex-1">
                                        <div class="text-xs text-blue-600 font-medium mb-2">
                                            <i class="pi pi-at mr-1"></i>{{ replyingToComment?.memberNickname || replyingToComment?.memberName }}님에게 답글 작성
                                        </div>
                                        <textarea 
                                            v-model="replyContent"
                                            placeholder="답글을 작성해주세요..."
                                            class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
                                            rows="2"
                                        ></textarea>
                                        
                                        <!-- 이미지 미리보기 (답글) -->
                                        <div v-if="replyImagePreview" class="mt-2 relative inline-block">
                                            <img 
                                                :src="replyImagePreview" 
                                                alt="미리보기" 
                                                class="w-24 h-24 object-cover rounded-lg border border-gray-300"
                                            />
                                            <button 
                                                @click="removeReplyImage"
                                                class="absolute -top-2 -right-2 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center hover:bg-red-600"
                                            >
                                                <i class="pi pi-times text-xs"></i>
                                            </button>
                                        </div>
                                        
                                        <div class="flex justify-between items-center mt-2">
                                            <label class="cursor-pointer">
                                                <input 
                                                    type="file" 
                                                    accept="image/*" 
                                                    @change="handleReplyImageSelect"
                                                    class="hidden"
                                                />
                                                <div class="flex items-center space-x-2 px-3 py-1 text-sm bg-white text-gray-700 rounded hover:bg-gray-100 transition-colors border border-gray-300">
                                                    <i class="pi pi-image text-sm"></i>
                                                    <span>이미지 첨부</span>
                                                </div>
                                            </label>
                                            <div class="flex space-x-2">
                                                <button 
                                                    @click="cancelReply"
                                                    class="px-4 py-1 text-sm bg-gray-200 text-gray-700 rounded hover:bg-gray-300 transition-colors"
                                                >
                                                    취소
                                                </button>
                                                <button 
                                                    @click="submitReply(replyingToComment.parentId || replyingToComment.id)"
                                                    :disabled="!replyContent.trim()"
                                                    class="px-4 py-1 text-sm bg-blue-500 text-white rounded hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                                >
                                                    답글 작성
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 댓글이 없는 경우 -->
                    <div v-if="comments.length === 0" class="text-center py-12">
                        <i class="pi pi-comments text-gray-300 text-5xl mb-4"></i>
                        <p class="text-gray-500">첫 번째 댓글을 작성해보세요!</p>
                    </div>
                    
                    <!-- Pagination -->
                    <div v-if="totalPages > 1" class="flex justify-center items-center gap-2 mt-8">
                        <button 
                            @click="loadPage(currentPage - 1)"
                            :disabled="currentPage === 0"
                            class="px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                        >
                            <i class="pi pi-chevron-left"></i>
                        </button>
                        
                        <div class="flex gap-2">
                            <button 
                                v-for="page in totalPages" 
                                :key="page"
                                @click="loadPage(page - 1)"
                                :class="currentPage === page - 1 ? 'bg-blue-500 text-white' : 'bg-white text-gray-700 hover:bg-gray-50'"
                                class="px-4 py-2 border border-gray-300 rounded-lg transition-colors"
                            >
                                {{ page }}
                            </button>
                        </div>
                        
                        <button 
                            @click="loadPage(currentPage + 1)"
                            :disabled="currentPage === totalPages - 1"
                            class="px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                        >
                            <i class="pi pi-chevron-right"></i>
                        </button>
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
import { ref, onMounted, computed, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { httpJson, httpForm } from '@/utils/http';
import { useConfirm } from 'primevue/useconfirm';
import { useAuthStore } from '@/stores/authStore';
import { useToast } from 'primevue/usetoast';
import type { RecipeDetail, RecipeComment, RecipeImage } from '@/types/recipe';
import { handleApiCall, handleApiCallVoid } from '@/utils/errorHandler';

const route = useRoute();
const router = useRouter();
const confirm = useConfirm();
const authStore = useAuthStore();
const toast = useToast();

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
    return recipe.value && currentMemberId.value && recipe.value.memberId === currentMemberId.value;
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
        difficultyText = difficultyCodes.value.get(detailCodeId) || detailCodeId;
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
            import.meta.env.VITE_API_BASE_URL_COOK,
            `/api/common-codes?codeGroup=COOKINGTIP`,
            { method: 'GET' }
        );
        
        const codes = Array.isArray(response) ? response : [];
        const difficultyCode = codes.find((code: { codeId: string; details?: Array<{ detailCodeId: string; codeName: string }> }) => code.codeId === 'DIFFICULTY');
        
        if (difficultyCode && difficultyCode.details) {
            difficultyCodes.value.clear();
            difficultyCode.details.forEach((detail) => {
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

const fetchComments = async (page: number = 0) => {
    try {
        const recipeId = route.params.id;
        const response = await httpJson(
            import.meta.env.VITE_API_BASE_URL_COOK,
            `/api/recipe/comments/${recipeId}/page?page=${page}&size=${pageSize}`,
            { method: 'GET' }
        );
        
        comments.value = response.comments;
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
    if (expandedComments.value.has(commentId)) {
        expandedComments.value.delete(commentId);
    } else {
        expandedComments.value.add(commentId);
    }
};

const goBack = () => {
    router.back();
};

const toggleLike = async () => {
    // 로그인 확인
    if (!isLoggedIn.value || !currentMemberId.value) {
        toast.add({
            severity: 'warn',
            summary: '로그인 필요',
            detail: '찜 기능을 사용하려면 로그인이 필요합니다.',
            life: 3000
        });
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
        toast.add({
            severity: 'info',
            summary: '링크 복사',
            detail: '링크가 클립보드에 복사되었습니다.',
            life: 3000
        });
        alert('링크가 클립보드에 복사되었습니다.');
    }
};

const handleCommentImageSelect = (event: Event) => {
    const target = event.target as HTMLInputElement;
    const file = target.files?.[0];
    if (file) {
        if (!file.type.startsWith('image/')) {
            toast.add({
                severity: 'error',
                summary: '파일 형식 오류',
                detail: '이미지 파일만 업로드할 수 있습니다.',
                life: 3000
            });
            return;
        }
        if (file.size > 5 * 1024 * 1024) {
            toast.add({
                severity: 'error',
                summary: '파일 크기 초과',
                detail: '이미지 크기는 5MB 이하여야 합니다.',
                life: 3000
            });
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
        toast.add({
            severity: 'warn',
            summary: '로그인 필요',
            detail: '댓글 기능을 사용하려면 로그인이 필요합니다.',
            life: 3000
        });
        return;
    }
    
    // 레시피 작성자는 댓글 작성 불가
    if (isRecipeAuthor.value) {
        alert('작성자는 답글만 작성이 가능합니다');
        return;
    }
    
    try {
        const recipeId = route.params.id;
        
        // 이미지가 있으면 multipart/form-data로 전송
        if (newCommentImage.value) {
            const formData = new FormData();
            formData.append('memberId', currentMemberId.value.toString());
            formData.append('content', newComment.value);
            formData.append('image', newCommentImage.value);
            
            await httpForm(
                import.meta.env.VITE_API_BASE_URL_COOK,
                `/api/recipe/comments/${recipeId}/with-image`,
                formData,
                { method: 'POST' }
            );
        } else {
            // 이미지가 없으면 JSON으로 전송
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
        }
        
        newComment.value = '';
        newCommentImage.value = null;
        newCommentImagePreview.value = null;
        
        // 댓글 목록 다시 불러오기 (첫 페이지로)
        await fetchComments(0);
    } catch (err) {
        console.error('Comment submission error:', err);
        alert('댓글 작성 중 오류가 발생했습니다.');
    }
};

const focusCommentTextarea = () => {
    // 레시피 작성자는 댓글 작성 불가
    if (isRecipeAuthor.value) {
        alert('작성자는 답글만 작성이 가능합니다');
    }
};

const handleReplyImageSelect = (event: Event) => {
    const target = event.target as HTMLInputElement;
    const file = target.files?.[0];
    if (file) {
        if (!file.type.startsWith('image/')) {
            toast.add({
                severity: 'error',
                summary: '파일 형식 오류',
                detail: '이미지 파일만 업로드할 수 있습니다.',
                life: 3000
            });
            return;
        }
        if (file.size > 5 * 1024 * 1024) {
            toast.add({
                severity: 'error',
                summary: '파일 크기 초과',
                detail: '이미지 크기는 5MB 이하여야 합니다.',
                life: 3000
            });
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

const submitReply = async (parentId: number) => {
    if (!replyContent.value.trim()) return;
    
    // 로그인 확인
    if (!isLoggedIn.value || !currentMemberId.value) {
        toast.add({
            severity: 'warn',
            summary: '로그인 필요',
            detail: '댓글 기능을 사용하려면 로그인이 필요합니다.',
            life: 3000
        });
        return;
    }
    
    try {
        const recipeId = route.params.id;
        
        // 부모 댓글 닉네임 prefix 추가
        let contentWithPrefix = replyContent.value;
        if (replyingToComment.value) {
            const parentNickname = replyingToComment.value.memberNickname || replyingToComment.value.memberName;
            contentWithPrefix = `@${parentNickname} ${replyContent.value}`;
        }
        
        // 이미지가 있으면 multipart/form-data로 전송
        if (replyImage.value) {
            const formData = new FormData();
            formData.append('memberId', currentMemberId.value.toString());
            formData.append('content', contentWithPrefix);
            formData.append('parentId', parentId.toString());
            formData.append('image', replyImage.value);
            
            await httpForm(
                import.meta.env.VITE_API_BASE_URL_COOK,
                `/api/recipe/comments/${recipeId}/with-image`,
                formData,
                { method: 'POST' }
            );
        } else {
            // 이미지가 없으면 JSON으로 전송
            await httpJson(
                import.meta.env.VITE_API_BASE_URL_COOK,
                `/api/recipe/comments/${recipeId}`,
                {
                    method: 'POST',
                    body: JSON.stringify({
                        memberId: currentMemberId.value,
                        content: contentWithPrefix,
                        parentId: parentId
                    })
                }
            );
        }
        
        replyContent.value = '';
        replyImage.value = null;
        replyImagePreview.value = null;
        replyingToCommentId.value = null;
        replyingToComment.value = null;
        
        // 댓글 목록 다시 불러오기
        await fetchComments(currentPage.value);
    } catch (err) {
        console.error('Reply submission error:', err);
        alert('답글 작성 중 오류가 발생했습니다.');
    }
};

const toggleReplyForm = (comment: any) => {
    // 로그인 확인
    if (!isLoggedIn.value) {
        toast.add({
            severity: 'warn',
            summary: '로그인 필요',
            detail: '댓글 기능을 사용하려면 로그인이 필요합니다.',
            life: 3000
        });
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
            expandedComments.value.add(rootCommentId);
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

const startEditComment = (comment: any) => {
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
            toast.add({
                severity: 'error',
                summary: '파일 형식 오류',
                detail: '이미지 파일만 업로드할 수 있습니다.',
                life: 3000
            });
            return;
        }
        if (file.size > 5 * 1024 * 1024) {
            toast.add({
                severity: 'error',
                summary: '파일 크기 초과',
                detail: '이미지 크기는 5MB 이하여야 합니다.',
                life: 3000
            });
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
                import.meta.env.VITE_API_BASE_URL_COOK,
                `/api/recipe/comments/${commentId}/with-image`,
                formData,
                { method: 'PUT' }
            );
        } else {
            // 이미지가 변경되지 않은 경우
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
        alert('댓글 수정 중 오류가 발생했습니다.');
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
            try {
                await httpJson(
                    import.meta.env.VITE_API_BASE_URL_COOK,
                    `/api/recipe/comments/${commentId}?memberId=${currentMemberId.value}`,
                    { method: 'DELETE' }
                );
                
                // 댓글 목록 다시 불러오기
                await fetchComments(currentPage.value);
            } catch (err) {
                console.error('Comment deletion error:', err);
                alert('댓글 삭제 중 오류가 발생했습니다.');
            } finally {
                loading.value = false;
            }
        },
        reject: () => {
            // 취소 시 아무것도 하지 않음
        }
    });
};

const isMyComment = (comment: any) => {
    return comment.memberId === currentMemberId.value;
};

const openImageModal = (image: any, index: number, event?: Event) => {
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
/* 커스텀 스타일 */
.animate-spin {
    animation: spin 1s linear infinite;
}

@keyframes spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
}
</style>
