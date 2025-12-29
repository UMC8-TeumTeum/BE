package umc.teumteum.server.global.infra.discord.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class DiscordMessageDto {
    private List<Embed> embeds;

    @Getter
    @Builder
    public static class Embed {
        private String title;
        private String description;
        private int color;
        private List<Field> fields;
    }

    @Getter
    @AllArgsConstructor
    public static class Field {
        private String name;
        private String value;
        private boolean inline;
    }
}