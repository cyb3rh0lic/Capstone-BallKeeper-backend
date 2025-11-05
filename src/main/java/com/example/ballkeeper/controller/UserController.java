package com.example.ballkeeper.controller;

import com.example.ballkeeper.api.dto.userDto.LoginRequest;
import com.example.ballkeeper.api.dto.userDto.SignUpRequest;
import com.example.ballkeeper.api.dto.userDto.UserResponse;
import com.example.ballkeeper.service.KakaoLoginService;
import com.example.ballkeeper.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final KakaoLoginService kakaoLoginService;

    @PostMapping("/signup")
    public UserResponse signup(@RequestBody SignUpRequest req) {
        return userService.signUp(req);
    }

    @PostMapping("/login")
    public UserResponse login(@RequestBody LoginRequest req) {
        return userService.login(req);
    }

    @GetMapping("/auth/kakao")
    public UserResponse kakaoLogin(@RequestParam String code) {
        // 프론트에서 전달받은 '인가 코드'로 카카오 로그인을 처리합니다.
        return kakaoLoginService.loginWithKakao(code);
    }
}
