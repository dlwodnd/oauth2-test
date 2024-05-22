package com.projcet.oauth2test.global.auth.oauth2.social_info;


import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class KakaoOAuth2UserInfo extends OAuth2UserInfo {
    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getName() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        return properties == null ? null : (String) properties.get("nickname");
    }

    @Override
    public String getEmail() {
        log.info("attributes: {}", attributes.toString());
        Map<String, Object> properties = (Map<String, Object>) attributes.get("kakao_account");
        log.info("properties: {}", properties.get("email").toString());
        return properties == null ? null : (String) properties.get("email");
    }

    @Override
    public String getImageUrl() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        return properties == null ? null : (String) properties.get("thumbnail_image");
    }
}
