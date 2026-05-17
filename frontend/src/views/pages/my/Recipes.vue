<script setup lang="ts">
import { getRecipeListByMember, deleteRecipe } from '@/api/recipeApi';
import { fetchMemberInfo } from '@/utils/auth';
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useConfirm } from 'primevue/useconfirm';
import Button from 'primevue/button';
import Badge from 'primevue/badge';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Paginator from 'primevue/paginator';
import PageStateBlock from '@/components/common/PageStateBlock.vue';
import { useAppToast } from '@/utils/toast';
import type { Recipe } from '@/types/recipe';
import type { PageState } from 'primevue/paginator';

const router = useRouter();
const confirm = useConfirm();
const { showSuccess, showError } = useAppToast();

const fetchRecipes = async (): Promise<Recipe[]> => {
    const currentMember = await fetchMemberInfo();
    if (!currentMember?.id) return [];
    return await getRecipeListByMember(currentMember.id);
};

const deleteRecipeById = async (id: number): Promise<void> => {
    await deleteRecipe(id);
};

// 컴포넌트 상태
const search = ref('');
const items = ref<Recipe[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);
const first = ref(0);
const rowsPerPage = ref(10);

// 레시피 목록 로드
const loadRecipes = async () => {
    loading.value = true;
    error.value = null;
    try {
        items.value = await fetchRecipes();
    } catch (err) {
        error.value = '레시피 목록을 불러오는 중 오류가 발생했습니다.';
        console.error('레시피 로드 오류:', err);
    } finally {
        loading.value = false;
    }
};

const handleCreateRecipe = async () => {
    router.push('/my/recipes/new');
};

// 레시피 수정
const handleEditRecipe = async (recipe: Recipe) => {
    router.push(`/my/recipes/${recipe.id}/edit`);
};

function onPageChange(event: PageState) {
    first.value = event.first;
    rowsPerPage.value = event.rows;
}

// 레시피 삭제
function confirmDelete(recipe: Recipe) {
    confirm.require({
        header: '레시피 삭제',
        message: `"${recipe.title}" 레시피를 삭제하시겠습니까?`,
        icon: 'pi pi-exclamation-triangle',
        rejectLabel: '취소',
        rejectClass: 'p-button-secondary',
        acceptLabel: '확인',
        acceptClass: 'p-button-danger',
        accept: async () => {
            try {
                await deleteRecipeById(recipe.id);
                showSuccess('레시피가 삭제되었습니다.');
                await loadRecipes();
            } catch (err) {
                showError((err instanceof Error ? err.message : null) || '삭제에 실패했습니다.');
            }
        }
    });
}

// 레시피 상세 페이지로 이동
const viewRecipeDetail = (id: number) => {
    router.push(`/recipe/${id}`);
};

// 필터링된 레시피 목록
const filteredRecipes = computed(() => {
    const q = search.value.trim();
    if (!q) return items.value;
    return items.value.filter((r) => r.title.includes(q));
});

const pagedRecipes = computed(() => filteredRecipes.value.slice(first.value, first.value + rowsPerPage.value));

// 검색어 변경 시 첫 페이지로 이동 (필터 결과가 줄어들면 빈 페이지를 가리키지 않도록)
watch(search, () => {
    first.value = 0;
});

// 컴포넌트 마운트 시 레시피 목록 로드
onMounted(() => {
    loadRecipes();
});
</script>

