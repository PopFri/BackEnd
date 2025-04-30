package popfri.spring.converter;

import popfri.spring.domain.RecHistory;
import popfri.spring.domain.VisitHistory;
import popfri.spring.web.dto.HistoryResponse;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HistoryConverter {
    public static HistoryResponse.RecHistoryGetResDTO getRecHistoryDto(List<RecHistory> historyList, String recType){
        //날짜로 grouping
        Map<LocalDate, List<RecHistory>> groupList = historyList.stream()
                .collect(Collectors.groupingBy(history -> history.getUpdatedAt().toLocalDate()));

        //build response
        List<HistoryResponse.HistoryResDTO> response = groupList.keySet().stream().map(date -> HistoryResponse.HistoryResDTO.builder()
                .date(date)
                .movieList(groupList.get(date).stream()
                        .map(movie -> HistoryResponse.HistoryMovieResDTO.builder()
                                .movieId(movie.getTmdbId())
                                .movieName(movie.getMovieName())
                                .imageUrl(movie.getPosterUrl())
                                .build())
                        .toList())
                .build())
                .sorted(Comparator.comparing(HistoryResponse.HistoryResDTO::getDate).reversed())
                .toList();

        return HistoryResponse.RecHistoryGetResDTO.builder()
                .recType(recType)
                .historyList(response)
                .build();
    }

    public static List<HistoryResponse.HistoryResDTO> getVisitHistoryDto(List<VisitHistory> historyList){
        //날짜로 grouping
        Map<LocalDate, List<VisitHistory>> groupList = historyList.stream()
                .collect(Collectors.groupingBy(history -> history.getUpdatedAt().toLocalDate()));

        //build response
        return groupList.keySet().stream().map(date -> HistoryResponse.HistoryResDTO.builder()
                        .date(date)
                        .movieList(groupList.get(date).stream()
                                .map(movie -> HistoryResponse.HistoryMovieResDTO.builder()
                                        .movieId(movie.getTmdbId())
                                        .movieName(movie.getMovieName())
                                        .imageUrl(movie.getPosterUrl())
                                        .build())
                                .toList())
                        .build())
                .sorted(Comparator.comparing(HistoryResponse.HistoryResDTO::getDate).reversed())
                .toList();
    }
}
