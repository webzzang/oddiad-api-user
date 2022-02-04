package com.exflyer.oddi.user.api.user.account.dto;

import com.exflyer.oddi.user.annotaions.EncryptField;
import com.exflyer.oddi.user.annotaions.Password;
import com.exflyer.oddi.user.annotaions.PhoneNumber;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberAddReq {

  @ApiModelProperty(value = "id", hidden = true)
  private String id;

  @ApiModelProperty(value = "이름", position = 0)
  @NotBlank
  private String name;

  @ApiModelProperty(value = "이메일(ID)", position = 1)
  @EncryptField
  @NotBlank
  @Email
  private String email;

  @ApiModelProperty(value = "비밀번호", position = 2)
  @NotBlank
  @Password
  private String password;

  @ApiModelProperty(value = "전화번호", position = 3)
  @EncryptField
  @NotBlank
  @PhoneNumber
  private String phoneNumber;

  @ApiModelProperty(value = "광고 수신 동의 여부 (default : false)", position = 6)
  private boolean receiveConsent = true;

  @ApiModelProperty(value = "광고 이메일수신 동의여부", position = 4)
  private boolean emailReceiveConsent = true;

  @ApiModelProperty(value = "동의약관 목록", position = 7)
  private List<TermsReq> terms;

}
