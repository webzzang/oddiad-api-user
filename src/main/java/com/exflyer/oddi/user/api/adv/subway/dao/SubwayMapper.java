package com.exflyer.oddi.user.api.adv.subway.dao;


import com.exflyer.oddi.user.api.adv.subway.dto.SubwayImage;
import com.exflyer.oddi.user.api.adv.subway.dto.SubwayLine;
import com.exflyer.oddi.user.api.adv.subway.dto.SubwayName;
import com.exflyer.oddi.user.api.adv.subway.dto.SubwayPartnerListRes;
import com.exflyer.oddi.user.api.adv.subway.dto.SubwayPartnerSearchReq;
import com.exflyer.oddi.user.api.adv.subway.dto.SubwayReadyPartnerSlotReq;
import com.exflyer.oddi.user.api.adv.subway.dto.SubwayReadyPartnerSlotRes;
import java.util.List;
import org.springframework.stereotype.Repository;


@Repository
public interface SubwayMapper {

    List<SubwayLine> findAbleSubwayLineList(long seq);

    List<SubwayName> findPartnerSubwayName();

    List<SubwayPartnerListRes> findSubwayPartnerList(SubwayPartnerSearchReq req);

    List<SubwayImage> findSubwayPartnerImage(long seq);

    List<SubwayReadyPartnerSlotRes> findReadyPartnerSlotList(SubwayReadyPartnerSlotReq req);

}
