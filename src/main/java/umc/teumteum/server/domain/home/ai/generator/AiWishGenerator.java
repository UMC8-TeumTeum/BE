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

  public List<AiWishDto> generate(AiWishOptionRequest request, String categoryName) {
    // 1. 프롬프트 생성
    String prompt = buildPrompt(request, categoryName);

    // 2. 요청 생성
    Map<String, Object> requestBody = Map.of(
        "model", gptConfig.getModel(),
        "messages", List.of(
            Map.of("role", "system", "content", "너는 간단한 활동 추천을 해주는 역할을 수행해야해."),
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

  private String buildPrompt(AiWishOptionRequest request, String categoryName) {
    // 기본 값들
    String location = Optional.ofNullable(request.getLocation()).orElse("실내");
    String duration = Optional.ofNullable(request.getEstimatedDuration())
        .map(EstimatedDuration::getDisplayName)
        .orElse("30m");

    return String.format(
        "지금 사용자는 %s에서 %s 정도 활동하고 싶어 해. 카테고리는 %s야."
            + "이 조건을 바탕으로 간단한 활동 3가지를 추천해줘."
            + "각 활동은 한 문장으로 짧게 제시하고 리스트 형식(-)으로 줘.",
        location,
        duration,
        categoryName
    );
  }

}
