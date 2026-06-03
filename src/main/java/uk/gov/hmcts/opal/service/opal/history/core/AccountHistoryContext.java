package uk.gov.hmcts.opal.service.opal.history.core;

import lombok.Getter;

@Getter
public class AccountHistoryContext {

    private final AccountHistoryType accountType;

    private final Long accountId;

    public AccountHistoryContext(AccountHistoryType accountType, Long accountId) {
        this.accountType = accountType;
        this.accountId = accountId;
    }
}