<template>
    <div class="page-container page-container--card page-container--wide recipes-root recipes-card">
        <div class="recipes-content">
            <div class="recipes-notice mb-6 p-4 bg-orange-50 border-l-4 border-orange-500 rounded-r">
                <i class="pi pi-info-circle recipes-notice__icon" aria-hidden="true"></i>
                <p class="recipes-notice__text">제목을 클릭하면 레시피 상세 페이지로 이동해요. 등록·수정·삭제는 여기서 할 수 있어요.</p>
            </div>

            <div class="recipes-toolbar mb-3">
                <h2 class="recipes-title">레시피 관리</h2>
                <div class="recipes-toolbar__actions">
                    <input
                        v-model="search"
                        type="search"
                        class="recipes-search"
                        placeholder="레시피 검색"
                        :disabled="loading"
                        aria-label="레시피 검색"
                    />
                    <Button
                        class="recipes-action-btn recipes-action-btn--create"
                        label="레시피 등록하기"
                        icon="pi pi-plus"
                        :disabled="loading"
                        aria-label="레시피 등록하기"
                        @click="handleCreateRecipe"
                    />
                </div>
            </div>

            <!-- 로딩 -->
            <PageStateBlock v-if="loading" state="loading" loading-message="레시피 목록을 불러오는 중..." />

            <!-- 에러 -->
            <PageStateBlock v-else-if="error" state="error" error-title="레시피 목록을 불러올 수 없습니다" :error-message="error" retry-label="다시 시도" @retry="loadRecipes" />

            <!-- 빈 상태 -->
            <PageStateBlock
                v-else-if="filteredRecipes.length === 0"
                compact-mobile
                state="empty"
                empty-icon="pi pi-book"
                empty-title="등록된 레시피가 없습니다"
                empty-message="첫 레시피를 등록해 보세요."
                empty-button-label="레시피 등록하기"
                @empty-action="handleCreateRecipe"
            />

            <!-- 레시피 테이블 -->
            <template v-else>
                <div class="recipes-mobile-list recipes-mobile-only">
                    <article v-for="(recipe, index) in pagedRecipes" :key="recipe.id" class="recipes-mobile-item">
                        <div class="recipes-mobile-item__header">
                            <span class="recipes-mobile-item__no">No. {{ first + index + 1 }}</span>
                            <div class="recipes-mobile-item__badges">
                                <Badge v-if="recipe.status === 'DRAFT'" value="초안" severity="warning" />
                                <Badge v-else value="발행" severity="info" />
                                <Badge :value="recipe.visibility === 'PUBLIC' ? '공개' : '비공개'" severity="secondary" />
                            </div>
                        </div>
                        <button type="button" class="recipes-mobile-item__title" @click="viewRecipeDetail(recipe.id)">
                            {{ recipe.title }}
                        </button>
                        <div class="recipes-mobile-item__actions">
                            <Button label="수정" icon="pi pi-pencil" class="recipes-item-action-btn" severity="secondary" outlined @click="handleEditRecipe(recipe)" />
                            <Button label="삭제" icon="pi pi-trash" class="recipes-item-action-btn" severity="danger" outlined @click="confirmDelete(recipe)" />
                        </div>
                    </article>
                    <Paginator
                        :first="first"
                        :rows="rowsPerPage"
                        :total-records="filteredRecipes.length"
                        template="PrevPageLink CurrentPageReport NextPageLink"
                        current-page-report-template="{currentPage} / {totalPages}"
                        class="recipes-mobile-paginator"
                        @page="onPageChange"
                    />
                </div>

                <div class="recipes-table-wrap recipes-desktop-only">
                    <DataTable
                        :value="filteredRecipes"
                        :paginator="true"
                        :first="first"
                        :rows="rowsPerPage"
                        :total-records="filteredRecipes.length"
                        data-key="id"
                        responsive-layout="scroll"
                        class="p-datatable-sm recipes-datatable"
                        @page="onPageChange"
                    >
                        <template #empty>
                            <div class="text-center py-8 text-gray-500">
                                <i class="pi pi-book text-4xl mb-2"></i>
                                <p>등록된 레시피가 없습니다.</p>
                            </div>
                        </template>
                        <Column header="No" style="width: 70px">
                            <template #body="{ index }">
                                {{ first + index + 1 }}
                            </template>
                        </Column>
                        <Column header="제목" style="min-width: 200px">
                            <template #body="{ data }">
                                <a href="#" class="recipe-title-link" @click.prevent="viewRecipeDetail(data.id)">
                                    {{ data.title }}
                                </a>
                            </template>
                        </Column>
                        <Column header="상태" style="width: 100px">
                            <template #body="{ data }">
                                <Badge v-if="data.status === 'DRAFT'" value="초안" severity="warning" />
                                <Badge v-else value="발행" severity="info" />
                            </template>
                        </Column>
                        <Column header="공개" style="width: 100px">
                            <template #body="{ data }">
                                <Badge v-if="data.visibility === 'PUBLIC'" value="공개" severity="secondary" />
                                <Badge v-else value="비공개" severity="secondary" />
                            </template>
                        </Column>
                        <Column header="액션" style="width: 120px">
                            <template #body="{ data }">
                                <Button icon="pi pi-pencil" text rounded size="small" severity="secondary" title="수정" @click="handleEditRecipe(data)" />
                                <Button icon="pi pi-trash" text rounded size="small" severity="danger" title="삭제" @click="confirmDelete(data)" />
                            </template>
                        </Column>
                    </DataTable>
                </div>
            </template>

        </div>
    </div>
