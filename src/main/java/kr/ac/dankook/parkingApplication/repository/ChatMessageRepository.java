package kr.ac.dankook.parkingApplication.repository;


import kr.ac.dankook.parkingApplication.document.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findAllByChatRoomId(String chatRoomId);
}
