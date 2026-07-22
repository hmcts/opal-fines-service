package uk.gov.hmcts.opal.mapper.legacy;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.dto.GetDefendantAccountConsolidatedAccountsResult;
import uk.gov.hmcts.opal.dto.legacy.LegacyConsolidatedAccount;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountConsolidatedAccountsResponse;
import uk.gov.hmcts.opal.generated.model.ConsolidatedAccountDefendantAccount;

@Mapper(componentModel = "spring")
public interface LegacyConsolidatedAccountMapper {

    @Mapping(target = "version", expression = "java(toVersion(legacy.getVersion()))")
    @Mapping(target = "payload", expression = "java(toSortedPayload(legacy.getConsolidatedAccounts()))")
    GetDefendantAccountConsolidatedAccountsResult toResponse(
        LegacyGetDefendantAccountConsolidatedAccountsResponse legacy);

    @BeanMapping(builder = @org.mapstruct.Builder(disableBuilder = true))
    ConsolidatedAccountDefendantAccount toResponse(LegacyConsolidatedAccount legacy);

    default List<ConsolidatedAccountDefendantAccount> toSortedPayload(List<LegacyConsolidatedAccount> accounts) {
        return Optional.ofNullable(accounts)
            .orElse(Collections.emptyList())
            .stream()
            .filter(account -> account != null)
            .map(this::toResponse)
            .sorted(Comparator.comparing(ConsolidatedAccountDefendantAccount::getAccountId,
                                         Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();
    }

    default BigInteger toVersion(Long version) {
        return version == null ? null : BigInteger.valueOf(version);
    }
}
