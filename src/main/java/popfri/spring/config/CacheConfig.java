package popfri.spring.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import popfri.spring.web.dto.MovieResponse;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, MovieResponse.TmdbDataByTitleDTO> tmdbTitleCache() {
        return Caffeine.newBuilder()
                .maximumSize(500) // 500개 넘으면 가장 오래된 거 삭제
                .expireAfterWrite(6, TimeUnit.HOURS) // 6시간 지나면 삭제
                .build();
    }

    @Bean
    public Cache<String, List<MovieResponse.MovieRankingDTO>> boxofficeCache() {
        return Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(6, TimeUnit.HOURS)
                .build();
    }
}