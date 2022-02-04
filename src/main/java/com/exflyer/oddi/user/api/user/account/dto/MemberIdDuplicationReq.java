package com.exflyer.oddi.user.api.user.account.dto;

import com.exflyer.oddi.user.annotaions.EncryptField;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberIdDuplicationReq {

  @ApiModelProperty(value = "이메일", position = 3)
  @NotBlank
  @EncryptField
  @Email
  private String email;
}
