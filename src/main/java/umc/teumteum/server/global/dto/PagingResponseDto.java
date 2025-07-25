package umc.teumteum.server.global.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PagingResponseDto<T> {

    @Schema(description = "콘텐츠 목록")
    private List<T> content;

    @Schema(description = "다음 페이지 존재 여부")
    private boolean hasNext;
}