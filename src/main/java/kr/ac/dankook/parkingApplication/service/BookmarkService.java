package kr.ac.dankook.parkingApplication.service;

import kr.ac.dankook.parkingApplication.dto.request.CoordinateRequest;
import kr.ac.dankook.parkingApplication.dto.request.LocationRequest;
import kr.ac.dankook.parkingApplication.dto.response.BookmarkParkingLotResponse;
import kr.ac.dankook.parkingApplication.dto.response.ParkingLotResponse;
import kr.ac.dankook.parkingApplication.dto.response.RouteResponse;
import kr.ac.dankook.parkingApplication.entity.Bookmark;
import kr.ac.dankook.parkingApplication.entity.Member;
import kr.ac.dankook.parkingApplication.entity.ParkingLot;
import kr.ac.dankook.parkingApplication.repository.BookmarkRepository;
import kr.ac.dankook.parkingApplication.repository.ParkingLotRepository;
import kr.ac.dankook.parkingApplication.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ExternalApiService externalApiService;
    private final ParkingLotScheduledService parkingLotScheduledService;

    @Transactional(readOnly = true)
    public List<ParkingLotResponse> getBaseTypeBookmarksList(Long memberId){
        List<Bookmark> bookmarkList = bookmarkRepository.findByMemberId(memberId);
        List<ParkingLotResponse> responseList = new ArrayList<>();
        bookmarkList.forEach(bookmark -> {
            ParkingLot parkingLot = bookmark.getParkingLot();
            ParkingLotResponse parkingLotResponse =
                    parkingLotScheduledService.convertToResponseEntity(parkingLot);
            responseList.add(parkingLotResponse);
        });
        return responseList;
    }
    @Transactional(readOnly = true)
    public List<BookmarkParkingLotResponse> getAllBookmarksListProcess(Long memberId, CoordinateRequest coordinateRequest){

        List<BookmarkParkingLotResponse> responseList = new ArrayList<>();
        List<Bookmark> bookmarkList = bookmarkRepository.findByMemberId(memberId);

        for(Bookmark bookmark : bookmarkList){

            ParkingLot parkingLot = bookmark.getParkingLot();

            LocationRequest locationRequest = LocationRequest.builder()
                    .startLatitude(coordinateRequest.getLatitude())
                    .startLongitude(coordinateRequest.getLongitude())
                    .endLatitude(parkingLot.getLatitude())
                    .endLongitude(parkingLot.getLongitude()).build();
            RouteResponse routeResponse = externalApiService.getRouteInformationProcess(locationRequest);
            ParkingLotResponse parkingLotResponse =
                    parkingLotScheduledService.convertToResponseEntity(parkingLot);
            responseList.add(new BookmarkParkingLotResponse(
                    EncryptionUtil.encrypt(bookmark.getId()),routeResponse, parkingLotResponse));
        }
        return responseList;
    }


    @Transactional(readOnly = true)
    public Long checkParkingLotIsBookmarkProcess(Long memberId, Long parkingId){
        Optional<Bookmark> targetEntity = bookmarkRepository.findByMemberIdAndParkingLotId(memberId,parkingId);
        return targetEntity.map(Bookmark::getId).orElse(null);
    }

    @Transactional
    public boolean addBookmarkProcess(Member member, Long parkingLotId){

        Optional<Bookmark> checkPresentItem = bookmarkRepository.
                findByMemberIdAndParkingLotId(member.getId(),parkingLotId);

        if(checkPresentItem.isPresent()) return false;
        Optional<ParkingLot> bookmarkParkingLot = parkingLotRepository.findById(parkingLotId);
        if (bookmarkParkingLot.isEmpty()) return false;

        Bookmark newBookmark = Bookmark.builder()
                .member(member)
                .parkingLot(bookmarkParkingLot.get())
                .build();
        bookmarkRepository.save(newBookmark);
        return true;
    }

    @Transactional
    public boolean deleteBookmarkProcess(Long bookmarkId){
        Optional<Bookmark> targetEntity = bookmarkRepository.findById(bookmarkId);
        if(targetEntity.isPresent()){
            bookmarkRepository.deleteById(bookmarkId);
            return true;
        }
        return false;
    }
}
