package com.knusrae.cook.api.domain.service;

import com.knusrae.common.custom.storage.ImageStorage;
import com.knusrae.common.domain.entity.Member;
import com.knusrae.common.domain.repository.MemberRepository;
import com.knusrae.cook.api.domain.entity.Recipe;
import com.knusrae.cook.api.domain.entity.RecipeComment;
import com.knusrae.cook.api.domain.repository.RecipeCommentRepository;
import com.knusrae.cook.api.domain.repository.RecipeRepository;
import com.knusrae.cook.api.dto.RecipeCommentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RecipeCommentService {
    private final RecipeCommentRepository recipeCommentRepository;
    private final RecipeRepository recipeRepository;
    private final MemberRepository memberRepository;
    private final ImageStorage imageStorage;

    /**
     * 댓글 생성
     */
    @Transactional
    public RecipeCommentDto createComment(Long recipeId, Long memberId, String content, Long parentId) {
        return createCommentWithImage(recipeId, memberId, content, parentId, null);
    }

    /**
     * 댓글 생성 (이미지 포함)
     */
    @Transactional
    public RecipeCommentDto createCommentWithImage(Long recipeId, Long memberId, String content, Long parentId, MultipartFile image) {
        // 레시피 조회
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + recipeId));

        // 부모 댓글 작성자 정보
        Long parentMemberId = null;
        String parentMemberNickname = null;
        Long rootParentId = parentId; // 실제로 저장할 parentId (최상위 댓글 ID)
        
        // parentId가 있는 경우 (답글), 부모 댓글이 존재하는지 확인
        if (parentId != null) {
            RecipeComment parentComment = recipeCommentRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent comment not found with id: " + parentId));
            
            // 부모 댓글 작성자 정보 조회
            parentMemberId = parentComment.getMemberId();
            Member parentMember = memberRepository.findById(parentMemberId).orElse(null);
            parentMemberNickname = parentMember != null ? parentMember.getNickname() : parentMember != null ? parentMember.getName() : "사용자";
            
            // 답글의 답글인 경우, 최상위 댓글 ID를 rootParentId로 설정
            if (parentComment.getParentId() != null) {
                rootParentId = parentComment.getParentId();
            }
        }

        // 이미지 업로드 처리
        String imageUrl = null;
        String imageStorageKey = null;
        if (image != null && !image.isEmpty()) {
            validateImage(image);
            String relativeDir = "comments/%d/%s".formatted(recipeId, LocalDate.now());
            ImageStorage.UploadResponse uploadResponse = imageStorage.upload(image, relativeDir);
            imageUrl = uploadResponse.url();
            imageStorageKey = uploadResponse.key();
        }

        // 댓글 생성 (rootParentId 사용하여 모든 답글을 같은 depth로 유지)
        RecipeComment comment = RecipeComment.builder()
                .recipe(recipe)
                .memberId(memberId)
                .content(content)
                .imageUrl(imageUrl)
                .imageStorageKey(imageStorageKey)
                .parentId(rootParentId)
                .build();

        RecipeComment savedComment = recipeCommentRepository.save(comment);

        // Member 정보 조회
        Member member = memberRepository.findById(memberId).orElse(null);
        String memberName = member != null ? member.getName() : "사용자";
        String memberNickname = member != null ? member.getNickname() : null;
        String memberProfileImage = member != null ? member.getProfileImage() : null;

        // DTO 생성 및 부모 댓글 작성자 정보 설정
        RecipeCommentDto dto = RecipeCommentDto.fromEntity(savedComment, memberName, memberNickname, memberProfileImage);
        dto.setParentMemberId(parentMemberId);
        dto.setParentMemberNickname(parentMemberNickname);
        
        return dto;
    }

    private void validateImage(MultipartFile file) {
        long max = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > max) {
            throw new IllegalArgumentException("파일이 너무 큽니다. (최대 5MB)");
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }
    }

    /**
     * 특정 레시피의 모든 댓글 조회 (대댓글 포함, 계층 구조)
     */
    public List<RecipeCommentDto> getCommentsByRecipeId(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + recipeId));

        // 모든 댓글 조회
        List<RecipeComment> allComments = recipeCommentRepository.findAllByRecipeOrderByCreatedAtDesc(recipe);

        // Member 정보 일괄 조회
        List<Long> memberIds = allComments.stream()
                .map(RecipeComment::getMemberId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, Member> memberMap = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, m -> m));

        // 댓글을 DTO로 변환
        Map<Long, RecipeCommentDto> commentMap = allComments.stream()
                .collect(Collectors.toMap(
                        RecipeComment::getId,
                        comment -> {
                            Member member = memberMap.get(comment.getMemberId());
                            String memberName = member != null ? member.getName() : "사용자";
                            String memberNickname = member != null ? member.getNickname() : null;
                            String memberProfileImage = member != null ? member.getProfileImage() : null;
                            return RecipeCommentDto.fromEntity(comment, memberName, memberNickname, memberProfileImage);
                        }
                ));

        // 최상위 댓글과 대댓글 분리
        List<RecipeCommentDto> topLevelComments = new ArrayList<>();
        
        for (RecipeCommentDto commentDto : commentMap.values()) {
            if (commentDto.getParentId() == null) {
                // 최상위 댓글인 경우
                commentDto.setReplies(new ArrayList<>());
                topLevelComments.add(commentDto);
            } else {
                // 대댓글인 경우, 부모 댓글에 추가
                RecipeCommentDto parentComment = commentMap.get(commentDto.getParentId());
                if (parentComment != null) {
                    if (parentComment.getReplies() == null) {
                        parentComment.setReplies(new ArrayList<>());
                    }
                    parentComment.getReplies().add(commentDto);
                }
            }
        }

        // 대댓글 정렬 (오래된 순)
        for (RecipeCommentDto comment : topLevelComments) {
            if (comment.getReplies() != null) {
                comment.getReplies().sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
            }
        }

        // 최상위 댓글 정렬 (최신순)
        topLevelComments.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        return topLevelComments;
    }
    
    /**
     * 특정 레시피의 댓글 조회 (Pagination 지원)
     */
    public Map<String, Object> getCommentsByRecipeIdWithPagination(Long recipeId, int page, int size) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + recipeId));

        // 최상위 댓글만 페이징 처리하여 조회
        Pageable pageable = PageRequest.of(page, size);
        Page<RecipeComment> topLevelCommentsPage = recipeCommentRepository
                .findAllByRecipeAndParentIdIsNullOrderByCreatedAtDesc(recipe, pageable);
        
        List<RecipeComment> topLevelComments = topLevelCommentsPage.getContent();
        
        // 최상위 댓글의 ID 목록
        List<Long> topLevelCommentIds = topLevelComments.stream()
                .map(RecipeComment::getId)
                .collect(Collectors.toList());
        
        // 해당 최상위 댓글들의 모든 답글 조회
        List<RecipeComment> allReplies = new ArrayList<>();
        for (Long commentId : topLevelCommentIds) {
            allReplies.addAll(recipeCommentRepository.findAllByParentIdOrderByCreatedAtAsc(commentId));
        }
        
        // 모든 댓글(최상위 + 답글)의 Member 정보 일괄 조회
        List<RecipeComment> allComments = new ArrayList<>(topLevelComments);
        allComments.addAll(allReplies);
        
        List<Long> memberIds = allComments.stream()
                .map(RecipeComment::getMemberId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, Member> memberMap = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, m -> m));

        // 댓글을 DTO로 변환
        Map<Long, RecipeCommentDto> commentMap = allComments.stream()
                .collect(Collectors.toMap(
                        RecipeComment::getId,
                        comment -> {
                            Member member = memberMap.get(comment.getMemberId());
                            String memberName = member != null ? member.getName() : "사용자";
                            String memberNickname = member != null ? member.getNickname() : null;
                            String memberProfileImage = member != null ? member.getProfileImage() : null;
                            return RecipeCommentDto.fromEntity(comment, memberName, memberNickname, memberProfileImage);
                        }
                ));

        // 최상위 댓글 DTO 목록 생성
        List<RecipeCommentDto> topLevelCommentDtos = new ArrayList<>();
        
        for (RecipeComment topLevelComment : topLevelComments) {
            RecipeCommentDto commentDto = commentMap.get(topLevelComment.getId());
            commentDto.setReplies(new ArrayList<>());
            
            // 답글 추가
            for (RecipeComment reply : allReplies) {
                if (reply.getParentId().equals(topLevelComment.getId())) {
                    commentDto.getReplies().add(commentMap.get(reply.getId()));
                }
            }
            
            // 답글 정렬 (오래된 순)
            commentDto.getReplies().sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
            
            topLevelCommentDtos.add(commentDto);
        }
        
        // 응답 데이터 구성
        Map<String, Object> response = new HashMap<>();
        response.put("comments", topLevelCommentDtos);
        response.put("currentPage", page);
        response.put("totalPages", topLevelCommentsPage.getTotalPages());
        response.put("totalComments", topLevelCommentsPage.getTotalElements());
        response.put("hasNext", topLevelCommentsPage.hasNext());
        response.put("hasPrevious", topLevelCommentsPage.hasPrevious());
        
        return response;
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public RecipeCommentDto updateComment(Long commentId, Long memberId, String content) {
        return updateCommentWithImage(commentId, memberId, content, null, false);
    }

    /**
     * 댓글 수정 (이미지 포함)
     */
    @Transactional
    public RecipeCommentDto updateCommentWithImage(Long commentId, Long memberId, String content, MultipartFile image, boolean removeImage) {
        RecipeComment comment = recipeCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found with id: " + commentId));

        // 작성자 확인
        if (!comment.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("댓글 작성자만 수정할 수 있습니다.");
        }

        String imageUrl = comment.getImageUrl();
        String imageStorageKey = comment.getImageStorageKey();

        // 이미지 삭제 요청
        if (removeImage && imageStorageKey != null) {
            imageStorage.deleteByKey(imageStorageKey);
            imageUrl = null;
            imageStorageKey = null;
        }

        // 새 이미지 업로드
        if (image != null && !image.isEmpty()) {
            // 기존 이미지가 있으면 삭제
            if (imageStorageKey != null) {
                imageStorage.deleteByKey(imageStorageKey);
            }
            
            validateImage(image);
            Long recipeId = comment.getRecipe().getId();
            String relativeDir = "comments/%d/%s".formatted(recipeId, LocalDate.now());
            ImageStorage.UploadResponse uploadResponse = imageStorage.upload(image, relativeDir);
            imageUrl = uploadResponse.url();
            imageStorageKey = uploadResponse.key();
        }

        comment.updateContentAndImage(content, imageUrl, imageStorageKey);

        // Member 정보 조회
        Member member = memberRepository.findById(memberId).orElse(null);
        String memberName = member != null ? member.getName() : "사용자";
        String memberNickname = member != null ? member.getNickname() : null;
        String memberProfileImage = member != null ? member.getProfileImage() : null;

        return RecipeCommentDto.fromEntity(comment, memberName, memberNickname, memberProfileImage);
    }

    /**
     * 댓글 삭제
     */
    @Transactional
    public void deleteComment(Long commentId, Long memberId) {
        RecipeComment comment = recipeCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found with id: " + commentId));

        // 작성자 확인
        if (!comment.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("댓글 작성자만 삭제할 수 있습니다.");
        }

        // 댓글 이미지 삭제
        if (comment.getImageStorageKey() != null) {
            try {
                imageStorage.deleteByKey(comment.getImageStorageKey());
            } catch (Exception e) {
                log.warn("댓글 이미지 삭제 실패: {}", e.getMessage());
            }
        }

        // 대댓글이 있는 경우에도 삭제 (cascade)
        if (comment.getParentId() == null) {
            // 최상위 댓글인 경우, 해당 댓글의 대댓글도 모두 삭제 (이미지 포함)
            List<RecipeComment> replies = recipeCommentRepository.findAllByParentIdOrderByCreatedAtAsc(commentId);
            for (RecipeComment reply : replies) {
                if (reply.getImageStorageKey() != null) {
                    try {
                        imageStorage.deleteByKey(reply.getImageStorageKey());
                    } catch (Exception e) {
                        log.warn("답글 이미지 삭제 실패: {}", e.getMessage());
                    }
                }
            }
            recipeCommentRepository.deleteAll(replies);
        }

        recipeCommentRepository.delete(comment);
    }

    /**
     * 특정 레시피의 댓글 개수 조회
     */
    public long getCommentCount(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + recipeId));
        return recipeCommentRepository.countByRecipe(recipe);
    }
}

