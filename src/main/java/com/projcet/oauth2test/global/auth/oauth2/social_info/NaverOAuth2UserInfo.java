package com.projcet.oauth2test.global.auth.oauth2.social_info;

import java.util.Map;

public class NaverOAuth2UserInfo extends OAuth2UserInfo {
    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        Map<String, Object> res = (Map<String, Object>)attributes.get("response");
        return res == null ? null : (String) res.get("id");
    }

    @Override
    public String getName() {
        Map<String, Object> res = (Map<String, Object>)attributes.get("response");
        return res == null ? null : (String) res.get("nickname");
    }

    @Override
    public String getEmail() {
        Map<String, Object> res = (Map<String, Object>)attributes.get("response");
        return res == null ? null : (String) res.get("email");
    }

    @Override
    public String getImageUrl() {
        Map<String, Object> res = (Map<String, Object>)attributes.get("response");
        return res == null ? null : (String) res.get("profile_image");
    }
}
