package popfri.spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import popfri.spring.apiPayload.code.status.ErrorStatus;
import popfri.spring.apiPayload.exception.handler.UserHandler;
import popfri.spring.domain.User;
import popfri.spring.repository.UserRepository;
import popfri.spring.web.dto.UserResponse;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User getUser (String provideId){
        User user = userRepository.findByProvideId(provideId);
        if(user == null){
            throw new UserHandler(ErrorStatus._USER_NOT_EXIST);
        }

        return user;
    }
}
