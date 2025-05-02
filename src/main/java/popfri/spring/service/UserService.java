package popfri.spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import popfri.spring.apiPayload.code.status.ErrorStatus;
import popfri.spring.apiPayload.exception.handler.UserHandler;
import popfri.spring.domain.User;
import popfri.spring.domain.enums.Gender;
import popfri.spring.repository.UserRepository;
import popfri.spring.web.dto.UserRequest;

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

    @Transactional
    public Boolean resignUser (User user){
        userRepository.delete(user);

        return true;
    }

    @Transactional
    public Boolean setGenderAndBirth(User user, UserRequest.AddGenderAndBirthDto request){
        if(user.getGender() == null) {
            switch (request.getGender()) {
                case "male" -> user.setGender(Gender.MALE);
                case "female" -> user.setGender(Gender.FEMALE);
                default -> throw new UserHandler(ErrorStatus._GENDER_INVALID);
            }
        } else
            throw new UserHandler(ErrorStatus._USER_DATA_EXIST);

        if(user.getBirth() == null)
            user.setBirth(request.getBirth());
        else
            throw new UserHandler(ErrorStatus._USER_DATA_EXIST);

        userRepository.save(user);

        return true;
    }
}
