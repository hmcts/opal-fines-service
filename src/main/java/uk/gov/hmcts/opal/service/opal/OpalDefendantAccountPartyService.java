package uk.gov.hmcts.opal.service.opal;

import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildContactDetails;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildEmployerDetails;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildLanguagePreferences;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildPartyAddressDetails;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildPartyDetails;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildVehicleDetails;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.common.AddressDetails;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;
import uk.gov.hmcts.opal.dto.common.EmployerDetails;
import uk.gov.hmcts.opal.dto.common.IndividualAlias;
import uk.gov.hmcts.opal.dto.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.common.LanguagePreferences;
import uk.gov.hmcts.opal.dto.common.OrganisationAlias;
import uk.gov.hmcts.opal.dto.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.VehicleDetails;
import uk.gov.hmcts.opal.entity.AliasEntity;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.amendment.RecordType;
import uk.gov.hmcts.opal.service.iface.DefendantAccountPartyServiceInterface;
import uk.gov.hmcts.opal.service.persistence.AliasRepositoryService;
import uk.gov.hmcts.opal.service.persistence.AmendmentRepositoryService;
import uk.gov.hmcts.opal.service.persistence.DebtorDetailRepositoryService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.service.persistence.PartyRepositoryService;
import uk.gov.hmcts.opal.util.VersionUtils;

@Service
@Slf4j(topic = "opal.OpalDefendantAccountService")
@RequiredArgsConstructor
public class OpalDefendantAccountPartyService implements DefendantAccountPartyServiceInterface {

    private final DefendantAccountRepositoryService defendantAccountRepositoryService;

    private final AmendmentRepositoryService amendmentRepositoryService;

    private final DebtorDetailRepositoryService debtorDetailRepositoryService;

    private final AliasRepositoryService aliasRepositoryService;

    private final PartyRepositoryService partyRepositoryService;


    @Override
    @Transactional(readOnly = true)
    public GetDefendantAccountPartyResponse getDefendantAccountParty(Long defendantAccountId,
        Long defendantAccountPartyId) {
        log.debug(":getDefendantAccountParty: Opal mode: accountId={}, partyId={}", defendantAccountId,
            defendantAccountPartyId);

        // Find the DefendantAccountEntity by ID
        DefendantAccountEntity account = defendantAccountRepositoryService.findById(defendantAccountId);

        // Find the DefendantAccountPartiesEntity by Party ID
        DefendantAccountPartiesEntity party = account.getParties().stream()
            .filter(p -> p.getDefendantAccountPartyId().equals(defendantAccountPartyId))
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException(
                "Defendant Account Party not found for accountId=" + defendantAccountId
                    + ", partyId=" + defendantAccountPartyId));

        List<AliasEntity> aliasEntity = aliasRepositoryService.findByPartyId(party.getParty().getPartyId());

        // Map entity to PartyDetails DTO
        DefendantAccountParty defendantAccountParty = mapDefendantAccountParty(party, aliasEntity);

