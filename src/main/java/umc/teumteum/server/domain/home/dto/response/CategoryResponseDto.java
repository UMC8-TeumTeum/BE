package umc.teumteum.server.domain.home.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "CategoryResponseDto : 카테고리 조회 응답 Dto ")
public class CategoryResponseDto {

    @Schema(description = "카테고리 ID" , example = "1")
    private Long categoryId;

    @Schema(description = "카테고리 이름" , example = "자기계발")
    private String categoryName;

}
