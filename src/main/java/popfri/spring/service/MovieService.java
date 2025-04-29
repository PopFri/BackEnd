package popfri.spring.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;
import popfri.spring.apiPayload.code.status.ErrorStatus;
import popfri.spring.apiPayload.exception.handler.MovieHandler;
import popfri.spring.web.dto.MovieResponse;
import org.springframework.beans.factory.annotation.Value;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovieService {
    private final OkHttpClient client = new OkHttpClient();

    @Value("${tmdb.api.key}")
    private String tmdbKey;

    // movie detail api 호출
    public String loadMovieDetail(String movieId) {
        Request request = new Request.Builder()
                .url("https://api.themoviedb.org/3/movie/" + movieId + "?language=ko")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer " + tmdbKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            } else {
                throw new MovieHandler(ErrorStatus._MOVIE_NOT_EXIST);
            }
        } catch (IOException e) {
            throw new MovieHandler(ErrorStatus._INTERNAL_SERVER_ERROR);
        }
    }

    // watch providers api 호출
    public String loadMovieProviders(String movieId) {
        Request request = new Request.Builder()
                .url("https://api.themoviedb.org/3/movie/" + movieId + "/watch/providers")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer " + tmdbKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            } else {
                throw new MovieHandler(ErrorStatus._MOVIE_NOT_EXIST);
            }
        } catch (IOException e) {
            throw new MovieHandler(ErrorStatus._INTERNAL_SERVER_ERROR);
        }
    }

    // 영화 이미지 호출
    public String loadMovieImages(String movieId) {
        Request request = new Request.Builder()
                .url("https://api.themoviedb.org/3/movie/" + movieId + "/images")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer " + tmdbKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            } else {
                throw new MovieHandler(ErrorStatus._MOVIE_NOT_EXIST);
            }
        } catch (IOException e) {
            throw new MovieHandler(ErrorStatus._INTERNAL_SERVER_ERROR);
        }
    }

    // 입력받은 내용의 정보 호출
    public String loadMovieInfo(String movieId, String api) {
        Request request = new Request.Builder()
                .url("https://api.themoviedb.org/3/movie/" + movieId + "/" + api + "?language=ko")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer " + tmdbKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            } else {
                throw new MovieHandler(ErrorStatus._MOVIE_NOT_EXIST);
            }
        } catch (IOException e) {
            throw new MovieHandler(ErrorStatus._INTERNAL_SERVER_ERROR);
        }
    }

    // 리뷰에 들어갈 영화 제목, 포스터 정보 호출
    public MovieResponse.MovieReviewDTO loadMovieReview(String movieId) {
        ObjectMapper objectMapper = new ObjectMapper();
        MovieResponse.MovieReviewDTO result = new MovieResponse.MovieReviewDTO();

        try {
            // 메인 정보
            String movieDetail = loadMovieDetail(movieId);
            JsonNode detailNode = objectMapper.readTree(movieDetail);
            result.setPosterUrl(detailNode.path("poster_path").asText());
            result.setMovieName(detailNode.path("title").asText());
        } catch (IOException e) {
            throw new MovieHandler(ErrorStatus._INTERNAL_SERVER_ERROR);
        }
        return result;
    }

    // 영화 상세 정보 호출
    public MovieResponse.MovieDetailDTO loadMovie(String movieId) {
        ObjectMapper objectMapper = new ObjectMapper();
        MovieResponse.MovieDetailDTO result = new MovieResponse.MovieDetailDTO();

        try {
            // 메인 정보
            String movieDetail = loadMovieDetail(movieId);
            JsonNode detailNode = objectMapper.readTree(movieDetail);
            result.setBackgroundImageUrl(detailNode.path("backdrop_path").asText());
            result.setImageUrl(detailNode.path("poster_path").asText());
            result.setTitle(detailNode.path("title").asText());
            result.setRuntime(detailNode.path("runtime").asInt());
            result.setRelease_date(detailNode.path("release_date").asText());
            result.setOverView(detailNode.path("overview").asText());
            JsonNode genresNode = detailNode.path("genres");
            List<MovieResponse.MovieDetailDTO.Genres> genres = objectMapper.readerForListOf(MovieResponse.MovieDetailDTO.Genres.class)
                    .readValue(genresNode);
            result.setGenres(genres);

            // 제공 플랫폼 (List<Providers>)
            String movieProviders = loadMovieProviders(movieId);
            JsonNode providersNode = objectMapper.readTree(movieProviders).path("results").path("KR").path("flatrate");
            if (providersNode.isArray()) {
                List<MovieResponse.MovieDetailDTO.Providers> providerList =
                        objectMapper.readerForListOf(MovieResponse.MovieDetailDTO.Providers.class)
                                .readValue(providersNode);
                result.setProviders(providerList);
            }

            // 출연진 (List<Cast>)
            String movieCredits = loadMovieInfo(movieId, "credits");
            JsonNode castNode = objectMapper.readTree(movieCredits).path("cast");
            List<JsonNode> limitedCastList = new ArrayList<>();
            for (int i = 0; i < Math.min(5, castNode.size()); i++) {
                limitedCastList.add(castNode.get(i));
            }
            List<MovieResponse.MovieDetailDTO.Cast> castList =
                    objectMapper.readerForListOf(MovieResponse.MovieDetailDTO.Cast.class)
                            .readValue(objectMapper.writeValueAsString(limitedCastList));
            result.setCast(castList);

            // 감독
            JsonNode directingNode = objectMapper.readTree(movieCredits).path("crew");
            String directorName = null;
            Double directorPopularity = 0.0;
            for (JsonNode castMember : directingNode) {
                if ("Directing".equals(castMember.path("known_for_department").asText())) {
                    if (castMember.path("popularity").asDouble() > directorPopularity) {
                        directorPopularity = castMember.path("popularity").asDouble();
                        directorName = castMember.path("name").asText();
                    }
                }
            }
            result.setDirecting(directorName);

            // 영상 (List<Videos>)
            String movieVideos = loadMovieInfo(movieId, "videos");
            JsonNode videosNode = objectMapper.readTree(movieVideos).path("results");
            List<JsonNode> limitedVideos = new ArrayList<>();
            for (int i = 0; i < Math.min(7, videosNode.size()); i++) {
                limitedVideos.add(videosNode.get(i));
            }
            List<MovieResponse.MovieDetailDTO.Videos> videosList =
                    objectMapper.readerForListOf(MovieResponse.MovieDetailDTO.Videos.class)
                            .readValue(objectMapper.writeValueAsString(limitedVideos));
            result.setVideos(videosList);

            // 이미지 (List<String>)
            String movieImages = loadMovieImages(movieId);
            JsonNode backdropsNode = objectMapper.readTree(movieImages).path("backdrops");
            List<JsonNode> limitedBackdrops = new ArrayList<>();
            for (int i = 0; i < Math.min(7, backdropsNode.size()); i++) {
                limitedBackdrops.add(backdropsNode.get(i));
            }
            List<MovieResponse.MovieDetailDTO.Images> imagesList =
                    objectMapper.readerForListOf(MovieResponse.MovieDetailDTO.Images.class)
                            .readValue(objectMapper.writeValueAsString(limitedBackdrops));
            result.setImages(imagesList);

        } catch (IOException e) {
            throw new MovieHandler(ErrorStatus._INTERNAL_SERVER_ERROR);
        }

        return result;
    }
}