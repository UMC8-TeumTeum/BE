package umc.teumteum.server.domain.home.service;

import umc.teumteum.server.domain.home.dto.request.ActivityRequestDto.AiWishOptionRequest;
import umc.teumteum.server.domain.home.dto.request.ActivityRequestDto.WishOptionRequest;
import umc.teumteum.server.domain.home.dto.response.ActivityResponseDto.WishResponse;
import umc.teumteum.server.domain.user.entity.User;

public interface ActivityService {

  WishResponse getMyWish(User user, WishOptionRequest request);

  WishResponse getAiWish(User user, AiWishOptionRequest request);
}
