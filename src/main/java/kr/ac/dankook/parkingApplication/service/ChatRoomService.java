package kr.ac.dankook.parkingApplication.service;

import kr.ac.dankook.parkingApplication.config.converter.ChatRoomEntityConverter;
import kr.ac.dankook.parkingApplication.config.converter.MemberEntityConverter;
import kr.ac.dankook.parkingApplication.document.ChatMessage;
import kr.ac.dankook.parkingApplication.dto.request.CreateChatRoomRequest;
import kr.ac.dankook.parkingApplication.dto.response.ChatRoomResponse;
import kr.ac.dankook.parkingApplication.entity.ChatRoom;
import kr.ac.dankook.parkingApplication.entity.ChatRoomMember;
import kr.ac.dankook.parkingApplication.entity.ChatRoomPin;
import kr.ac.dankook.parkingApplication.entity.Member;
import kr.ac.dankook.parkingApplication.exception.ApiErrorCode;
import kr.ac.dankook.parkingApplication.exception.ApiException;
import kr.ac.dankook.parkingApplication.repository.ChatMessageRepository;
import kr.ac.dankook.parkingApplication.repository.ChatRoomMemberRepository;
import kr.ac.dankook.parkingApplication.repository.ChatRoomPinRepository;
import kr.ac.dankook.parkingApplication.repository.ChatRoomRepository;
import kr.ac.dankook.parkingApplication.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberEntityConverter memberEntityConverter;
    private final ChatRoomEntityConverter chatRoomEntityConverter;
    private final ChatRoomPinRepository chatRoomPinRepository;
    private final ChatRedisService chatRedisService;

    @Transactional(readOnly = true)
    public boolean isParticipateChatRoomProcess(Long roomId,Long memberId){

        ChatRoom chatRoom = chatRoomEntityConverter.getChatRoomByRoomId(roomId);
        Member member = memberEntityConverter.getMemberByMemberId(memberId);
        Optional<ChatRoomMember> targetChatRoomMember = chatRoomMemberRepository
                .findByChatRoomAndMember(chatRoom, member);
        return targetChatRoomMember.isPresent();
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getChatRoomByKeywordProcess(String keyword){
        List<ChatRoom> chatRooms = chatRoomRepository
                .findByChatTitleContainingIgnoreCase(keyword);
        return chatRooms.stream().map(chatRoomEntityConverter::convertChatRoomEntity).toList();
    }


    @Transactional
    public void saveLastMessageProcess(Long roomId, String content){

        ChatRoom chatRoom = chatRoomEntityConverter.getChatRoomByRoomId(roomId);
        chatRoom.setLastMessage(content);
        chatRoom.setLastMessageTime(LocalDateTime.now());
        chatRoomRepository.save(chatRoom);

    }

    @Transactional
    public ChatRoomResponse createChatRoomProcess(CreateChatRoomRequest request){

        ChatRoom chatRoom = ChatRoom.builder()
                .chatTitle(request.getTitle()).build();
        chatRoomRepository.save(chatRoom);
        Member member = memberEntityConverter.getMemberByEncryptMemberId(request.getMemberId());
        ChatRoomMember newMember = ChatRoomMember.builder()
                .member(member)
                .chatRoom(chatRoom).build();
        chatRoom.getMembers().add(newMember);
        return chatRoomEntityConverter.convertChatRoomEntity(chatRoom);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getAllChatRoomProcess(){
        List<ChatRoom> chatRoomList = chatRoomRepository.findAll();
        return convertChatRoomListResponse(chatRoomList);
    }

    private List<ChatRoomResponse> convertChatRoomListResponse(List<ChatRoom> chatRoomList){
        List<ChatRoomResponse> chatRoomResponseList = new ArrayList<>();
        for(ChatRoom chatRoom : chatRoomList){
            chatRoomResponseList.add(chatRoomEntityConverter.convertChatRoomEntity(chatRoom));
        }
        return chatRoomResponseList;
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getChatRoomByMemberProcess(Long memberId){
        Member member = memberEntityConverter.getMemberByMemberId(memberId);
        List<ChatRoom> chatRooms = chatRoomMemberRepository.
                findDistinctChatRoomByMember(member);
        return convertChatRoomListResponse(chatRooms);
    }

    @Transactional
    public ChatRoomResponse registerNewMemberToChatRoomProcess(Long roomId, Long memberId){

        ChatRoom chatRoom = chatRoomEntityConverter.getChatRoomByRoomId(roomId);
        Member member = memberEntityConverter.getMemberByMemberId(memberId);
        ChatRoomMember newMember = ChatRoomMember.builder()
                .chatRoom(chatRoom)
                .member(member)
                .build();
        chatRoomMemberRepository.save(newMember);
        return chatRoomEntityConverter.convertChatRoomEntity(chatRoom);
    }

    @Transactional
    public void unregisterMemberToChatRoomProcess(Long roomId,Long memberId){

        ChatRoom chatRoom = chatRoomEntityConverter.getChatRoomByRoomId(roomId);
        Member member = memberEntityConverter.getMemberByMemberId(memberId);
        Optional<ChatRoomMember> targetChatRoomMember = chatRoomMemberRepository.
                findByChatRoomAndMember(chatRoom, member);
        if (targetChatRoomMember.isEmpty()){
            throw new ApiException(ApiErrorCode.CHATROOM_MEMBER_NOT_FOUND);
        }
        Optional<ChatRoomPin> targetPin = chatRoomPinRepository
                .findByChatRoomAndMember(chatRoom,member);
        targetPin.ifPresent(chatRoomPinRepository::delete);

        chatRoomMemberRepository.delete(targetChatRoomMember.get());
        List<ChatRoomMember> chatRoomMembers = chatRoomMemberRepository.findByChatRoom(chatRoom);

        chatRedisService.deleteUnreadCount(roomId,memberId);

        if (chatRoomMembers.isEmpty()){
            List<ChatMessage> messages = chatMessageRepository.findAllByChatRoomId(EncryptionUtil.encrypt(roomId));
            chatMessageRepository.deleteAll(messages);
            chatRoomRepository.delete(chatRoom);
        }
    }
}
