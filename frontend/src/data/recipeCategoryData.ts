import type { RecipeCategory } from '@/types/recipeCategory';

export const RECIPE_CATEGORIES: RecipeCategory[] = [
    {
        id: 1,
        name: '한식',
        icon: 'fa-solid fa-k',
        filter: { codeId: 'COOKING_STYLE', detailCodeIds: ['1001'] }
    },
    {
        id: 2,
        name: '국·탕·찌개',
        icon: 'fa-solid fa-bowl-food',
        filter: { codeId: 'COOKING_MENU_FORM', detailCodeIds: ['1002'] }
    },
    {
        id: 3,
        name: '밥·덮밥·볶음밥',
        icon: 'fa-solid fa-bowl-rice',
        filter: { codeId: 'COOKING_MENU_FORM', detailCodeIds: ['1001'] }
    },
    {
        id: 4,
        name: '면·파스타',
        icon: 'fa-solid fa-plate-wheat',
        description: '라면/국수/파스타',
        filter: { codeId: 'COOKING_MENU_FORM', detailCodeIds: ['1003', '1004', '1005'] }
    },
    {
        id: 5,
        name: '반찬·밑반찬',
        icon: 'fa-solid fa-box-archive',
        filter: { codeId: 'COOKING_MENU_FORM', detailCodeIds: ['1014'] }
    },
    {
        id: 6,
        name: '고기요리',
        icon: 'fa-solid fa-drumstick-bite',
        filter: { codeId: 'COOKING_MAIN_INGREDIENT', detailCodeIds: ['1001', '1002', '1003', '1022', '1023'] }
    },
    {
        id: 7,
        name: '해산물요리',
        icon: 'fa-solid fa-fish',
        filter: { codeId: 'COOKING_MAIN_INGREDIENT', detailCodeIds: ['1004', '1005', '1006', '1007', '1008'] }
    },
    {
        id: 8,
        name: '샐러드·건강식',
        icon: 'fa-solid fa-leaf',
        filter: { codeId: 'COOKING_MENU_FORM', detailCodeIds: ['1017'] }
    },
    {
        id: 9,
        name: '초간단·자취요리',
        icon: 'fa-solid fa-clock',
        description: '10~15분, 재료 적은 레시피',
        filter: { codeId: 'COOKING_LEVEL', detailCodeIds: ['1001', '1002', '1007', '1012'] }
    },
    {
        id: 10,
        name: '도시락',
        icon: 'fa-solid fa-box',
        filter: { codeId: 'COOKING_TARGET', detailCodeIds: ['1012'] }
    },
    {
        id: 11,
        name: '야식·안주',
        icon: 'fa-solid fa-beer-mug-empty',
        filter: { codeId: 'COOKING_MENU_FORM', detailCodeIds: ['1019', '1020'] }
    },
    {
        id: 12,
        name: '디저트·베이킹',
        icon: 'fa-solid fa-cookie-bite',
        filter: { codeId: 'COOKING_DESSERT', detailCodeIds: ['1001', '1002', '1003', '1004', '1005'] }
    }
];
