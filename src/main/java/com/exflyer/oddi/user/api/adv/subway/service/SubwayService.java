package com.exflyer.oddi.user.api.adv.subway.service;

import com.exflyer.oddi.user.api.adv.oddi.dao.OddiMapper;
import com.exflyer.oddi.user.api.adv.oddi.dto.FileListRes;
import com.exflyer.oddi.user.api.adv.oddi.dto.OddiReadyPartnerSlotReq;
import com.exflyer.oddi.user.api.adv.oddi.dto.OddiReadyPartnerSlotRes;
import com.exflyer.oddi.user.api.adv.subway.dao.SubwayMapper;
import com.exflyer.oddi.user.api.adv.subway.dto.SubwayInfoList;
import com.exflyer.oddi.user.api.adv.subway.dto.SubwayLine;
import com.exflyer.oddi.user.api.adv.subway.dto.SubwayName;
import com.exflyer.oddi.user.api.adv.subway.dto.SubwayPartnerListRes;
import com.exflyer.oddi.user.api.adv.subway.dto.SubwayPartnerSearchReq;
import com.exflyer.oddi.user.models.Youtube;
import com.exflyer.oddi.user.api.adv.subway.dto.SubwayReadyPartnerSlotReq;
import com.exflyer.oddi.user.api.adv.subway.dto.SubwayReadyPartnerSlotRes;
import com.exflyer.oddi.user.models.Youtube;
import java.text.ParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class SubwayService {

    @Autowired
    private SubwayMapper subwayMapper;

    @Autowired
    private OddiMapper oddiMapper;

    public SubwayInfoList getSubwayList() {
        SubwayInfoList subwayInfoList = new SubwayInfoList();
        subwayInfoList.setSubwayLineList(subwayMapper.findAbleSubwayLineList(0));
        subwayInfoList.setSubwayNameList(subwayMapper.findPartnerSubwayName());
        return subwayInfoList;
    }

    public Youtube findVodInfo(Long partnerSeq, Long productSeq, String channelType){
        return oddiMapper.findVodInfo(partnerSeq, productSeq, channelType);
    }

    public List<SubwayPartnerListRes> getPartnerList(SubwayPartnerSearchReq req) {
        if(req.getSubwayCode() != null && req.getSubwayCode().equalsIgnoreCase("transfer")){
            req.setTransfer(true);
            req.setSubwayCode(null);
        }
        List<SubwayPartnerListRes> res = subwayMapper.findSubwayPartnerList(req);
        if(res != null){
            res.forEach(r->{
                r.setSubwayLineList(subwayMapper.findAbleSubwayLineList(r.getSeq()));
                r.setSubwayImageList(subwayMapper.findSubwayPartnerImage(r.getSeq()));

                Youtube youtube = findVodInfo(r.getSeq(), null, req.getChannelType());
                r.setYoutube(youtube);
            });
        }
        return res;
    }

    /**
     * 지정된 기간내의 사용가능 슬롯 수 조회
     *
     * @param req 조회조건
     * @return List
     */
    public List<SubwayReadyPartnerSlotRes> findReadyPartnerSlotList(SubwayReadyPartnerSlotReq req) throws ParseException {
        return subwayMapper.findReadyPartnerSlotList(req.countEndDate());
    }
}
