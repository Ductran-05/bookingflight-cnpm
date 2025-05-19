package com.cnpm.bookingflight.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ResLoginDTO {
    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

}
