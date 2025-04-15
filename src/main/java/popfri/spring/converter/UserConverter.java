package popfri.spring.converter;

import popfri.spring.domain.User;
import popfri.spring.web.dto.UserResponse;

public class UserConverter {

    public static UserResponse.UserGetResDTO getUserDto (User user){
        return UserResponse.UserGetResDTO.builder()
                .userId(user.getUserId())
                .name(user.getUserName())
                .email(user.getUserEmail())
                .imageUrl(user.getImageUrl())
                .build();
    }
}
