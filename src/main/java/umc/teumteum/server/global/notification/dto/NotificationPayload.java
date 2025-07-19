package umc.teumteum.server.global.notification.dto;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import umc.teumteum.server.global.notification.type.NotificationType;

@Getter
@Builder
@AllArgsConstructor
public class NotificationPayload {
  private final String title;
  private final String content;
  private final NotificationType type;
  private final Map<String,String> data;

  public Map<String, String> toFcmData() {
    Map<String, String> result = new HashMap<>(data);
    result.put("type", type.name());
    return result;
  }

}
