package kr.ac.dankook.parkingApplication.dto.response;

import kr.ac.dankook.parkingApplication.dto.request.LocationRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RouteResponse {
    private LocationRequest locationRequest;
    private String distance;
    private String time;
    private String taxiFare;
    private String tollFare;
}
