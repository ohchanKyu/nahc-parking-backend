package kr.ac.dankook.parkingApplication.controller;

import jakarta.validation.Valid;
import kr.ac.dankook.parkingApplication.document.TrafficMessage;
import kr.ac.dankook.parkingApplication.dto.request.CoordinateRequest;
import kr.ac.dankook.parkingApplication.dto.request.FilterRequest;
import kr.ac.dankook.parkingApplication.dto.response.*;
import kr.ac.dankook.parkingApplication.exception.ApiErrorCode;
import kr.ac.dankook.parkingApplication.exception.ValidationException;
import kr.ac.dankook.parkingApplication.service.ParkingLotService;
import kr.ac.dankook.parkingApplication.service.ParkingTrafficScheduledService;
import kr.ac.dankook.parkingApplication.util.DecryptId;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/parking-lot")
public class ParkingLotController {

    private final ParkingLotService parkingLotService;
    private final ParkingTrafficScheduledService parkingTrafficScheduledService;

    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<String>>> getParkingLotTypes(){
        return ResponseEntity.ok(new ApiResponse<>(200,
                parkingLotService.getParkingLotTypesProcess()));
    }
    @GetMapping("/regionCodes")
    public ResponseEntity<ApiResponse<List<String>>> getParkingLotRegionCodes(){
        return ResponseEntity.ok(new ApiResponse<>(200,
                parkingLotService.getParkingLotRegionCodesProcess()));
    }

    @GetMapping("/traffic")
    public ResponseEntity<ApiResponse<List<TrafficMessage>>> getAllParkingTraffic(){
        return ResponseEntity.ok(new ApiResponse<>(200,
                parkingTrafficScheduledService.getTrafficMessagesProcess()));
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<ParkingLotResponse>>> getAllParkingLotResponse(){
        return ResponseEntity.ok(new ApiResponse<>(200,parkingLotService.getAllParkingLotProcess()));
    }

    @GetMapping("/{parkingLotId}")
    public ResponseEntity<ApiResponse<ParkingLotResponse>> getParkingLot(
            @PathVariable @DecryptId  Long parkingLotId){
        return ResponseEntity.ok(new ApiResponse<>(200,parkingLotService.findByIdProcess(parkingLotId)));
    }
    @GetMapping("/search/{keyword}")
    public ResponseEntity<ApiResponse<KeywordListResponse>> searchByKeyword(
            @PathVariable String keyword){
        List<KeywordResponse> keywordResponses = parkingLotService.findByKeywordProcess(keyword);
        return ResponseEntity.ok(new ApiResponse<>(200,new KeywordListResponse(keywordResponses,keywordResponses.size())));
    }
    @PostMapping("/around")
    public ResponseEntity<ApiResponse<List<ParkingLotResponseWithRouteInfo>>> getAroundParkingLot(
            @RequestBody @Valid  CoordinateRequest coordinatesRequest,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()){
            validateBindingResult(bindingResult);
        }
        return ResponseEntity.ok(new ApiResponse<>(200,parkingLotService.getAroundParkingLotProcess(coordinatesRequest)));
    }

    @PostMapping("/filter")
    public ResponseEntity<ApiResponse<List<ParkingLotResponse>>> getParkingLotByFilter(
            @RequestBody @Valid FilterRequest filterRequest,
            BindingResult bindingResult
    ){
        if (bindingResult.hasErrors()){
            validateBindingResult(bindingResult);
        }
        return ResponseEntity.ok(new ApiResponse<>(200,
                parkingLotService.findByFilterProcess(filterRequest)));
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
