package com.cnpm.bookingflight.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YearlyTicketResponse {
    private int year;
    private List<MonthlyTicketData> months;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyTicketData {
        private int month;
        private int maxTickets;
        private int ticketsSold;
    }
}