        return GetDefendantAccountPartyResponse.builder()
            .defendantAccountParty(defendantAccountParty)
            .version(account.getVersion())
            .build();

    }

    private DefendantAccountParty mapDefendantAccountParty(
        DefendantAccountPartiesEntity partyEntity, List<AliasEntity> aliases) {

        PartyEntity party = partyEntity.getParty();
        Optional<DebtorDetailEntity> debtorDetail = debtorDetailRepositoryService.findByPartyId(party.getPartyId());

        return DefendantAccountParty.builder()
            .defendantAccountPartyType(partyEntity.getAssociationType())
            .isDebtor(partyEntity.getDebtor())
            .partyDetails(buildPartyDetails(party, aliases))
            .address(buildPartyAddressDetails(party))
            .contactDetails(buildContactDetails(party))
            .vehicleDetails(buildVehicleDetails(debtorDetail))
            .employerDetails(buildEmployerDetails(debtorDetail))
            .languagePreferences(buildLanguagePreferences(debtorDetail))
            .build();
    }

    // TODO - Created PO-2452 to fix bumping the version with a more atomically correct method
    private DefendantAccountEntity bumpVersion(Long accountId) {
        DefendantAccountEntity entity = defendantAccountRepositoryService.findById(accountId);
        entity.setVersionNumber(entity.getVersion().add(BigInteger.ONE).longValueExact());
        return defendantAccountRepositoryService.saveAndFlush(entity);
    }

    @Override
    @Transactional
    public GetDefendantAccountPartyResponse replaceDefendantAccountParty(
        Long accountId,
        Long dapId,
        DefendantAccountParty request,
        String ifMatch,
        String businessUnitId,
        String postedBy,
        String businessUserId) {

        DefendantAccountEntity account = defendantAccountRepositoryService.findById(accountId);

        if (account.getBusinessUnit() == null
            || account.getBusinessUnit().getBusinessUnitId() == null
            || !String.valueOf(account.getBusinessUnit().getBusinessUnitId()).equals(businessUnitId)) {
            throw new EntityNotFoundException("Defendant Account not found in business unit " + businessUnitId);
        }

        VersionUtils.verifyIfMatch(account, ifMatch, accountId, "replaceDefendantAccountParty");
        amendmentRepositoryService.auditInitialiseStoredProc(accountId, RecordType.DEFENDANT_ACCOUNTS);

        DefendantAccountPartiesEntity dap = account.getParties().stream()
            .filter(p -> p.getDefendantAccountPartyId().equals(dapId))
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException(
                "Defendant Account Party not found for accountId=" + accountId + ", partyId=" + dapId));

        PartyEntity party = dap.getParty();

        Long requestedPartyId = OpalDefendantAccountBuilders.safeParseLong(
            request != null && request.getPartyDetails() != null ? request.getPartyDetails().getPartyId() : null);

        if (party == null) {
            if (requestedPartyId == null) {
                throw new IllegalArgumentException("party_id is required");
            }
            party = partyRepositoryService.findById(requestedPartyId);   // loads & manages the entity
            dap.setParty(party);
        } else {
            if (requestedPartyId != null && !Objects.equals(party.getPartyId(), requestedPartyId)) {
                throw new IllegalArgumentException("Switching party is not allowed");
            }

            party = partyRepositoryService.findById(party.getPartyId());
            dap.setParty(party);
        }

        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        dap.setAssociationType(request.getDefendantAccountPartyType());
        dap.setDebtor(request.getIsDebtor());

        OpalDefendantAccountBuilders.applyPartyCoreReplace(party, request.getPartyDetails());
        OpalDefendantAccountBuilders.applyPartyAddressReplace(party, request.getAddress());
        OpalDefendantAccountBuilders.applyPartyContactReplace(party, request.getContactDetails());

        boolean isDebtor = Boolean.TRUE.equals(request.getIsDebtor());
        replaceDebtorDetail(
            party.getPartyId(),
            request.getVehicleDetails(),
            request.getEmployerDetails(),
            request.getLanguagePreferences(),
            isDebtor
        );

        replaceAliasesForParty(party.getPartyId(), request.getPartyDetails());

        amendmentRepositoryService.auditFinaliseStoredProc(
            account.getDefendantAccountId(),
            RecordType.DEFENDANT_ACCOUNTS,
            Short.parseShort(businessUnitId),
            postedBy,
            account.getProsecutorCaseReference(),
            "ACCOUNT_ENQUIRY"
        );

        List<AliasEntity> aliasEntity = party.getPartyId() == null
            ? Collections.emptyList()
            : aliasRepositoryService.findByPartyId(party.getPartyId());

        return GetDefendantAccountPartyResponse.builder()
            .defendantAccountParty(mapDefendantAccountParty(dap, aliasEntity))
            .version(bumpVersion(accountId).getVersion())
            .build();
    }

    private void replaceAliasesForParty(Long partyId, PartyDetails pd) {
        if (partyId == null || pd == null || pd.getOrganisationFlag() == null) {
            return;
        }

        PartyEntity party = partyRepositoryService.findById(partyId);

        List<AliasEntity> existing = aliasRepositoryService.findByPartyId(partyId);

        Map<Long, AliasEntity> byId = new HashMap<>();
        for (AliasEntity e : existing) {
            if (e.getAliasId() != null) {
                byId.put(e.getAliasId(), e);
            }
        }

        List<AliasEntity> toPersist = new ArrayList<>();
        Set<Long> keepIds = new HashSet<>();

        if (Boolean.TRUE.equals(pd.getOrganisationFlag())) {
            List<OrganisationAlias> orgAliases = Optional.ofNullable(pd.getOrganisationDetails())
                .map(OrganisationDetails::getOrganisationAliases)
                .orElse(Collections.emptyList());

            for (OrganisationAlias a : orgAliases) {
                if (a == null) {
                    continue;
                }

                String idStr = a.getAliasId();
                Long id = (idStr == null || idStr.trim().isEmpty()) ? null : Long.valueOf(idStr.trim());

                AliasEntity row = upsertAlias(
                    byId, party,
                    id, a.getSequenceNumber(),
                    a.getOrganisationName(),
                    null, null,
                    true
                );
                toPersist.add(row);
                if (row.getAliasId() != null) {
                    keepIds.add(row.getAliasId());
                }
            }

        } else {
            List<IndividualAlias> indAliases = Optional.ofNullable(pd.getIndividualDetails())
                .map(IndividualDetails::getIndividualAliases)
                .orElse(Collections.emptyList());

            for (IndividualAlias a : indAliases) {
                if (a == null) {
                    continue;
                }

                String idStr = a.getAliasId();
                Long id = (idStr == null || idStr.trim().isEmpty()) ? null : Long.valueOf(idStr.trim());

                AliasEntity row = upsertAlias(
                    byId, party,
                    id, a.getSequenceNumber(),
                    null,
                    a.getForenames(), a.getSurname(),
                    false
                );
                toPersist.add(row);
                if (row.getAliasId() != null) {
                    keepIds.add(row.getAliasId());
                }
            }
        }

        if (!toPersist.isEmpty()) {
            List<AliasEntity> persisted = aliasRepositoryService.saveAll(toPersist);
            for (AliasEntity p : persisted) {
                if (p.getAliasId() != null) {
                    keepIds.add(p.getAliasId());
                }
            }
        }

        deletePartyAliasesNotIn(partyId, keepIds);
        aliasRepositoryService.flush();
    }

    /**
     * Upsert a single alias: - if aliasId present, updates the existing row (must belong to this party) - if aliasId
     * null, creates a new row (insert) Also normalizes org/individual fields.
     */
    private AliasEntity upsertAlias(
        Map<Long, AliasEntity> byId,
        PartyEntity party,
        Long aliasId,
        Integer sequenceNumber,
        String orgName,
        String forenames,
        String surname,
        boolean isOrg
    ) {

        AliasEntity row;
        if (aliasId != null) {
            row = byId.get(aliasId);
            if (row == null) {
                throw new EntityNotFoundException(
                    "Alias not found for partyId=" + party.getPartyId() + ", aliasId=" + aliasId);
            }
        } else {
            row = new AliasEntity();
        }

        row.setParty(party);
        row.setSequenceNumber(sequenceNumber);

        if (isOrg) {
            row.setOrganisationName(orgName);
            row.setForenames(null);
            row.setSurname(null);
        } else {
            row.setOrganisationName(null);
            row.setForenames(forenames);
            row.setSurname(surname);
        }
        return row;
    }

    private void deletePartyAliasesNotIn(Long partyId, Set<Long> keepIds) {
        if (keepIds == null || keepIds.isEmpty()) {
            aliasRepositoryService.deleteByPartyId(partyId);
        } else {
            aliasRepositoryService.deleteByPartyIdNotIn(partyId, keepIds);
        }
    }

    private void replaceDebtorDetail(Long partyId,
        VehicleDetails vehicle,
        EmployerDetails employer,
        LanguagePreferences language,
        boolean isDebtor) {

        if (partyId == null) {
            return;
        }

        if (!isDebtor) {
            return;
        }

        DebtorDetailEntity debtor = debtorDetailRepositoryService.findById(partyId)
            .orElseThrow(() -> new EntityNotFoundException("debtor_detail not found with id: " + partyId));

        if (debtor == null) {
            debtor = new DebtorDetailEntity();
            debtor.setPartyId(partyId);
        }

        debtor.setVehicleMake(vehicle != null ? vehicle.getVehicleMakeAndModel() : null);
        debtor.setVehicleRegistration(vehicle != null ? vehicle.getVehicleRegistration() : null);

        if (employer != null) {
            debtor.setEmployerName(employer.getEmployerName());
            debtor.setEmployeeReference(employer.getEmployerReference());
            debtor.setEmployerEmail(employer.getEmployerEmailAddress());
            debtor.setEmployerTelephone(employer.getEmployerTelephoneNumber());

            AddressDetails ea = employer.getEmployerAddress();
            if (ea != null) {
                debtor.setEmployerAddressLine1(ea.getAddressLine1());
                debtor.setEmployerAddressLine2(ea.getAddressLine2());
                debtor.setEmployerAddressLine3(ea.getAddressLine3());
                debtor.setEmployerAddressLine4(ea.getAddressLine4());
                debtor.setEmployerAddressLine5(ea.getAddressLine5());
                debtor.setEmployerPostcode(ea.getPostcode());
            } else {
                debtor.setEmployerAddressLine1(null);
                debtor.setEmployerAddressLine2(null);
                debtor.setEmployerAddressLine3(null);
                debtor.setEmployerAddressLine4(null);
                debtor.setEmployerAddressLine5(null);
                debtor.setEmployerPostcode(null);
            }
        } else {
            debtor.setEmployerName(null);
            debtor.setEmployeeReference(null);
            debtor.setEmployerEmail(null);
            debtor.setEmployerTelephone(null);
            debtor.setEmployerAddressLine1(null);
            debtor.setEmployerAddressLine2(null);
            debtor.setEmployerAddressLine3(null);
            debtor.setEmployerAddressLine4(null);
            debtor.setEmployerAddressLine5(null);
            debtor.setEmployerPostcode(null);
        }

        if (language != null) {
            debtor.setDocumentLanguage(language.getDocumentLanguagePreference() != null
                ? language.getDocumentLanguagePreference().getLanguageCode() : null);
            debtor.setHearingLanguage(language.getHearingLanguagePreference() != null
                ? language.getHearingLanguagePreference().getLanguageCode() : null);
            debtor.setDocumentLanguageDate(LocalDate.now());
            debtor.setHearingLanguageDate(LocalDate.now());
        } else {
            debtor.setDocumentLanguage(null);
            debtor.setHearingLanguage(null);
            debtor.setDocumentLanguageDate(null);
            debtor.setHearingLanguageDate(null);
        }

        debtorDetailRepositoryService.save(debtor);
    }

}

