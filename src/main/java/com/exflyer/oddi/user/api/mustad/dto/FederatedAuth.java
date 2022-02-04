package com.exflyer.oddi.user.api.mustad.dto;

import java.util.Map;
import lombok.Data;

@Data
public class FederatedAuth {

    private String clientId;

    private Map<String, Object> logins;

    private Boolean isTerms;

    public FederatedAuth(Map<String, Object> map) {
        this.clientId = "portal-dev";
        this.logins = map;
        this.isTerms= true;
    }
}
