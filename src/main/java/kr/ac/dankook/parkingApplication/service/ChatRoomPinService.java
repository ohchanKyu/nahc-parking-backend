package kr.ac.dankook.parkingApplication.service;

import kr.ac.dankook.parkingApplication.config.converter.ChatRoomEntityConverter;
import kr.ac.dankook.parkingApplication.config.converter.MemberEntityConverter;
import kr.ac.dankook.parkingApplication.entity.ChatRoom;
import kr.ac.dankook.parkingApplication.entity.ChatRoomPin;
import kr.ac.dankook.parkingApplication.entity.Member;
import kr.ac.dankook.parkingApplication.repository.ChatRoomPinRepository;
import kr.ac.dankook.parkingApplication.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomPinService {

    private final ChatRoomPinRepository chatRoomPinRepository;
    private final MemberEntityConverter memberEntityConverter;
    private final ChatRoomEntityConverter chatRoomEntityConverter;

    @Transactional(readOnly = true)
    public String isPinProcess(Long roomId,Long memberId){

        Member member = memberEntityConverter.getMemberByMemberId(memberId);
        ChatRoom chatRoom = chatRoomEntityConverter.getChatRoomByRoomId(roomId);

        Optional<ChatRoomPin> targetPin = chatRoomPinRepository
                .findByChatRoomAndMember(chatRoom,member);
        return targetPin.map(chatRoomPin -> EncryptionUtil.encrypt(chatRoomPin.getId())).orElse(null);
    }

    @Transactional
    public String savePinProcess(Long roomId,Long memberId){

        Member member = memberEntityConverter.getMemberByMemberId(memberId);
        ChatRoom chatRoom = chatRoomEntityConverter.getChatRoomByRoomId(roomId);

        ChatRoomPin newPin = ChatRoomPin.builder()
                .chatRoom(chatRoom).member(member).build();
        chatRoomPinRepository.save(newPin);
        return EncryptionUtil.encrypt(newPin.getId());
    }

    @Transactional
    public boolean deletePinProcess(Long pinId){
        Optional<ChatRoomPin> targetPin = chatRoomPinRepository.findById(pinId);
        if (targetPin.isPresent()){
            chatRoomPinRepository.deleteById(pinId);
            return true;
        }
        return false;
    }

}
