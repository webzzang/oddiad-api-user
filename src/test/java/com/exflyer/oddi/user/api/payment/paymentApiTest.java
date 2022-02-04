package com.exflyer.oddi.user.api.payment;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.exflyer.oddi.user.api.payment.dto.InicisCancelReq;
import com.exflyer.oddi.user.api.payment.dto.InicisCancelRes;
import com.exflyer.oddi.user.api.payment.dto.PaymentCancelReq;
import com.exflyer.oddi.user.api.payment.dto.PaymentCancelRes;
import com.exflyer.oddi.user.api.user.account.dto.MemberIdDuplicationReq;
import com.exflyer.oddi.user.models.PgPaymentLog;
import com.exflyer.oddi.user.share.LocalDateUtils;
import com.exflyer.oddi.user.share.TokenGenerator;
import com.google.gson.Gson;
import com.inicis.std.util.SignatureUtil;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class paymentApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Gson gson;

    @Autowired
    private TokenGenerator tokenGenerator;


    @DisplayName("이니시스 결제취소")
    @Test
    public void cancelTest() throws Exception {

        MvcResult mvcResult = mockMvc
            .perform(
                MockMvcRequestBuilders.put("/payment/33/196")
                    .header(TokenGenerator.Header, tokenGenerator.getTestToken())
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();
        Map result = gson.fromJson(mvcResult.getResponse().getContentAsString(), Map.class);
        String responseCode = result.get("code").toString();
        Assertions.assertThat(responseCode).isEqualTo("000");
    }


}
