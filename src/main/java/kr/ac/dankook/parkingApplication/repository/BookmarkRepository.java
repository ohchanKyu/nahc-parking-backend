package kr.ac.dankook.parkingApplication.repository;

import kr.ac.dankook.parkingApplication.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findByMemberId(Long memberId);
    Optional<Bookmark> findByMemberIdAndParkingLotId(Long memberId, Long parkingLotId);
}
