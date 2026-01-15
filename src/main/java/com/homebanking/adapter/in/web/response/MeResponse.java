package com.homebanking.adapter.in.web.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class MeResponse {
    private Long id;
    private String email;
    private String name;
    private String lastName;
    private List<AccountDto> accounts;

    @Data
    @Builder
    public static class AccountDto {
        private Long id;
        private String cbu;
        private String alias;
        private BigDecimal balance;
    }
}