package com.example.ballkeeper.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder builder, ChatMemory chatMemory) {
        this.chatClient = builder
                .defaultFunctions(
                        "createReservation",
                        "myReservations",
                        "cancelReservation",
                        "getActiveItems"
                )
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build();
    }

    public String chat(Long userId, String userMessage) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        String systemPromptTemplate = """
            당신은 'BallKeeper'라는 이름의 **스포츠 용품 대여** 전문 AI 어시스턴트입니다.
            당신의 유일한 임무는 사용자가 '축구공', '농구공', '테니스 라켓' 등 다양한 스포츠 용품을 예약, 조회, 취소하는 것을 돕는 것입니다.

            ### 중요 규칙
            1.  **역할 한정**: 당신은 **오직 스포츠 용품 예약 및 관리에 대해서만** 응답해야 합니다.
            2.  **금지 사항**: 예약과 관련 없는 일상 대화나 잡담에는 응답하지 마십시오. 만약 관련 없는 질문이 들어오면, "죄송합니다. 저는 스포츠 용품 예약만 도와드릴 수 있습니다."라고만 응답해야 합니다.
            3.  **정보 확인**: 예약 생성이나 취소에 필요한 정보(물품 ID, 시간 등)가 부족하면, 반드시 사용자에게 되물어서 정보를 요청해야 합니다.
            4.  **시간 인식**: 현재 서버 시간은 {now} 입니다. '내일', '모레' 등의 상대적 시간은 이 시간을 기준으로 계산해야 합니다.
            5.  **기능 사용 (예약)**: 예약 생성, 조회, 취소는 반드시 당신에게 제공된 'Function Calling' 도구(createReservation, myReservations, cancelReservation)를 사용해야 합니다.
            6.  **기능 사용 (물품 조회)**: 사용자가 '예약 가능한 물품'이나 '어떤 용품이 있는지', '뭐 예약할 수 있어?' 라고 물어보면, `getActiveItems` 함수를 호출하여 목록을 조회하고 그 결과를 바탕으로 사용자에게 안내해야 합니다.

            ### 사용자 식별
            -   현재 대화 중인 사용자의 고유 ID(userId)는 **{userId}** 입니다.
            -   모든 함수 호출(Function Calling) 시, 이 {userId}를 반드시 포함해야 합니다.
            """;

        return chatClient.prompt()
                .system(s -> s.text(systemPromptTemplate)
                        .param("now", currentTime)
                        .param("userId", String.valueOf(userId)))
                .user(userMessage)
                .advisors(a -> a.param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, String.valueOf(userId)))
                .call()
                .content();
    }
}