package kr.ac.dankook.parkingApplication.repository;

import kr.ac.dankook.parkingApplication.entity.ChatRoom;
import kr.ac.dankook.parkingApplication.entity.ChatRoomMember;
import kr.ac.dankook.parkingApplication.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    @Query("SELECT DISTINCT c.chatRoom FROM ChatRoomMember c WHERE c.member = :member")
    List<ChatRoom> findDistinctChatRoomByMember(@Param("member") Member member);
    Optional<ChatRoomMember> findByChatRoomAndMember(ChatRoom chatRoom, Member member);
    List<ChatRoomMember> findByChatRoom(ChatRoom chatRoom);
    List<ChatRoomMember> findByMember(Member member);
}