package uk.gov.hmcts.opal.service.opal.history.core;

import java.math.BigInteger;
import lombok.Getter;

@Getter
public class AccountHistoryContext {

    private final AccountHistoryType accountType;

    private final Long accountId;

    private final BigInteger version;

    public AccountHistoryContext(AccountHistoryType accountType, Long accountId) {
        this(accountType, accountId, null);
    }

    public AccountHistoryContext(AccountHistoryType accountType, Long accountId, BigInteger version) {
        this.accountType = accountType;
        this.accountId = accountId;
        this.version = version;
    }

    public AccountHistoryContext withVersion(BigInteger version) {
        return new AccountHistoryContext(accountType, accountId, version);
    }
}
