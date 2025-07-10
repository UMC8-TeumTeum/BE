package umc.teumteum.server.domain.teum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.teumteum.server.domain.teum.dto.TeumRequestDto;

@Service
@RequiredArgsConstructor
public class TeumServiceImpl implements TeumService {

    @Override
    @Transactional
    public Long createRequest(TeumRequestDto requestDto) {
        // TODO : 틈 요청 로직 추후 구현
        return 1L;
    }
}
