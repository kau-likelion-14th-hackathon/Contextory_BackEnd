package com.kbj.contextory.user.service;

import com.kbj.contextory.global.api.ErrorCode;
import com.kbj.contextory.global.exception.GeneralException;
import com.kbj.contextory.user.domain.User;
import com.kbj.contextory.user.dto.request.CreateTestUserRequest;
import com.kbj.contextory.user.dto.response.UserProfileResponse;
import com.kbj.contextory.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfileService {
    private final UserRepository userRepository;

    @Transactional
    public UserProfileResponse createTestUser(CreateTestUserRequest request){
        User newUser = User.builder()
                .username(request.getUsername())
                .introduction(request.getIntroduction())
                .build();

        User savedUser;
        try{
            savedUser = userRepository.save(newUser);
        }
        catch(Exception e){
            throw new GeneralException(ErrorCode.BAD_REQUEST);
        }
        return UserProfileResponse.from(savedUser);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new GeneralException(ErrorCode.USER_NOT_FOUND));

        return UserProfileResponse.from(user);
    }
}
