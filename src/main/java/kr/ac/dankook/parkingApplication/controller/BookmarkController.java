package kr.ac.dankook.parkingApplication.controller;

import jakarta.validation.Valid;
import kr.ac.dankook.parkingApplication.config.converter.MemberEntityConverter;
import kr.ac.dankook.parkingApplication.dto.request.CoordinateRequest;
import kr.ac.dankook.parkingApplication.dto.response.ApiResponse;
import kr.ac.dankook.parkingApplication.dto.response.BookmarkParkingLotResponse;
import kr.ac.dankook.parkingApplication.dto.response.ParkingLotResponse;
import kr.ac.dankook.parkingApplication.entity.Member;
import kr.ac.dankook.parkingApplication.exception.ApiErrorCode;
import kr.ac.dankook.parkingApplication.exception.ValidationException;
import kr.ac.dankook.parkingApplication.service.BookmarkService;
import kr.ac.dankook.parkingApplication.util.DecryptId;
import kr.ac.dankook.parkingApplication.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/parking-lot/bookmark")
public class BookmarkController {

    private final MemberEntityConverter memberEntityConverter;
    private final BookmarkService bookmarkService;

    @GetMapping("/{memberId}/{parkingLotId}")
    public ResponseEntity<ApiResponse<String>> getParkingLotIsBookmark(
            @PathVariable @DecryptId  Long memberId,
            @PathVariable @DecryptId  Long parkingLotId){

        String encryptionKey = null;
        Long key = bookmarkService.checkParkingLotIsBookmarkProcess(memberId,parkingLotId);
        if (key != null)  encryptionKey = EncryptionUtil.encrypt(key);

        return ResponseEntity.ok(new ApiResponse<>(
                200,encryptionKey));
    }
    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponse<List<ParkingLotResponse>>> getBookmarkParkingLotsByBaseType(
            @PathVariable @DecryptId  Long memberId
    ){
        return ResponseEntity.ok(new ApiResponse<>(
                200,bookmarkService.getBaseTypeBookmarksList(memberId)
        ));
    }
    @PostMapping("/{memberId}")
    public ResponseEntity<ApiResponse<List<BookmarkParkingLotResponse>>> getAllBookmarkList(
            @PathVariable @DecryptId  Long memberId,
            @RequestBody @Valid CoordinateRequest coordinateRequest,
            BindingResult bindingResult
    ){
        if (bindingResult.hasErrors()){
            validateBindingResult(bindingResult);
        }
        return ResponseEntity.ok(new ApiResponse<>(
                200,bookmarkService.getAllBookmarksListProcess(memberId,coordinateRequest)));
    }

    @PostMapping("/{memberId}/{parkingLotId}")
    public ResponseEntity<ApiResponse<Boolean>> addBookmark(
            @PathVariable @DecryptId  Long memberId,
            @PathVariable @DecryptId  Long parkingLotId){
        Member targetMember = memberEntityConverter.getMemberByMemberId(memberId);
        return ResponseEntity.ok(new ApiResponse<>(
                200,bookmarkService.addBookmarkProcess(targetMember,parkingLotId)));
    }

    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<ApiResponse<Boolean>> deleteBookmark(
            @PathVariable @DecryptId  Long bookmarkId){
        return ResponseEntity.ok(new ApiResponse<>(
                200,bookmarkService.deleteBookmarkProcess(bookmarkId)));

    }
    private void validateBindingResult(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessages = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(","));
            throw new ValidationException(ApiErrorCode.INVALID_REQUEST,errorMessages);
        }
    }
}
