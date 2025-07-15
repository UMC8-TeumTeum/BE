package umc.teumteum.server.domain.teum.converter;

import umc.teumteum.server.domain.teum.dto.teum.TeumRequestDto;
import umc.teumteum.server.domain.teum.entity.TeumRequest;
import umc.teumteum.server.domain.teum.entity.TeumResponse;
import umc.teumteum.server.domain.teum.entity.enums.ResponseStatus;
import umc.teumteum.server.domain.teum.exception.status.TeumErrorStatus;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.exception.GeneralException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Function;

public class TeumConverter {

    public static TeumRequest toTeumRequest(TeumRequestDto dto, User sender) {
        return TeumRequest.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .date(LocalDate.parse(dto.getDate()))
                .startTime(LocalTime.parse(dto.getStartTime()))
                .endTime(LocalTime.parse(dto.getEndTime()))
                .graphicId(dto.getGraphicId())
                .user(sender)
                .build();
    }

    public static List<TeumResponse> toTeumResponses(
            List<Long> receiverUserIds, Long senderId,
            TeumRequest request, Function<Long, User> userFetcher
    ) {
        return receiverUserIds.stream()
                .distinct()
                .map(receiverId -> {
                    if (receiverId.equals(senderId)) {
                        throw new GeneralException(TeumErrorStatus.CANNOT_REQUEST_SELF);
                    }
                    User receiver = userFetcher.apply(receiverId);
                    return TeumResponse.builder()
                            .teumRequest(request)
                            .receiverUser(receiver)
                            .status(ResponseStatus.PENDING)
                            .readAt(null)
                            .message("")
                            .build();
                })
                .toList();
    }

}
