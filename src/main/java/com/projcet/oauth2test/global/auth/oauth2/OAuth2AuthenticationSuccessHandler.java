package com.projcet.oauth2test.global.auth.oauth2;



import com.projcet.oauth2test.global.auth.jwt.JwtProperties;
import com.projcet.oauth2test.global.auth.jwt.JwtTokenProvider;
import com.projcet.oauth2test.global.security.MyPrincipal;
import com.projcet.oauth2test.global.security.MyUserDetails;
import com.projcet.oauth2test.global.security.UserInfo;
import com.projcet.oauth2test.global.utils.CookieUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static com.projcet.oauth2test.global.auth.oauth2.OAuth2AuthenticationRequestBasedOnCookieRepository.REDIRECT_URI_PARAM_COOKIE_NAME;


@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final OAuth2AuthenticationRequestBasedOnCookieRepository repository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final CookieUtils cookieUtils;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        String targetUrl = determineTargetUrl(request, response, authentication);
        log.info("targetUrl: {}", targetUrl);
        if(response.isCommitted()) {
            log.error("Response has already been committed. Unable to redirect to {}", targetUrl);
            return;
        }

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<String> redirectUri = cookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                                                .map(Cookie::getValue);

        if(redirectUri.isPresent() && !hasAuthorizedRedirectUri(redirectUri.get())) {
            throw new IllegalArgumentException("Sorry!, Unauthorized Redirect URI");
        }

        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());

        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
        MyPrincipal myPrincipal = myUserDetails.getMyPrincipal();

        String at = jwtTokenProvider.generateAccessToken(myPrincipal);
        String rt = jwtTokenProvider.generateRefreshToken(myPrincipal);

        //rt > cookie에 담을꺼임
        int rtCookieMaxAge = jwtProperties.getJwt().getRefreshTokenCookieMaxAge();
        cookieUtils.deleteCookie(response, "rt");
        cookieUtils.setCookie(response, "rt", rt, rtCookieMaxAge);

        UserInfo userInfo = myUserDetails.getUserInfo();

        return UriComponentsBuilder.fromUriString(targetUrl)
                                    .queryParam("access_token", at)
                                    .queryParam("user_pk", userInfo)
                                    .queryParam("nickname", userInfo.getNickname()).encode()
                                    .queryParam("pic", userInfo.getUserPic())
                                    .build()
                                    .toUriString();
    }

    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        repository.removeAuthorizationRequestCookies(response);
    }

    private boolean hasAuthorizedRedirectUri(String uri) {
        URI clientRedriectUri = URI.create(uri);
        log.info("clientRedriectUri.getHost(): {}", clientRedriectUri.getHost());
        log.info("clientRedriectUri.getPort(): {}", clientRedriectUri.getPort());

        return jwtProperties.getOauth2().getAuthorizedRedirectUris()
                .stream()
                .anyMatch(redirectUri -> {
                    URI authorizedURI = URI.create(redirectUri);
                    if(authorizedURI.getHost().equalsIgnoreCase(clientRedriectUri.getHost())
                            && authorizedURI.getPort() == clientRedriectUri.getPort()) {
                        return true;
                    }
                    return false;
                });
    }
}

