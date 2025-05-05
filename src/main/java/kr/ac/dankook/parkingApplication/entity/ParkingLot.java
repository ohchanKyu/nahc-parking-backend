package kr.ac.dankook.parkingApplication.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParkingLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String isCurrent;
    private String regionCode;
    private String name;
    private Double longitude; // 경도
    private Double latitude; // 위도
    private String type;
    private String category;
    private String phoneNumber; // 연락처
    private String address;
    private int totalSpace;
    private String weekdayStartTime; // 평일운영시작시각
    private String weekdayEndTime; // 평일운영종료시각
    private String weekendStartTime; // 주말운영시작시간
    private String weekendEndTime; // 주말운영종료시각
    private String holidayStartTime; // 공휴일운영시작시각
    private String holidayEndTime; // 공휴일운영종료시각
    private String feeInfo; // 요금정보
}
