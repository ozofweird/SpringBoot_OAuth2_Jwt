package com.tistory.ozofweird.springboot_oauth2_jwt.security.oauth2;

import com.tistory.ozofweird.springboot_oauth2_jwt.domain.user.AuthProvider;
import com.tistory.ozofweird.springboot_oauth2_jwt.domain.user.User;
import com.tistory.ozofweird.springboot_oauth2_jwt.domain.user.UserRepository;
import com.tistory.ozofweird.springboot_oauth2_jwt.exception.OAuth2AuthenticationProcessingException;
import com.tistory.ozofweird.springboot_oauth2_jwt.security.UserPrincipal;
import com.tistory.ozofweird.springboot_oauth2_jwt.security.oauth2.user.OAuth2UserInfo;
import com.tistory.ozofweird.springboot_oauth2_jwt.security.oauth2.user.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    // 시용자 정보 추출
    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
        if(StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("OAuth2 공급자(구글, 네이버, ...) 에서 이메일을 찾을 수 없습니다.");
        }

        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;
        if(userOptional.isPresent()) {
            user = userOptional.get();
            if(!user.getProvider().equals(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))) {
                throw new OAuth2AuthenticationProcessingException(
                        user.getProvider() + "계정을 사용하기 위해서 로그인을 해야합니다.");
            }
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    // DB에 존재하지 않을 경우 새로 등록
    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {

        return userRepository.save(User.builder()
                .name(oAuth2UserInfo.getName())
                .email(oAuth2UserInfo.getEmail())
                .imageUrl(oAuth2UserInfo.getImageUrl())
                .provider(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))
                .providerId(oAuth2UserInfo.getId())
                .build()
        );
    }

    // DB에 존재할 경우 정보 업데이트
    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {

        return userRepository.save(existingUser
                .update(
                        oAuth2UserInfo.getName(),
                        oAuth2UserInfo.getImageUrl()
                )
        );
    }
}
