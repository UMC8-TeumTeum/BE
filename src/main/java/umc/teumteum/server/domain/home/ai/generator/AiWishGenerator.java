package umc.teumteum.server.domain.home.ai.generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import umc.teumteum.server.domain.home.dto.request.ActivityRequestDto.AiWishOptionRequest;
import umc.teumteum.server.domain.home.dto.response.ActivityResponseDto.AiWishDto;
import umc.teumteum.server.domain.home.dto.response.ActivityResponseDto.WishDto;
import umc.teumteum.server.domain.home.entity.enums.EstimatedDuration;
import umc.teumteum.server.global.config.GptConfig;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiWishGenerator {

  private final WebClient gptWebClient;
  private final GptConfig gptConfig;

  public List<AiWishDto> generate(AiWishOptionRequest request, String categoryName, String locationName) {
    // 1. 프롬프트 생성
    String prompt = buildPrompt(request, categoryName, locationName);

    // 2. 요청 생성
    Map<String, Object> requestBody = Map.of(
        "model", gptConfig.getModel(),
        "messages", List.of(
            Map.of("role", "system", "content",
                "너는 간단한 활동을 추천하는 도우미다. 반드시 한국어로 답한다. " +
                    "출력은 하이픈(-)으로 시작하는 리스트 3줄만 작성한다. " +
                    "각 줄은 공백 포함 10자 이내여야 한다. " +
                    "머릿말, 설명, 번호, 따옴표, 코드블록, 빈 줄 등 리스트 외 텍스트는 절대 쓰지 마라."
            ),
            Map.of("role", "user", "content", prompt)
        ),
        "temperature", gptConfig.getTemperature()
    );
    // 3. 요청 보냄
    try {
      String content = gptWebClient.post()
          .bodyValue(requestBody)
          .retrieve()
          .bodyToMono(JsonNode.class)
          .map(json -> json.get("choices").get(0).get("message").get("content").asText())
          .block();

      log.info("GPT 응답: {}", content);

      return parseWishList(content, request.getEstimatedDuration());

    } catch (Exception e) {
      log.warn("AI 콘텐츠 생성 실패. fallback 실행: {}", e.getMessage());
      return fallbackWish(request.getEstimatedDuration());
    }
  }

  private List<AiWishDto> fallbackWish(EstimatedDuration estimatedDuration) {
    return List.of(
        AiWishDto.builder()
            .id(UUID.randomUUID().toString())
            .title("AI 추천을 불러올 수 없어요.")
            .estimatedDuration(estimatedDuration)
            .build()
    );

  }

  private List<AiWishDto> parseWishList(String content, EstimatedDuration estimatedDuration) {
    List<AiWishDto> result = new ArrayList<>();

    for (String line : content.split("\n")) {
      line = line.replaceFirst("[-\\d. ]+", "").trim();
      if(!line.isEmpty()){
        result.add(AiWishDto.builder()
                .id(UUID.randomUUID().toString())
                .title(line)
                .estimatedDuration(estimatedDuration)
            .build());
      }

    }
    return result;
  }

  private String buildPrompt(AiWishOptionRequest request, String categoryName, String locationName) {
    String duration = request.getEstimatedDuration().getDisplayName();

    return String.format(
        "사용자는 %s에서 %s 정도 활동하고 싶어 한다. 카테고리는 %s이다.%n" +
            "조건에 맞는 활동을 정확히 3가지만 추천하라.%n" +
            "- 각 항목은 공백 포함 10자 이내로 작성하라.%n" +
            "- 만약 활동이 10글자를 넘어가는 경우 그냥 다른 활동을 추천해라.%n" +
            "- 하이픈(-)으로 시작하는 리스트 형태로만 출력하라.%n" +
            "- 다른 설명이나 추가 문장은 절대 쓰지 마라.%n" +
            "예시 형식:%n" +
            "- 청계천 걷기%n" +
            "- 간단한 스트레칭%n" +
            "- 회사 계단오르기",
        locationName,
        duration,
        categoryName
    );
  }

}
