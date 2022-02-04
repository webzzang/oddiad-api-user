package com.exflyer.oddi.user.api.adv.subway.dto;

import com.exflyer.oddi.user.api.adv.oddi.dto.OddiReadyPartnerSlotRes;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubwayReadyPartnerSlotSearchResult {

    @ApiModelProperty(value = "파트너 슬롯 사용현황 목록", position = 0)
    private List<SubwayReadyPartnerSlotRes> partnerSlotList;
}