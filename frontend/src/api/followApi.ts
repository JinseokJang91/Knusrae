import { httpJson } from '@/utils/http';
import { getApiBaseUrl } from '@/utils/constants';
import type { Follow, FollowListResponse } from '@/types/follow';

const BASE_URL = getApiBaseUrl('member');

/**
 * 팔로우
 */
export async function followUser(memberId: number): Promise<Follow> {
  const response = await httpJson<Follow>(
    BASE_URL,
    `/api/follows/${memberId}`,
    { method: 'POST' }
  );
  return response;
}

/**
 * 언팔로우
 */
export async function unfollowUser(memberId: number): Promise<void> {
  await httpJson<void>(
    BASE_URL,
    `/api/follows/${memberId}`,
    { method: 'DELETE' }
  );
}

/**
 * 팔로우 여부 확인
 */
export async function checkFollowing(memberId: number): Promise<{ isFollowing: boolean }> {
  const response = await httpJson<{ isFollowing: boolean }>(
    BASE_URL,
    `/api/follows/${memberId}/check`,
    { method: 'GET' }
  );
  return response;
}

/**
 * 팔로워 목록 조회
 */
export async function getFollowers(
  memberId: number,
  page: number = 0,
  size: number = 20
): Promise<FollowListResponse> {
  const response = await httpJson<FollowListResponse>(
    BASE_URL,
    `/api/members/${memberId}/followers?page=${page}&size=${size}`,
    { method: 'GET' }
  );
  return response;
}

/**
 * 팔로잉 목록 조회
 */
export async function getFollowings(
  memberId: number,
  page: number = 0,
  size: number = 20
): Promise<FollowListResponse> {
  const response = await httpJson<FollowListResponse>(
    BASE_URL,
    `/api/members/${memberId}/followings?page=${page}&size=${size}`,
    { method: 'GET' }
  );
  return response;
}

/**
 * 특정 회원 정보 조회 (공개 프로필)
 */
export async function getUserProfile(memberId: number): Promise<{
  id: number;
  name: string;
  nickname: string;
  profileImage?: string;
  bio?: string;
  followerCount: number;
  followingCount: number;
}> {
  const response = await httpJson<{
    id: number;
    name: string;
    nickname: string;
    profileImage?: string;
    bio?: string;
    followerCount: number;
    followingCount: number;
  }>(
    BASE_URL,
    `/api/member/${memberId}`,
    { method: 'GET' }
  );
  return response;
}
