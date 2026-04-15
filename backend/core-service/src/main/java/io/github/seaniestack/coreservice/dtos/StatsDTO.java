package io.github.seaniestack.coreservice.dtos;

import lombok.Data;

@Data
public class StatsDTO {
    long activeLoans;
    long totalBooks;
    long registeredUsers;
}
