import type { FaqCategory, FaqItem } from '@/types/faq';

export const FAQ_CATEGORIES: FaqCategory[] = [
    { id: 'recipe', label: '레시피', icon: 'pi pi-book', sortOrder: 1 },
    { id: 'ingredient', label: '재료', icon: 'pi pi-list', sortOrder: 2 },
    { id: 'member', label: '회원/계정', icon: 'pi pi-user', sortOrder: 3 },
    { id: 'ranking', label: '추천/랭킹', icon: 'pi pi-chart-line', sortOrder: 4 },
    { id: 'etc', label: '기타', icon: 'pi pi-question-circle', sortOrder: 5 }
];

export const FAQ_ITEMS: FaqItem[] = [
    // 레시피
    {
        id: 'recipe-1',
        categoryId: 'recipe',
        question: '레시피는 어떻게 등록하나요?',
        answer: '상단 메뉴에서 "내 레시피"를 클릭한 후 "레시피 등록" 버튼을 누르세요. 요리 정보, 재료, 조리 단계, 이미지를 입력하고 저장하면 됩니다.'
    },
    {
        id: 'recipe-2',
        categoryId: 'recipe',
        question: '레시피를 검색하려면?',
        answer: '상단 중앙의 검색창에 검색어를 입력하고 엔터를 누르거나 검색 아이콘을 클릭하세요. 레시피 제목, 재료, 카테고리 등으로 검색할 수 있습니다.'
    },
    {
        id: 'recipe-3',
        categoryId: 'recipe',
        question: '레시피 카테고리는 어떻게 활용되나요?',
        answer: '카테고리 메뉴에서 한식, 중식, 일식 등 국적별, 메인 재료별, 조리법별로 레시피를 찾아볼 수 있습니다. 레시피 등록 시 카테고리를 선택하면 해당 분류로 노출됩니다.'
    },
    // 재료
    {
        id: 'ingredient-1',
        categoryId: 'ingredient',
        question: '재료 보관법은 어디서 볼 수 있나요?',
        answer: '상단 메뉴에서 "재료 관리"를 클릭한 후 "보관법" 탭을 선택하세요. 재료를 클릭하면 보관 방법과 유통기한 등을 확인할 수 있습니다.'
    },
    {
        id: 'ingredient-2',
        categoryId: 'ingredient',
        question: '재료 손질법은 어디서 볼 수 있나요?',
        answer: '재료 관리에서 "손질법" 탭을 선택하면 됩니다. 각 재료별로 손질 방법, 커팅 팁 등을 확인할 수 있습니다.'
    },
    {
        id: 'ingredient-3',
        categoryId: 'ingredient',
        question: '재료 정보 요청은 어떻게 하나요?',
        answer: '재료 상세 페이지에서 "정보 요청" 버튼을 클릭하세요. 보관법 또는 손질법 중 필요한 타입을 선택하고 요청 사항을 입력하면 관리자 검토 후 등록됩니다.'
    },
    // 회원/계정
    {
        id: 'member-1',
        categoryId: 'member',
        question: '어떤 소셜 로그인을 지원하나요?',
        answer: '네이버, 카카오, 구글 소셜 로그인을 지원합니다. 로그인 페이지에서 원하는 플랫폼을 선택하여 간편하게 가입·로그인할 수 있습니다.'
    },
    {
        id: 'member-2',
        categoryId: 'member',
        question: '프로필 사진은 어떻게 변경하나요?',
        answer: '마이페이지에서 "내 정보 수정" 탭을 선택한 후 프로필 이미지 영역을 클릭하면 사진을 업로드할 수 있습니다. (최대 10MB, jpg/png/gif/webp 지원)'
    },
    {
        id: 'member-3',
        categoryId: 'member',
        question: '찜한 레시피는 어디서 확인하나요?',
        answer: '마이페이지의 "찜 목록" 탭에서 확인할 수 있습니다. 레시피 카드의 하트 아이콘을 다시 클릭하면 찜에서 해제됩니다.'
    },
    // 추천/랭킹
    {
        id: 'ranking-1',
        categoryId: 'ranking',
        question: '오늘의 레시피는 어떻게 선정되나요?',
        answer: '오늘의 레시피는 인기도, 조회수, 찜 수 등을 종합하여 추천됩니다. 매일 다양한 레시피를 만나보실 수 있습니다.'
    },
    {
        id: 'ranking-2',
        categoryId: 'ranking',
        question: '랭킹 기준은 무엇인가요?',
        answer: '주간·월간 랭킹은 해당 기간 동안의 조회수, 찜 수, 댓글·리뷰 수 등을 반영하여 산정됩니다. 랭킹 메뉴에서 주간/월간 탭을 선택해 확인하세요.'
    },
    // 기타
    {
        id: 'etc-1',
        categoryId: 'etc',
        question: '1:1 문의는 어디서 할 수 있나요?',
        answer: '마이페이지의 "1:1 문의" 탭에서 문의 내역을 확인하고 새 문의를 등록할 수 있습니다. 로그인 후 이용 가능합니다.'
    },
    {
        id: 'etc-2',
        categoryId: 'etc',
        question: '레시피 이미지는 몇 장까지 올릴 수 있나요?',
        answer: '레시피당 여러 장의 이미지를 등록할 수 있습니다. 메인 이미지 1장을 지정하면 목록에서 해당 이미지가 대표로 노출됩니다.'
    }
];
