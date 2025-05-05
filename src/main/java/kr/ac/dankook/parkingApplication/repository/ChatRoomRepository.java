package kr.ac.dankook.parkingApplication.repository;


import kr.ac.dankook.parkingApplication.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {
    List<ChatRoom> findByChatTitleContainingIgnoreCase(String keyword);
}
