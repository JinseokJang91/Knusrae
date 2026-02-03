/**
 * 레시피 등록/수정 폼에서 사용하는 드래프트 타입 (Create/Edit 공통)
 */

export interface IngredientItemDraft {
    id: string;
    name: string;
    quantity: string | null;
    unit: string;
    customUnitName?: string;
}

export interface IngredientGroupDraft {
    id: string;
    type: string;
    customTypeName?: string;
    items: IngredientItemDraft[];
}

export interface RecipeStepDraft {
    id: string;
    file?: File | null;
    previewUrl?: string;
    text: string;
    /** 수정 시 기존 이미지 URL (object URL이 아닌 서버 URL) */
    existingImageUrl?: string;
}

export interface CommonCodeDetailOption {
    detailCodeId: string;
    codeName: string;
}

export interface CommonCodeOption {
    codeId: string;
    codeName: string;
    details: CommonCodeDetailOption[];
}

export function generateRecipeFormId(): string {
    return crypto.randomUUID();
}
