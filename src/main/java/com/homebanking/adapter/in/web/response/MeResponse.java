package com.homebanking.adapter.in.web.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class MeResponse {
    private UUID id;
    private String email;
    private String name;
    private String lastName;
    private List<AccountDto> accounts;

    @Data
    @Builder
    public static class AccountDto {
        private UUID id;
        private String cbu;
        private String alias;
        private BigDecimal balance;
    }
}

