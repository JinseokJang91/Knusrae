/**
 * 팔로우 관계 타입
 */
export interface Follow {
  id: number;
  followerId: number;
  followingId: number;
  createdAt: string;
}

/**
 * 팔로워 항목 타입
 */
export interface FollowerItem {
  followId: number;
  followerId: number;
  followerNickname: string;
  followerProfileImage?: string;
  createdAt: string;
}

/**
 * 팔로잉 항목 타입
 */
export interface FollowingItem {
  followId: number;
  followingId: number;
  followingNickname: string;
  followingProfileImage?: string;
  createdAt: string;
}

/**
 * 팔로우 목록 응답 타입
 */
export interface FollowListResponse {
  content: FollowerItem[] | FollowingItem[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
}
