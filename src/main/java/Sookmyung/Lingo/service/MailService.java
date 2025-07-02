package Sookmyung.Lingo.service;

import Sookmyung.Lingo.common.CustomException;
import Sookmyung.Lingo.common.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender javaMailSender;
    private static final String senderEmail = "2025lingo@gmail.com";

    /*
     * 인증 코드 생성 메서드
     * 랜덤으로 소문자, 대문자, 숫자를 조합하여 8자리 인증 코드를 생성합니다.
     * @return 생성된 인증 코드 문자열
     * */
    public String createNumber() {
        Random random = new Random();
        StringBuilder key = new StringBuilder();

        for (int i = 0; i < 8; i++) { // 인증 코드 8자리
            int index = random.nextInt(3); // 랜덤으로 0, 1, 2 중 하나 선택

            switch (index) {
                case 0 -> key.append((char) (random.nextInt(26) + 97)); // 소문자
                case 1 -> key.append((char) (random.nextInt(26) + 65)); // 대문자
                case 2 -> key.append(random.nextInt(10)); // 숫자
            }
        }
        return key.toString();
    }

    public MimeMessage createMail(String mail, String number) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();

        message.setFrom(senderEmail);
        message.setRecipients(MimeMessage.RecipientType.TO, mail);
        message.setSubject("[Lingo] 비밀번호 찾기 인증 번호");
        String body = "";
        body += "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd; border-radius: 10px; background-color: #f9f9f9;'>";
        body += "<h2 style='color: #4A90E2;'>[Lingo] 비밀번호 찾기</h2>";
        body += "<p>요청하신 인증 번호입니다:</p>";
        body += "<div style='font-size: 24px; font-weight: bold; color: #333; margin: 20px 0;'>[ " + number + " ]</div>";
        body += "<p style='color: #555;'>계정에 등록된 이메일로 발송되었습니다.</p>";
        body += "<p style='color: #555;'>직접 요청하지 않으셨다면 즉시 비밀번호를 변경해 주세요.</p>";
        body += "<p style='color: #555;'>인증 번호는 10분 동안 유효합니다.</p>";
        body += "<p style='margin-top: 30px;'>감사합니다.<br><strong>Lingo 팀 드림</strong></p>";
        body += "</div>";

        message.setText(body, "UTF-8", "html");

        return message;
    }

    // 메일 발송
    public String sendVerifyCodeMessage(String sendEmail) throws MessagingException {
        String number = createNumber(); // 랜덤 인증번호 생성

        MimeMessage message = createMail(sendEmail, number); // 메일 생성
        try {
            javaMailSender.send(message); // 메일 발송
        } catch (MailException e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.EMAIL_SEND_ERROR); // 이메일 발송 오류 발생 시 예외 처리
        }

        return number; // 생성된 인증번호 반환
    }
}
