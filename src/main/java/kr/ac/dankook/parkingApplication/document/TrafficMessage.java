package kr.ac.dankook.parkingApplication.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "NaHC_Traffic")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrafficMessage {

    @Id
    private String poiCode;
    private String message;
    private String state;
    private double latitude;
    private double longitude;
    private int speed;
}
