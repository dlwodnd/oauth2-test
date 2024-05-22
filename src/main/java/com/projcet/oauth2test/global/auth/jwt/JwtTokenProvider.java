package com.projcet.oauth2test.global.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projcet.oauth2test.global.security.MyPrincipal;
import com.projcet.oauth2test.global.security.MyUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

import static io.jsonwebtoken.Jwts.claims;
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {
    private final ObjectMapper objectMapper;
    private final JwtProperties JWTProperties;
    private SecretKeySpec secretKeySpec;

    @PostConstruct
    public void init(){
        this.secretKeySpec = new SecretKeySpec(JWTProperties.getJwt().getSecret().getBytes()
                , SignatureAlgorithm.HS256.getJcaName());
    }
    public Claims createClaims(MyPrincipal myprincipal){
        try {
            String json = objectMapper.writeValueAsString(myprincipal);
            return claims()
                    .add("user",json)
                    .build();
        }catch (Exception e) {
            return null;
        }
    }
    public String resolveToken(HttpServletRequest request){
        String auth = request.getHeader(JWTProperties.getJwt().getHeaderSchemeName());
        if(auth == null){
            return null;
        }
        if(auth.startsWith(JWTProperties.getJwt().getTokenType())){
            return auth.substring(JWTProperties.getJwt().getTokenType().length()).trim();
        }
        return null;
    }
    public Claims getAllClaims(String token){
        return Jwts.parser()
                .verifyWith(secretKeySpec)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    public UserDetails getUserDetailsFromToken(String token){
        try {
            Claims claims = getAllClaims(token);
            String json = (String) claims.get("user");
            MyPrincipal myPrincipal = objectMapper.readValue(json,MyPrincipal.class);
            return MyUserDetails.builder()
                    .myPrincipal(myPrincipal)
                    .build();
        }catch (Exception e){
            return null;
        }
    }
    public Authentication getAuthentication(String token){
        UserDetails userDetails = getUserDetailsFromToken(token);
        return userDetails == null ? null : new UsernamePasswordAuthenticationToken(userDetails,"",userDetails.getAuthorities());
    }
    private String generateToken(MyPrincipal myprincipal, long tokenValidMs){

        return Jwts.builder()
                .claims(createClaims(myprincipal))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + tokenValidMs))
                .signWith(secretKeySpec)
                .compact();
    }
    public String generateRefreshToken(MyPrincipal myprincipal){
        return generateToken(myprincipal, JWTProperties.getJwt().getRefreshTokenExpiry());
    }
    public String generateAccessToken(MyPrincipal myprincipal){
        return generateToken(myprincipal, JWTProperties.getJwt().getAccessTokenExpiry());
    }
    public boolean isValidateToken(String token){
        try {
            return !getAllClaims(token).getExpiration().before(new Date());
        }catch (Exception e){
            return false;
        }
    }

}
