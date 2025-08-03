package umc.teumteum.server.domain.home.ai.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Component;
import umc.teumteum.server.domain.home.dto.response.ActivityResponseDto.WishDto;

@Component
public class AiWishContentSerializer {
  private final ObjectMapper objectMapper = new ObjectMapper();

  public String serialize(List<WishDto> content) {
    try {
      return objectMapper.writeValueAsString(content);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("직렬화 실패", e);
    }
  }

  public List<WishDto> deserialize(String json) {
    try {
      return objectMapper.readValue(json, new TypeReference<List<WishDto>>() {});
    } catch (IOException e) {
      throw new RuntimeException("역직렬화 실패", e);
    }
  }


}
