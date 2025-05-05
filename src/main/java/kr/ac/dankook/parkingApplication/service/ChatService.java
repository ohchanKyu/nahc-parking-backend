package kr.ac.dankook.parkingApplication.service;

import jakarta.transaction.Transactional;
import kr.ac.dankook.parkingApplication.document.ChatMessage;
import kr.ac.dankook.parkingApplication.dto.request.ChatMessageRequest;
import kr.ac.dankook.parkingApplication.dto.response.ChatResponse;
import kr.ac.dankook.parkingApplication.entity.Member;
import kr.ac.dankook.parkingApplication.repository.ChatMessageRepository;
import kr.ac.dankook.parkingApplication.repository.MemberRepository;
import kr.ac.dankook.parkingApplication.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public List<ChatResponse> findAllChatMessagesProcess(String roomId) {
        List<ChatMessage> chatMessages = chatMessageRepository.findAllByChatRoomId(roomId);
        return chatMessages.stream().map(this::convertChatMessageToChatResponse).toList();
    }

    @Transactional
    public ChatMessage saveChatMessageProcess(ChatMessageRequest messageRequest){
        ChatMessage newMessage = ChatMessage.builder()
                .content(messageRequest.getContent())
                .chatRoomId(messageRequest.getRoomId())
                .memberId(messageRequest.getMemberId())
                .createdTime(LocalDateTime.now())
                .build();
        return chatMessageRepository.save(newMessage);
    }

    public ChatResponse convertChatMessageToChatResponse(ChatMessage message){

        Optional<Member> targetMember = memberRepository.findById(EncryptionUtil.decrypt(message.getMemberId()));
        String name = targetMember.isPresent() ? targetMember.get().getName() : "알 수 없음";
        String memberId = targetMember.isPresent() ? EncryptionUtil.encrypt(targetMember.get().getId()) : "알 수 없음";

        return ChatResponse.builder()
                .content(message.getContent())
                .time(message.getCreatedTime())
                .name(name).memberId(memberId).build();
    }
}
