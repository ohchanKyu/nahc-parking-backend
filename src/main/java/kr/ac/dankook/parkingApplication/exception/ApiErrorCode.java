package kr.ac.dankook.parkingApplication.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ApiErrorCode implements ErrorCode{

    CHATROOM_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND,"Not found ChatRoom Member with ChatRoom and Member key."),
    CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND,"Not found ChatRoom with your primary key."),
    MAIL_SEND_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"Error during sending mail"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request parameters."),
    DUPLICATE_ID(HttpStatus.INTERNAL_SERVER_ERROR, "This is duplicated Id."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "RefreshToken is Expired. Please Login again."),
    REFRESH_TOKEN_NOT_EXIST(HttpStatus.UNAUTHORIZED, "RefreshToken is not exist in database. Please Login again."),
    REFRESH_TOKEN_NOT_EQUAL(HttpStatus.UNAUTHORIZED, "RefreshToken is not equal in database. Please Login again."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "Not found member with your primary key or userId"),
    DECRYPT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Error during decrypting primary key."),
    JSON_CONVERT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"Failed to convert Json with Object"),
    JSON_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "JSON parsing error.");

    private final HttpStatus httpStatus;
    private final String message;
}
