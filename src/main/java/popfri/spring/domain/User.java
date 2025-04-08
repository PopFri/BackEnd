package popfri.spring.domain;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "user_email", length = 50, unique = true)
    private String userEmail;

    @Column(name = "user_name", length = 10)
    private String userName;

    @Column(name = "image_url", length = 300)
    private String imageUrl;

    @Column(name = "login_type", length = 10)
    private String loginType;

    @Column(name = "access_token")
    private String accessToken;
}
