package com.exflyer.oddi.user.jwt.dto;

import lombok.Data;

@Data
public class JwtResult {

  private String accessToken;

  private String refreshToken;

  private boolean passwordReset;

  public JwtResult(String accessToken, String refreshToken, boolean passwordReset) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.passwordReset = passwordReset;
  }
}
