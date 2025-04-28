package popfri.spring.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Getter
@Setter
public class MovieDetailResponse {

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class MovieDetailDTO {
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
}