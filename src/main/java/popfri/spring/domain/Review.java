package popfri.spring.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "tmdb_id")
    private Long movieId;

    @Column(name = "movie_name", length = 100)
    private String movieName;

    @Column(name = "poster_url", length = 1000)
    private String posterUrl;

    @Column(name = "create_at")
    private LocalDateTime createdAt;

    @Column(name = "text", length = 150)
    private String reviewContent;

    @Builder.Default
    @Column(name="like_count")
    private Integer likeCount = 0;

    @Builder.Default
    @Column(name="dislike_count")
    private Integer dislikeCount = 0;

    public void addLike() {
        if (this.likeCount == null) this.likeCount = 0;
        this.likeCount++;
    }

    public void addDislike() {
        if (this.dislikeCount == null) this.dislikeCount = 0;
        this.dislikeCount++;
    }


    public void removeLike() {
        if (this.likeCount == null) {
            this.likeCount = 0;
            return;
        }
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }


    public void removeDislike() {
        if (this.dislikeCount == null) {
            this.dislikeCount = 0;
            return;
        }
        if (this.dislikeCount > 0) {
            this.dislikeCount--;
        }
    }

}
