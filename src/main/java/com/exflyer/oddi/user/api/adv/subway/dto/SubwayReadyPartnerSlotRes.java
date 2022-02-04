package com.exflyer.oddi.user.api.adv.subway.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SubwayReadyPartnerSlotRes {

    @ApiModelProperty(value = "광고처 시퀀스", position = 0)
    private Long partnerSeq;

    @ApiModelProperty(value = "광고처 총 슬롯 수", position = 1)
    private String totalSlot;

    @ApiModelProperty(value = "광고처 사용 슬롯 수", position = 2)
    private String usedSlot;

    @ApiModelProperty(value = "대기슬롯 수", position = 3)
    private String readySlot;
}