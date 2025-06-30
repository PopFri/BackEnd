package popfri.spring.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import popfri.spring.apiPayload.code.status.ErrorStatus;
import popfri.spring.apiPayload.exception.handler.MovieHandler;
import popfri.spring.domain.Review;
import popfri.spring.repository.RecHistoryRepository;
import popfri.spring.repository.ReviewRepository;
import popfri.spring.web.dto.GPTRequest;
import popfri.spring.web.dto.GPTResponse;
import popfri.spring.web.dto.MovieResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {
    private final ReviewRepository reviewRepository;
    private final RecHistoryRepository recHistoryRepository;
    private final OkHttpClient client = new OkHttpClient();

    @Value("${tmdb.api.key}")
    private String tmdbKey;

    private final String gptModel = "gpt-3.5-turbo";
    private final String gptUrl = "https://api.openai.com/v1/chat/completions";
    @Value("${OPENAI_API_KEY}")
    private String gptKey;

    @Value("${KOFIC_API_KEY}")
    private String koficKey;

    private final List<String> dummyDate = List.of("20140712", "20220805", "20180420", "20241115");
    private Integer dummyIndex = 0;
    private final Map<String, List<String>> dummyMovie = Map.ofEntries(
            Map.entry("20140712", List.of(
                    "군도: 민란의 시대",
                    "드래곤 길들이기 2",
                    "혹성탈출: 반격의 서막",
                    "신의 한 수",
                    "주온 : 끝의 시작",
                    "트랜스포머: 사라진 시대",
                    "프란시스 하",
                    "마담 프루스트의 비밀정원",
                    "그녀",
                    "명량"
            )),
            Map.entry("20220805", List.of(
                    "한산: 용의 출현",
                    "비상선언",
                    "탑건: 매버릭",
                    "미니언즈 2",
                    "뽀로로 극장판 드래곤캐슬 대모험",
                    "헤어질 결심",
                    "외계+인 1부",
                    "명탐정 코난: 할로윈의 신부",
                    "토르: 러브 앤 썬더"
            )),
            Map.entry("20180420", List.of(
                    "어벤져스: 인피니티 워",
                    "램페이지",
                    "혹성탈출: 반격의 서막",
                    "레디 플레이어 원",
                    "콰이어트 플레이스",
                    "곤지암",
                    "지금 만나러 갑니다",
                    "퍼시픽 림: 업라이징",
                    "리틀 포레스트"
            )),
            Map.entry("20241115", List.of(
                    "글래디에이터 Ⅱ",
                    "청설",
                    "사흘",
                    "베놈: 라스트 댄스",
                    "아마존 활명수",
                    "히든페이스",
                    "연소일기",
                    "날씨의 아이",
                    "컨택트",
                    "4월이 되면 그녀는"
            ))
    );

    private final Executor asyncExecutor = Executors.newFixedThreadPool(10);
    private final Cache<String, MovieResponse.TmdbDataByTitleDTO> tmdbTitleCache;
    private final Cache<String, List<MovieResponse.MovieRankingDTO>> boxofficeCache;

    public CompletableFuture<MovieResponse.MovieDetailDTO> loadMovieAsync(String movieId) {
        return CompletableFuture.supplyAsync(() -> loadMovie(movieId), asyncExecutor);
    }

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

    // 영화 상세 정보 호출
    public MovieResponse.MovieDetailDTO loadMovie(String movieId) {
        ObjectMapper objectMapper = new ObjectMapper();
        MovieResponse.MovieDetailDTO result = new MovieResponse.MovieDetailDTO();

        try {
            result.setMovieId(movieId);
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
            for (int i = 0; i < Math.min(9, backdropsNode.size()); i++) {
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

    //GPT 상황별 영화 추천
    public String getSitMovieToGPT(String situation){
        //WebClient build
        String prompt = "다음 상황에서 추천하는 영화 이름 하나를 출력해줘. 영화는 인지도가 10점 만점에 6점 이상인 영화로 추천해줘."
                + situation + "\n 다른 부연설명이나 외적 설정(ex. \"\", 각종 이모지) 없이 영화 제목만 출력해줘.+\n" +
                "또한 괄호를 통해 영문 번역본 제공하지 말고 영화제목만 알려줘. (ex. 라라랜드)";
        GPTRequest.gptReqDTO request = new GPTRequest.gptReqDTO(gptModel, prompt);
        WebClient webClient = WebClient.builder()
                .baseUrl(gptUrl)
                .defaultHeader("Authorization", "Bearer " + gptKey)
                .build();

        //connect to GPT
        GPTResponse.gptResDTO response;
        try {
            response = webClient.post()
                    .uri(gptUrl)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GPTResponse.gptResDTO.class)
                    .block();
        } catch (WebClientResponseException.BadRequest e) {
            throw new MovieHandler(ErrorStatus._GPT_CONNECT_FAIL);
        }

        //save response
        if (response != null) {
            log.info("Situation GPT Answer: " + response.getChoices().get(0).getMessage().getContent());
            return response.getChoices().get(0).getMessage().getContent();
        } else {
            throw new MovieHandler(ErrorStatus._GPT_CONNECT_FAIL);
        }
    }

    //GPT 시간별 영화 추천
    public String getTimeMovieToGPT(){
        LocalTime now = LocalTime.now();
        log.info("Current Time: " + now);

        //WebClient build
        String prompt = "다음으로 주어지는 시간에 어울리는 영화 이름을 출력해줘. 영화는 인지도가 10점 만점에 6점 이상인 영화들로 추천해줘." +
                        "실제로 존재하는 영화인지 검증하고 답변해줘.\n " +
                        now + "\n 다른 부연설명이나 외적 설정(ex. \"\", 각종 이모지) 없이 영화 제목만 출력해줘. " +
                        "또한 괄호를 통해 영문 번역본 제공하지 말고 영화제목만 알려줘. (ex. 오펜하이머)";
        GPTRequest.gptReqDTO request = new GPTRequest.gptReqDTO(gptModel, prompt);
        WebClient webClient = WebClient.builder()
                .baseUrl(gptUrl)
                .defaultHeader("Authorization", "Bearer " + gptKey)
                .build();

        //connect to GPT
        GPTResponse.gptResDTO response;
        try {
            response = webClient.post()
                    .uri(gptUrl)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GPTResponse.gptResDTO.class)
                    .block();
        } catch (WebClientResponseException.BadRequest e) {
            throw new MovieHandler(ErrorStatus._GPT_CONNECT_FAIL);
        }

        //save response
        if (response != null) {
            log.info("Situation GPT Answer: " + response.getChoices().get(0).getMessage().getContent());
            return response.getChoices().get(0).getMessage().getContent();
        } else {
            throw new MovieHandler(ErrorStatus._GPT_CONNECT_FAIL);
        }
    }

    //TMDB 영화 검색
    public MovieResponse.RecMovieResDTO getMovieIdToName(String name){
        //webClient build
        WebClient webClient = WebClient.builder()
                .baseUrl("https://api.themoviedb.org/3/search/movie")
                .defaultHeader("Authorization", "Bearer " + tmdbKey)
                .build();

        //tmdb connect
        MovieResponse.TmdbMovieRecResDTO response;
        response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("query", name)
                        .queryParam("include_adult", true)
                        .queryParam("language", "ko")
                        .queryParam("region", "ko")
                        .build())
                .retrieve()
                .bodyToMono(MovieResponse.TmdbMovieRecResDTO.class)
                .block();

        //response build
        if(response != null) {
            MovieResponse.TmdbMovieRecResDTO.Result result;

            if (!response.getResults().isEmpty()) {
                result = response.getResults().stream()
                        .max(Comparator.comparing(MovieResponse.TmdbMovieRecResDTO.Result::getPopularity))
                        .get();
                return MovieResponse.RecMovieResDTO.builder()
                        .movieId(result.getId())
                        .movieName(result.getTitle())
                        .imageUrl(result.getPoster_path())
                        .build();
            } else
                throw new MovieHandler(ErrorStatus._MOVIE_NOT_EXIST);
        }
        else
            throw new MovieHandler(ErrorStatus._TMDB_CONNECT_FAIL);
    }

    //TMDB 영화 추천
    public List<MovieResponse.RecMovieResDTO> recommendMovieFromTMDB(Integer movieId){
        //webClient build
        WebClient webClient = WebClient.builder()
                .baseUrl("https://api.themoviedb.org/3")
                .defaultHeader("Authorization", "Bearer " + tmdbKey)
                .build();

        //tmdb connect
        MovieResponse.TmdbMovieRecResDTO response;
        response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/{movieId}/recommendations")
                        .queryParam("language", "ko")
                        .build(movieId))
                .retrieve()
                .bodyToMono(MovieResponse.TmdbMovieRecResDTO.class)
                .defaultIfEmpty(new MovieResponse.TmdbMovieRecResDTO())
                .block();

        //response build
        if(response != null) {
            return response.getResults().stream()
                    .sorted(Comparator.comparing(MovieResponse.TmdbMovieRecResDTO.Result::getPopularity).reversed())
                    .map(result -> MovieResponse.RecMovieResDTO.builder()
                            .movieId(result.getId())
                            .movieName(result.getTitle())
                            .imageUrl(result.getPoster_path())
                            .build())
                    .toList();
        } else
            throw new MovieHandler(ErrorStatus._TMDB_CONNECT_FAIL);
    }

    // 영화 제목으로 TMDB API 호출
    public MovieResponse.TmdbDataByTitleDTO searchTmdbMovieByTitle(String title) {
        MovieResponse.TmdbDataByTitleDTO cached = tmdbTitleCache.getIfPresent(title);
        if (cached != null) return cached;

        WebClient webClient = WebClient.builder()
                .baseUrl("https://api.themoviedb.org/3/search/movie")
                .defaultHeader("Authorization", "Bearer " + tmdbKey)
                .build();

        MovieResponse.TmdbDataByTitleListDTO movieList = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("query", title)
                        .queryParam("include_adult", false)
                        .queryParam("language", "ko")
                        .queryParam("page", "1")
                        .build())
                .retrieve()
                .bodyToMono(MovieResponse.TmdbDataByTitleListDTO.class)
                .block();

        if (movieList == null) throw new MovieHandler(ErrorStatus._TMDB_CONNECT_FAIL);

        MovieResponse.TmdbDataByTitleDTO result = movieList.getMovieDataList().stream()
                .max(Comparator.comparingDouble(MovieResponse.TmdbDataByTitleDTO::getPopularity))
                .orElse(null);

        if (result != null) {
            tmdbTitleCache.put(title, result);
        }

        return result;
    }

    // 박스오피스 랭킹 반환
    public List<MovieResponse.MovieRankingDTO> getBoxofficeRanking(String date) {
        List<MovieResponse.MovieRankingDTO> cached = boxofficeCache.getIfPresent(date);
        if (cached != null) return cached;

        WebClient webClient = WebClient.builder()
                .baseUrl("http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json")
                .build();

        MovieResponse.BoxofficeMovieDataDTO rankingMovie = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("key", koficKey)
                        .queryParam("targetDt", date)
                        .build())
                .retrieve()
                .bodyToMono(MovieResponse.BoxofficeMovieDataDTO.class)
                .block();

        if (rankingMovie == null) throw new MovieHandler(ErrorStatus._KOFIC_CONNECT_FAIL);
        if (rankingMovie.getBoxOfficeResult().getMovieDataList().isEmpty())
            throw new MovieHandler(ErrorStatus._MOVIE_DATE_FAIL);

        List<MovieResponse.MovieRankingDTO> rankingList = new ArrayList<>();
        for (MovieResponse.BoxofficeMovieDataDTO.BoxofficeResult.MovieData movieData : rankingMovie.getBoxOfficeResult().getMovieDataList()) {
            MovieResponse.TmdbDataByTitleDTO tmdbData = searchTmdbMovieByTitle(movieData.getTitle());
            if (tmdbData == null) {
                rankingList.add(MovieResponse.MovieRankingDTO.builder()
                        .rank(movieData.getRank())
                        .movieName(movieData.getTitle())
                        .backgroundImageUrl(null)
                        .movieId(0L)
                        .overView("정보가 없습니다.")
                        .imageUrl(null)
                        .build());
            } else {
                rankingList.add(MovieResponse.MovieRankingDTO.builder()
                        .rank(movieData.getRank())
                        .movieName(movieData.getTitle())
                        .backgroundImageUrl(tmdbData.getBackgroundImageUrl())
                        .movieId(tmdbData.getMovieId())
                        .overView(tmdbData.getOverView())
                        .imageUrl(tmdbData.getImageUrl())
                        .build());
            }
        }

        boxofficeCache.put(date, rankingList);
        return rankingList;
    }
    // 탐색된 영화 리스트 반환
    public MovieResponse.MovieDiscoveryDTO getDiscoveryMovieList() {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
//        LocalDate startDate = LocalDate.parse("20031201", formatter);
//        LocalDate endDate = LocalDate.now();
//
//        long startEpochDay = startDate.toEpochDay();
//        long endEpochDay = endDate.toEpochDay();
//
//        long randomDay = ThreadLocalRandom.current().nextLong(startEpochDay, endEpochDay + 1);
//        LocalDate randomDate = LocalDate.ofEpochDay(randomDay);
//
//        String date = randomDate.format(formatter);
//        List<MovieResponse.MovieRankingDTO> rankingList = getBoxofficeRanking(date);

        String date = dummyDate.get(dummyIndex);
        dummyIndex = (dummyIndex + 1) % dummyDate.size();
        List<MovieResponse.MovieRankingDTO> rankingList = dummyMovie.get(date).stream()
                .map(movie -> MovieResponse.MovieRankingDTO.builder()
                        .movieName(movie)
                        .build())
                .toList();

        List<CompletableFuture<MovieResponse.MovieDetailDTO>> futures = rankingList.stream()
                .map(r -> {
                    MovieResponse.TmdbDataByTitleDTO tmdbData = searchTmdbMovieByTitle(r.getMovieName());
                    if (tmdbData == null) return null;
                    return loadMovieAsync(String.valueOf(tmdbData.getMovieId()));
                })
                .filter(Objects::nonNull)
                .toList();

        List<MovieResponse.MovieDetailDTO> movieList = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList();

        return MovieResponse.MovieDiscoveryDTO.builder()
                .date(date.substring(0, 4) + "." + date.substring(4, 6) + "." + date.substring(6, 8) + " Box Office Rank")
                .movies(movieList)
                .build();
    }


    //영화 탐색 결과 반환
    public MovieResponse.MovieDiscoveryResultDTO getMovieDiscoveryResult(List<MovieResponse.DiscoveryMovie> choosedMovie) {
        List<MovieResponse.RecMovieResDTO> recommendMovie = new ArrayList<>();
        int maxRecommendSize = 15;
        int recommendSize = maxRecommendSize / choosedMovie.size();
        for(MovieResponse.DiscoveryMovie movie: choosedMovie) {
            List<MovieResponse.RecMovieResDTO> allRecommended = recommendMovieFromTMDB(Integer.parseInt(movie.getId()));
            if (allRecommended == null || allRecommended.size() == 0) {
                continue;
            }
            List<MovieResponse.RecMovieResDTO> randomRecommended = new Random()
                    .ints(0, allRecommended.size())
                    .distinct()
                    .limit(recommendSize)
                    .mapToObj(allRecommended::get)
                    .toList();
            for(MovieResponse.RecMovieResDTO recommendedMovie: randomRecommended) {
                recommendMovie.add(recommendedMovie);
            }
        }

        return MovieResponse.MovieDiscoveryResultDTO.builder()
                .choosed(choosedMovie)
                .recommend(recommendMovie)
                .build();
    }

    public List<MovieResponse.RecReviewMovieResDTO> recReviewMovie(){
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

        return reviewRepository.findTop10ByCreatedAtAfter(oneMonthAgo).stream()
                .sorted(Comparator.comparingInt(
                        (Review review) -> review.getLikeCount() * 2 - review.getDislikeCount()
                ).reversed())
                .map(review -> MovieResponse.RecReviewMovieResDTO.builder()
                        .movieId(review.getMovieId())
                        .movieName(review.getMovieName())
                        .posterUrl(review.getPosterUrl())
                        .reviewId(review.getReviewId())
                        .reviewContents(review.getReviewContent())
                        .likeCnt(review.getLikeCount())
                        .userName(review.getUser().getUserName())
                        .profileUrl(review.getUser().getImageUrl())
                        .build())
                .limit(10)
                .toList();
    }
}