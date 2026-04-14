package io.github.seaniestack.supportservice.dtos;

import lombok.Data;

@Data
public class AnalyticsDTO {

    Long totalBorrows;
    Long activeBorrows;
    Long overdueBorrows;
    Long totalFinesCollected;
    Long unpaidFines;
}
