package com.knusrae.cook.api.ingredient.domain.service;

import com.knusrae.cook.api.ingredient.domain.entity.IngredientRequest;
import com.knusrae.cook.api.ingredient.domain.repository.IngredientRequestRepository;
import com.knusrae.cook.api.ingredient.dto.IngredientRequestDto;
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

    @Transactional(readOnly = true)
    public List<IngredientRequestDto> getUserRequests(Long memberId) {
        return requestRepository.findByMemberIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(IngredientRequestDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<IngredientRequestDto> getAllRequests(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return requestRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(IngredientRequestDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<IngredientRequestDto> getRequestsByStatus(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return requestRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                .map(IngredientRequestDto::fromEntity);
    }

    public IngredientRequestDto updateRequestStatus(Long requestId, String status) {
        IngredientRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("요청을 찾을 수 없습니다: " + requestId));
        request.updateStatus(status);
        IngredientRequest updated = requestRepository.save(request);
        return IngredientRequestDto.fromEntity(updated);
    }
}
