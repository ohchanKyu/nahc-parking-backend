package kr.ac.dankook.parkingApplication.repository;

import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface CurrentParkingLotRepository {
    void saveCurrentInfo(String name,String currentInfo);
    Optional<String> findCurrentInfoByName(String name);
}
