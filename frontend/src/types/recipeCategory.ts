/** DB 공통코드 기준 필터 (메인 codeId + 상세 detailCodeIds) */
export interface RecipeCategoryFilter {
    codeId: string;
    detailCodeIds: string[];
}

/** 탑바/메뉴용 요리 카테고리 쇼트컷 (복합 카테고리는 여러 detailCodeId로 OR 필터) */
export interface RecipeCategory {
    id: number;
    name: string;
    icon: string;
    description?: string;
    filter: RecipeCategoryFilter;
}
