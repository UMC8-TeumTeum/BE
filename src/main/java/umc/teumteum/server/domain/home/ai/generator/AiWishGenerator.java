package umc.teumteum.server.domain.home.ai.generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import umc.teumteum.server.domain.home.dto.request.ActivityRequestDto.AiWishOptionRequest;
import umc.teumteum.server.domain.home.dto.response.ActivityResponseDto.WishDto;
import umc.teumteum.server.domain.home.entity.enums.EstimatedDuration;
import umc.teumteum.server.global.config.GptConfig;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiWishGenerator {

  private final WebClient gptWebClient;
  private final GptConfig gptConfig;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public List<WishDto> generate(AiWishOptionRequest request, String categoryName) {
    // 1. 프롬프트 생성
    String prompt = buildPrompt(request, categoryName);

    // 2. 요청 생성
    Map<String, Object> requestBody = Map.of(
        "model", gptConfig.getModel(),
        "messages", List.of(
            Map.of("role", "system", "content", prompt),
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
      return fallbackWish(request.getEstimatedDuration());
    }
  }

  private List<WishDto> fallbackWish(EstimatedDuration estimatedDuration) {
    return List.of(
        WishDto.builder()
            .id(1L)
            .content("")
            .estimatedDuration(estimatedDuration)
            .build()
    );

  }

  private List<WishDto> parseWishList(String content, EstimatedDuration estimatedDuration) {
    List<WishDto> result = new ArrayList<>();
    Long id = 1L;

    for (String line : content.split("\n")) {
      line = line.replaceFirst("[-\\d. ]+", "").trim();
      if(!line.isEmpty()){
        result.add(WishDto.builder()
                .id(id++)
                .content(line)
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
