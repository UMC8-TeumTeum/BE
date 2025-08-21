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

  public List<AiWishDto> generate(AiWishOptionRequest request, String categoryName, String locationName) {
    // 1. 프롬프트 생성
    String prompt = buildPrompt(request, categoryName, locationName);

    // 2. 요청 생성
    Map<String, Object> requestBody = Map.of(
        "model", gptConfig.getModel(),
        "messages", List.of(
            Map.of("role", "system", "content",
                "너는 간단한 활동을 추천하는 도우미다. 반드시 한국어로 답한다. " +
                    "출력은 하이픈(-)으로 시작하는 정확히 3줄의 리스트만 작성한다. " +
                    "각 줄은 '행동'을 나타내는 동사구로 시작해야 한다(예: ~하기, ~가기, ~타기, ~찍기, ~즐기기). " +
                    "각 항목 길이는 하이픈과 공백을 제외하고 공백 미포함 10자 이내여야 한다(문자수≤10). " +
                    "가능하면 단어를 붙여 써라(예: 청계천걷기, 팔굽혀펴기). " +
                    "세 항목은 서로 의미가 확연히 달라야 하며, 동의어·형태만 바꾼 표현·미미한 수식어 변경은 금지한다. " +
                    "막연한 장소명(예: 작은공원, 아무카페)만 쓰지 말고, 행동 자체가 드러나게 표현하라. " +
                    "안전하고 상식적인 활동만 제안하라(위험·불법·타인불편 유발·과도한 비용/장비 유도 금지). " +
                    "출력 전 다음을 자체 검증하라: (1) 각 항목이 동사구인가, (2) 공백 미포함 10자 이내인가, (3) 서로 의미가 충분히 다른가. " +
                    "조건을 하나라도 어기면 그 항목을 버리고 새로운 항목으로 즉시 교체하라. " +
                    "머릿말, 설명, 번호, 따옴표, 코드블록, 빈 줄, 이모지 등 리스트 외 텍스트는 절대 쓰지 마라."
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
        "사용자는 %1$s에서 %2$s 정도 활동하고 싶어 한다. 카테고리는 %3$s이다.%n" +
            "다음 조건을 만족하는 활동을 정확히 3개 추천하라.%n" +
            "- 각 항목은 동사구로 시작하고 명사구만 쓰지 마라(예: ~하기, ~가기, ~타기, ~찍기, ~즐기기, ~감상하기, ~쓰기, ~읽기, ~작성하기, ~먹기, ~보기).%n" +
            "- 각 항목 길이는 하이픈과 공백 제외, 공백 미포함 10자 이내(문자수≤10). 넘으면 다른 활동으로 바꿔라.%n" +
            "- 세 항목은 '다양성 축' 중 최소 두 가지 이상에서 서로 다르게 구성하라:%n" +
            "  · 장소: 실내/실외/이동형  · 강도: 저강도/중강도  · 사회성: 혼자/둘이/여럿%n" +
            "  · 도구: 무도구/간단도구  · 맥락: 자연/도시/직장/학교  · 모드: 운동/휴식/창의/정리%n" +
            "- 사용자가 제시한 위치(%1$s), 시간(%2$s), 카테고리(%3$s)를 자연스럽게 반영하되, 불필요한 고유명사 남발은 피하라.%n" +
            "- 과도하게 흔한 추천만 반복하지 마라(예: 산책하기/물마시기/스트레칭하기만 반복 금지).%n" +
            "- 하이픈(-)으로 시작하는 리스트 3줄만 출력하고 다른 텍스트는 절대 쓰지 마라.%n" +
            "예시 형식(예시는 참고용일 뿐 그대로 복사 금지):%n" +
            "- 청계천걷기%n" +
            "- 계단오르기%n" +
            "- 벤치딥스하기",
        locationName, duration, categoryName
    );
  }

}
