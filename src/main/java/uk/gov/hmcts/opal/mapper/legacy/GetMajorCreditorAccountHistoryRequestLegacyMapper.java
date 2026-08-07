package uk.gov.hmcts.opal.mapper.legacy;

import java.time.LocalDate;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHistoryLegacyRequest;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GetMajorCreditorAccountHistoryRequestLegacyMapper {

    List<String> MAJOR_CREDITOR_HISTORY_ITEM_TYPES = List.of("Financial");

    default GetMajorCreditorAccountHistoryLegacyRequest toLegacyRequest(
        Long majorCreditorAccountId,
        LocalDate dateFrom,
        LocalDate dateTo
    ) {
        return GetMajorCreditorAccountHistoryLegacyRequest.builder()
            .creditorAccountId(String.valueOf(majorCreditorAccountId))
            .fromDate(dateFrom)
            .toDate(dateTo)
            .itemTypes(MAJOR_CREDITOR_HISTORY_ITEM_TYPES)
            .build();
    }
}
