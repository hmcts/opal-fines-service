package uk.gov.hmcts.opal.service.report.operation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.report.operation.SummaryOperationReportRowDto;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.debtordetail.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.report.ReportMetadataContext;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SummaryRowDtoCoreMapper extends CommonMappingHelper {

    @Mapping(target = "header1", constant = "DETAIL")
    @Mapping(target = "accountNo", source = "entity.accountNumber")
    @Mapping(target = "imposingCourt", source = "entity.enforcingCourt.name")
    @Mapping(target = "collectionOrder", source = "entity.collectionOrder", qualifiedByName = "booleanToYesNo")
    @Mapping(target = "balance", source = "entity.accountBalance")
    @Mapping(target = "parentOrGuardian", ignore = true)
    @Mapping(target = "lastEnforcementDate", ignore = true)
    SummaryOperationReportRowDto map(DefendantAccountEntity entity, ReportMetadataContext context);

    @Mapping(target = "company", source = ".", qualifiedByName = "organisationToYesNo")
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
    void mapParty(PartyEntity party, @MappingTarget SummaryOperationReportRowDto dto);

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
    void mapDebtor(DebtorDetailEntity debtor, @MappingTarget
        SummaryOperationReportRowDto dto);

}