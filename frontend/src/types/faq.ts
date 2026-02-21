export type FaqCategoryId = 'recipe' | 'ingredient' | 'member' | 'ranking' | 'etc';

export interface FaqItem {
    id: string;
    categoryId: FaqCategoryId;
    question: string;
    answer: string;
}

export interface FaqCategory {
    id: FaqCategoryId;
    label: string;
    icon?: string;
    sortOrder: number;
}
