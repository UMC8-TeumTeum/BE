package umc.teumteum.server.global.infra.discord.service;

public interface DiscordService {
    void sendReportNotification(Long reportId, String targetType, String reasonTitle);
}