package com.knusrae.cook.api.domain.service;

import com.knusrae.cook.api.domain.entity.IngredientRequest;
import com.knusrae.cook.api.domain.repository.IngredientRequestRepository;
import com.knusrae.cook.api.dto.IngredientRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class IngredientRequestService {
    
    private final IngredientRequestRepository requestRepository;
    
    /**
     * 재료 정보 요청 생성
     */
    public IngredientRequestDto createRequest(String ingredientName, String requestType, String message, Long memberId) {
        IngredientRequest request = IngredientRequest.builder()
                .ingredientName(ingredientName)
                .requestType(requestType)
                .message(message)
                .memberId(memberId)
                .status("PENDING")
                .build();
        
        IngredientRequest saved = requestRepository.save(request);
        return IngredientRequestDto.fromEntity(saved);
    }
    
    /**
     * 사용자의 요청 목록 조회
     */
    @Transactional(readOnly = true)
    public List<IngredientRequestDto> getUserRequests(Long memberId) {
        return requestRepository.findByMemberIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(IngredientRequestDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 관리자용 요청 목록 조회 (페이지네이션)
     */
    @Transactional(readOnly = true)
    public Page<IngredientRequestDto> getAllRequests(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return requestRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(IngredientRequestDto::fromEntity);
    }
    
    /**
     * 상태별 요청 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<IngredientRequestDto> getRequestsByStatus(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return requestRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                .map(IngredientRequestDto::fromEntity);
    }
    
    /**
     * 요청 상태 업데이트
     */
    public IngredientRequestDto updateRequestStatus(Long requestId, String status) {
        IngredientRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("요청을 찾을 수 없습니다: " + requestId));
        
        request.updateStatus(status);
        IngredientRequest updated = requestRepository.save(request);
        return IngredientRequestDto.fromEntity(updated);
    }
}
