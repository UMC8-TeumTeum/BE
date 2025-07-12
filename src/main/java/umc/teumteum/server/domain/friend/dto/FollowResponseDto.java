package umc.teumteum.server.domain.friend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(title = "FollowResponseDto : 팔로우 응답 DTO")
public class FollowResponseDto {

    @Schema(description = "팔로우 ID", example = "0")
    private Long followId;
}
