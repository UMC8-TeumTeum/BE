package umc.teumteum.server.domain.fcm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.common.BaseEntity;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FcmToken extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(unique = true)
  private String token;
  private LocalDateTime registeredAt;
  private Boolean isActive;

  @ManyToOne(fetch = FetchType.LAZY)
  private User user;

  public void activate() {
    this.isActive = true;
    this.registeredAt = LocalDateTime.now();
  }

  public void deactivate() {
    this.isActive = false;
  }

}