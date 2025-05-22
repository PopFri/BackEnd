package popfri.spring.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
public class MovieResponse {

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class MovieDetailDTO {
        private String movieId;
        private String backgroundImageUrl;
        private String imageUrl;
        private String title;
        private String directing;
        private String release_date;
        private Integer runtime;
        private List<Providers> providers;
        private String overView;
        private List<Genres> genres;
        private List<Cast> cast;
        private List<Videos> videos;
        private List<Images> images;

        @JsonIgnoreProperties(ignoreUnknown = true)
        @Getter
        @Setter
        public static class Providers {
            String logo_path;
            String provider_id;
            String provider_name;
            String display_priority;
        }

        @Getter
        @Setter
        public static class Genres {
            String id;
            String name;
        }
        @JsonIgnoreProperties(ignoreUnknown = true)
        @Getter
        @Setter
        public static class Cast {
            String name;
            String character;
            String profile_path;
        }
        @JsonIgnoreProperties(ignoreUnknown = true)
        @Getter
        @Setter
        public static class Videos {
            String key;
            String name;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        @Getter
        @Setter
        public static class Images {
            String file_path;
        }
    }
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "MOVIE_RES_01 : 영화 추천 리스트 응답")
    public static class RecMovieResDTO {
        Integer movieId;
        String movieName;
        String imageUrl;
    }

    @Data
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "TMDB_RES_01 : 영화 검색 응답")
    public static class TmdbMovieRecResDTO {
        Integer page;
        List<Result> results;
        @Data
        @Getter
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Result{
            Integer id;
            String title;
            Double popularity;
            String poster_path;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class MovieRankingDTO {
        private Integer rank;
        private Long movieId;
        private String backgroundImageUrl;
        private String imageUrl;
        private String movieName;
        private String overView;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class BoxofficeMovieDataDTO {
        @JsonProperty("boxOfficeResult")
        private BoxofficeResult boxOfficeResult;

        @Getter
        @Setter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class BoxofficeResult{
            @JsonIgnoreProperties(ignoreUnknown = true)
            @Getter
            @Setter
            @JsonProperty("dailyBoxOfficeList")
            List<MovieData> movieDataList;

            @Getter
            @Setter
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class MovieData {
                @JsonProperty("movieNm")
                private String title;
                @JsonProperty("rank")
                private Integer rank;
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class TmdbDataByTitleListDTO {
        @JsonProperty("results")
        List<TmdbDataByTitleDTO> movieDataList;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class TmdbDataByTitleDTO {
        @JsonProperty("id")
        private Long movieId;

        @JsonProperty("title")
        private String title;

        @JsonProperty("overview")
        private String overView;

        @JsonProperty("backdrop_path")
        private String backgroundImageUrl;

        @JsonProperty("poster_path")
        private String imageUrl;

        @JsonProperty("release_date")
        private String releaseDate;

        @JsonProperty("popularity")
        private Double popularity;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "MOVIE_RES_02 : 영화한줄평 추천 리스트 응답")
    public static class RecReviewMovieResDTO {
        Long movieId;
        String movieName;
        String posterUrl;
        Long reviewId;
        String reviewContents;
        Integer likeCnt;
        String userName;
        String profileUrl;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class MovieDiscoveryDTO {
        private String date;
        private List<MovieDetailDTO> movies;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class MovieDiscoveryResultDTO {
        private List<DiscoveryMovie> choosed;
        private List<RecMovieResDTO> recommend;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class DiscoveryMovie {
        String id;
        String name;
        String imageUrl;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class RecHistoryDTO {
        private Integer movieId;
        private String movieName;
        private String posterUrl;
    }
}