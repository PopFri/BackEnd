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
public class RecHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rec_id")
    private Long recId;

    @Column(name = "tmdb_id")
    private Integer tmdbId;

    @Column(name = "movie_name", columnDefinition = "VARCHAR(100)")
    private String movieName;

    @Column(name = "poster_url", columnDefinition = "VARCHAR(1000)")
    private String posterUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "rec_type", columnDefinition = "VARCHAR(15)")
    private RecType recType;

    @Setter
    @Column(name = "rec_cnt")
    private Integer recCnt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
