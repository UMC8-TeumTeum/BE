package umc.teumteum.server.domain.teum.converter;

import java.time.Duration;
import umc.teumteum.server.domain.teum.dto.common.TimeSlot;
import umc.teumteum.server.domain.teum.dto.teum.TeumReceivedResponseDto;
import umc.teumteum.server.domain.teum.dto.teum.TeumRequestDto;
import umc.teumteum.server.domain.teum.dto.teum.TeumResendRequestDto;
import umc.teumteum.server.domain.teum.entity.TeumRequest;
import umc.teumteum.server.domain.teum.entity.TeumResponse;
import umc.teumteum.server.domain.teum.entity.enums.ResponseStatus;
import umc.teumteum.server.domain.teum.exception.status.TeumErrorStatus;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.exception.GeneralException;
import umc.teumteum.server.domain.teum.dto.common.ParticipantDto;
import umc.teumteum.server.global.util.S3Util;

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

    public static TeumReceivedResponseDto toReceivedResponseDto(TeumResponse response, S3Util s3Util) {
        TeumRequest request = response.getTeumRequest();
        User sender = request.getUser();

        return TeumReceivedResponseDto.builder()
                .responseId(response.getId())
                .requestId(request.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .graphicId(request.getGraphicId())
                .isRead(response.getReadAt() != null)
                .receiverCount(request.getTeumResponses().size())
                .senderUser(ParticipantDto.builder()
                        .userId(sender.getId())
                        .nickname(sender.getNickname())
                        .profileImageUrl(s3Util.toPresignedUrl(sender.getProfileImageKey(),
                            Duration.ofMinutes(30)))
                        .build())
                .date(request.getDate().toString())
                .timeSlot(TimeSlot.builder()
                        .start(request.getStartTime().toString())
                        .end(request.getEndTime().toString())
                        .build())
                .build();
    }


    public static List<TeumReceivedResponseDto> toReceivedResponseDtoList(List<TeumResponse> responses, S3Util s3Util) {
        return responses.stream()
                .map(response -> toReceivedResponseDto(response, s3Util))
                .toList();
    }

    public static TeumRequest toResendTeumRequest(TeumRequest parent, TeumResendRequestDto dto, User resender) {
        return TeumRequest.builder()
                .title(parent.getTitle())
                .description(parent.getDescription())
                .date(parent.getDate())
                .startTime(LocalTime.parse(dto.getStartTime()))
                .endTime(LocalTime.parse(dto.getEndTime()))
                .graphicId(parent.getGraphicId())
                .user(resender)
                .parentRequest(parent)
                .build();
    }

    public static TeumResponse toResendTeumResponse(TeumRequest request, User newReceiver) {
        return TeumResponse.builder()
                .teumRequest(request)
                .receiverUser(newReceiver)
                .status(ResponseStatus.PENDING)
                .message("")
                .readAt(null)
                .build();
    }

}
