package umc.teumteum.server.global.infra.discord.service;

public interface DiscordService {
    void sendReportNotification(Long reportId, String targetType, Long targetId, String reasonTitle);
}