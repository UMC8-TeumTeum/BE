package umc.teumteum.server.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(title = "UserSearchResponseDto : 닉네임 검색 응답 DTO")
public class UserSearchResponseDto {

    @Schema(description = "유저 ID", example = "1")
    private Long userId;

    @Schema(description = "닉네임", example = "string")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "string")
    private String profileImageUrl;

    @Schema(description = "직종/분야", example = "string")
    private String job;
}
