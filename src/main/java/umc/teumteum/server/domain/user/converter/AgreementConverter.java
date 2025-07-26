package umc.teumteum.server.domain.user.converter;

import umc.teumteum.server.domain.user.dto.OnboardingRequestDto;
import umc.teumteum.server.domain.user.entity.Agreement;
import umc.teumteum.server.domain.user.entity.User;

public class AgreementConverter {

    public static Agreement toAgreement(OnboardingRequestDto.AgreeRequest request, User user) {
        return Agreement.builder()
                .user(user)
                .thirdPartyConsent(request.getThirdPartyConsent())
                .marketingConsent(request.getMarketingConsent())
                .build()
                ;
    }
}