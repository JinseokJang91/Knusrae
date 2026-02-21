/**
 * 추천 크리에이터 타입
 */
export interface Creator {
    memberId: number;
    nickname: string;
    profileImage?: string;
    bio?: string;
    followerCount: number;
    followingCount: number;
    recipeCount: number;
    totalHits: number;
    totalFavoriteCount: number;
    isFollowing: boolean;
    recommendReason: string;
}
