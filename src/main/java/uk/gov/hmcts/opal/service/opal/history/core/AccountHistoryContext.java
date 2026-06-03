package uk.gov.hmcts.opal.service.opal.history.core;

import lombok.Getter;

@Getter
public class AccountHistoryContext {

    private final AccountHistoryType accountType;

    private final Long accountId;

    private final Integer version;

    public AccountHistoryContext(AccountHistoryType accountType, Long accountId) {
        this(accountType, accountId, null);
    }

    public AccountHistoryContext(AccountHistoryType accountType, Long accountId, Integer version) {
        this.accountType = accountType;
        this.accountId = accountId;
        this.version = version;
    }

    public AccountHistoryContext withVersion(Integer version) {
        return new AccountHistoryContext(accountType, accountId, version);
    }
}
