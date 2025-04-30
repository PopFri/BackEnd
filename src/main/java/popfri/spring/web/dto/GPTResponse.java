package popfri.spring.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class GPTResponse {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class gptChatMessage {
        private String role;
        private String content;
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class gptResDTO {

        private List<Choice> choices;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Choice {
            private int index;
            private gptChatMessage message;
        }
    }
}
