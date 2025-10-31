package umc.teumteum.server.unit.home.ai.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import umc.teumteum.server.domain.home.ai.generator.AiWishGenerator;
import umc.teumteum.server.domain.home.dto.request.ActivityRequestDto.AiWishOptionRequest;
import umc.teumteum.server.domain.home.dto.response.ActivityResponseDto.AiWishDto;
import umc.teumteum.server.domain.home.entity.enums.EstimatedDuration;
import umc.teumteum.server.global.config.GptConfig;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiWishGenerator - AI 위시 생성 단위 테스트")
class AiWishGeneratorTest {

  @Mock
  private WebClient gptWebClient;

  @Mock
  private GptConfig gptConfig;

  @Mock
  private WebClient.RequestBodyUriSpec requestBodyUriSpec;

  @Mock
  private WebClient.RequestBodySpec requestBodySpec;

  @Mock
  private WebClient.ResponseSpec responseSpec;

  @InjectMocks
  private AiWishGenerator aiWishGenerator;

  private ObjectMapper objectMapper;

  @BeforeEach
  void setup() {
    objectMapper = new ObjectMapper();
    when(gptConfig.getModel()).thenReturn("gpt-3.5-turbo");
    when(gptConfig.getTemperature()).thenReturn(0.7);
  }

  @Test
  @DisplayName("[generate] - TC1 정상적인 GPT 응답으로 3개의 AI 위시 생성 - userJob 포함")
  void generate_withValidResponse_returns3Wishes() throws Exception {
    // given
    AiWishOptionRequest request = AiWishOptionRequest.builder()
        .estimatedDuration(EstimatedDuration.MINUTES_10)
        .locationId(1L)
        .categoryId(1L)
        .build();

    String gptResponse = """
        - 청계천걷기: 맑은 날씨에 청계천을 따라 여유롭게 산책하는 활동
        - 계단오르기: 가까운 공원 계단을 오르며 간단히 운동하기
        - 사진찍기: 주변 풍경이나 카페에서 감성 사진 남기기
        """;

    JsonNode mockResponse = objectMapper.createObjectNode()
        .set("choices", objectMapper.createArrayNode()
            .add(objectMapper.createObjectNode()
                .set("message", objectMapper.createObjectNode()
                    .put("content", gptResponse))));

    when(gptWebClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestBodySpec);
    when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(mockResponse));

    // when
    List<AiWishDto> result = aiWishGenerator.generate(request, "운동", "집", "개발자");

