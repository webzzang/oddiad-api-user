package com.exflyer.oddi.user.api.adv.partner.dto;

import com.exflyer.oddi.user.models.Code;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PartnerJoinReq {

    @NotNull
    @ApiModelProperty(value = "name", position = 0)
    private String name;

    @NotNull
    @ApiModelProperty(value = "phone_number", position = 0)
    private String phoneNumber;

    @ApiModelProperty(value = "location", position = 0)
    private String location;

    @ApiModelProperty(value = "business", position = 0)
    private String business;

    @ApiModelProperty(value = "contents", position = 0)
    private String contents;

    @ApiModelProperty(value = "reg_date", position = 0)
    private LocalDateTime regDate;

}
