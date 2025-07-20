package umc.teumteum.server.domain.home.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.teumteum.server.domain.home.entity.enums.EstimatedDuration;
import umc.teumteum.server.domain.home.entity.mapping.WishCategory;
import umc.teumteum.server.domain.user.entity.User;
import umc.teumteum.server.global.common.BaseEntity;

import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wish")
public class Wish extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "estimated_duration", nullable = false)
    private EstimatedDuration estimatedDuration;


    /*
        양방향 연관관계
    */
    @OneToMany(mappedBy = "wish", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WishCategory> wishCategories;

    public void setWishCategories(List<WishCategory> wishCategories) {
        this.wishCategories = wishCategories;
        for (WishCategory wishCategory : wishCategories) {
            wishCategory.setWish(this);
        }
    }

    public void update(String title, String content, EstimatedDuration duration) {
        this.title = title;
        this.content = content;
        this.estimatedDuration = duration;
    }
}
