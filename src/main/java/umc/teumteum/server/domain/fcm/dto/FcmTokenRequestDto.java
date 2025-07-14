package umc.teumteum.server.domain.fcm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "FcmTokenRequestDto : FCM 토큰 등록 요청 DTO", description = "알림 기능을 이용하기 위해 FCM 클라이언트 토큰을 등록 요청하는 Dto 입니다. ")
public class FcmTokenRequestDto {

  @Schema(description = "디바이스에서 발급받은 FCM 토큰", example = "c7DJdddsaaaasssdvvv")
  private String token;

}
