package kr.ac.dankook.parkingApplication.service;

import kr.ac.dankook.parkingApplication.dto.response.ParkingLotResponse;
import kr.ac.dankook.parkingApplication.entity.ParkingLot;
import kr.ac.dankook.parkingApplication.repository.CurrentParkingLotRepository;
import kr.ac.dankook.parkingApplication.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParkingLotScheduledService implements CurrentParkingLotRepository {


    private final RedisTemplate<String,String> redisTemplate;
    private final WebClient webClient;

    @Autowired
    public ParkingLotScheduledService(
            @Value("${api.seoul-parking.endpoint}") String seoulParingLotEndPoint,
            RedisTemplate<String, String> redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
        this.webClient = WebClient.builder().baseUrl(seoulParingLotEndPoint).build();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void saveCurrentInfo(String name, String currentInfo) {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        String existingInfo = operations.get(name);
        if (existingInfo != null && !existingInfo.isEmpty()) {
            delete(name);
        }
        operations.set(name, currentInfo);
    }

    public ParkingLotResponse convertToResponseEntity(ParkingLot parkingLot){

        ParkingLotResponse parkingLotResponse = ParkingLotResponse.builder()
                .id(EncryptionUtil.encrypt(parkingLot.getId()))
                .name(parkingLot.getName())
                .regionCode(parkingLot.getRegionCode())
                .longitude(parkingLot.getLongitude())
                .latitude(parkingLot.getLatitude())
                .type(parkingLot.getType())
                .category(parkingLot.getCategory())
                .phoneNumber(parkingLot.getPhoneNumber())
                .address(parkingLot.getAddress())
                .totalSpace(parkingLot.getTotalSpace())
                .weekdayStartTime(parkingLot.getWeekdayStartTime())
                .weekdayEndTime(parkingLot.getWeekdayEndTime())
                .weekendStartTime(parkingLot.getWeekendStartTime())
                .weekendEndTime(parkingLot.getWeekendEndTime())
                .holidayStartTime(parkingLot.getHolidayStartTime())
                .holidayEndTime(parkingLot.getHolidayEndTime())
                .feeInfo(parkingLot.getFeeInfo()).build();

        if (parkingLot.getIsCurrent().equals("1")){
            Optional<String> currentInfo = findCurrentInfoByName(parkingLot.getName());
            parkingLotResponse.setCurrentInfo(currentInfo.orElse("데이터 준비중입니다."));
        }else{
            parkingLotResponse.setCurrentInfo("지원 안함");
        }
        return parkingLotResponse;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public Optional<String> findCurrentInfoByName(String name) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String currentInfo = valueOperations.get(name);
        return Optional.ofNullable(currentInfo);
    }

    private void delete(String name){
        redisTemplate.delete(name);
    }

    @Scheduled(cron = "0 */3 * * * *") // 3 minutes
    public void updateCurrentInfoScheduler() {
        log.info("Update Current Parking Lot Information");

        webClient.get()
                .uri("")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(this::parsingDataAndSave);
    }

    private void parsingDataAndSave(String body){
        try{
            JSONObject json = new JSONObject(body);
            JSONArray resultArray = json.getJSONObject("GetParkingInfo").getJSONArray("row");
            for(Object object : resultArray){
                JSONObject resultObject = (JSONObject) object;
                String name = resultObject.getString("PKLT_NM");
                int totalCapacity = resultObject.getInt("TPKCT");
                int currentParking = resultObject.getInt("NOW_PRK_VHCL_CNT");
                int currentCapacity = totalCapacity - currentParking;
                if (currentCapacity > 0){
                    saveCurrentInfo(name,currentCapacity+"대 주차 가능");
                }else{
                    saveCurrentInfo(name,"0대 주차 가능");
                }
            }
        }catch(JSONException e) {
            log.error("Fetch error during update current parking lot information - {}",e.getMessage());
        }
    }

}
