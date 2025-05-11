package kr.ac.dankook.parkingApplication.config.converter;

import kr.ac.dankook.parkingApplication.dto.response.ChatRoomResponse;
import kr.ac.dankook.parkingApplication.entity.ChatRoom;
import kr.ac.dankook.parkingApplication.exception.ApiErrorCode;
import kr.ac.dankook.parkingApplication.exception.ApiException;
import kr.ac.dankook.parkingApplication.repository.ChatRoomRepository;
import kr.ac.dankook.parkingApplication.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ChatRoomEntityConverter {

    private final ChatRoomRepository chatRoomRepository;

    private ChatRoom getChatRoomFromRepository(Long roomId){
        Optional<ChatRoom> targetRoom = chatRoomRepository.findById(roomId);
        if(targetRoom.isPresent()){
            return targetRoom.get();
        }
        throw new ApiException(ApiErrorCode.CHATROOM_NOT_FOUND);
    }

    @Transactional(readOnly = true)
    public ChatRoom getChatRoomByRoomId(Long roomId){
        return getChatRoomFromRepository(roomId);
    }

    public ChatRoomResponse convertChatRoomEntity(ChatRoom chatRoom){
        return ChatRoomResponse.builder()
                .chatTitle(chatRoom.getChatTitle())
                .roomId(EncryptionUtil.encrypt(chatRoom.getId()))
                .createdDateTime(chatRoom.getCreatedDateTime())
                .lastMessage(chatRoom.getLastMessage())
                .lastMessageTime(chatRoom.getLastMessageTime())
                .memberCount(chatRoom.getMembers().size())
                .build();
    }
}
