package kr.ac.dankook.parkingApplication.service;

import kr.ac.dankook.parkingApplication.config.converter.MemberEntityConverter;
import kr.ac.dankook.parkingApplication.dto.request.FindIdRequest;
import kr.ac.dankook.parkingApplication.entity.*;
import kr.ac.dankook.parkingApplication.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberEntityConverter memberEntityConverter;
    private final BookmarkRepository bookmarkRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomPinRepository chatRoomPinRepository;

    @Transactional
    public boolean deleteMemberProcess(Long memberId){
        Member member = memberEntityConverter.getMemberByMemberId(memberId);
        // bookmark delete
        List<Bookmark> bookmarks = bookmarkRepository.findByMemberId(memberId);
        bookmarkRepository.deleteAll(bookmarks);
        // ChatRoom
        List<ChatRoomMember> chatRooms = chatRoomMemberRepository.findByMember(member);
        chatRoomMemberRepository.deleteAll(chatRooms);
        // ChatRoom pin
        List<ChatRoomPin> chatRoomPins = chatRoomPinRepository.findByMember(member);
        chatRoomPinRepository.deleteAll(chatRoomPins);
        memberRepository.deleteById(memberId);
        return true;
    }

    @Transactional(readOnly = true)
    public List<String> findUserIdProcess(FindIdRequest findIdRequest){
        return memberRepository.findByNameAndEmail(findIdRequest.getName(), findIdRequest.getEmail())
                .stream()
                .map(Member::getUserId)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Member findMemberByUserIdProcess(String userId){
        Optional<Member> member = memberRepository.findByUserId(userId);
        return member.orElse(null);
    }
}
