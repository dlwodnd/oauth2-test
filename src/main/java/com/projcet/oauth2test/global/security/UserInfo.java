package com.projcet.oauth2test.global.security;


import com.projcet.oauth2test.domain.user.config.Platform;
import com.projcet.oauth2test.domain.user.entity.Role;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfo {
    private Long userPk;
    private String userEmail;
    private String userPw;
    private String userPic;
    private String nickname;
    private Platform socialType;
    private Role role;
}
