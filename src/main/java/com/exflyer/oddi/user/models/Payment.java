package com.exflyer.oddi.user.models;

import com.exflyer.oddi.user.api.payment.dto.InicisAuthMobileResult;
import com.exflyer.oddi.user.api.payment.dto.InicisAuthResult;
import com.exflyer.oddi.user.api.payment.dto.InicisMobileReqResult;
import com.exflyer.oddi.user.api.payment.dto.InicisReqResult;
import com.exflyer.oddi.user.api.payment.dto.PaymentRes;
import com.exflyer.oddi.user.share.LocalDateUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@ApiModel("결제")
@Table(name="payment")
@NoArgsConstructor
public class Payment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 순번
     */
    @Id
    @ApiModelProperty("순번")
    @Column(name = "seq", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    /**
     *종류(결제,취소)
     */
    @ApiModelProperty("종류(결제,취소)")
    @Column(name = "type")
    private String type;

    /**
     *회원 id
     */
    @ApiModelProperty("회원 id")
    @Column(name = "member_id")
    private String memberId;

    /**
     *광고 순번
     */
    @ApiModelProperty("광고 순번")
    @Column(name = "adv_seq")
    private Long advSeq;

    /**
     *금액
     */
    @ApiModelProperty("금액")
    @Column(name = "price")
    private Integer price;

    /**
     *채널 종류(오디존, 지하철)
     */
    @ApiModelProperty("채널 종류(오디존, 지하철)")
    @Column(name = "channel_type")
    private String channelType;

    /**
     *상품 이름
     */
    @ApiModelProperty("상품 이름")
    @Column(name = "product_name")
    private String productName;

    /**
     *광고 이름
     */
    @ApiModelProperty("광고 이름")
    @Column(name = "adv_name")
    private String advName;

    /**
     * 광고 시작 날짜
     */
    @ApiModelProperty("광고 시작 날짜")
    @Column(name = "adv_start_date")
    private String advStartDate;

    /**
     *광고 종료 날짜
     */
    @ApiModelProperty("광고 종료 날짜")
    @Column(name = "adv_end_date")
    private String advEndDate;

    /**
     *생성 날짜
     */
    @ApiModelProperty("생성 날짜")
    @Column(name = "reg_date")
    private LocalDateTime regDate;

    /**
     *응답 코드(PG 연동)
     */
    @ApiModelProperty("응답 코드(PG 연동)")
    @Column(name = "response_code")
    private String responseCode;

    /**
     *응답 메세지(PG 연동)
     */
    @ApiModelProperty("응답 메세지(PG 연동)")
    @Column(name = "response_message")
    private String responseMessage;

    /**
     *연동 날짜(PG 연동)
     */
    @ApiModelProperty("연동 날짜(PG 연동)")
    @Column(name = "sync_date")
    private LocalDateTime syncDate;

    /**
     *성공 여부(PG 연동)
     */
    @ApiModelProperty("성공")
    @Column(name = "success")
    private Boolean success;

    public void setPayment(InicisAuthResult res, String type) {
        this.responseCode = res.getResultCode();
        this.responseMessage = res.getResultMsg();
        this.type = type;
        this.syncDate = LocalDateUtils.krNow();
        this.success = res.getResultCode() == "0000" ?  false : true;
    }

    public void setCancelPayment(InicisReqResult res, String type) {
        this.responseCode = res.getResultCode();
        this.responseMessage = res.getResultMsg();
        this.type = type;
        this.syncDate = LocalDateUtils.krNow();
        this.success = res.getResultCode() == "0000" ?  false : true;
    }

    public Payment(PaymentRes req) {
        this.type = req.getType();
        this.memberId = req.getMemberId();
        this.advSeq = req.getAdvSeq();
        this.price = req.getPrice();
        this.channelType = req.getChannelType();
        this.productName = req.getProductName();
        this.advName = req.getAdvName();
        this.advStartDate = req.getAdvStartDate();
        this.advEndDate = req.getAdvEndDate();
        this.regDate = req.getRegDate();

    }

    public void setPayment(InicisAuthMobileResult res, String type) {
        this.responseCode = res.getResultCode();
        this.responseMessage = res.getResultMsg();
        this.type = type;
        this.syncDate = LocalDateUtils.krNow();
        this.success = res.getResultCode() == "00" ?  false : true;
    }

    public void setCancelPayment(InicisMobileReqResult res, String type) {
        this.responseCode = res.getP_STATUS();
        this.responseMessage = res.getP_RMESG1();
        this.type = type;
        this.syncDate = LocalDateUtils.krNow();
        this.success = res.getP_STATUS() == "00" ?  false : true;
    }
}
