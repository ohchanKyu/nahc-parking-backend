package kr.ac.dankook.parkingApplication.controller;

import kr.ac.dankook.parkingApplication.document.ChatMessage;
import kr.ac.dankook.parkingApplication.dto.request.ChatMessageRequest;
import kr.ac.dankook.parkingApplication.dto.response.ApiMessageResponse;
import kr.ac.dankook.parkingApplication.dto.response.ApiResponse;
import kr.ac.dankook.parkingApplication.dto.response.ChatResponse;
import kr.ac.dankook.parkingApplication.service.ChatRedisService;
import kr.ac.dankook.parkingApplication.service.ChatRoomService;
import kr.ac.dankook.parkingApplication.service.ChatService;
import kr.ac.dankook.parkingApplication.util.DecryptId;
import kr.ac.dankook.parkingApplication.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;
    private final ChatRoomService chatRoomService;
    private final ChatRedisService chatRedisService;

    @GetMapping("/chat/message/{roomId}")
    public ResponseEntity<ApiResponse<List<ChatResponse>>> getChatMessages(@PathVariable("roomId") String roomId) {
        List<ChatResponse> chatResponses = chatService.findAllChatMessagesProcess(roomId);
        return ResponseEntity.ok(new ApiResponse<>(200,chatResponses));
    }

    @PostMapping("/chat/unread-clear/{roomId}/{memberId}")
    public ResponseEntity<ApiMessageResponse> clearUnreadCount(
            @PathVariable @DecryptId Long roomId,
            @PathVariable @DecryptId Long memberId
    ){
        chatRedisService.clearUnreadCount(roomId,memberId);
        return ResponseEntity.ok(new ApiMessageResponse(true,200,"Clear unread count"));
    }

    @GetMapping("/chat/unread-count/{roomId}/{memberId}")
    public ResponseEntity<ApiResponse<Integer>> getUnreadCount(
            @PathVariable @DecryptId Long roomId,
            @PathVariable @DecryptId Long memberId
    ){
        return ResponseEntity.ok(new ApiResponse<>(200,chatRedisService.getUnreadCount(roomId,memberId)));
    }

    @MessageMapping("/chat/message")
    public ResponseEntity<Void> receiveMessage(
            @Payload ChatMessageRequest chatMessageRequest){

        ChatMessage newChatMessage = chatService.saveChatMessageProcess(chatMessageRequest);
        CompletableFuture.runAsync(() -> {
            try {
                Long roomId = EncryptionUtil.decrypt(chatMessageRequest.getRoomId());
                chatRedisService.updateAllUnreadCount(roomId);
                chatRoomService.saveLastMessageProcess(
                        EncryptionUtil.decrypt(chatMessageRequest.getRoomId()),
                        chatMessageRequest.getContent());

            } catch (Exception e) {
                log.error("Error while processing async task - {}",e.getMessage());
            }
        });
        messagingTemplate.convertAndSend("/sub/chatroom/" + chatMessageRequest.getRoomId(),
                chatService.convertChatMessageToChatResponse(newChatMessage));
        return ResponseEntity.ok().build();
    }
}
