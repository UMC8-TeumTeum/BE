package umc.teumteum.server.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.global.common.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "agreement")
public class Agreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder.Default
    @Column(name = "tos_consent", nullable = false)
    private Boolean tosConsent = true;

    @Column(name = "privacy_consent", nullable = false)
    private Boolean privacyConsent = true;

    @Column(name = "third_party_consent", nullable = false)
    private Boolean thirdPartyConsent;

    @Column(name = "marketing_consent", nullable = false)
    private Boolean marketingConsent;
}