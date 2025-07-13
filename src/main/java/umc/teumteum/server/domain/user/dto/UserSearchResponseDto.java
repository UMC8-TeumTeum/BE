package umc.teumteum.server.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(title = "UserSearchResponseDto : 사용자 검색 응답 DTO")
public class UserSearchResponseDto {

    @Schema(description = "유저 번호", example = "1")
    private Long userId;
}
