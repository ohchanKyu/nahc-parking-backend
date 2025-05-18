package kr.ac.dankook.parkingApplication.service;

import kr.ac.dankook.parkingApplication.document.TrafficMessage;
import kr.ac.dankook.parkingApplication.repository.TrafficMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParkingTrafficScheduledService {

    private final WebClient webClient;
    private final TrafficMessageRepository trafficMessageRepository;

    public List<TrafficMessage> getTrafficMessagesProcess() {
        return trafficMessageRepository.findAll();
    }

    @Autowired
    public ParkingTrafficScheduledService(
            @Value("${api.seoul-traffic.endpoint}") String trafficEndpoint,
            TrafficMessageRepository trafficMessageRepository
    ) {
        this.trafficMessageRepository = trafficMessageRepository;
        this.webClient = WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                                .build())
                .baseUrl(trafficEndpoint).build();
    }

    @Scheduled(cron = "0 */3 * * * *") // 3 minutes
    public void updateCurrentInfoScheduler() {
        log.info("Update Current Traffic Information");

        for (int i = 1; i <= 128; i++) {

            if (i == 22 || i == 28 || i == 57 || i == 62 || i ==65 ||
                i == 69 || i == 75 || i == 97) continue;

            String poiCode = String.format("POI%03d", i);
            webClient.get()
                    .uri("/" + poiCode)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(body -> parsingDataAndSave(body, poiCode));
        }
    }

    private void parsingDataAndSave(String body,String targetCode){
        try{
            JSONObject json = new JSONObject(body);
            JSONObject cityDataObject = json.getJSONObject("CITYDATA");

            String poiCode = cityDataObject.getString("AREA_CD");
            JSONObject trafficObject = cityDataObject.getJSONObject("ROAD_TRAFFIC_STTS")
                    .getJSONObject("AVG_ROAD_DATA");
            String trafficMessage = trafficObject.getString("ROAD_MSG");
            String state = trafficObject.getString("ROAD_TRAFFIC_IDX");
            int trafficSpeed = trafficObject.getInt("ROAD_TRAFFIC_SPD");

            Optional<TrafficMessage> targetMessage = trafficMessageRepository
                    .findById(poiCode);
            if (targetMessage.isPresent()) {
                TrafficMessage target = targetMessage.get();
                target.setMessage(trafficMessage);
                target.setSpeed(target.getSpeed());
                target.setState(state);
                trafficMessageRepository.save(target);
            }else{
                TrafficMessage message = TrafficMessage.builder()
                        .message(trafficMessage)
                        .speed(trafficSpeed)
                        .state(state).poiCode(poiCode)
                        .build();
                trafficMessageRepository.save(message);
            }
        }catch(JSONException e) {
            log.error("Fetch error during update traffic information - {} - {}",e.getMessage(),targetCode);
        }
    }

}
