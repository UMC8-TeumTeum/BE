package umc.teumteum.server.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class UserResponseDTO {

  @Getter
  @Builder
  @AllArgsConstructor
  public static class MyPageDTO {
    @Schema(description = "유저 ID", example = "1")
    private Long userId;
    @Schema(description = "닉네임", example = "고냐니")
    private String nickname;
    @Schema(description = "프로필 이미지 URL", example = "https://~")
    private String profileImageUrl;
    @Schema(description = "직종/분야", example = "개발자")
    private String job;


  }
}
