package uk.gov.hmcts.opal.service.opal;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.gov.hmcts.opal.dto.DraftAccountSummaryDto;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface DraftAccountMapper {

    @Mapping(source = "version", target = "versionNumber")
    @Mapping(source = "createdDate", target = "createdDate", qualifiedByName = "toOffsetDateTime")
    @Mapping(source = "validatedDate", target = "validatedDate", qualifiedByName = "toOffsetDateTime")
    @Mapping(source = "accountStatusDate", target = "accountStatusDate", qualifiedByName = "toLocalDate")
    @Mapping(source = "businessUnit.businessUnitId", target = "businessUnitId")
    @Mapping(source = "statusMessage", target = "statusMessage")
    @Mapping(source = "validatedByName", target = "validatedByName")
    @Mapping(source = "submittedByName", target = "submittedByName")
    DraftAccountSummaryDto toDto(DraftAccountEntity entity);

    @Named("toOffsetDateTime")
    static OffsetDateTime toOffsetDateTime(LocalDateTime localDateTime) {
        return localDateTime == null ? null : localDateTime.atOffset(ZoneOffset.UTC);
    }

    @Named("toLocalDate")
    static LocalDate toLocalDate(LocalDateTime accountStatusDate) {
        return accountStatusDate == null ? null : accountStatusDate.toLocalDate();
    }
}