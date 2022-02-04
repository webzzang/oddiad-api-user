package com.exflyer.oddi.user.api.user.account.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class MemberResign {

  @ApiModelProperty(value = "id", hidden = true)
  private String id;

  @ApiModelProperty(value = "회원상태", position = 3)
  private String stateCode;

  public MemberResign(String id, String stateCode) {
    this.id = id;
    this.stateCode = stateCode;
  }


}
