package kr.ac.dankook.parkingApplication.service;

import kr.ac.dankook.parkingApplication.dto.request.CoordinateRequest;
import kr.ac.dankook.parkingApplication.dto.request.FilterRequest;
import kr.ac.dankook.parkingApplication.dto.request.LocationRequest;
import kr.ac.dankook.parkingApplication.dto.response.*;
import kr.ac.dankook.parkingApplication.entity.ParkingLot;
import kr.ac.dankook.parkingApplication.repository.ParkingLotRepository;
import kr.ac.dankook.parkingApplication.util.DateUtil;
import kr.ac.dankook.parkingApplication.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ParkingLotService {

    private final ParkingLotRepository parkingLotRepository;
    private final ParkingLotScheduledService parkingLotScheduledService;
    private final ExternalApiService externalApiService;

    public List<String> getParkingLotTypesProcess(){
        return parkingLotRepository.findDistinctType();
    }

    public List<String> getParkingLotRegionCodesProcess(){
        return parkingLotRepository.findDistinctRegionCodes();
    }

    public List<ParkingLotResponse> findByFilterProcess(FilterRequest filterRequest){

        String regionCode = filterRequest.getRegionCode();
        String type = filterRequest.getType();
        List<ParkingLot> parkingLots = parkingLotRepository.findByRegionCode(regionCode);
        if (type != null && !type.isEmpty()){
            parkingLots =  parkingLots.stream()
                    .filter(parkingLot -> parkingLot.getType()
                            .equals(type))
                    .toList();
        }
        if (filterRequest.isFree()){
            parkingLots = parkingLots.stream()
                    .filter(parkingLot -> parkingLot.getFeeInfo().equals("무료"))
                    .toList();
        }
        if (filterRequest.isCurrent()){
            parkingLots = parkingLots.stream()
                    .filter(parkingLot -> parkingLot.getIsCurrent().equals("1"))
                    .toList();
        }
        if (filterRequest.isOpen()){
            parkingLots = parkingLots.stream()
                    .filter(DateUtil::getOperatingStatus).toList();
        }
        return parkingLots.stream().map(parkingLotScheduledService::convertToResponseEntity).toList();
    }

    public ParkingLotResponse findByIdProcess(Long parkingLotId){
        Optional<ParkingLot> parkingLotOptional = parkingLotRepository.findById(parkingLotId);
        return parkingLotOptional.map(parkingLotScheduledService::convertToResponseEntity).orElse(null);
    }
    public List<KeywordResponse> findByKeywordProcess(String keyword){

        List<KeywordResponse> keywordResponses = new ArrayList<>();
        List<ParkingLot> parkingLots = parkingLotRepository.findByNameContainingIgnoreCase(keyword);
        for(ParkingLot parkingLot : parkingLots){
            keywordResponses.add(new KeywordResponse(
                    EncryptionUtil.encrypt(parkingLot.getId()),
                    parkingLot.getName(),
                    parkingLot.getRegionCode(),
                    parkingLot.getLatitude(),
                    parkingLot.getLongitude()
            ));
        }
        return keywordResponses;
    }

    private List<DistanceResponse> sortAllParkingByDistanceProcess(CoordinateRequest coordinatesRequest){

        List<DistanceResponse> distanceResponses = new ArrayList<>();
        List<ParkingLot> allParkingLotList = parkingLotRepository.findAll();

        for (ParkingLot parkingLot : allParkingLotList) {
            LocationRequest request = new LocationRequest(
                    coordinatesRequest.getLatitude(),
                    coordinatesRequest.getLongitude(),
                    parkingLot.getLatitude(),
                    parkingLot.getLongitude()
            );
            distanceResponses.add(
                    new DistanceResponse(
                            getDistanceProcess(request),
                            parkingLot
                    )
            );
        }

        distanceResponses.sort(Comparator.comparingDouble(DistanceResponse::getDistance));
        return distanceResponses;
    }

    public List<ParkingLotResponse> getAllParkingLotProcess(){

        List<ParkingLotResponse> parkingLotResponseList = new ArrayList<>();
        List<ParkingLot> parkingLots = parkingLotRepository.findAll();
        for(ParkingLot parkingLot : parkingLots){
            ParkingLotResponse parkingLotResponse = parkingLotScheduledService.
                    convertToResponseEntity(parkingLot);
            parkingLotResponseList.add(parkingLotResponse);
        }
        return parkingLotResponseList;
    }

    public List<ParkingLotResponseWithRouteInfo> getAroundParkingLotProcess(CoordinateRequest coordinateRequest){

        List<ParkingLotResponseWithRouteInfo> responseList = new ArrayList<>();

        List<DistanceResponse> sortByDistanceList = sortAllParkingByDistanceProcess(coordinateRequest);
        List<DistanceResponse> fetchByIsOpenList = new ArrayList<>();

        for(DistanceResponse distanceResponse : sortByDistanceList){
            ParkingLot target = distanceResponse.getParkingLot();
            if (DateUtil.getOperatingStatus(target)){
                fetchByIsOpenList.add(distanceResponse);
            }
            if (fetchByIsOpenList.size() == 10) break;
        }

        for(DistanceResponse distanceResponse : fetchByIsOpenList){
            ParkingLot targetParkingLot = distanceResponse.getParkingLot();
            ParkingLotResponse parkingLotResponse = parkingLotScheduledService
                    .convertToResponseEntity(targetParkingLot);
            LocationRequest locationRequest = LocationRequest.builder()
                    .startLatitude(coordinateRequest.getLatitude())
                    .startLongitude(coordinateRequest.getLongitude())
                    .endLatitude(targetParkingLot.getLatitude())
                    .endLongitude(targetParkingLot.getLongitude()).build();
            RouteResponse routeResponse = externalApiService.getRouteInformationProcess(locationRequest);
            responseList.add(new ParkingLotResponseWithRouteInfo(parkingLotResponse,routeResponse));
        }
        return responseList;
    }

    private double getDistanceProcess(LocationRequest locationRequest) {
        double theta = locationRequest.getEndLongitude() - locationRequest.getStartLongitude();
        double dist = Math.sin(deg2rad(locationRequest.getStartLatitude())) *
                Math.sin(deg2rad(locationRequest.getEndLatitude())) +
                Math.cos(deg2rad(locationRequest.getStartLatitude())) *
                        Math.cos(deg2rad(locationRequest.getEndLatitude())) *
                        Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515 * 1609.344;
        return dist / 1000;
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }


}
