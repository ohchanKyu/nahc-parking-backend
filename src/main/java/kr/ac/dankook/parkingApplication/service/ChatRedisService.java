package kr.ac.dankook.parkingApplication.service;

import kr.ac.dankook.parkingApplication.config.converter.ChatRoomEntityConverter;
import kr.ac.dankook.parkingApplication.entity.ChatRoom;
import kr.ac.dankook.parkingApplication.entity.ChatRoomMember;
import kr.ac.dankook.parkingApplication.entity.Member;
import kr.ac.dankook.parkingApplication.repository.ChatRoomMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRedisService {

    private final RedisTemplate<String,Integer> integerRedisTemplate;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomEntityConverter chatRoomEntityConverter;

    @SuppressWarnings("ConstantConditions")
    public void clearUnreadCount(Long roomId, Long memberId){
        ValueOperations<String, Integer> operations = integerRedisTemplate.opsForValue();
        String key = roomId+"_"+memberId;
        Integer currentUnread = operations.get(key);
        if (currentUnread != null){
            delete(key);
        }
        operations.set(key,0);
    }

    public int getUnreadCount(Long roomId,Long memberId){
        ValueOperations<String, Integer> operations = integerRedisTemplate.opsForValue();
        String key = roomId+"_"+memberId;
        return operations.get(key);
    }

    public void deleteUnreadCount(Long roomId, Long memberId){
        String key = roomId+"_"+memberId;
        delete(key);
    }

    public void updateAllUnreadCount(Long roomId){

        ValueOperations<String, Integer> operations = integerRedisTemplate.opsForValue();
        ChatRoom chatRoom = chatRoomEntityConverter
                .getChatRoomByRoomId(roomId);
        List<ChatRoomMember> allMembers = chatRoomMemberRepository.findByChatRoom(chatRoom);

        for(ChatRoomMember chatRoomMember : allMembers){
            Member member = chatRoomMember.getMember();
            String key = roomId+"_"+member.getId();
            operations.increment(key);
        }
    }
    private void delete(String key){
        integerRedisTemplate.delete(key);
    }
}
