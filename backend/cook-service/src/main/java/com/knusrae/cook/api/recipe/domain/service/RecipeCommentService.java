package com.knusrae.cook.api.recipe.domain.service;

import com.knusrae.common.custom.storage.ImageStorage;
import com.knusrae.common.domain.entity.Member;
import com.knusrae.common.domain.repository.MemberRepository;
import com.knusrae.cook.api.recipe.domain.constants.RecipeConstants;
import com.knusrae.cook.api.recipe.domain.entity.Recipe;
import com.knusrae.cook.api.recipe.domain.entity.RecipeComment;
import com.knusrae.cook.api.recipe.domain.repository.RecipeCommentRepository;
import com.knusrae.cook.api.recipe.domain.repository.RecipeRepository;
import com.knusrae.cook.api.recipe.dto.RecipeCommentDto;
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
import java.util.Arrays;
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

    @Transactional
    public RecipeCommentDto createComment(Long recipeId, Long memberId, String content, Long parentId) {
        return createCommentWithImage(recipeId, memberId, content, parentId, null);
    }

    @Transactional
    public RecipeCommentDto createCommentWithImage(Long recipeId, Long memberId, String content, Long parentId, MultipartFile image) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + recipeId));
        Long parentMemberId = null;
        String parentMemberNickname = null;
        Long rootParentId = parentId;
        if (parentId != null) {
            RecipeComment parentComment = recipeCommentRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent comment not found with id: " + parentId));
            parentMemberId = parentComment.getMemberId();
            Member parentMember = memberRepository.findById(parentMemberId).orElse(null);
            parentMemberNickname = parentMember != null ? parentMember.getNickname() : parentMember != null ? parentMember.getName() : "사용자";
            if (parentComment.getParentId() != null) {
                rootParentId = parentComment.getParentId();
            }
        }
        String imageUrl = null;
        String imageStorageKey = null;
        if (image != null && !image.isEmpty()) {
            validateImage(image);
            String relativeDir = "comments/%d/%s".formatted(recipeId, LocalDate.now());
            ImageStorage.UploadResponse uploadResponse = imageStorage.upload(image, relativeDir);
            imageUrl = uploadResponse.url();
            imageStorageKey = uploadResponse.key();
        }
        RecipeComment comment = RecipeComment.builder()
                .recipe(recipe)
                .memberId(memberId)
                .content(content)
                .imageUrl(imageUrl)
                .imageStorageKey(imageStorageKey)
                .parentId(rootParentId)
                .build();
        RecipeComment savedComment = recipeCommentRepository.save(comment);
        Member member = memberRepository.findById(memberId).orElse(null);
        String memberName = member != null ? member.getName() : "사용자";
        String memberNickname = member != null ? member.getNickname() : null;
        String memberProfileImage = member != null ? member.getProfileImage() : null;
        RecipeCommentDto dto = RecipeCommentDto.fromEntity(savedComment, memberName, memberNickname, memberProfileImage);
        dto.setParentMemberId(parentMemberId);
        dto.setParentMemberNickname(parentMemberNickname);
        return dto;
    }

    private void validateImage(MultipartFile file) {
        if (file.getSize() > RecipeConstants.MAX_COMMENT_IMAGE_SIZE) {
            throw new IllegalArgumentException("파일이 너무 큽니다. 최대 " + (RecipeConstants.MAX_COMMENT_IMAGE_SIZE / (1024 * 1024)) + "MB까지 가능합니다.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }
        String filename = file.getOriginalFilename();
        if (filename != null) {
            String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
            List<String> allowedExtensions = Arrays.asList(RecipeConstants.ALLOWED_IMAGE_EXTENSIONS);
            if (!allowedExtensions.contains(extension)) {
                throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. 허용된 형식: " + String.join(", ", allowedExtensions));
            }
        }
    }

    public List<RecipeCommentDto> getCommentsByRecipeId(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + recipeId));
        List<RecipeComment> allComments = recipeCommentRepository.findAllByRecipeOrderByCreatedAtDesc(recipe);
        List<Long> memberIds = allComments.stream()
                .map(RecipeComment::getMemberId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Member> memberMap = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, m -> m));
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
        List<RecipeCommentDto> topLevelComments = new ArrayList<>();
        for (RecipeCommentDto commentDto : commentMap.values()) {
            if (commentDto.getParentId() == null) {
                commentDto.setReplies(new ArrayList<>());
                topLevelComments.add(commentDto);
            } else {
                RecipeCommentDto parentComment = commentMap.get(commentDto.getParentId());
                if (parentComment != null) {
                    if (parentComment.getReplies() == null) {
                        parentComment.setReplies(new ArrayList<>());
                    }
                    parentComment.getReplies().add(commentDto);
                }
            }
        }
        for (RecipeCommentDto comment : topLevelComments) {
            if (comment.getReplies() != null) {
                comment.getReplies().sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
            }
        }
        topLevelComments.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return topLevelComments;
    }

    public Map<String, Object> getCommentsByRecipeIdWithPagination(Long recipeId, int page, int size) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + recipeId));
        Pageable pageable = PageRequest.of(page, size);
        Page<RecipeComment> topLevelCommentsPage = recipeCommentRepository
                .findAllByRecipeAndParentIdIsNullOrderByCreatedAtDesc(recipe, pageable);
        List<RecipeComment> topLevelComments = topLevelCommentsPage.getContent();
        List<Long> topLevelCommentIds = topLevelComments.stream()
                .map(RecipeComment::getId)
                .collect(Collectors.toList());
        List<RecipeComment> allReplies = new ArrayList<>();
        for (Long commentId : topLevelCommentIds) {
            allReplies.addAll(recipeCommentRepository.findAllByParentIdOrderByCreatedAtAsc(commentId));
        }
        List<RecipeComment> allComments = new ArrayList<>(topLevelComments);
        allComments.addAll(allReplies);
        List<Long> memberIds = allComments.stream()
                .map(RecipeComment::getMemberId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Member> memberMap = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, m -> m));
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
        List<RecipeCommentDto> topLevelCommentDtos = new ArrayList<>();
        for (RecipeComment topLevelComment : topLevelComments) {
            RecipeCommentDto commentDto = commentMap.get(topLevelComment.getId());
            commentDto.setReplies(new ArrayList<>());
            for (RecipeComment reply : allReplies) {
                if (reply.getParentId().equals(topLevelComment.getId())) {
                    commentDto.getReplies().add(commentMap.get(reply.getId()));
                }
            }
            commentDto.getReplies().sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
            topLevelCommentDtos.add(commentDto);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("comments", topLevelCommentDtos);
        response.put("currentPage", page);
        response.put("totalPages", topLevelCommentsPage.getTotalPages());
        response.put("totalComments", topLevelCommentsPage.getTotalElements());
        response.put("hasNext", topLevelCommentsPage.hasNext());
        response.put("hasPrevious", topLevelCommentsPage.hasPrevious());
        return response;
    }

    @Transactional
    public RecipeCommentDto updateComment(Long commentId, Long memberId, String content) {
        return updateCommentWithImage(commentId, memberId, content, null, false);
    }

    @Transactional
    public RecipeCommentDto updateCommentWithImage(Long commentId, Long memberId, String content, MultipartFile image, boolean removeImage) {
        RecipeComment comment = recipeCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found with id: " + commentId));
        if (!comment.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("댓글 작성자만 수정할 수 있습니다.");
        }
        String imageUrl = comment.getImageUrl();
        String imageStorageKey = comment.getImageStorageKey();
        if (removeImage && imageStorageKey != null) {
            imageStorage.deleteByKey(imageStorageKey);
            imageUrl = null;
            imageStorageKey = null;
        }
        if (image != null && !image.isEmpty()) {
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
        Member member = memberRepository.findById(memberId).orElse(null);
        String memberName = member != null ? member.getName() : "사용자";
        String memberNickname = member != null ? member.getNickname() : null;
        String memberProfileImage = member != null ? member.getProfileImage() : null;
        return RecipeCommentDto.fromEntity(comment, memberName, memberNickname, memberProfileImage);
    }

    @Transactional
    public void deleteComment(Long commentId, Long memberId) {
        RecipeComment comment = recipeCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found with id: " + commentId));
        if (!comment.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("댓글 작성자만 삭제할 수 있습니다.");
        }
        if (comment.getImageStorageKey() != null) {
            try {
                imageStorage.deleteByKey(comment.getImageStorageKey());
            } catch (Exception e) {
                log.warn("댓글 이미지 삭제 실패: {}", e.getMessage());
            }
        }
        if (comment.getParentId() == null) {
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

    public long getCommentCount(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + recipeId));
        return recipeCommentRepository.countByRecipe(recipe);
    }
}
