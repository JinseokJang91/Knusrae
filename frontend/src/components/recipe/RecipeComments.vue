<script setup lang="ts">
import type { RecipeComment } from '@/types/recipe';

const props = defineProps<{
    comments: RecipeComment[];
    isLoggedIn: boolean;
    isRecipeAuthor: boolean;
    memberProfileImage: string | null | undefined;
    newComment: string;
    newCommentImagePreview: string | null;
    replyContent: string;
    replyImagePreview: string | null;
    replyingToCommentId: number | null;
    replyingToComment: RecipeComment | null;
    editingCommentId: number | null;
    editingContent: string;
    editingImagePreview: string | null;
    expandedComments: Set<number>;
    totalPages: number;
    currentPage: number;
    currentMemberId: number | null;
    formatDate: (dateString: string) => string;
}>();

defineEmits<{
    'update:newComment': [value: string];
    'update:replyContent': [value: string];
    'update:editingContent': [value: string];
    'submit-comment': [];
    'focus-comment-textarea': [];
    'comment-image-select': [event: Event];
    'remove-comment-image': [];
    'go-login': [];
    'submit-reply': [];
    'cancel-reply': [];
    'reply-image-select': [event: Event];
    'remove-reply-image': [];
    'toggle-reply-form': [comment: RecipeComment];
    'start-edit-comment': [comment: RecipeComment];
    'cancel-edit-comment': [];
    'edit-image-select': [event: Event];
    'remove-edit-image': [];
    'update-comment': [commentId: number];
    'delete-comment': [commentId: number];
    'toggle-replies-visibility': [commentId: number];
    'load-page': [page: number];
    'open-image': [payload: { url: string }];
    'go-to-member-profile': [memberId: number];
}>();

const isMyComment = (comment: RecipeComment) => comment.memberId === props.currentMemberId;
</script>

