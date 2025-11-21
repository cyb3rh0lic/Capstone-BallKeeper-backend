package com.example.ballkeeper.service;

import com.example.ballkeeper.domain.user.UserAccount;
import com.example.ballkeeper.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserAccountRepository userAccountRepository;

    // 사용자 ID를 키로 하여 SseEmitter를 관리하는 저장소
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * 클라이언트가 구독을 요청할 때 호출됩니다.
     */
    public SseEmitter subscribe(Long userId) {
        // 타임아웃을 1시간(3600초)으로 설정 (기본값은 짧음)
        SseEmitter emitter = new SseEmitter(3600000L);

        emitters.put(userId, emitter);

        // 연결 종료/타임아웃/에러 시 목록에서 제거
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError((e) -> emitters.remove(userId));

        // 503 에러 방지를 위한 더미 데이터 전송
        try {
            emitter.send(SseEmitter.event().name("connect").data("Connected!"));
        } catch (IOException e) {
            emitters.remove(userId);
        }

        return emitter;
    }

    /**
     * 특정 사용자에게 알림 전송
     */
    public void sendToUser(Long userId, String message) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(message));
            } catch (IOException e) {
                emitters.remove(userId);
            }
        }
    }

    /**
     * 현재 접속 중인 모든 관리자에게 알림 전송
     */
    public void sendToAdmins(String message) {
        List<UserAccount> admins = userAccountRepository.findAllByOrderByAdminDescIdAsc()
                .stream().filter(UserAccount::isAdmin).toList();

        for (UserAccount admin : admins) {
            sendToUser(admin.getId(), message);
        }
    }
}