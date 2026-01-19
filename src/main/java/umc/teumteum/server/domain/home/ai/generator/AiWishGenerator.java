package umc.teumteum.server.domain.home.ai.generator;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import umc.teumteum.server.domain.home.dto.request.ActivityRequestDto.AiWishOptionRequest;
import umc.teumteum.server.domain.home.dto.response.ActivityResponseDto.AiWishDto;
import umc.teumteum.server.domain.home.entity.enums.EstimatedDuration;
import umc.teumteum.server.global.config.GptConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiWishGenerator {

  private final WebClient gptWebClient;
  private final GptConfig gptConfig;

  public List<AiWishDto> generate(AiWishOptionRequest request, String categoryName, String locationName, String userJob) {
    // 1. 프롬프트 생성
    String prompt = buildPrompt(request, categoryName, locationName, userJob);

    // 2. 요청 생성
    Map<String, Object> requestBody = Map.of(
        "model", gptConfig.getModel(),
        "messages", List.of(
                    Map.of("role", "system", "content",
                            "너는 사용자의 취향에 맞게 활동을 추천하는 도우미다. 반드시 한국어로 답한다.\n" +
                                    "이때 가능하면 사용자의 직종과 분야를 반영하여 활동을 추천하라.\n" +
                                    "출력은 하이픈(-)으로 시작하는 정확히 3줄의 리스트만 작성한다.\n" +
                                    "각 줄은 '제목: 설명' 형식으로 구성하라.\n" +
                                    "제목(title)은 20자 이내여야 하며, 반드시 행동을 나타내는 동사구(~하기, ~가기 등)로 시작해야 한다.\n" +
                                    "설명(content)은 반드시 사용자를 향해 직접 말하는 문장으로 작성하고, 정중한 하세요체로 끝내라.\n" +
                                    "설명은 항상 '~하세요' 또는 '~가세요'와 같이 동사로 끝나야 한다.\n" +
                                    "설명에 '~입니다', '~해요', '~할 수 있어요', 명사형(하기, 쌓기 등)으로 끝나는 문장은 절대 사용하지 마라.\n" +
                                    "설명 문장 안에 쉼표로 나열된 행동 설명을 쓰지 말고, 하나의 자연스러운 문장으로 작성하라.\n" +
                                    "세 항목은 서로 장소, 분위기, 신체 사용 정도, 사회성이 분명히 달라야 한다.\n" +
                                    "머릿말, 번호, 따옴표, 코드블록, 빈 줄, 이모지 등 리스트 외 텍스트는 절대 쓰지 마라."),
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
            .content("일시적인 오류로 인해 AI 추천을 가져올 수 없습니다. 잠시 후 다시 시도해주세요.")
            .estimatedDuration(estimatedDuration)
            .build()
    );
  }

  private List<AiWishDto> parseWishList(String content, EstimatedDuration estimatedDuration) {
    List<AiWishDto> result = new ArrayList<>();

    for (String line : content.split("\n")) {
      line = line.replaceFirst("[-\\d. ]+", "").trim();

      if (!line.isEmpty()) {
        String title;
        String description;

        if (line.contains(":")) {
          String[] parts = line.split(":", 2);
          title = parts[0].trim();
          description = parts[1].trim();
        } else {
          title = line;
          description = "";
        }

        if (title.length() > 20) {
          title = title.substring(0, 20);
        }
        if (description.length() > 100) {
          description = description.substring(0, 100);
        }

        if (!description.isEmpty() && !description.endsWith("다.") && !description.endsWith(".") && !description.endsWith("요.") && !description.endsWith("함.") && !description.endsWith("함")) {
          description = description + "입니다.";
        }

        result.add(AiWishDto.builder()
            .id(UUID.randomUUID().toString())
            .title(title)
            .content(description)
            .estimatedDuration(estimatedDuration)
            .build());
      }
    }

    return result;
  }


  private String buildPrompt(AiWishOptionRequest request, String categoryName, String locationName, String userJob) {
    String duration = request.getEstimatedDuration().getDisplayName();

    return String.format(
        "%4$s 직종/분야에 종사하는(혹은 관심이 있는) 사용자는 %1$s에서 %2$s 정도 활동하고 싶어 한다. 카테고리는 %3$s이다.%n" +
            "다음 조건을 만족하는 활동을 정확히 3개 추천하라.%n" +
            "- 각 항목은 '제목: 설명' 형식으로 작성하라.%n" +
            "- 제목(title)은 동사구(~하기, ~가기 등)로 시작하고, 공백 포함 20자 이내여야 한다.%n" +
            "- 설명(content)은 활동의 이유나 분위기를 100자 이내로 자연스럽게 풀어써라.%n" +
            "- 세 활동은 장소, 강도, 사회성, 맥락 등이 서로 달라야 한다.%n" +
            "- 안전하고 상식적인 활동만 추천하라.%n" +
            "출력 예시(예시는 복사 금지):%n" +
            "- 청계천걷기: 맑은 날씨에 청계천을 따라 여유롭게 산책하는 활동%n" +
            "- 계단오르기: 가까운 공원 계단을 오르며 간단히 운동하기%n" +
            "- 사진찍기: 주변 풍경이나 카페에서 감성 사진 남기기",
        locationName, duration, categoryName, userJob
    );
  }

}
