package umc.teumteum.server.global.infra.discord.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import umc.teumteum.server.global.infra.discord.dto.DiscordMessageDto;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordServiceImpl implements DiscordService {

    @Value("${discord.webhook.url}")
    private String webhookUrl;

    private final RestTemplate restTemplate;

    @Override
    @Async
    public void sendReportNotification(Long reportId, String targetType, Long targetId, String reasonTitle) {
        List<DiscordMessageDto.Field> fields = List.of(
                new DiscordMessageDto.Field("신고 번호", "#" + reportId, true),
                new DiscordMessageDto.Field("대상 타입", targetType, true),
                new DiscordMessageDto.Field("신고 사유", reasonTitle, true)
        );

        DiscordMessageDto.Embed embed = DiscordMessageDto.Embed.builder()
                .title("🚨 새로운 신고가 접수되었습니다")
                .description("관리자 페이지에서 상세 내용을 확인해주세요.")
                .color(15158332)
                .fields(fields)
                .build();

        DiscordMessageDto message = DiscordMessageDto.builder()
                .embeds(List.of(embed))
                .build();

        try {
            restTemplate.postForEntity(webhookUrl, message, String.class);
        } catch (Exception e) {
            log.error("Discord 웹훅 발송 실패: {}", e.getMessage());
        }
    }
}