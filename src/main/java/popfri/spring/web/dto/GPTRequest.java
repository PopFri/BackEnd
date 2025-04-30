package popfri.spring.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class GPTRequest {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class gptChatMessage {
        private String role;
        private String content;
    }

    @Data
    public static class gptReqDTO {

        private String model;
        private List<gptChatMessage> messages;
        private int n;

        public gptReqDTO(String model, String message) {
            this.model = model;
            this.messages = new ArrayList<>();
            this.messages.add(new gptChatMessage("user",message));

            this.n = 1;
        }
    }
}
