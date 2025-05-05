package kr.ac.dankook.parkingApplication.controller;

import jakarta.validation.Valid;
import kr.ac.dankook.parkingApplication.dto.request.AddressRequest;
import kr.ac.dankook.parkingApplication.dto.request.LocationRequest;
import kr.ac.dankook.parkingApplication.dto.response.ApiResponse;
import kr.ac.dankook.parkingApplication.dto.response.CoordinateResponse;
import kr.ac.dankook.parkingApplication.dto.response.RouteResponse;
import kr.ac.dankook.parkingApplication.exception.ApiErrorCode;
import kr.ac.dankook.parkingApplication.exception.ValidationException;
import kr.ac.dankook.parkingApplication.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/external-api/location")
public class LocationController {

    private final LocationService locationService;

    @PostMapping("/address-to-coordinate")
    public ResponseEntity<ApiResponse<CoordinateResponse>> getCoordinateProcess(
            @RequestBody @Valid AddressRequest address, BindingResult bindingResult
    ){
        if (bindingResult.hasErrors()){
            validateBindingResult(bindingResult);
        }
        return ResponseEntity.ok(new ApiResponse<>(200,locationService.getCoordinateByAddressProcess(address.getAddress())));
    }
    @PostMapping("/route")
    public ResponseEntity<ApiResponse<RouteResponse>> getRouteProcess(
            @RequestBody @Valid LocationRequest locationRequest, BindingResult bindingResult
    ){
        if (bindingResult.hasErrors()){
            validateBindingResult(bindingResult);
        }
        return ResponseEntity.ok(new ApiResponse<>(200,locationService.getRouteInformationProcess(locationRequest)));
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
