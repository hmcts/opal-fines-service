package uk.gov.hmcts.opal.service.opal;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountStatus;
import uk.gov.hmcts.opal.exception.UnprocessableException;
import uk.gov.hmcts.opal.generated.model.CollectionOrderCommon;
import uk.gov.hmcts.opal.generated.model.EnforcementCourtDefendantAccount;
import uk.gov.hmcts.opal.generated.model.EnforcementOverrideDefendantAccount;
import uk.gov.hmcts.opal.generated.model.UpdateDefendantAccountRequestPayload;

@Component
public class DefendantAccountControlValidator {

    private static final Set<DefendantAccountStatus> BLOCKED_ACCOUNT_STATUSES = EnumSet.of(
        DefendantAccountStatus.ACCOUNT_CONSOLIDATED,
        DefendantAccountStatus.ACCOUNT_WRITTEN_OFF,
        DefendantAccountStatus.TRANSFER_OUT_PENDING,
        DefendantAccountStatus.TRANSFER_OUT_ACKNOWLEDGED,
        DefendantAccountStatus.TRANSFER_OUT_TO_NI_SCOTLAND
    );

    private static final Set<String> PAYMENT_TERMS_BLOCKED_LAST_ENFORCEMENTS = Set.of(
        "EO", "BWTD", "BWTU", "CLAMPO", "CONF", "CW", "CWN", "DW", "NBWT", "REW", "S136", "SC", "SUMM",
        "UPWO", "MPSO"
    );

    private static final Set<String> PAYMENT_CARD_BLOCKED_LAST_ENFORCEMENTS = Set.of(
        "ABDC", "AEO", "AEOC", "BWTD", "BWTU", "CLAMPO", "CW", "DW", "NBWT", "S136"
    );

    public void validateCanMutateParty(DefendantAccountEntity account) {
        validate(account, Check.ACCOUNT_STATUS);
    }

    public void validateCanAddPaymentTerms(DefendantAccountEntity account) {
        validate(account, Check.ACCOUNT_STATUS, Check.PAYMENT_TERMS_LAST_ENFORCEMENT, Check.ZERO_BALANCE);
    }

    public void validateCanAddPaymentCardRequest(DefendantAccountEntity account) {
        validate(account, Check.ACCOUNT_STATUS, Check.PAYMENT_CARD_LAST_ENFORCEMENT, Check.ZERO_BALANCE);
    }

    public void validateCanUpdateProtectedFields(DefendantAccountEntity account) {
        validate(account, Check.ACCOUNT_STATUS);
    }

    public boolean isProtectedUpdate(UpdateDefendantAccountRequest request, DefendantAccountEntity account) {
        UpdateDefendantAccountRequestPayload payload = request.getPayload();
        return isEnforcementCourtChange(payload.getEnforcementCourt(), account)
            || isCollectionOrderChange(payload.getCollectionOrder(), account)
            || isEnforcementOverrideChange(payload.getEnforcementOverride(), account);
    }

    public void validateCanRemoveEnforcementHold(DefendantAccountEntity account) {
        validate(account, Check.ACCOUNT_STATUS);
    }

    private boolean isEnforcementCourtChange(EnforcementCourtDefendantAccount enforcementCourt,
                                             DefendantAccountEntity account) {
        if (enforcementCourt == null) {
            return false;
        }
        Long currentCourtId = Optional.ofNullable(account.getEnforcingCourt())
            .map(CourtEntity::getCourtId)
            .orElse(null);
        return !Objects.equals(enforcementCourt.getCourtId(), currentCourtId);
    }

    private boolean isCollectionOrderChange(CollectionOrderCommon collectionOrder, DefendantAccountEntity account) {
        if (collectionOrder == null) {
            return false;
        }
        return !Objects.equals(collectionOrder.getCollectionOrderFlag(), account.getCollectionOrder())
            || !Objects.equals(collectionOrder.getCollectionOrderDate(), account.getCollectionOrderEffectiveDate());
    }

    private boolean isEnforcementOverrideChange(EnforcementOverrideDefendantAccount enforcementOverride,
                                                DefendantAccountEntity account) {
        if (enforcementOverride == null) {
            return false;
        }
        String requestedResultId = enforcementOverride.getEnforcementOverrideResult() == null
            ? null
            : enforcementOverride.getEnforcementOverrideResult().getEnforcementOverrideResultId();
        Long requestedEnforcerId = enforcementOverride.getEnforcer() == null
            ? null
            : enforcementOverride.getEnforcer().getEnforcerId();
        Short requestedLjaId = enforcementOverride.getLja() == null
            ? null
            : enforcementOverride.getLja().getLjaId();

        return !Objects.equals(requestedResultId, account.getEnforcementOverrideResultId())
            || !Objects.equals(requestedEnforcerId, account.getEnforcementOverrideEnforcerId())
            || !Objects.equals(requestedLjaId, account.getEnforcementOverrideTfoLjaId());
    }

    private void validate(DefendantAccountEntity account, Check... checks) {
        List<String> failures = List.of(checks).stream()
            .map(check -> check.failureMessage(account))
            .filter(message -> message != null)
            .toList();

        if (!failures.isEmpty()) {
            throw new UnprocessableException("Defendant account update blocked: " + String.join("; ", failures)
                + ".");
        }
    }

    private enum Check {
        ACCOUNT_STATUS {
            @Override
            String failureMessage(DefendantAccountEntity account) {
                DefendantAccountStatus accountStatus = account.getAccountStatus();
                if (accountStatus != null && BLOCKED_ACCOUNT_STATUSES.contains(accountStatus)) {
                    return "Account Status Check failed because account_status is " + accountStatus.getLabel();
                }
                return null;
            }
        },
        PAYMENT_TERMS_LAST_ENFORCEMENT {
            @Override
            String failureMessage(DefendantAccountEntity account) {
                String lastEnforcement = account.getLastEnforcement();
                if (lastEnforcement != null && PAYMENT_TERMS_BLOCKED_LAST_ENFORCEMENTS.contains(lastEnforcement)) {
                    return "Payment terms last enforcement check failed because last_enforcement is "
                        + lastEnforcement;
                }
                return null;
            }
        },
        PAYMENT_CARD_LAST_ENFORCEMENT {
            @Override
            String failureMessage(DefendantAccountEntity account) {
                String lastEnforcement = account.getLastEnforcement();
                if (lastEnforcement != null && PAYMENT_CARD_BLOCKED_LAST_ENFORCEMENTS.contains(lastEnforcement)) {
                    return "Payment card request last enforcement check failed because last_enforcement is "
                        + lastEnforcement;
                }
                return null;
            }
        },
        ZERO_BALANCE {
            @Override
            String failureMessage(DefendantAccountEntity account) {
                BigDecimal accountBalance = account.getAccountBalance();
                if (accountBalance != null && accountBalance.compareTo(BigDecimal.ZERO) <= 0) {
                    return "Zero balance check failed because account_balance is " + accountBalance;
                }
                return null;
            }
        };

        abstract String failureMessage(DefendantAccountEntity account);
    }
}
