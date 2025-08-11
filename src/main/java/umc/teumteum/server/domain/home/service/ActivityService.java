package umc.teumteum.server.domain.home.service;

import umc.teumteum.server.domain.home.dto.request.ActivityRequestDto.AiWishOptionRequest;
import umc.teumteum.server.domain.home.dto.request.ActivityRequestDto.AiWishSaveRequest;
import umc.teumteum.server.domain.home.dto.request.ActivityRequestDto.WishOptionRequest;
import umc.teumteum.server.domain.home.dto.response.ActivityResponseDto.AiSaveResponse;
import umc.teumteum.server.domain.home.dto.response.ActivityResponseDto.AiWishResponse;
import umc.teumteum.server.domain.home.dto.response.ActivityResponseDto.WishResponse;
import umc.teumteum.server.domain.user.entity.User;

public interface ActivityService {

  WishResponse getMyWish(User user, WishOptionRequest request);

  AiWishResponse getAiWish(User user, AiWishOptionRequest request);

  AiSaveResponse assginAiWish(User user, AiWishSaveRequest request);
}
