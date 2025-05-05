package kr.ac.dankook.parkingApplication.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import kr.ac.dankook.parkingApplication.config.principal.PrincipalDetails;
import kr.ac.dankook.parkingApplication.dto.request.SignInRequest;
import kr.ac.dankook.parkingApplication.dto.request.SignupRequest;
import kr.ac.dankook.parkingApplication.dto.request.TokenRequest;
import kr.ac.dankook.parkingApplication.dto.response.TokenResponse;
import kr.ac.dankook.parkingApplication.entity.Member;
import kr.ac.dankook.parkingApplication.exception.ApiErrorCode;
import kr.ac.dankook.parkingApplication.exception.ApiException;
import kr.ac.dankook.parkingApplication.jwt.JwtRedisHandler;
import kr.ac.dankook.parkingApplication.jwt.JwtTokenProvider;
import kr.ac.dankook.parkingApplication.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {


    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRedisHandler jwtRedisHandler;

    @Transactional(readOnly = true)
    public boolean isExistUserIdProcess(String userId){
        return memberRepository.existsByUserId(userId);
    }

    @Transactional
    public TokenResponse signupProcess(SignupRequest signupRequest) throws IOException {

        if (isExistUserIdProcess(signupRequest.getUserId())){
            throw new ApiException(ApiErrorCode.DUPLICATE_ID);
        }
        String encodePassword = passwordEncoder.encode(signupRequest.getPassword());
        Member newMember = Member.builder()
                .email(signupRequest.getEmail())
                .name(signupRequest.getName())
                .password(encodePassword)
                .userId(signupRequest.getUserId())
                .roles("ROLE_USER")
                .build();
        memberRepository.save(newMember);
        memberRepository.flush();
        TokenResponse token = signInProcess(
                new SignInRequest(newMember.getUserId(),signupRequest.getPassword())
        );
        return new TokenResponse(token.getAccessToken(),token.getRefreshToken());
    }

    @Transactional
    public TokenResponse signInProcess(SignInRequest signInRequest) {

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(signInRequest.getUserId(), signInRequest.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        TokenResponse token = jwtTokenProvider.generateToken(authentication);
        String userId = signInRequest.getUserId();
        jwtRedisHandler.save(userId,token.getRefreshToken());
        return token;

    }

    public TokenResponse reissueTokenProcess(TokenRequest tokenRequest){

        String targetRefreshToken = tokenRequest.getRefreshToken();

        Authentication authentication;
        try {
            authentication = jwtTokenProvider.validateToken(targetRefreshToken);
            log.info("Verification RefreshToken - {}",targetRefreshToken);
        } catch (JWTVerificationException e){
            log.info("Not Valid Refresh Token -{}",targetRefreshToken);
            throw new ApiException(ApiErrorCode.REFRESH_TOKEN_EXPIRED);
        }
        String targetUserId = jwtTokenProvider.getUserIdFromToken(tokenRequest.getRefreshToken());
        Optional<String> redisRefreshToken = jwtRedisHandler.findByUserId(targetUserId);
        if (redisRefreshToken.isEmpty()){
            log.info("Not Exists Refresh Token In Redis DB -{} -{}",targetUserId,targetRefreshToken);
            throw new ApiException(ApiErrorCode.REFRESH_TOKEN_NOT_EXIST);
        }else{
            if (!targetRefreshToken.equals(redisRefreshToken.get())){
                log.info("Not Equals Refresh Token In Redis DB -{} -{}",targetUserId,targetRefreshToken);
                throw new ApiException(ApiErrorCode.REFRESH_TOKEN_NOT_EQUAL);
            }
        }
        TokenResponse newTokenDto = jwtTokenProvider.generateToken(authentication);
        log.info("New Refresh Token -{} -{}",targetUserId,newTokenDto.getRefreshToken());
        jwtRedisHandler.save(targetUserId,newTokenDto.getRefreshToken());
        return newTokenDto;
    }

    public boolean logoutProcess(PrincipalDetails userDetails){
        String userId = userDetails.getUsername();
        Optional<String> redisRefreshToken = jwtRedisHandler.findByUserId(userId);
        if (redisRefreshToken.isPresent()){
            log.info("Logout User. User Id -{}",userId);
            jwtRedisHandler.delete(userId);
        }else{
            log.info("Already Logout User. User Id -{}",userId);
            return false;
        }
        SecurityContextHolder.clearContext();
        return true;
    }
}
