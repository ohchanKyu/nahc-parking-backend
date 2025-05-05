package kr.ac.dankook.parkingApplication.dto.response;

import kr.ac.dankook.parkingApplication.entity.ParkingLot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DistanceResponse {

    private double distance;
    private ParkingLot parkingLot;
}
