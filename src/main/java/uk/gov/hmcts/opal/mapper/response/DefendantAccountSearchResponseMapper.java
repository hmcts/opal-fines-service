package uk.gov.hmcts.opal.mapper.response;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto;
import uk.gov.hmcts.opal.dto.search.AliasDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.generated.model.DefendantAccountSearchAliasDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountSearchCheckDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountSearchChecksDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountSearchResultDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PostDefendantAccountSearchResponseDefendantAccount;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DefendantAccountSearchResponseMapper {

    default PostDefendantAccountSearchResponseDefendantAccount toResponse(DefendantAccountSearchResultsDto results) {
        List<DefendantAccountSearchResultDefendantAccount> defendantAccounts =
            results == null ? List.of() : toSearchResults(results.getDefendantAccounts());

        return PostDefendantAccountSearchResponseDefendantAccount.builder()
            .count(results == null ? 0 : results.getCount())
            .defendantAccounts(defendantAccounts)
            .build();
    }

    List<DefendantAccountSearchResultDefendantAccount> toSearchResults(List<DefendantAccountSummaryDto> results);

    @Mapping(target = "birthDate", source = "birthDate")
    @Mapping(target = "accountVersion", source = "accountVersion")
    DefendantAccountSearchResultDefendantAccount toSearchResult(DefendantAccountSummaryDto result);

    DefendantAccountSearchAliasDefendantAccount toAlias(AliasDto alias);

    DefendantAccountSearchChecksDefendantAccount toChecks(DefendantAccountSummaryDto.Checks checks);

    DefendantAccountSearchCheckDefendantAccount toCheck(DefendantAccountSummaryDto.WarnError warnError);

    default LocalDate toLocalDate(String value) {
        return value == null ? null : LocalDate.parse(value);
    }

    default Long toLong(BigInteger value) {
        return value == null ? null : value.longValue();
    }
}
