package com.exflyer.oddi.user.enums;

import lombok.Getter;

public enum ApiResponseCodes {

  SUCCESS(200, "000", "success"),
  NOT_DUPLICATE(200, "000", "중복 되지 않은 정보 입니다."),
  BAD_REQUEST(400, "001", "잘못된 요청 입니다."),
  NOT_FOUND(404, "002", "정보를 찾을 수 없습니다."),
  TOKEN_EXPIRED(401, "003", "토큰이 만료 되었습니다."),
  AUTHENTIFICATION(401, "004", "인증 정보가 잘못 되었습니다."),
  MISS_MATCH(400, "005", "정보가 일치 하지 않습니다."),
  DUPLICATE(400, "007", "중복된 정보 입니다."),
  FORBIDDEN(403, "008", "접근 권한이 없습니다."),
  INIT_PASSWORD(400, "009", "비밀번호 재설정이 필요 합니다."),
  NEED_TO_PASSWORD_CHANGED(200, "010", "비밀번호 변경이 필요 합니다."),
  PASSWORD_CHANGE_DAY_OVER(200, "011", "비밀번호 변경 주기가 지났습니다."),
  EXPIRED_REQ_TIME(400, "012", "요청 시간이 초과 되었습니다."),
  SMS_SEND_FAIL(500, "013", "문자 발송에 실패 하였습니다."),
  AUDIT_NOT_FOUND(400, "014", "광고승인건으로 수정불가 합니다."),

  PASSWORD_CHANGE(400, "015", "패스워드가 일치하지 않습니다."),
  PASSWORD_NEW_CHANGE(400, "016", "변경하실 패스워드가 불일치 합니다."),

  // 인증번호 관련
  INVALID_VERIFICATION_NUMBER(403, "100", "인증번호가 불일치 합니다."),
  EXPIRED_VERIFICATION_NUMBER(403, "101", "인증번호 시간이 만료 되었습니다."),
  NOT_VERIFICATION(403, "102", "전화번호 인증을 하지 않았습니다."),

  // 파일관련
  INVALID_EXTENSION(400, "800", "허용 되지 않는 파일 입니다."),

  AWS_S3_FAIL(500, "900", "AWS S3 오류"),
  INTERNAL(500, "999", "관리자에게 문의 하세요"),

  USER_CTS002(400,"CTS002", "정지 회원입니다."),
  USER_CTS003(400,"CTS003", "비밀번호오류 입니다."),
  USER_CTS004(400,"CTS004", "휴면 회원입니다."),

  KAKAO_NOTI_ERROR(500, "500", "카카오 알림톡 전송이 실패하였습니다."),

  ;


  @Getter
  private final int status;

  @Getter
  private final String code;

  @Getter
  private final String message;


  ApiResponseCodes(int status, String code, String message) {
    this.status = status;
    this.code = code;
    this.message = message;

  }
}
