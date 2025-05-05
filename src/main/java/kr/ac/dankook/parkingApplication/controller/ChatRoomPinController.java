package kr.ac.dankook.parkingApplication.controller;

import kr.ac.dankook.parkingApplication.dto.response.ApiResponse;
import kr.ac.dankook.parkingApplication.service.ChatRoomPinService;
import kr.ac.dankook.parkingApplication.util.DecryptId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/pin")
public class ChatRoomPinController {

    private final ChatRoomPinService chatRoomPinService;

    @GetMapping("/{roomId}/{memberId}")
    public ResponseEntity<ApiResponse<String>> IsPin(
            @PathVariable @DecryptId Long roomId,
            @PathVariable @DecryptId Long memberId
    ){
        return ResponseEntity.ok(new ApiResponse<>(200,
                chatRoomPinService.isPinProcess(roomId,memberId)));
    }

    @PostMapping("/{roomId}/{memberId}")
    public ResponseEntity<ApiResponse<String>> SetPin(
            @PathVariable @DecryptId Long roomId,
            @PathVariable @DecryptId Long memberId
    ){
        return ResponseEntity.ok(new ApiResponse<>(200,
                chatRoomPinService.savePinProcess(roomId,memberId)));
    }

    @DeleteMapping("/{pinId}")
    public ResponseEntity<ApiResponse<Boolean>> SetPin(
            @PathVariable @DecryptId Long pinId
    ){
        return ResponseEntity.ok(new ApiResponse<>(200,
                chatRoomPinService.deletePinProcess(pinId)));
    }
}
