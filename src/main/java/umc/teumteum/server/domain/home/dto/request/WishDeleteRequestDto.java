package umc.teumteum.server.domain.home.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(title = "WishDeleteRequestDTO : 위시 삭제 요청 DTO")
public class WishDeleteRequestDto {

    @NotEmpty
    @NotNull
    @Schema(description = "위시 ID 리스트", example = "[1,2,3]")
    private List<Long> wishIds;
}