    // then
    assertThat(result).hasSize(3);
    assertThat(result.get(0).getTitle()).isEqualTo("청계천걷기");
    assertThat(result.get(0).getContent()).isEqualTo("맑은 날씨에 청계천을 따라 여유롭게 산책하는 활동입니다.");
    assertThat(result.get(1).getTitle()).isEqualTo("계단오르기");
    assertThat(result.get(1).getContent()).isEqualTo("가까운 공원 계단을 오르며 간단히 운동하기입니다.");
    assertThat(result.get(2).getTitle()).isEqualTo("사진찍기");
    assertThat(result.get(2).getContent()).isEqualTo("주변 풍경이나 카페에서 감성 사진 남기기입니다.");
  }

  @Test
  @DisplayName("[generate] - TC2 제목과 설명이 구분자(:)로 분리된 경우 정상 파싱")
  void generate_withTitleAndContent_parsesBoth() throws Exception {
    // given
    AiWishOptionRequest request = AiWishOptionRequest.builder()
        .estimatedDuration(EstimatedDuration.MINUTES_20)
        .locationId(2L)
        .categoryId(2L)
        .build();

    String gptResponse = """
        - 독서하기: 조용한 도서관에서 좋아하는 책을 읽으며 지식을 쌓는 시간입니다.
        - 명상하기: 눈을 감고 호흡에 집중하며 마음의 평화를 찾는 활동입니다.
        - 일기쓰기: 오늘 하루를 돌아보며 감정을 정리하고 기록하는 시간입니다.
        """;

    JsonNode mockResponse = objectMapper.createObjectNode()
        .set("choices", objectMapper.createArrayNode()
            .add(objectMapper.createObjectNode()
                .set("message", objectMapper.createObjectNode()
                    .put("content", gptResponse))));

    when(gptWebClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestBodySpec);
    when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(mockResponse));

    // when
    List<AiWishDto> result = aiWishGenerator.generate(request, "독서", "학교", "교사");

    // then
    assertThat(result).hasSize(3);
    assertThat(result.get(0).getTitle()).isEqualTo("독서하기");
    assertThat(result.get(0).getContent()).isEqualTo("조용한 도서관에서 좋아하는 책을 읽으며 지식을 쌓는 시간입니다.");
    assertThat(result.get(1).getTitle()).isEqualTo("명상하기");
    assertThat(result.get(2).getTitle()).isEqualTo("일기쓰기");
  }

  @Test
  @DisplayName("[generate] - TC3 제목만 있고 설명이 없는 경우 content는 빈 문자열")
  void generate_withTitleOnly_contentIsEmpty() throws Exception {
    // given
    AiWishOptionRequest request = AiWishOptionRequest.builder()
        .estimatedDuration(EstimatedDuration.MINUTES_30)
        .locationId(3L)
        .categoryId(3L)
        .build();

    String gptResponse = """
        - 스트레칭하기
        - 요가하기
        - 필라테스하기
        """;

    JsonNode mockResponse = objectMapper.createObjectNode()
        .set("choices", objectMapper.createArrayNode()
            .add(objectMapper.createObjectNode()
                .set("message", objectMapper.createObjectNode()
                    .put("content", gptResponse))));

    when(gptWebClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestBodySpec);
    when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(mockResponse));

    // when
    List<AiWishDto> result = aiWishGenerator.generate(request, "운동", "회사", "직장인");

    // then
    assertThat(result).hasSize(3);
    assertThat(result.get(0).getTitle()).isEqualTo("스트레칭하기");
    assertThat(result.get(0).getContent()).isEmpty();
    assertThat(result.get(1).getContent()).isEmpty();
    assertThat(result.get(2).getContent()).isEmpty();
  }

  @Test
  @DisplayName("[generate] - TC4 제목이 20자를 초과하는 경우 잘라냄")
  void generate_titleExceeds20Chars_truncates() throws Exception {
    // given
    AiWishOptionRequest request = AiWishOptionRequest.builder()
        .estimatedDuration(EstimatedDuration.MINUTES_10)
        .locationId(1L)
        .categoryId(1L)
        .build();

    String gptResponse = """
        - 청계천을따라걸으며자연을만끽하기: 도심 속 자연을 느낄 수 있는 활동
        """;

    JsonNode mockResponse = objectMapper.createObjectNode()
        .set("choices", objectMapper.createArrayNode()
            .add(objectMapper.createObjectNode()
                .set("message", objectMapper.createObjectNode()
                    .put("content", gptResponse))));

    when(gptWebClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestBodySpec);
    when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(mockResponse));

    // when
    List<AiWishDto> result = aiWishGenerator.generate(request, "운동", "집", "개발자");

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTitle()).hasSize(20);
    assertThat(result.get(0).getTitle()).isEqualTo("청계천을따라걸으며자연을만끽하기".substring(0, 20));
  }

  @Test
  @DisplayName("[generate] - TC5 설명이 100자를 초과하는 경우 잘라냄")
  void generate_contentExceeds100Chars_truncates() throws Exception {
    // given
    AiWishOptionRequest request = AiWishOptionRequest.builder()
        .estimatedDuration(EstimatedDuration.MINUTES_10)
        .locationId(1L)
        .categoryId(1L)
        .build();

    String longContent = "A".repeat(120);
    String gptResponse = String.format("- 산책하기: %s", longContent);

    JsonNode mockResponse = objectMapper.createObjectNode()
        .set("choices", objectMapper.createArrayNode()
            .add(objectMapper.createObjectNode()
                .set("message", objectMapper.createObjectNode()
                    .put("content", gptResponse))));

    when(gptWebClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestBodySpec);
    when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(mockResponse));

    // when
    List<AiWishDto> result = aiWishGenerator.generate(request, "운동", "집", "개발자");

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getContent()).hasSize(100);
  }

  @Test
  @DisplayName("[generate] - TC6 설명이 종결어미(다., 요. 등)로 끝나지 않으면 '입니다.' 추가")
  void generate_contentWithoutEnding_addsEnding() throws Exception {
    // given
    AiWishOptionRequest request = AiWishOptionRequest.builder()
        .estimatedDuration(EstimatedDuration.MINUTES_10)
        .locationId(1L)
        .categoryId(1L)
        .build();

    String gptResponse = """
        - 산책하기: 공원에서 걷기
        - 독서하기: 책을 읽는 활동
        """;

    JsonNode mockResponse = objectMapper.createObjectNode()
        .set("choices", objectMapper.createArrayNode()
            .add(objectMapper.createObjectNode()
                .set("message", objectMapper.createObjectNode()
                    .put("content", gptResponse))));

    when(gptWebClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestBodySpec);
    when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(mockResponse));

    // when
    List<AiWishDto> result = aiWishGenerator.generate(request, "운동", "집", "개발자");

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getContent()).endsWith("입니다.");
    assertThat(result.get(1).getContent()).endsWith("입니다.");
  }

  @Test
  @DisplayName("[generate] - TC7 설명이 '다.', '요.', '함.' 등으로 끝나면 '입니다.' 추가 안함")
  void generate_contentWithProperEnding_doesNotAddEnding() throws Exception {
    // given
    AiWishOptionRequest request = AiWishOptionRequest.builder()
        .estimatedDuration(EstimatedDuration.MINUTES_10)
        .locationId(1L)
        .categoryId(1L)
        .build();

    String gptResponse = """
        - 산책하기: 공원을 걷는 활동입니다.
        - 독서하기: 책을 읽어요.
        - 명상하기: 마음을 정리함.
        """;

    JsonNode mockResponse = objectMapper.createObjectNode()
        .set("choices", objectMapper.createArrayNode()
            .add(objectMapper.createObjectNode()
                .set("message", objectMapper.createObjectNode()
                    .put("content", gptResponse))));

    when(gptWebClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestBodySpec);
    when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(mockResponse));

    // when
    List<AiWishDto> result = aiWishGenerator.generate(request, "운동", "집", "개발자");

    // then
    assertThat(result).hasSize(3);
    assertThat(result.get(0).getContent()).isEqualTo("공원을 걷는 활동입니다.");
    assertThat(result.get(1).getContent()).isEqualTo("책을 읽어요.");
    assertThat(result.get(2).getContent()).isEqualTo("마음을 정리함.");
  }

  @Test
  @DisplayName("[generate] - TC8 GPT API 호출 실패 시 fallback 위시 반환")
  void generate_apiCallFails_returnsFallback() {
    // given
    AiWishOptionRequest request = AiWishOptionRequest.builder()
        .estimatedDuration(EstimatedDuration.MINUTES_10)
        .locationId(1L)
        .categoryId(1L)
        .build();

    when(gptWebClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestBodySpec);
    when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.error(new RuntimeException("API Error")));

    // when
    List<AiWishDto> result = aiWishGenerator.generate(request, "운동", "집", "개발자");

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTitle()).isEqualTo("AI 추천을 불러올 수 없어요.");
    assertThat(result.get(0).getContent()).isEqualTo("일시적인 오류로 인해 AI 추천을 가져올 수 없습니다. 잠시 후 다시 시도해주세요.");
    assertThat(result.get(0).getEstimatedDuration()).isEqualTo(EstimatedDuration.MINUTES_10);
  }

  @Test
  @DisplayName("[generate] - TC9 빈 줄이 포함된 응답도 정상 처리")
  void generate_withEmptyLines_ignoresEmptyLines() throws Exception {
    // given
    AiWishOptionRequest request = AiWishOptionRequest.builder()
        .estimatedDuration(EstimatedDuration.MINUTES_10)
        .locationId(1L)
        .categoryId(1L)
        .build();

    String gptResponse = """
        - 산책하기: 공원 산책
        
        - 독서하기: 책 읽기
        
        """;

    JsonNode mockResponse = objectMapper.createObjectNode()
        .set("choices", objectMapper.createArrayNode()
            .add(objectMapper.createObjectNode()
                .set("message", objectMapper.createObjectNode()
                    .put("content", gptResponse))));

    when(gptWebClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestBodySpec);
    when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(mockResponse));

    // when
    List<AiWishDto> result = aiWishGenerator.generate(request, "운동", "집", "개발자");

    // then
    assertThat(result).hasSize(2);
  }

  @Test
  @DisplayName("[generate] - TC10 userJob이 null인 경우도 정상 처리")
  void generate_withNullUserJob_handlesGracefully() throws Exception {
    // given
    AiWishOptionRequest request = AiWishOptionRequest.builder()
        .estimatedDuration(EstimatedDuration.MINUTES_10)
        .locationId(1L)
        .categoryId(1L)
        .build();

    String gptResponse = "- 산책하기: 공원 산책하기";

    JsonNode mockResponse = objectMapper.createObjectNode()
        .set("choices", objectMapper.createArrayNode()
            .add(objectMapper.createObjectNode()
                .set("message", objectMapper.createObjectNode()
                    .put("content", gptResponse))));

    when(gptWebClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestBodySpec);
    when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(mockResponse));

    // when
    List<AiWishDto> result = aiWishGenerator.generate(request, "운동", "집", null);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTitle()).isEqualTo("산책하기");
  }

  @Test
  @DisplayName("[generate] - TC11 각 AI 위시는 고유한 UUID를 가짐")
  void generate_eachWishHasUniqueId() throws Exception {
    // given
    AiWishOptionRequest request = AiWishOptionRequest.builder()
        .estimatedDuration(EstimatedDuration.MINUTES_10)
        .locationId(1L)
        .categoryId(1L)
        .build();

    String gptResponse = """
        - 산책하기: 활동1
        - 독서하기: 활동2
        - 명상하기: 활동3
        """;

    JsonNode mockResponse = objectMapper.createObjectNode()
        .set("choices", objectMapper.createArrayNode()
            .add(objectMapper.createObjectNode()
                .set("message", objectMapper.createObjectNode()
                    .put("content", gptResponse))));

    when(gptWebClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestBodySpec);
    when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(mockResponse));

    // when
    List<AiWishDto> result = aiWishGenerator.generate(request, "운동", "집", "개발자");

    // then
    assertThat(result).hasSize(3);
    assertThat(result.get(0).getId()).isNotNull();
    assertThat(result.get(1).getId()).isNotNull();
    assertThat(result.get(2).getId()).isNotNull();
    assertThat(result.get(0).getId()).isNotEqualTo(result.get(1).getId());
    assertThat(result.get(1).getId()).isNotEqualTo(result.get(2).getId());
  }

  @Test
  @DisplayName("[generate] - TC12 EstimatedDuration이 모든 위시에 올바르게 설정됨")
  void generate_estimatedDurationSetCorrectly() throws Exception {
    // given
    AiWishOptionRequest request = AiWishOptionRequest.builder()
        .estimatedDuration(EstimatedDuration.HOUR_1)
        .locationId(1L)
        .categoryId(1L)
        .build();

    String gptResponse = """
        - 등산하기: 산을 오르는 활동
        - 조깅하기: 공원에서 달리기
        """;

    JsonNode mockResponse = objectMapper.createObjectNode()
        .set("choices", objectMapper.createArrayNode()
            .add(objectMapper.createObjectNode()
                .set("message", objectMapper.createObjectNode()
                    .put("content", gptResponse))));

    when(gptWebClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestBodySpec);
    when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(mockResponse));

    // when
    List<AiWishDto> result = aiWishGenerator.generate(request, "운동", "실외", "직장인");

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getEstimatedDuration()).isEqualTo(EstimatedDuration.HOUR_1);
    assertThat(result.get(1).getEstimatedDuration()).isEqualTo(EstimatedDuration.HOUR_1);
  }
}