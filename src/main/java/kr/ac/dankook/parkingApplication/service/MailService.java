package kr.ac.dankook.parkingApplication.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import kr.ac.dankook.parkingApplication.dto.response.MailResponse;
import kr.ac.dankook.parkingApplication.entity.Member;
import kr.ac.dankook.parkingApplication.exception.ApiErrorCode;
import kr.ac.dankook.parkingApplication.exception.ApiException;
import kr.ac.dankook.parkingApplication.exception.MailException;
import kr.ac.dankook.parkingApplication.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String,String> redisTemplate;
    private static final long AUTH_MAIL_TOKEN_EXPIRE_TIME = 1000 * 60 * 15; // 15분

    public MailResponse createVerifyCodeMailResponse(Member member) throws IOException {

        String verificationCode = generateVerificationCode();

        saveVerificationCode(member, verificationCode);
        String content = "해당 인증번호를 통해 인증을 완료해주세요. - " + verificationCode;
       return MailResponse.builder()
                .mailAddress(member.getEmail())
                .title("[Parking-Mate] 인증번호 안내 이메일 입니다.")
                .content(content)
                .build();
    }

    public void sendMail(MailResponse mailResponse){
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(mailResponse.getMailAddress());
            helper.setSubject(mailResponse.getTitle());
            helper.setText(mailResponse.getContent(), true);
            helper.setFrom("ogyuchan01@gmail.com");
            helper.setReplyTo("ogyuchan01@gmail.com");

            log.info("Sending email to: {}", mailResponse.getMailAddress());
            mailSender.send(mimeMessage);

        } catch (MessagingException ex) {
            log.error("Failed to send email", ex);
            throw new MailException(ApiErrorCode.MAIL_SEND_ERROR);
        }
    }

    @Transactional
    public void changePasswordByEmailProcess(String authMailToken, String newPassword){

        String userId = authMailToken.split("_")[1];
        Member member = memberRepository.findByUserId(userId).orElseThrow(() ->
                new ApiException(ApiErrorCode.MEMBER_NOT_FOUND));
        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
        delete(authMailToken);
    }

    @SuppressWarnings("ConstantConditions")
    public boolean isVerifyCodeProcess(String authMailToken,String verifyCode){
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String storedCode = valueOperations.get(authMailToken);
        return storedCode != null && storedCode.equals(verifyCode);
    }

    private String generateVerificationCode(){
        return RandomStringUtils.randomAlphanumeric(10);
    }

    @SuppressWarnings("ConstantConditions")
    private void saveVerificationCode(Member member, String verificationCode) {

        String authMailToken = member.getEmail()+"_"+member.getUserId();
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        String existingToken = operations.get(authMailToken);
        if (existingToken != null && !existingToken.isEmpty()) {
            delete(authMailToken);
        }
        operations.set(authMailToken, verificationCode, AUTH_MAIL_TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);
    }
    public void delete(String authMailToken){
        redisTemplate.delete(authMailToken);
    }
}