</template>

<style scoped>
/* Comments/Inquiries·Favorites와 동일 — page-container--card 패딩 + 오렌지 톤 */
.recipes-root {
    width: 100%;
    min-width: 0;
    box-sizing: border-box;
}

.recipes-card {
    background: #ffedd5;
}

.recipes-notice {
    display: flex;
    align-items: flex-start;
    gap: 0.75rem;
}

.recipes-notice__icon {
    font-size: 1.125rem;
    color: var(--orange-500, #f97316);
    flex-shrink: 0;
    margin-top: 0.125rem;
}

.recipes-notice__text {
    margin: 0;
    color: #374151;
    font-style: italic;
    font-size: 0.875rem;
    line-height: 1.45;
    letter-spacing: 0.01em;
}

@media (max-width: 767px) {
    .recipes-notice {
        gap: 0.5rem;
    }

    .recipes-notice__icon {
        font-size: 1.0625rem;
        margin-top: 0.0625rem;
    }

    .recipes-notice__text {
        font-size: 0.8125rem;
        line-height: 1.5;
    }
}

@media (max-width: 480px) {
    .recipes-notice__icon {
        font-size: 1rem;
    }

    .recipes-notice__text {
        font-size: 0.75rem;
        line-height: 1.45;
    }
}

.recipes-title {
    margin: 0;
    font-size: 1.125rem;
    font-weight: 600;
    line-height: 1.35;
    color: var(--p-text-color, #374151);
}

.recipes-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 0.5rem 0.75rem;
}

.recipes-toolbar__actions {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 0.5rem;
    min-width: 0;
}

.recipes-search {
    width: 100%;
    max-width: 14rem;
    padding: 0.5rem 0.75rem;
    font-size: 0.875rem;
    line-height: 1.35;
    color: var(--p-text-color, #374151);
    border: 1px solid var(--p-surface-border, #d1d5db);
    border-radius: var(--p-border-radius-md, 6px);
    background: var(--p-input-background, #fff);
    box-sizing: border-box;
}

.recipes-search:focus {
    outline: none;
    border-color: var(--p-primary-color);
    box-shadow: 0 0 0 2px color-mix(in srgb, var(--p-primary-color) 20%, transparent);
}

.recipes-search:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.recipes-toolbar :deep(.recipes-action-btn.p-button) {
    flex-shrink: 0;
}

.recipes-content {
    min-width: 0;
    min-height: 500px;
}

/* 테이블 영역: Inquiries와 동일한 카드형 래퍼 */
.recipes-table-wrap {
    background: var(--p-card-background, #ffffff);
    border: 1px solid var(--p-card-border-color, rgba(0, 0, 0, 0.08));
    border-radius: var(--p-card-border-radius, 12px);
    box-shadow: var(--p-card-shadow, 0 1px 3px rgba(0, 0, 0, 0.06));
    overflow: hidden;
    padding-top: 1rem;
}

.recipes-datatable :deep(.p-datatable-wrapper) {
    border: none;
}

.recipes-datatable :deep(.p-datatable-header),
.recipes-datatable :deep(.p-datatable-thead > tr > th),
.recipes-datatable :deep(.p-datatable-tbody > tr > td) {
    background: transparent;
    border-color: var(--p-surface-border, rgba(0, 0, 0, 0.08));
}

.recipes-datatable :deep(.p-datatable-thead > tr > th) {
    padding-top: 1rem;
}

.recipes-datatable :deep(.p-datatable-thead > tr > th:first-child),
.recipes-datatable :deep(.p-datatable-tbody > tr > td:first-child) {
    padding-left: 1.25rem;
}

.recipes-mobile-list {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
}

.recipes-mobile-item {
    background: var(--p-card-background, #ffffff);
    border: 1px solid var(--p-card-border-color, rgba(0, 0, 0, 0.08));
    border-radius: 12px;
    padding: 0.875rem;
    box-shadow: var(--p-card-shadow, 0 1px 3px rgba(0, 0, 0, 0.06));
}

.recipes-mobile-item__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 0.75rem;
}

.recipes-mobile-item__no {
    font-size: 0.8125rem;
    color: #6b7280;
}

.recipes-mobile-item__badges {
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-end;
    gap: 0.375rem;
}

.recipes-mobile-item__title {
    margin-top: 0.625rem;
    border: none;
    background: transparent;
    color: var(--p-primary-color);
    font-size: 0.9375rem;
    font-weight: 600;
    line-height: 1.4;
    text-align: left;
    cursor: pointer;
    padding: 0;
    word-break: break-word;
}

.recipes-mobile-item__title:hover {
    text-decoration: underline;
}

.recipes-mobile-item__actions {
    margin-top: 0.75rem;
    display: flex;
    gap: 0.5rem;
}

.recipes-mobile-paginator {
    margin-top: 0.25rem;
}

.recipes-desktop-only {
    display: none;
}

@media (min-width: 768px) {
    .recipes-mobile-only {
        display: none;
    }

    .recipes-desktop-only {
        display: block;
    }

    .recipes-toolbar {
        flex-wrap: nowrap;
    }

    .recipes-toolbar__actions {
        flex-wrap: nowrap;
        flex-shrink: 0;
    }

    .recipes-search {
        width: 14rem;
        max-width: 14rem;
        flex: 0 0 auto;
    }
}

.recipe-title-link {
    color: var(--p-primary-color);
    text-decoration: none;
}

.recipe-title-link:hover {
    text-decoration: underline;
}

@media (max-width: 767px) {
    .recipes-root.recipes-card {
        background: var(--surface-card, #fff);
        border-radius: 0;
        padding: 0;
        border: none;
        box-shadow: none;
    }

    .recipes-content {
        min-height: 0;
    }

    .recipes-title {
        font-size: 1rem;
        flex-shrink: 0;
    }

    .recipes-toolbar {
        flex-wrap: nowrap;
        align-items: center;
        gap: 0.5rem;
    }

    .recipes-toolbar__actions {
        flex: 1;
        flex-wrap: nowrap;
        justify-content: flex-end;
        gap: 0.375rem;
        min-width: 0;
    }

    .recipes-search {
        flex: 1;
        min-width: 0;
        max-width: none;
        padding: 0.4375rem 0.625rem;
        font-size: 0.8125rem;
    }

    .recipes-toolbar :deep(.recipes-action-btn.p-button) {
        min-height: 0;
        padding: 0.4375rem 0.75rem;
        font-size: 0.8125rem;
        line-height: 1.25;
    }

    .recipes-toolbar :deep(.recipes-action-btn .p-button-icon) {
        font-size: 0.8125rem;
    }

    .recipes-toolbar :deep(.recipes-action-btn--create .p-button-label) {
        display: none;
    }

    .recipes-toolbar :deep(.recipes-action-btn--create.p-button) {
        padding: 0.4375rem 0.5rem;
        width: 2.25rem;
        justify-content: center;
    }

    .recipes-toolbar :deep(.recipes-action-btn--create .p-button-icon) {
        margin: 0;
    }

    .recipes-mobile-item__title {
        font-size: 0.875rem;
    }

    .recipes-mobile-item__actions :deep(.recipes-item-action-btn.p-button) {
        min-height: 0;
        padding: 0.4375rem 0.75rem;
        font-size: 0.8125rem;
        line-height: 1.25;
    }

    .recipes-mobile-item__actions :deep(.recipes-item-action-btn .p-button-icon) {
        font-size: 0.8125rem;
    }

    .recipes-mobile-item__actions :deep(.recipes-item-action-btn .p-button-label) {
        line-height: 1.25;
    }
}

@media (min-width: 768px) {
    .recipes-toolbar :deep(.recipes-action-btn.p-button) {
        padding: 0.5rem 0.875rem;
        font-size: 0.875rem;
    }
}

@media (max-width: 480px) {
    .recipes-title {
        font-size: 0.9375rem;
    }

    .recipes-search {
        padding: 0.375rem 0.5rem;
        font-size: 0.75rem;
    }

    .recipes-toolbar :deep(.recipes-action-btn.p-button) {
        padding: 0.375rem 0.625rem;
        font-size: 0.75rem;
    }

    .recipes-toolbar :deep(.recipes-action-btn--create.p-button) {
        padding: 0.375rem 0.4375rem;
        width: 2rem;
    }

    .recipes-mobile-item__title {
        font-size: 0.8125rem;
    }

    .recipes-mobile-item__actions :deep(.recipes-item-action-btn.p-button) {
        padding: 0.375rem 0.625rem;
        font-size: 0.75rem;
    }
}
</style>
