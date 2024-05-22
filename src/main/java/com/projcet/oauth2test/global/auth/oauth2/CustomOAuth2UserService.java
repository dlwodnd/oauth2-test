package com.projcet.oauth2test.global.auth.oauth2;



import com.projcet.oauth2test.domain.user.config.Platform;
import com.projcet.oauth2test.domain.user.entity.Role;
import com.projcet.oauth2test.domain.user.entity.User;
import com.projcet.oauth2test.domain.user.repository.UserRepository;
import com.projcet.oauth2test.global.auth.oauth2.social_info.OAuth2UserInfo;
import com.projcet.oauth2test.global.auth.oauth2.social_info.OAuth2UserInfoFactory;
import com.projcet.oauth2test.global.security.MyPrincipal;
import com.projcet.oauth2test.global.security.MyUserDetails;
import com.projcet.oauth2test.global.security.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final OAuth2UserInfoFactory factory;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);
        try {
            return this.process(userRequest, user);
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalAuthenticationServiceException(e.getMessage(), e.getCause());
        }
    }

    private OAuth2User process(OAuth2UserRequest userRequest, OAuth2User user) {
        Platform socialType
                    = Platform.valueOf(userRequest.getClientRegistration()
                                                            .getRegistrationId()
                                                            .toUpperCase()
                    );

        OAuth2UserInfo oAuth2UserInfo = factory.getOAuth2UserInfo(socialType, user.getAttributes());
        User userEntity = userRepository.findByEmail(oAuth2UserInfo.getEmail())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(oAuth2UserInfo.getEmail())
                            .nickname(oAuth2UserInfo.getName())
                            .profileImage(oAuth2UserInfo.getImageUrl())
                            .role(Role.USER)
                            .build();
                    return userRepository.save(newUser);
                });
        userRepository.saveAndFlush(userEntity);

        MyPrincipal myPrincipal = MyPrincipal.builder()
                .userPk(userEntity.getId())
                .role(userEntity.getRole().name())
                .build();
         UserInfo userInfo = UserInfo.builder()
                .userPk(userEntity.getId())
                .userPic(userEntity.getProfileImage())
                .userEmail(userEntity.getEmail())
                .role(userEntity.getRole())
                .nickname(userEntity.getNickname())
                .build();
        return MyUserDetails.builder()
                .userInfo(userInfo)
                .myPrincipal(myPrincipal)
                .attributes(user.getAttributes())
                .build();
    }

}
