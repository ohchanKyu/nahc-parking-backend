package kr.ac.dankook.parkingApplication.repository;

import kr.ac.dankook.parkingApplication.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingLotRepository extends JpaRepository<ParkingLot,Long> {
    List<ParkingLot> findByNameContainingIgnoreCase(String keyword);
    List<ParkingLot> findByIsCurrent(String isCurrent);
    @Query("SELECT p FROM ParkingLot p WHERE p.address LIKE CONCAT('%', :keyword, '%')")
    List<ParkingLot> findByAddressContainingQuotes(@Param("keyword") String keyword);
    @Query("SELECT distinct regionCode from ParkingLot")
    List<String> findDistinctRegionCodes();
    @Query("SELECT distinct type from ParkingLot")
    List<String> findDistinctType();
    List<ParkingLot> findByRegionCode(String regionCode);
}
