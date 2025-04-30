package popfri.spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import popfri.spring.apiPayload.code.status.ErrorStatus;
import popfri.spring.apiPayload.exception.handler.HistoryHandler;
import popfri.spring.domain.RecHistory;
import popfri.spring.domain.User;
import popfri.spring.domain.enums.RecType;
import popfri.spring.repository.RecHistoryRepository;
import popfri.spring.web.dto.MovieResponse;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HistoryService {
    private final RecHistoryRepository recHistoryRepository;

    public void saveRecHistory(User user, List<MovieResponse.RecMovieResDTO> responseList, RecType recType){
        List<RecHistory> historyList = recHistoryRepository.findByUser(user);

        List<RecHistory> result = responseList.stream()
                .map(response -> {
                    Optional<RecHistory> recHistory = historyList.stream()
                            .filter(history -> Objects.equals(history.getTmdbId(), response.getMovieId()) && history.getRecType().equals(recType))
                            .findAny();

                    if(recHistory.isPresent()) {
                        RecHistory oldRecHistory = recHistory.get();
                        oldRecHistory.setRecCnt(oldRecHistory.getRecCnt() + 1);
                        return oldRecHistory;
                    } else {
                        return RecHistory.builder()
                                .recType(recType)
                                .tmdbId(response.getMovieId())
                                .movieName(response.getMovieName())
                                .posterUrl(response.getImageUrl())
                                .recCnt(1)
                                .user(user)
                                .build();
                    }
                })
                .toList();

        recHistoryRepository.saveAll(result);
    }

    //추천 기록 조회 서비스
    public List<RecHistory> getRecHistory(User user, String option){
        return switch (option) {
            case "default" -> recHistoryRepository.findDistinctTop10ByUserOrderByCreatedAtDesc(user);
            case "situation" -> recHistoryRepository.findByUserAndRecType(user, RecType.SITUATION);
            case "time" -> recHistoryRepository.findByUserAndRecType(user, RecType.TIME);
            case "popfri" -> recHistoryRepository.findByUserAndRecType(user, RecType.POPFRI);
            default -> throw new HistoryHandler(ErrorStatus._OPTION_NOT_EXIST);
        };
    }
}
