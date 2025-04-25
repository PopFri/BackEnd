package popfri.spring.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;
import popfri.spring.web.dto.MovieDetailResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovieDetailService {
    private final OkHttpClient client = new OkHttpClient();

    public String getMovieDetail(String movieId) {
        Request request = new Request.Builder()
                .url("https://api.themoviedb.org/3/movie/" + movieId + "?language=ko")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI0MzQ3MDA4ZGI1ZTIyNDllNGRkNmNjMmQ1OGMxYWRmYiIsIm5iZiI6MTc0MTMyOTUwNS43NjQsInN1YiI6IjY3Y2E5NDYxMWY5ZWYzNTMyZGFmYzkyMCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.HiGSzkCsrcTBs7haL475_6ITUzvoWUR_m2vlYOJSLkg")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            } else {
                return "요청 실패: " + response.code();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "에러 발생: " + e.getMessage();
        }
    }

    public String getMovieProviders(String movieId) {
        Request request = new Request.Builder()
                .url("https://api.themoviedb.org/3/movie/" + movieId + "/watch/providers")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI0MzQ3MDA4ZGI1ZTIyNDllNGRkNmNjMmQ1OGMxYWRmYiIsIm5iZiI6MTc0MTMyOTUwNS43NjQsInN1YiI6IjY3Y2E5NDYxMWY5ZWYzNTMyZGFmYzkyMCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.HiGSzkCsrcTBs7haL475_6ITUzvoWUR_m2vlYOJSLkg")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            } else {
                return "요청 실패: " + response.code();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "에러 발생: " + e.getMessage();
        }
    }

    public String getMovieImages(String movieId) {
        Request request = new Request.Builder()
                .url("https://api.themoviedb.org/3/movie/" + movieId + "/images")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI0MzQ3MDA4ZGI1ZTIyNDllNGRkNmNjMmQ1OGMxYWRmYiIsIm5iZiI6MTc0MTMyOTUwNS43NjQsInN1YiI6IjY3Y2E5NDYxMWY5ZWYzNTMyZGFmYzkyMCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.HiGSzkCsrcTBs7haL475_6ITUzvoWUR_m2vlYOJSLkg")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            } else {
                return "요청 실패: " + response.code();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "에러 발생: " + e.getMessage();
        }
    }

    public String getMovieInfo(String movieId, String api) {
        Request request = new Request.Builder()
                .url("https://api.themoviedb.org/3/movie/" + movieId + "/" + api + "?language=ko")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI0MzQ3MDA4ZGI1ZTIyNDllNGRkNmNjMmQ1OGMxYWRmYiIsIm5iZiI6MTc0MTMyOTUwNS43NjQsInN1YiI6IjY3Y2E5NDYxMWY5ZWYzNTMyZGFmYzkyMCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.HiGSzkCsrcTBs7haL475_6ITUzvoWUR_m2vlYOJSLkg")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            } else {
                return "요청 실패: " + response.code();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "에러 발생: " + e.getMessage();
        }
    }

    public MovieDetailResponse getMovie(String movieId) {
        ObjectMapper objectMapper = new ObjectMapper();
        MovieDetailResponse movieDetailResponse = new MovieDetailResponse();
        MovieDetailResponse.Result result = new MovieDetailResponse.Result();
        movieDetailResponse.setResult(result);

        try {
            // 메인 정보
            String movieDetail = getMovieDetail(movieId);
            JsonNode detailNode = objectMapper.readTree(movieDetail);
            result.setBackgroundImageUrl(detailNode.path("backdrop_path").asText());
            result.setImageUrl(detailNode.path("poster_path").asText());
            result.setTitle(detailNode.path("title").asText());
            result.setRuntime(detailNode.path("runtime").asInt());
            result.setRelease_date(detailNode.path("release_date").asText());
            result.setOverView(detailNode.path("overview").asText());
            JsonNode genresNode = detailNode.path("genres");
            List<MovieDetailResponse.Result.Genres> genres = objectMapper.readerForListOf(MovieDetailResponse.Result.Genres.class)
                    .readValue(genresNode);
            result.setGenres(genres);

            // 제공 플랫폼 (List<Providers>)
            String movieProviders = getMovieProviders(movieId);
            JsonNode providersNode = objectMapper.readTree(movieProviders).path("results").path("KR").path("flatrate");
            if (providersNode.isArray()) {
                List<MovieDetailResponse.Result.Providers> providerList =
                        objectMapper.readerForListOf(MovieDetailResponse.Result.Providers.class)
                                .readValue(providersNode);
                result.setProviders(providerList);
            }

            // 출연진 (List<Cast>)
            String movieCredits = getMovieInfo(movieId, "credits");
            JsonNode castNode = objectMapper.readTree(movieCredits).path("cast");
            List<JsonNode> limitedCastList = new ArrayList<>();
            for (int i = 0; i < Math.min(5, castNode.size()); i++) {
                limitedCastList.add(castNode.get(i));
            }
            List<MovieDetailResponse.Result.Cast> castList =
                    objectMapper.readerForListOf(MovieDetailResponse.Result.Cast.class)
                            .readValue(objectMapper.writeValueAsString(limitedCastList));
            result.setCast(castList);

            // 감독
            JsonNode directingNode = objectMapper.readTree(movieCredits).path("crew");
            String directorName = null;
            for (JsonNode castMember : directingNode) {
                if ("Directing".equals(castMember.path("known_for_department").asText())) {
                    directorName = castMember.path("name").asText();
                    break;
                }
            }
            result.setDirecting(directorName);

            // 영상 (List<Videos>)
            String movieVideos = getMovieInfo(movieId, "videos");
            JsonNode videosNode = objectMapper.readTree(movieVideos).path("results");
            List<JsonNode> limitedVideos = new ArrayList<>();
            for (int i = 0; i < Math.min(7, videosNode.size()); i++) {
                limitedVideos.add(videosNode.get(i));
            }
            List<MovieDetailResponse.Result.Videos> videosList =
                    objectMapper.readerForListOf(MovieDetailResponse.Result.Videos.class)
                            .readValue(objectMapper.writeValueAsString(limitedVideos));
            result.setVideos(videosList);

            // 이미지 (List<String>)
            String movieImages = getMovieImages(movieId);
            JsonNode backdropsNode = objectMapper.readTree(movieImages).path("backdrops");
            List<JsonNode> limitedBackdrops = new ArrayList<>();
            for (int i = 0; i < Math.min(7, backdropsNode.size()); i++) {
                limitedBackdrops.add(backdropsNode.get(i));
            }
            List<MovieDetailResponse.Result.Images> imagesList =
                    objectMapper.readerForListOf(MovieDetailResponse.Result.Images.class)
                            .readValue(objectMapper.writeValueAsString(limitedBackdrops));
            result.setImages(imagesList);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return movieDetailResponse;
    }
}
