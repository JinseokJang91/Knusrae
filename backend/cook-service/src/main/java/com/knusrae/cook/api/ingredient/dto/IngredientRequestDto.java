package com.knusrae.cook.api.ingredient.dto;

import com.knusrae.cook.api.ingredient.domain.entity.IngredientRequest;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class IngredientRequestDto {
    private Long id;
    private String ingredientName;
    private String requestType;
    private String message;
    private Long memberId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static IngredientRequestDto fromEntity(IngredientRequest request) {
        if (request == null) {
            return null;
        }
        return IngredientRequestDto.builder()
                .id(request.getId())
                .ingredientName(request.getIngredientName())
                .requestType(request.getRequestType())
                .message(request.getMessage())
                .memberId(request.getMemberId())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
