package uk.gov.hmcts.opal.service.report;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.AssociationType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReportRowDtoMapper {

    @Mapping(target = "header1", constant = "DETAIL")
    @Mapping(target = "accountNo", source = "accountNumber")
    @Mapping(target = "imposed", source = "amountImposed")
    @Mapping(target = "paidsf", source = "amountPaid")
    @Mapping(target = "balance", source = "accountBalance")
    @Mapping(target = "dlmove", source = "lastMovementDate")
    @Mapping(target = "lastEnf", source = "lastEnforcement")
    @Mapping(target = "did", source = "jailDays")
    @Mapping(target = "pcr", source = "prosecutorCaseReference")
    EnforcementReportRowDto map(DefendantAccountEntity entity, @Context ReportEnrichmentService enrichment);


    default PartyEntity pickPrimaryParty(DefendantAccountEntity account) {
        if (account == null) {
            return null;
        }
        List<DefendantAccountPartiesEntity> links = account.getParties();
        if (links == null || links.isEmpty()) {
            return null;
        }

        for (DefendantAccountPartiesEntity link : links) {
            if (link == null) {
                continue;
            }
            if (link.getAssociationType().equals(AssociationType.DEFENDANT)) {
                return link.getParty();
            }
        }

        // 2) Prefer debtor == TRUE
        for (DefendantAccountPartiesEntity link : links) {
            if (link == null) {
                continue;
            }
            if (Boolean.TRUE.equals(link.getDebtor())) {
                return link.getParty();
            }
        }

        for (DefendantAccountPartiesEntity link : links) {
            if (link == null) {
                continue;
            }
            PartyEntity p = link.getParty();
            if (p != null) {
                return p;
            }
        }

        return null;
    }

    @AfterMapping
    default void enrich(DefendantAccountEntity entity, @MappingTarget EnforcementReportRowDto dto,
        @Context ReportEnrichmentService enrichment) {

        // Primary party values
        PartyEntity party = pickPrimaryParty(entity);
        if (party != null) {
            // company flag: PartyEntity.organisation is a boolean
            dto.setCompany(party.isOrganisation() ? "Y" : "N");

            // defname: organisation_name OR "surname, forenames" for person, truncated to 34 chars
            String name;
            if (party.isOrganisation()) {
                name = party.getOrganisationName();
            } else {
                String surname = party.getSurname() == null ? "" : party.getSurname();
                String forenames = party.getForenames() == null ? "" : ", " + party.getForenames();
                name = surname + (forenames.trim().isEmpty() ? "" : forenames);
            }
            if (name != null && name.length() > 34) {
                name = name.substring(0, 34);
            }
            dto.setDefname(name);

            dto.setDob(party.getBirthDate());
            dto.setNino(party.getNiNumber());
            dto.setAddress1(party.getAddressLine1());
            dto.setAddress2(party.getAddressLine2());
            dto.setAddress3(party.getAddressLine3());
            dto.setPostcode(party.getPostcode());
            dto.setMobtel(party.getMobileTelephoneNumber());
            dto.setHometel(party.getHomeTelephoneNumber());
            dto.setBustel(party.getWorkTelephoneNumber());
            dto.setEmail1(party.getPrimaryEmailAddress());
            dto.setEmail2(party.getSecondaryEmailAddress());
        }

        // Populate imposingCourt from enforcingCourt (if present)
        if (entity != null && entity.getEnforcingCourt() != null) {
            try {
                // choose name here; if you prefer court_code use getCourtCode() or similar
                dto.setImposingCourt(entity.getEnforcingCourt().getName());
            } catch (Throwable ignored) {
                ignored.getMessage();
            }
        }

        // Debtor/employer fields from enrichment (prefetched)
        Long partyId = (party == null) ? null : party.getPartyId();
        if (partyId != null && enrichment != null) {
            Optional<DebtorDetailEntity> debtorOpt = enrichment.getDebtorForParty(partyId);
            debtorOpt.ifPresent(d -> {
                dto.setVehicleReg(d.getVehicleRegistration());
                dto.setVehicleMake(d.getVehicleMake());
                dto.setEmpRef(d.getEmployeeReference());
                dto.setEmpName(d.getEmployerName());
                dto.setEmpAdd1(d.getEmployerAddressLine1());
                dto.setEmpAdd2(d.getEmployerAddressLine2());
                dto.setEmpAdd3(d.getEmployerAddressLine3());
                dto.setEmpAdd4(d.getEmployerAddressLine4());
                dto.setEmpAdd5(d.getEmployerAddressLine5());
                dto.setEmpPCode(d.getEmployerPostcode());
                dto.setEmpTel(d.getEmployerTelephone());
                dto.setEmpEmail(d.getEmployerEmail());
            });
        }

        // Latest enforcement from enrichment (prefetched)
        Long acctId = (entity == null) ? null : entity.getDefendantAccountId();
        if (acctId != null && enrichment != null) {
            Optional<EnforcementEntity.Lite> latestOpt = enrichment.getLatestEnforcementForAccount(acctId);
            latestOpt.ifPresent(e -> {
                dto.setLeDate(LocalDate.from(e.getPostedDate()));
                dto.setEnfReason(e.getReason());
                dto.setUser(e.getPostedBy());
                dto.setWarrNo(e.getWarrantReference());
                dto.setEnfCrt(e.getHearingCourtId() == null ? null : String.valueOf(e.getHearingCourtId()));
                String resultId = null;
                try {
                    resultId = e.getResultId();
                } catch (Throwable ignored) {
                    ignored.getMessage();
                }
                if (resultId == null && entity != null) {
                    resultId = entity.getLastEnforcement();
                }
                if (resultId != null && "PRIS".equalsIgnoreCase(resultId)) {
                    dto.setEdrDate(LocalDate.from(e.getHearingDate()));
                }
            });
        }

        // collection order: Boolean -> "Y"/"N"/null
        if (entity != null) {
            Boolean co = entity.getCollectionOrder();
            if (co == null) {
                dto.setCo(null);
            } else {
                dto.setCo(Boolean.TRUE.equals(co) ? "Y" : "N");
            }
        }

        // Parent/Guardian flag: check associationType == "Parent/Guardian" on defendant_account_parties
        if (entity != null && entity.getParties() != null && !entity.getParties().isEmpty()) {
            boolean hasPg = false;
            for (DefendantAccountPartiesEntity link : entity.getParties()) {
                if (link == null) {
                    continue;
                }
                if (link.getAssociationType() == null) {
                    continue;
                }
                if (link.getAssociationType().equals(AssociationType.PARENT_GUARDIAN)) {
                    hasPg = true;
                    break;
                }
            }
            dto.setPg(hasPg ? "Y" : "N");
        } else {
            dto.setPg(null);
        }

        // Ensure pcr / did are set if not mapped already (MapStruct maps pcr/did from entity)
        if (dto.getPcr() == null && entity != null) {
            dto.setPcr(entity.getProsecutorCaseReference());
        }
        if (dto.getDid() == null && entity != null) {
            dto.setDid(entity.getJailDays());
        }
    }
}