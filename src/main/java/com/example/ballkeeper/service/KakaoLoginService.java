package com.example.ballkeeper.service;

import com.example.ballkeeper.api.dto.kakaoDto.KakaoProfile;
import com.example.ballkeeper.api.dto.userDto.UserResponse;
import com.example.ballkeeper.domain.user.UserAccount;
import com.example.ballkeeper.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KakaoLoginService {

    private final UserAccountRepository userAccountRepository;
    private final WebClient.Builder webClientBuilder;
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String kakaoTokenUri;

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String kakaoUserInfoUri;

    /**
     * 프론트에서 받은 인가 코드로 카카오 로그인을 처리
     */
    @Transactional
    public UserResponse loginWithKakao(String code) {
        // 인가 코드로 카카오에 액세스 토큰 요청
        String accessToken = getKakaoToken(code).block();

        if (accessToken == null) {
            throw new RuntimeException("카카오 토큰을 받지 못했습니다.");
        }

        // 액세스 토큰으로 카카오에 사용자 정보 요청
        KakaoProfile kakaoProfile = getKakaoUserInfo(accessToken).block();

        if (kakaoProfile == null || kakaoProfile.getKakaoAccount() == null) {
            throw new RuntimeException("카카오 사용자 정보를 받지 못했습니다.");
        }

        // 카카오 정보로 우리 서비스에 회원가입 또는 로그인 처리
        UserAccount user = registerOrLoginUser(kakaoProfile);

        // UserResponse DTO로 변환하여 반환
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.isAdmin());
    }

    /**
     * 카카오 이메일로 사용자를 찾거나, 없으면 새로 회원가입
     */
    private UserAccount registerOrLoginUser(KakaoProfile kakaoProfile) {
        String email = kakaoProfile.getKakaoAccount().getEmail();
        String name = kakaoProfile.getKakaoAccount().getProfile().getNickname();

        // 이메일로 기존 사용자 조회
        return userAccountRepository.findByEmail(email).orElseGet(() -> {
            // 신규 사용자일 경우 회원가입
            UserAccount newUser = UserAccount.builder()
                    .email(email)
                    .name(name)
                    .admin(false)
                    // password_hash가 not-null이므로 임의의 값(UUID)을 넣음
                    .password(UUID.randomUUID().toString())
                    .build();
            return userAccountRepository.save(newUser);
        });
    }

    /**
     * 카카오 서버에 액세스 토큰을 요청
     */
    private Mono<String> getKakaoToken(String code) {
        WebClient webClient = webClientBuilder.baseUrl(kakaoTokenUri).build();

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", kakaoClientId)
                        .queryParam("redirect_uri", kakaoRedirectUri)
                        .queryParam("code", code)
                        .build())
                .header("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    return response.split("\"access_token\":\"")[1].split("\"")[0];
                });
    }

    /**
     * 카카오 서버에 사용자 정보를 요청합니다.
     */
    private Mono<KakaoProfile> getKakaoUserInfo(String accessToken) {
        WebClient webClient = webClientBuilder.baseUrl(kakaoUserInfoUri).build();

        return webClient.get()
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .bodyToMono(KakaoProfile.class);
    }
}