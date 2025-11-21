package com.example.ballkeeper.service;

import com.example.ballkeeper.api.dto.userDto.LoginRequest;
import com.example.ballkeeper.api.dto.userDto.SignUpRequest;
import com.example.ballkeeper.api.dto.userDto.UserResponse;
import com.example.ballkeeper.domain.user.UserAccount;
import com.example.ballkeeper.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class UserService {
    private final UserAccountRepository userAccountRepository;

    @Transactional
    public UserResponse signUp(SignUpRequest req) {
        if (userAccountRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        var user = UserAccount.builder()
                .email(req.email())
                .password(req.password()) // 추후 BCrypt로 교체
                .name(req.name())
                .admin(req.admin())
                .build();
        userAccountRepository.save(user);
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.isAdmin());
    }

    @Transactional(readOnly = true)
    public UserResponse login(LoginRequest req) {
        var user = userAccountRepository.findByEmail(req.email())
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));
        if (!user.getPassword().equals(req.password())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.isAdmin());
    }

    private void assertAdmin(Long adminId) {
        UserAccount admin = userAccountRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("관리자 계정이 없습니다."));
        if (!admin.isAdmin()) {
            throw new AccessDeniedException("관리자 권한이 필요합니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(Long adminId) {
        assertAdmin(adminId);
        return userAccountRepository.findAllByOrderByAdminDescIdAsc()
                .stream()
                .map(u -> new UserResponse(u.getId(), u.getEmail(), u.getName(), u.isAdmin()))
                .collect(Collectors.toList());
    }

    public UserResponse toggleAdminAuthority(Long adminId, Long targetUserId) {
        assertAdmin(adminId);

        UserAccount targetUser = userAccountRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (targetUser.getId().equals(adminId)) {
            throw new IllegalStateException("자기 자신의 권한은 변경할 수 없습니다.");
        }

        targetUser.setAdmin(!targetUser.isAdmin()); // 권한 토글
        return new UserResponse(targetUser.getId(), targetUser.getEmail(), targetUser.getName(), targetUser.isAdmin());
    }
}
