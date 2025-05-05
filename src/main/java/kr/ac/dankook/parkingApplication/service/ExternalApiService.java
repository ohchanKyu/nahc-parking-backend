package kr.ac.dankook.parkingApplication.service;

import kr.ac.dankook.parkingApplication.dto.request.CoordinateRequest;
import kr.ac.dankook.parkingApplication.dto.request.LocationRequest;
import kr.ac.dankook.parkingApplication.dto.response.RouteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalApiService {

    @Value("${api.kakao.app-key}")
    private String KAKAO_APP_KEY;
    @Value("${api.kakao.route-host}")
    private String KAKAO_ROUTE_HOST;
    @Value("${api.kakao.route-path}")
    private String KAKAO_ROUTE_PATH;

    public RouteResponse getRouteInformationProcess(LocationRequest locationRequest){

        String apiKey = "KakaoAK " + KAKAO_APP_KEY;
        RestClient restClient = RestClient.create();

        CoordinateRequest startLocation =
                new CoordinateRequest(locationRequest.getStartLatitude(), locationRequest.getStartLongitude());
        CoordinateRequest endLocation =
                new CoordinateRequest(locationRequest.getEndLatitude(), locationRequest.getEndLongitude());

        String responseBody = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host(KAKAO_ROUTE_HOST)
                        .path(KAKAO_ROUTE_PATH)
                        .queryParam("origin",startLocation.getLongitude()+","+ startLocation.getLatitude())
                        .queryParam("destination",endLocation.getLongitude()+","+ endLocation.getLatitude())
                        .queryParam("waypoints","")
                        .build())
                .header("Authorization", apiKey)
                .header("Content-Type","application/json")
                .retrieve()
                .body(String.class);
        try{
            JSONObject json = new JSONObject(responseBody);
            JSONArray routes = json.getJSONArray("routes");
            JSONObject routeObject = (JSONObject) routes.get(0);
            JSONObject summaryObject = routeObject.getJSONObject("summary");

            String distance;
            int distanceObject = summaryObject.getInt("distance");
            if (distanceObject >= 1000){
                distanceObject = Math.round(distanceObject / 1000.f);
                distance = distanceObject+"km";
            }else {
                distance = distanceObject + "m";
            }
            int duration = Math.round(summaryObject.getInt("duration") / 60.0f);
            int taxiFare = summaryObject.getJSONObject("fare").getInt("taxi");
            int tollFare = summaryObject.getJSONObject("fare").getInt("toll");
            return new RouteResponse(
                    locationRequest,distance,duration+"분",taxiFare+"원",tollFare+"원");
        }catch(JSONException e){
            log.info(e.toString());
            log.info("Route Process Error. Return Default Message - {}",e.toString());
            return new RouteResponse(
                    locationRequest,"정보 없음", "정보 없음", "정보 없음", "정보 없음");
        }
    }
}
