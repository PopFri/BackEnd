package popfri.spring.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_email", length = 50)
    private String userEmail;

    @Column(name = "user_name", length = 10)
    private String userName;

    @Column(name = "image_url", length = 300)
    private String imageUrl;

    @Column(name = "provide_id", length = 300, unique = true)
    private String provideId;

    @Column(name = "login_type", length = 10)
    private String loginType;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<RecHistory> userFolderList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Review> review;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<LikeReview> likeReviewList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<DislikeReview> dislikeReviewList;
}
