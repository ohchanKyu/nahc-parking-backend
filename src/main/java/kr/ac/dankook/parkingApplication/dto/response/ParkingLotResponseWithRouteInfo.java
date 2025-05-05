package kr.ac.dankook.parkingApplication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParkingLotResponseWithRouteInfo {

    private ParkingLotResponse parkingLotResponse;
    private RouteResponse routeResponse;
}
