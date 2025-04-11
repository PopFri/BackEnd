package popfri.spring.OAuth2.dto;

import lombok.*;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String role;
    private String email;
    private String name;
    private String provideId;
}