<template>
    <div id="comments" class="bg-white rounded-2xl shadow-lg p-8 mb-8">
        <h2 class="text-3xl font-bold text-gray-800 mb-8 flex items-center">
            <i class="pi pi-comments mr-3 text-gray-500"></i>
            댓글 ({{ comments.length }})
        </h2>

        <!-- 댓글 작성 -->
        <div class="mb-6">
            <div v-if="isLoggedIn" class="flex space-x-4">
                <div class="w-10 h-10 bg-orange-100 rounded-full flex items-center justify-center flex-shrink-0 overflow-hidden">
                    <img v-if="memberProfileImage" :src="memberProfileImage" alt="프로필" class="w-full h-full object-cover" />
                    <i v-else class="pi pi-user text-orange-600"></i>
                </div>
                <div class="flex-1">
                    <textarea
                        :value="newComment"
                        @input="$emit('update:newComment', ($event.target as HTMLTextAreaElement).value)"
                        @focus="$emit('focus-comment-textarea')"
                        :disabled="isRecipeAuthor"
                        :placeholder="isRecipeAuthor ? '작성자는 답글만 작성이 가능합니다' : '댓글을 작성해주세요...'"
                        class="w-full p-4 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-500 focus:border-transparent resize-none"
                        :class="{ 'bg-gray-100 cursor-not-allowed': isRecipeAuthor }"
                        rows="3"
                    ></textarea>
                    <div v-if="newCommentImagePreview" class="mt-2 relative inline-block">
                        <img :src="newCommentImagePreview" alt="미리보기" class="w-24 h-24 object-cover rounded-lg border border-gray-300" />
                        <button @click="$emit('remove-comment-image')" class="absolute -top-2 -right-2 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center hover:bg-red-600">
                            <i class="pi pi-times text-xs"></i>
                        </button>
                    </div>
                    <div class="flex justify-between items-center mt-2">
                        <label class="cursor-pointer" :class="{ 'opacity-50 cursor-not-allowed': isRecipeAuthor }">
                            <input type="file" accept="image/*" @change="$emit('comment-image-select', $event)" :disabled="isRecipeAuthor" class="hidden" />
                            <div class="flex items-center space-x-2 px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors">
                                <i class="pi pi-image"></i>
                                <span>이미지 첨부</span>
                            </div>
                        </label>
                        <button
                            @click="$emit('submit-comment')"
                            :disabled="!newComment.trim() || isRecipeAuthor"
                            class="px-6 py-2 bg-orange-500 text-white rounded-lg hover:bg-orange-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                        >
                            댓글 작성
                        </button>
                    </div>
                </div>
            </div>
            <div v-else class="p-6 bg-orange-50 rounded-lg border border-orange-200 text-center">
                <i class="pi pi-lock text-gray-400 text-3xl mb-2"></i>
                <p class="text-gray-600 mb-3">댓글을 작성하려면 로그인이 필요합니다.</p>
                <button @click="$emit('go-login')" class="px-6 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600 transition-colors">로그인하기</button>
            </div>
        </div>

        <!-- 댓글 목록 -->
        <div class="space-y-6">
            <div v-for="comment in comments" :key="comment.id" class="space-y-4">
                <!-- 최상위 댓글 -->
                <div class="flex space-x-4 p-4 bg-orange-50 rounded-lg border-l-4 border-orange-500">
                    <div
                        class="w-10 h-10 bg-orange-100 rounded-full flex items-center justify-center flex-shrink-0 overflow-hidden cursor-pointer hover:opacity-80 transition-opacity"
                        @click="comment.memberId && $emit('go-to-member-profile', comment.memberId)"
                    >
                        <img v-if="comment.memberProfileImage" :src="comment.memberProfileImage" alt="프로필" class="w-full h-full object-cover" />
                        <i v-else class="pi pi-user text-orange-600"></i>
                    </div>
                    <div class="flex-1 min-w-0">
                        <div class="flex items-center justify-between mb-2">
                            <div class="flex items-center space-x-2">
                                <span class="font-medium text-gray-800 cursor-pointer hover:text-primary-600 transition-colors" @click="comment.memberId && $emit('go-to-member-profile', comment.memberId)">{{
                                    comment.memberNickname || comment.memberName
                                }}</span>
                                <span class="text-sm text-gray-500">{{ formatDate(comment.createdAt) }}</span>
                                <span v-if="comment.updatedAt && comment.updatedAt !== comment.createdAt" class="text-xs text-gray-400">(수정됨)</span>
                            </div>
                            <div class="flex items-center space-x-2">
                                <template v-if="isMyComment(comment)">
                                    <button @click="$emit('start-edit-comment', comment)" class="text-sm text-gray-500 hover:text-gray-700">수정</button>
                                    <button @click="$emit('delete-comment', comment.id)" class="text-sm text-red-500 hover:text-red-700">삭제</button>
                                </template>
                                <button v-if="!isMyComment(comment)" @click="$emit('toggle-reply-form', comment)" class="text-sm text-gray-800 hover:text-gray-600 font-medium">답글</button>
                            </div>
                        </div>
                        <div v-if="editingCommentId === comment.id">
                            <textarea
                                :value="editingContent"
                                @input="$emit('update:editingContent', ($event.target as HTMLTextAreaElement).value)"
                                class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-500 focus:border-transparent resize-none mb-2"
                                rows="2"
                            ></textarea>
                            <div v-if="editingImagePreview" class="mb-2 relative inline-block">
                                <img :src="editingImagePreview" alt="미리보기" class="w-24 h-24 object-cover rounded-lg border border-gray-300" />
                                <button @click="$emit('remove-edit-image')" class="absolute -top-2 -right-2 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center hover:bg-red-600">
                                    <i class="pi pi-times text-xs"></i>
                                </button>
                            </div>
                            <div class="flex justify-between items-center">
                                <label class="cursor-pointer">
                                    <input type="file" accept="image/*" @change="$emit('edit-image-select', $event)" class="hidden" />
                                    <div class="flex items-center space-x-2 px-3 py-1 text-sm bg-gray-100 text-gray-700 rounded hover:bg-gray-200 transition-colors">
                                        <i class="pi pi-image text-sm"></i>
                                        <span>이미지 변경</span>
                                    </div>
                                </label>
                                <div class="flex space-x-2">
                                    <button @click="$emit('cancel-edit-comment')" class="px-4 py-1 text-sm bg-gray-200 text-gray-700 rounded hover:bg-gray-300 transition-colors">취소</button>
                                    <button
                                        @click="$emit('update-comment', comment.id)"
                                        :disabled="!editingContent.trim()"
                                        class="px-4 py-1 text-sm bg-gray-500 text-white rounded hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                    >
                                        수정 완료
                                    </button>
                                </div>
                            </div>
                        </div>
                        <div v-else>
                            <div class="flex items-start gap-4">
                                <p class="text-gray-700 mb-2 whitespace-pre-wrap flex-1">{{ comment.content }}</p>
                                <div v-if="comment.imageUrl" @click="$emit('open-image', { url: comment.imageUrl })" class="w-20 h-20 flex-shrink-0 cursor-pointer rounded-lg overflow-hidden border border-gray-200 hover:opacity-80 transition-opacity">
                                    <img :src="comment.imageUrl" alt="댓글 이미지" class="w-full h-full object-cover" />
                                </div>
                            </div>
                            <button v-if="comment.children && comment.children.length > 0" @click="$emit('toggle-replies-visibility', comment.id)" class="text-sm text-gray-600 hover:text-gray-800 font-medium mt-2">
                                <i :class="expandedComments.has(comment.id) ? 'pi pi-chevron-up' : 'pi pi-chevron-down'" class="mr-1"></i>
                                {{ expandedComments.has(comment.id) ? '접기' : `답글 (${comment.children.length})` }}
                            </button>
                        </div>
                    </div>
                </div>

                <!-- 답글 작성 폼 (최상위 댓글에 대한) -->
                <div v-if="replyingToCommentId === comment.id && !comment.parentId" class="ml-14 flex space-x-4 p-4 bg-orange-50 rounded-lg border-2 border-orange-200">
                    <div class="w-8 h-8 bg-orange-100 rounded-full flex items-center justify-center flex-shrink-0 overflow-hidden">
                        <img v-if="memberProfileImage" :src="memberProfileImage" alt="프로필" class="w-full h-full object-cover" />
                        <i v-else class="pi pi-user text-orange-600 text-sm"></i>
                    </div>
                    <div class="flex-1">
                        <div class="text-xs text-gray-600 font-medium mb-2"><i class="pi pi-at mr-1"></i>{{ replyingToComment ? replyingToComment.memberNickname || replyingToComment.memberName : '' }}님에게 답글 작성</div>
                        <textarea
                            :value="replyContent"
                            @input="$emit('update:replyContent', ($event.target as HTMLTextAreaElement).value)"
                            placeholder="답글을 작성해주세요..."
                            class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-500 focus:border-transparent resize-none"
                            rows="2"
                        ></textarea>
                        <div v-if="replyImagePreview" class="mt-2 relative inline-block">
                            <img :src="replyImagePreview" alt="미리보기" class="w-24 h-24 object-cover rounded-lg border border-gray-300" />
                            <button @click="$emit('remove-reply-image')" class="absolute -top-2 -right-2 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center hover:bg-red-600">
                                <i class="pi pi-times text-xs"></i>
                            </button>
                        </div>
                        <div class="flex justify-between items-center mt-2">
                            <label class="cursor-pointer">
                                <input type="file" accept="image/*" @change="$emit('reply-image-select', $event)" class="hidden" />
                                <div class="flex items-center space-x-2 px-3 py-1 text-sm bg-white text-gray-700 rounded hover:bg-gray-100 transition-colors border border-gray-300">
                                    <i class="pi pi-image text-sm"></i>
                                    <span>이미지 첨부</span>
                                </div>
                            </label>
                            <div class="flex space-x-2">
                                <button @click="$emit('cancel-reply')" type="button" class="px-4 py-1 text-sm bg-gray-200 text-gray-700 rounded hover:bg-gray-300 transition-colors">취소</button>
                                <button
                                    @click="$emit('submit-reply')"
                                    type="button"
                                    :disabled="!replyContent.trim() || !replyingToComment"
                                    class="px-4 py-1 text-sm bg-gray-500 text-white rounded hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                >
                                    답글 작성
                                </button>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 답글 목록 -->
                <div v-if="comment.children && comment.children.length > 0 && expandedComments.has(comment.id)" class="ml-14 space-y-4">
                    <div v-for="reply in comment.children" :key="reply.id" class="space-y-4">
                        <div class="flex space-x-4 p-4 bg-orange-100 rounded-lg border-l-4 border-orange-400">
                            <div
                                class="w-8 h-8 bg-orange-100 rounded-full flex items-center justify-center flex-shrink-0 overflow-hidden cursor-pointer hover:opacity-80 transition-opacity"
                                @click="reply.memberId && $emit('go-to-member-profile', reply.memberId)"
                            >
                                <img v-if="reply.memberProfileImage" :src="reply.memberProfileImage" alt="프로필" class="w-full h-full object-cover" />
                                <i v-else class="pi pi-user text-orange-600 text-sm"></i>
                            </div>
                            <div class="flex-1 min-w-0">
                                <div class="flex items-center justify-between mb-2">
                                    <div class="flex items-center space-x-2">
                                        <span class="font-medium text-gray-800 cursor-pointer hover:text-primary-600 transition-colors" @click="reply.memberId && $emit('go-to-member-profile', reply.memberId)">{{
                                            reply.memberNickname || reply.memberName
                                        }}</span>
                                        <span class="text-sm text-gray-500">{{ formatDate(reply.createdAt) }}</span>
                                        <span v-if="reply.updatedAt && reply.updatedAt !== reply.createdAt" class="text-xs text-gray-400">(수정됨)</span>
                                    </div>
                                    <div class="flex items-center space-x-2">
                                        <template v-if="isMyComment(reply)">
                                            <button @click="$emit('start-edit-comment', reply)" class="text-sm text-gray-500 hover:text-gray-700">수정</button>
                                            <button @click="$emit('delete-comment', reply.id)" class="text-sm text-red-500 hover:text-red-700">삭제</button>
                                        </template>
                                        <button v-if="!isMyComment(reply)" @click="$emit('toggle-reply-form', reply)" class="text-sm text-gray-800 hover:text-gray-600 font-medium">답글</button>
                                    </div>
                                </div>
                                <div v-if="editingCommentId === reply.id">
                                    <textarea
                                        :value="editingContent"
                                        @input="$emit('update:editingContent', ($event.target as HTMLTextAreaElement).value)"
                                        class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-500 focus:border-transparent resize-none mb-2"
                                        rows="2"
                                    ></textarea>
                                    <div v-if="editingImagePreview" class="mb-2 relative inline-block">
                                        <img :src="editingImagePreview" alt="미리보기" class="w-24 h-24 object-cover rounded-lg border border-gray-300" />
                                        <button @click="$emit('remove-edit-image')" class="absolute -top-2 -right-2 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center hover:bg-red-600">
                                            <i class="pi pi-times text-xs"></i>
                                        </button>
                                    </div>
                                    <div class="flex justify-between items-center">
                                        <label class="cursor-pointer">
                                            <input type="file" accept="image/*" @change="$emit('edit-image-select', $event)" class="hidden" />
                                            <div class="flex items-center space-x-2 px-3 py-1 text-sm bg-gray-100 text-gray-700 rounded hover:bg-gray-200 transition-colors">
                                                <i class="pi pi-image text-sm"></i>
                                                <span>이미지 변경</span>
                                            </div>
                                        </label>
                                        <div class="flex space-x-2">
                                            <button @click="$emit('cancel-edit-comment')" class="px-4 py-1 text-sm bg-gray-200 text-gray-700 rounded hover:bg-gray-300 transition-colors">취소</button>
                                            <button
                                                @click="$emit('update-comment', reply.id)"
                                                :disabled="!editingContent.trim()"
                                                class="px-4 py-1 text-sm bg-gray-500 text-white rounded hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                            >
                                                수정 완료
                                            </button>
                                        </div>
                                    </div>
                                </div>
                                <div v-else>
                                    <div class="flex items-start gap-4">
                                        <p class="text-gray-700 whitespace-pre-wrap flex-1">
                                            <template v-if="reply.content.startsWith('@')">
                                                <span class="font-bold text-gray-600">{{ reply.content.split(' ')[0] }}</span>
                                                {{ reply.content.substring(reply.content.indexOf(' ')) }}
                                            </template>
                                            <template v-else>{{ reply.content }}</template>
                                        </p>
                                        <div
                                            v-if="reply.imageUrl"
                                            @click="$emit('open-image', { url: reply.imageUrl })"
                                            class="w-20 h-20 flex-shrink-0 cursor-pointer rounded-lg overflow-hidden border border-gray-200 hover:opacity-80 transition-opacity"
                                        >
                                            <img :src="reply.imageUrl" alt="답글 이미지" class="w-full h-full object-cover" />
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <!-- 답글에 대한 답글 작성 폼 -->
                        <div v-if="replyingToCommentId === reply.id" class="flex space-x-4 p-4 bg-orange-50 rounded-lg border-2 border-orange-200">
                            <div class="w-8 h-8 bg-orange-100 rounded-full flex items-center justify-center flex-shrink-0 overflow-hidden">
                                <img v-if="memberProfileImage" :src="memberProfileImage" alt="프로필" class="w-full h-full object-cover" />
                                <i v-else class="pi pi-user text-orange-600 text-sm"></i>
                            </div>
                            <div class="flex-1">
                                <div class="text-xs text-gray-600 font-medium mb-2"><i class="pi pi-at mr-1"></i>{{ replyingToComment?.memberNickname || replyingToComment?.memberName }}님에게 답글 작성</div>
                                <textarea
                                    :value="replyContent"
                                    @input="$emit('update:replyContent', ($event.target as HTMLTextAreaElement).value)"
                                    placeholder="답글을 작성해주세요..."
                                    class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-500 focus:border-transparent resize-none"
                                    rows="2"
                                ></textarea>
                                <div v-if="replyImagePreview" class="mt-2 relative inline-block">
                                    <img :src="replyImagePreview" alt="미리보기" class="w-24 h-24 object-cover rounded-lg border border-gray-300" />
                                    <button @click="$emit('remove-reply-image')" class="absolute -top-2 -right-2 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center hover:bg-red-600">
                                        <i class="pi pi-times text-xs"></i>
                                    </button>
                                </div>
                                <div class="flex justify-between items-center mt-2">
                                    <label class="cursor-pointer">
                                        <input type="file" accept="image/*" @change="$emit('reply-image-select', $event)" class="hidden" />
                                        <div class="flex items-center space-x-2 px-3 py-1 text-sm bg-white text-gray-700 rounded hover:bg-gray-100 transition-colors border border-gray-300">
                                            <i class="pi pi-image text-sm"></i>
                                            <span>이미지 첨부</span>
                                        </div>
                                    </label>
                                    <div class="flex space-x-2">
                                        <button @click="$emit('cancel-reply')" type="button" class="px-4 py-1 text-sm bg-gray-200 text-gray-700 rounded hover:bg-gray-300 transition-colors">취소</button>
                                        <button
                                            @click="$emit('submit-reply')"
                                            type="button"
                                            :disabled="!replyContent.trim() || !replyingToComment"
                                            class="px-4 py-1 text-sm bg-gray-500 text-white rounded hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
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
        </div>

        <div v-if="comments.length === 0" class="text-center py-12">
            <i class="pi pi-comments text-gray-300 text-5xl mb-4"></i>
            <p class="text-gray-500">첫 번째 댓글을 작성해보세요!</p>
        </div>

        <div v-if="totalPages > 1" class="flex justify-center items-center gap-2 mt-8">
            <button @click="$emit('load-page', currentPage - 1)" :disabled="currentPage === 0" class="px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors">
                <i class="pi pi-chevron-left"></i>
            </button>
            <div class="flex gap-2">
                <button
                    v-for="page in totalPages"
                    :key="page"
                    @click="$emit('load-page', page - 1)"
                    :class="currentPage === page - 1 ? 'bg-gray-500 text-white' : 'bg-white text-gray-700 hover:bg-gray-50'"
                    class="px-4 py-2 border border-gray-300 rounded-lg transition-colors"
                >
                    {{ page }}
                </button>
            </div>
            <button
                @click="$emit('load-page', currentPage + 1)"
                :disabled="currentPage === totalPages - 1"
                class="px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
                <i class="pi pi-chevron-right"></i>
            </button>
        </div>
    </div>
</template>
