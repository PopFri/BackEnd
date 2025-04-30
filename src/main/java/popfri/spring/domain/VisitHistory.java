package popfri.spring.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import popfri.spring.domain.common.BaseEntity;
import popfri.spring.domain.enums.RecType;

@Entity
@Getter
@Builder
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class VisitHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "visit_id")
    private Long visitId;

    @Column(name = "tmdb_id")
    private Integer tmdbId;

    @Column(name = "movie_name", columnDefinition = "VARCHAR(100)")
    private String movieName;

    @Column(name = "poster_url", columnDefinition = "VARCHAR(1000)")
    private String posterUrl;

    @Setter
    @Column(name = "visit_cnt")
    private Integer visitCnt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
