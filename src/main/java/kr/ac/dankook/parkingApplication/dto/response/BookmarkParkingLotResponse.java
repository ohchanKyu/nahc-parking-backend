package kr.ac.dankook.parkingApplication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookmarkParkingLotResponse {

    private String bookmarkId;
    private RouteResponse routeResponse;
    private ParkingLotResponse parkingLotResponse;
}
