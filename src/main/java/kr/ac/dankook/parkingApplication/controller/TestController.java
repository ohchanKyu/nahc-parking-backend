package kr.ac.dankook.parkingApplication.controller;

import kr.ac.dankook.parkingApplication.dto.response.CoordinateResponse;
import kr.ac.dankook.parkingApplication.entity.ParkingLot;
import kr.ac.dankook.parkingApplication.repository.ParkingLotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final ParkingLotRepository parkingLotRepository;
    @Value("${api.kakao.app-key}")
    private String KAKAO_APP_KEY;
    @Value("${api.kakao.address-to-coordinate-host}")
    private String KAKAO_ADDRESS_TO_COORDINATE_HOST;
    @Value("${api.kakao.address-to-coordinate-path}")
    private String KAKAO_ADDRESS_TO_COORDINATE_PATH;

    private static final double BASE_LAT = 37.565385;
    private static final double BASE_LNG = 127.00339;
    private static final double OFFSET = 0.05;

    @GetMapping("/test/patch2")
    public String patch2(){
        List<ParkingLot> response = parkingLotRepository.findByAddressContainingQuotes("\"");
        for(ParkingLot parkingLot : response){
            String newString = parkingLot.getAddress().replace("\"", "");
            parkingLot.setAddress(newString);
            parkingLot.setRegionCode(newString.split(" ")[0]);
            parkingLotRepository.save(parkingLot);
        }
        return "success";
    }
    public static String convertToHHMM(String time) {
        if (time.matches("^([01]?\\d|2[0-3]):[0-5]\\d$")) {
            return formatWithLeadingZero(time);
        }
        if (time.matches("^\\d+$")) {
            return convertNumberToHHMM(time);
        }
        if (time.matches("^([0-9]|1[0-9]|2[0-3]):[0-9]$")) {
            return formatWithLeadingZero(time.substring(0, time.indexOf(":")) + ":0" + time.substring(time.indexOf(":") + 1));
        }
        return null;
    }

    private static String convertNumberToHHMM(String numStr) {
        int hours, minutes;

        if (numStr.length() == 1) {
            hours = Integer.parseInt(numStr);
            minutes = 0;
        } else if (numStr.length() == 2) {
            hours = Integer.parseInt(numStr);
            minutes = 0;
        } else if (numStr.length() == 3) {
            hours = Integer.parseInt(numStr.substring(0, 1));
            minutes = Integer.parseInt(numStr.substring(1));
        } else if (numStr.length() == 4) {
            hours = Integer.parseInt(numStr.substring(0, 2));
            minutes = Integer.parseInt(numStr.substring(2));
        } else {
            return null;
        }
        if (isValidTime(hours, minutes)) {
            return String.format("%02d:%02d", hours, minutes);
        }
        return null;
    }

    private static boolean isValidTime(int hours, int minutes) {
        return (hours >= 0 && hours < 24) && (minutes >= 0 && minutes < 60);
    }

    private static String formatWithLeadingZero(String time) {
        String[] parts = time.split(":");
        return String.format("%02d:%02d", Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }


    @GetMapping("/test/patch")
    public String patchTime(){
        List<ParkingLot> parkingLots = parkingLotRepository.findAll();
        for(ParkingLot parkingLot: parkingLots){
            String patch = parkingLot.getWeekdayStartTime();
            if (patch.matches("^([01]?\\d|2[0-3]):[0-5]\\d$")) continue;
            String newTime = convertToHHMM(patch);
            if (newTime != null) {
                parkingLot.setWeekdayStartTime(newTime);
                parkingLotRepository.save(parkingLot);
            }
        }
        return "Good";
    }

    @GetMapping("/test/current")
    public List<String> getCurrentParking(){
        List<String> errorList = new ArrayList<>();
        List<ParkingLot> parkingLots = parkingLotRepository.findByIsCurrent("1");
        for(ParkingLot parkingLot : parkingLots){
            String address = parkingLot.getAddress();
            if (parkingLot.getLatitude() == null) {
                CoordinateResponse response = getCoordinateByAddressProcess(address);
                if (response == null) {
                    Random random = new Random();
                    double randomLat = BASE_LAT + (random.nextDouble() * 2 * OFFSET - OFFSET);
                    double randomLng = BASE_LNG + (random.nextDouble() * 2 * OFFSET - OFFSET);
                    parkingLot.setLatitude(randomLat);
                    parkingLot.setLongitude(randomLng);
                    parkingLotRepository.save(parkingLot);
                    log.info("Save Coordinate");
                } else {
                    parkingLot.setLatitude(response.getLatitude());
                    parkingLot.setLongitude(response.getLongitude());
                    parkingLotRepository.save(parkingLot);
                }
            }
        }
        return errorList;
    }
    public CoordinateResponse getCoordinateByAddressProcess(String address){

        String apiKey = "KakaoAK " + KAKAO_APP_KEY;
        RestClient restClient = RestClient.create();

        String responseBody = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host(KAKAO_ADDRESS_TO_COORDINATE_HOST)
                        .path(KAKAO_ADDRESS_TO_COORDINATE_PATH)
                        .queryParam("query", address)
                        .build())
                .header("Authorization", apiKey)
                .retrieve()
                .body(String.class);
        try {
            JSONObject json = new JSONObject(responseBody);
            JSONArray documents = json.getJSONArray("documents");
            double longitude = documents.getJSONObject(0).getDouble("x");
            double latitude = documents.getJSONObject(0).getDouble("y");
            return new CoordinateResponse(latitude, longitude);
        } catch (JSONException e) {
            log.info("Coordinate Process Error");
            return null;
        }
    }
}
