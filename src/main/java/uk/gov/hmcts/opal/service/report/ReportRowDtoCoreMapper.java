package uk.gov.hmcts.opal.service.report;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.debtordetail.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReportRowDtoCoreMapper {

    String YES = "Y";
    String NO = "N";
    String EMPTY_STRING = "";

    @Mapping(target = "header1", constant = "DETAIL")
    @Mapping(target = "accountNo", source = "entity.accountNumber")
    @Mapping(target = "imposingCourt", source = "entity.enforcingCourt.name")
    @Mapping(target = "collectionOrder", source = "entity.collectionOrder", qualifiedByName = "booleanToYesNo")
    @Mapping(target = "parentOrGuardian", ignore = true)
    @Mapping(target = "lastEnforcementDate", ignore = true)
    EnforcementReportRowDto map(DefendantAccountEntity entity, ReportMetadataContext context);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "company", expression = "java(party.isOrganisation() ? YES : NO)")
    @Mapping(target = "defendantName", expression = "java(truncate34(buildDefendantName(party)))")
    @Mapping(target = "dateOfBirth", source = "birthDate")
    @Mapping(target = "nationalInsuranceNo", source = "niNumber")
    @Mapping(target = "address1", source = "addressLine1")
    @Mapping(target = "address2", source = "addressLine2")
    @Mapping(target = "address3", source = "addressLine3")
    @Mapping(target = "postcode", source = "postcode")
    @Mapping(target = "mobTel", source = "mobileTelephoneNumber")
    @Mapping(target = "homeTel", source = "homeTelephoneNumber")
    @Mapping(target = "businessTel", source = "workTelephoneNumber")
    @Mapping(target = "email1", source = "primaryEmailAddress")
    @Mapping(target = "email2", source = "secondaryEmailAddress")
    void mapParty(PartyEntity party, @MappingTarget EnforcementReportRowDto dto);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "vehicleReg", source = "vehicleRegistration")
    @Mapping(target = "vehicleMake", source = "vehicleMake")
    @Mapping(target = "employeeRef", source = "employeeReference")
    @Mapping(target = "employerName", source = "employerName")
    @Mapping(target = "employerAddress1", source = "employerAddressLine1")
    @Mapping(target = "employerAddress2", source = "employerAddressLine2")
    @Mapping(target = "employerAddress3", source = "employerAddressLine3")
    @Mapping(target = "employerAddress4", source = "employerAddressLine4")
    @Mapping(target = "employerAddress5", source = "employerAddressLine5")
    @Mapping(target = "employerPostcode", source = "employerPostcode")
    @Mapping(target = "employerTel", source = "employerTelephone")
    @Mapping(target = "employerEmail", source = "employerEmail")
    void mapDebtor(DebtorDetailEntity debtor, @MappingTarget EnforcementReportRowDto dto);

    default String buildDefendantName(PartyEntity party) {
        if (party.isOrganisation()) {
            return party.getOrganisationName();
        }
        String surname = party.getSurname() == null ? EMPTY_STRING : party.getSurname();
        String forenames = party.getForenames() == null ? EMPTY_STRING : party.getForenames();
        return forenames.isBlank() ? surname : surname + ", " + forenames;
    }

    default String truncate34(String value) {
        return value != null && value.length() > 34
            ? value.substring(0, 34)
            : value;
    }

    @Named("booleanToYesNo")
    default String booleanToYesNo(Boolean value) {
        return value == null ? null : (value ? YES : NO);
    }
}