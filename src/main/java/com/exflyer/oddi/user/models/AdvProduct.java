package com.exflyer.oddi.user.models;

import com.exflyer.oddi.user.share.LocalDateUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 광고_묶음상품
 */
@Data
@Entity
@NoArgsConstructor
@ApiModel("광고_묶음상품")
@Table(name = "adv_product")
public class AdvProduct implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private AdvProductPk advProductPk;

    /**
     * 광고 슬롯
     */
    @ApiModelProperty("광고 슬롯")
    @Column(name = "request_slot", nullable = false)
    private Integer requestSlot;

    /**
     * 광고 슬롯
     */
    @ApiModelProperty("광고 슬롯")
    @Column(name = "reg_date", nullable = false)
    private LocalDateTime regDate;

    public AdvProduct(Integer requestSlot) {
        this.requestSlot = requestSlot;
        this.regDate = LocalDateUtils.krNow();
    }
}