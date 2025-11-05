package com.example.ballkeeper.api.dto.kakaoDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class KakaoProfile {

    private Long id;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Getter
    @ToString
    public static class KakaoAccount {
        private Profile profile;
        private String email;
    }

    @Getter
    @ToString
    public static class Profile {
        private String nickname;
    }
}