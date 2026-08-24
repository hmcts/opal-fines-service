package uk.gov.hmcts.opal.service.opal;

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
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.generated.model.PartyResponseDefendantAccount;
import uk.gov.hmcts.opal.generated.model.AddPartyRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.IndividualAliasCommonStrict;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.LanguagePreferencesCommonStrict;
import uk.gov.hmcts.opal.generated.model.LanguagePreferenceCommonStrict;
import uk.gov.hmcts.opal.generated.model.OrganisationAliasCommon;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.PartyDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.PartyEmployerDetailsDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PartyVehicleDetailsDefendantAccount;
import uk.gov.hmcts.opal.mapper.response.DefendantAccountPartyEntityResponseMapper;
import uk.gov.hmcts.opal.dto.request.RemoveDefendantAccountPartyRequest;
import uk.gov.hmcts.opal.dto.response.RemoveDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.entity.AliasEntity;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.debtordetail.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.debtordetail.Language;
import uk.gov.hmcts.opal.entity.defendantaccount.AssociationType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountPartiesRepository;
import uk.gov.hmcts.opal.service.iface.DefendantAccountPartyServiceInterface;
import uk.gov.hmcts.opal.service.persistence.AliasRepositoryService;
import uk.gov.hmcts.opal.service.persistence.AmendmentRepositoryService;
import uk.gov.hmcts.opal.service.persistence.DebtorDetailRepositoryService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.service.persistence.PartyRepositoryService;
import uk.gov.hmcts.opal.util.VersionUtils;

@Service
@Slf4j(topic = "opal.OpalDefendantAccountPartyService")
@RequiredArgsConstructor
public class OpalDefendantAccountPartyService implements DefendantAccountPartyServiceInterface {

    public static final String FUNCTION_CODE_ACCOUNT_ENQUIRY = "ACCOUNT_ENQUIRY";
    private static final String DEFENDANT_ACCOUNT_PARTY_NOT_FOUND = "Defendant Account Party not found for accountId=";
    private static final String PARTY_ID = ", partyId=";

    private final DefendantAccountRepositoryService defendantAccountRepositoryService;

    private final AmendmentRepositoryService amendmentRepositoryService;

    private final DebtorDetailRepositoryService debtorDetailRepositoryService;

    private final AliasRepositoryService aliasRepositoryService;

    private final PartyRepositoryService partyRepositoryService;

    private final DefendantAccountPartiesRepository defendantAccountPartiesRepository;

    private final DefendantAccountControlValidator defendantAccountControlValidator;

    private final DefendantAccountPartyEntityResponseMapper defendantAccountPartyEntityResponseMapper;

    @Override
    @Transactional(readOnly = true)
    public PartyResponseDefendantAccount getDefendantAccountParty(Long defendantAccountId,
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
                DEFENDANT_ACCOUNT_PARTY_NOT_FOUND + defendantAccountId + PARTY_ID + defendantAccountPartyId));

        List<AliasEntity> aliasEntity = aliasRepositoryService.findByPartyId(party.getParty().getPartyId());

        return PartyResponseDefendantAccount.builder()
            .defendantAccountParty(mapDefendantAccountParty(party, aliasEntity))
            .version(account.getVersion())
            .build();

    }

    @Override
    @Transactional
    public PartyResponseDefendantAccount addDefendantAccountParty(
        Long accountId, String businessUnitId,
        String businessUserId, String postedBy, String postedByName, String ifMatch,
        AddPartyRequestDefendantAccount request) {

        if (request == null || request.getDefendantAccountParty() == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        PartyDefendantAccount requestParty = request.getDefendantAccountParty();
        PartyDetailsCommonStrict partyDetails = requestParty.getPartyDetails();

        if (partyDetails == null || partyDetails.getOrganisationFlag() == null) {
            throw new IllegalArgumentException("party_details.organisation_flag is required");
        }

        DefendantAccountEntity account = defendantAccountRepositoryService.findById(accountId);

        log.debug(":addDefendantAccountParty: Opal mode: accountId={}, buId={}, postedBy={}, businessUserId={}",
            accountId, businessUnitId, postedBy, businessUserId);

        validateAccountExistsInBusinessUnit(account, businessUnitId);

        VersionUtils.verifyIfMatch(account, ifMatch, accountId, "addDefendantAccountParty");
        defendantAccountControlValidator.validateCanMutateParty(account);
        amendmentRepositoryService.auditInitialiseStoredProc(accountId, AssociatedRecordType.DEFENDANT_ACCOUNTS);

        // Save the party record
        PartyEntity party = new PartyEntity();
        OpalDefendantAccountBuilders.applyPartyCoreReplace(party, partyDetails);
        OpalDefendantAccountBuilders.applyPartyAddressReplace(party, requestParty.getAddress());
        OpalDefendantAccountBuilders.applyPartyContactReplace(party, value(requestParty.getContactDetails()));
        party = partyRepositoryService.save(party);

        // Save the association
        DefendantAccountPartiesEntity defendantAccountParty = DefendantAccountPartiesEntity.builder()
            .party(party)
            .associationType(AssociationType.getByLabel(requestParty.getDefendantAccountPartyType().getValue()))
            .debtor(requestParty.getIsDebtor())
            .build();

        account.addPartyAssociation(defendantAccountParty);

        // Add Debtor Details if this party is a debtor
        if (Boolean.TRUE.equals(requestParty.getIsDebtor())) {
            // Check if a DebtorDetail record exists for this party
            Optional<DebtorDetailEntity> existingDebtor =
                debtorDetailRepositoryService.findById(party.getPartyId());
            if (existingDebtor.isPresent()) {
                debtorDetailRepositoryService.updateDebtorDetail(
                    existingDebtor.get(),
                    value(requestParty.getVehicleDetails()),
                    value(requestParty.getEmployerDetails()),
                    value(requestParty.getLanguagePreferences())
                );
            } else {
                debtorDetailRepositoryService.addDebtorDetail(
                    party.getPartyId(),
                    value(requestParty.getVehicleDetails()),
                    value(requestParty.getEmployerDetails()),
                    value(requestParty.getLanguagePreferences())
                );
            }
        }

        replaceAliasesForParty(party.getPartyId(), partyDetails);

        amendmentRepositoryService.auditFinaliseStoredProc(
            account.getDefendantAccountId(),
            AssociatedRecordType.DEFENDANT_ACCOUNTS,
            Short.parseShort(businessUnitId),
            postedBy,
            postedByName,
            account.getProsecutorCaseReference(),
            FUNCTION_CODE_ACCOUNT_ENQUIRY
        );

        List<AliasEntity> aliasEntity = aliasRepositoryService.findByPartyId(party.getPartyId());

        // Flush the managed entity to the DB to ensure the updated version is returned.
        BigInteger newVersion = defendantAccountRepositoryService.saveAndFlush(account).getVersion();

        return PartyResponseDefendantAccount.builder()
            .defendantAccountParty(mapDefendantAccountParty(defendantAccountParty, aliasEntity))
            .version(newVersion)
            .build();
    }

    private PartyDefendantAccount mapDefendantAccountParty(
        DefendantAccountPartiesEntity partyEntity, List<AliasEntity> aliases) {

        PartyEntity party = partyEntity.getParty();
        DebtorDetailEntity debtorDetail = debtorDetailRepositoryService.findByPartyId(party.getPartyId()).orElse(null);

        return defendantAccountPartyEntityResponseMapper.toGeneratedResponse(partyEntity, debtorDetail, aliases);
    }

    private BigInteger bumpVersion(DefendantAccountEntity account) {
        return defendantAccountRepositoryService.incrementVersionNumber(
            account.getDefendantAccountId(),
            account.getVersion()
        );
    }

    @Override
    @Transactional
    public PartyResponseDefendantAccount replaceDefendantAccountParty(
        Long accountId, Long dapId, PartyDefendantAccount request, String ifMatch, String businessUnitId,
        String postedBy, String postedByName, String businessUserId) {

        DefendantAccountEntity account = defendantAccountRepositoryService.findById(accountId);

        log.debug(":replaceDefendantAccountParty: Opal mode: accountId={}, dapId={}, buId={}, postedBy={}, "
                + "businessUserId={}", accountId, dapId, businessUnitId, postedBy, businessUserId);

        validateAccountExistsInBusinessUnit(account, businessUnitId);

        VersionUtils.verifyIfMatch(account, ifMatch, accountId, "replaceDefendantAccountParty");

        DefendantAccountPartiesEntity dap = account.getParties().stream()
            .filter(p -> p.getDefendantAccountPartyId().equals(dapId))
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException(
                DEFENDANT_ACCOUNT_PARTY_NOT_FOUND + accountId + PARTY_ID + dapId));

        if (isParentGuardianReplacement(dap)) {
            defendantAccountControlValidator.validateCanMutateParty(account);
        }

        amendmentRepositoryService.auditInitialiseStoredProc(accountId, AssociatedRecordType.DEFENDANT_ACCOUNTS);

        PartyEntity party = dap.getParty();

        log.debug("replaceDefendantAccountParty: existing partyId: {}", party != null ? party.getPartyId() : null);

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

        Objects.requireNonNull(party, "Party must be present before updating defendant account party");

        log.debug("replaceDefendantAccountParty: changed(?) partyId: {}", party.getPartyId());

        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        PartyDetailsCommonStrict partyDetails = request.getPartyDetails();
        if (isConvertingFromIndividualToOrganisation(party, partyDetails)) {
            removeParentGuardianParties(account, dapId);
        }

        Optional.ofNullable(request.getDefendantAccountPartyType())
            .map(PartyDefendantAccount.DefendantAccountPartyTypeEnum::getValue)
            .map(AssociationType::getByLabel)
            .ifPresent(dap::setAssociationType);
        dap.setDebtor(request.getIsDebtor());

        PartyDetailsCommonStrict requestPartyDetails = request.getPartyDetails();
        String requestOrganisationName = Optional.ofNullable(requestPartyDetails)
            .map(PartyDetailsCommonStrict::getOrganisationDetails)
            .map(OpalDefendantAccountPartyService::value)
            .map(OrganisationDetailsCommonStrict::getOrganisationName)
            .orElse("");
        String requestSurname = Optional.ofNullable(requestPartyDetails)
            .map(PartyDetailsCommonStrict::getIndividualDetails)
            .map(OpalDefendantAccountPartyService::value)
            .map(IndividualDetailsCommonStrict::getSurname)
            .orElse("");
        log.debug("replaceDefendantAccountParty:     request org: {}, surname: {}",
            requestOrganisationName, requestSurname);
        log.debug("replaceDefendantAccountParty:  pre-update org: {}, surname: {}",
            party.getOrganisationName(),
            party.getSurname());

        OpalDefendantAccountBuilders.applyPartyCoreReplace(party, partyDetails);
        OpalDefendantAccountBuilders.applyPartyAddressReplace(party, request.getAddress());
        OpalDefendantAccountBuilders.applyPartyContactReplace(party, value(request.getContactDetails()));

        log.debug("replaceDefendantAccountParty: post-update org: {}, surname: {}", party.getOrganisationName(),
            party.getSurname());

        boolean isDebtor = Boolean.TRUE.equals(request.getIsDebtor());
        replaceDebtorDetail(party.getPartyId(), value(request.getVehicleDetails()), value(request.getEmployerDetails()),
            value(request.getLanguagePreferences()), isDebtor
        );

        replaceAliasesForParty(party.getPartyId(), partyDetails);

        amendmentRepositoryService.auditFinaliseStoredProc(
            account.getDefendantAccountId(),
            AssociatedRecordType.DEFENDANT_ACCOUNTS,
            Short.parseShort(businessUnitId),
            postedBy,
            postedByName,
            account.getProsecutorCaseReference(),
            FUNCTION_CODE_ACCOUNT_ENQUIRY
        );

        List<AliasEntity> aliasEntity = party.getPartyId() == null
            ? Collections.emptyList()
            : aliasRepositoryService.findByPartyId(party.getPartyId());

        return PartyResponseDefendantAccount.builder()
            .defendantAccountParty(mapDefendantAccountParty(dap, aliasEntity))
            .version(bumpVersion(account))
            .build();
    }

    @Override
    @Transactional
    public RemoveDefendantAccountPartyResponse removeDefendantAccountParty(Long defendantAccountId,
        Long defendantAccountPartyId, Short businessUnitId, String businessUserId, String postedBy,
        String postedByName, String ifMatch, RemoveDefendantAccountPartyRequest request) {

        DefendantAccountEntity account = defendantAccountRepositoryService.findById(defendantAccountId);

        log.debug(":removeDefendantAccountParty: accountId={}, dapId={}, buId={}, postedBy={}",
            defendantAccountId, defendantAccountPartyId, businessUnitId, postedBy);

        validateAccountExistsInBusinessUnit(account, String.valueOf(businessUnitId));

        VersionUtils.verifyIfMatch(account, ifMatch, defendantAccountId, "removeDefendantAccountParty");
        defendantAccountControlValidator.validateCanMutateParty(account);
        amendmentRepositoryService
            .auditInitialiseStoredProc(defendantAccountId, AssociatedRecordType.DEFENDANT_ACCOUNTS);

        // Verify the DAP association is valid for this Defendant Account
        account.getParties().stream()
            .filter(p -> p.getDefendantAccountPartyId().equals(defendantAccountPartyId))
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException(
                DEFENDANT_ACCOUNT_PARTY_NOT_FOUND + defendantAccountId + PARTY_ID + defendantAccountPartyId));

        account.getParties().removeIf(p -> p.getDefendantAccountPartyId().equals(defendantAccountPartyId));

        amendmentRepositoryService.auditFinaliseStoredProc(
            account.getDefendantAccountId(),
            AssociatedRecordType.DEFENDANT_ACCOUNTS,
            businessUnitId,
            postedBy,
            postedByName,
            account.getProsecutorCaseReference(),
            FUNCTION_CODE_ACCOUNT_ENQUIRY
        );

        // Flush the managed entity to the DB to ensure the updated version is returned.
        BigInteger newVersion = defendantAccountRepositoryService.saveAndFlush(account).getVersion();

        return RemoveDefendantAccountPartyResponse.builder()
            .defendantAccountPartyId(String.valueOf(defendantAccountPartyId))
            .version(newVersion)
            .build();
    }

    private boolean isConvertingFromIndividualToOrganisation(PartyEntity party,
                                                              PartyDetailsCommonStrict partyDetails) {
        return !party.isOrganisation()
            && Boolean.TRUE.equals(Optional.ofNullable(partyDetails)
                .map(PartyDetailsCommonStrict::getOrganisationFlag)
                .orElse(null));
    }

    private boolean isParentGuardianReplacement(DefendantAccountPartiesEntity dap) {
        return AssociationType.PARENT_GUARDIAN.equals(dap.getAssociationType());
    }

    private void removeParentGuardianParties(DefendantAccountEntity account, Long dapId) {
        int deletedRows = defendantAccountPartiesRepository.deleteByAccountIdAndAssociationTypeExcludingDapId(
            account.getDefendantAccountId(),
            AssociationType.PARENT_GUARDIAN,
            dapId
        );
        if (deletedRows > 0) {
            log.info("replaceDefendantAccountParty: removed {} parent/guardian parties for accountId={}",
                deletedRows, account.getDefendantAccountId());
        }
    }

    private void replaceAliasesForParty(Long partyId, PartyDetailsCommonStrict pd) {
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
            List<OrganisationAliasCommon> orgAliases = Optional.ofNullable(value(pd.getOrganisationDetails()))
                .map(OrganisationDetailsCommonStrict::getOrganisationAliases)
                .map(OpalDefendantAccountPartyService::value)
                .orElse(Collections.emptyList());

            for (OrganisationAliasCommon a : orgAliases) {
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
            List<IndividualAliasCommonStrict> indAliases = Optional.ofNullable(value(pd.getIndividualDetails()))
                .map(IndividualDetailsCommonStrict::getIndividualAliases)
                .map(OpalDefendantAccountPartyService::value)
                .orElse(Collections.emptyList());

            for (IndividualAliasCommonStrict a : indAliases) {
                if (a == null) {
                    continue;
                }

                String idStr = a.getAliasId();
                Long id = (idStr == null || idStr.trim().isEmpty()) ? null : Long.valueOf(idStr.trim());

                AliasEntity row = upsertAlias(
                    byId, party,
                    id, a.getSequenceNumber(),
                    null,
                    value(a.getForenames()), a.getSurname(),
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

    private void replaceDebtorDetail(Long partyId, PartyVehicleDetailsDefendantAccount vehicle,
        PartyEmployerDetailsDefendantAccount employer, LanguagePreferencesCommonStrict language, boolean isDebtor) {

        log.debug("replaceDebtorDetail: partyId: {}, isDebtor: {}", partyId, isDebtor);

        if (partyId == null) {
            return;
        }

        if (!isDebtor) {
            return;
        }

        DebtorDetailEntity debtor = debtorDetailRepositoryService.findById(partyId)
            .orElseThrow(() -> new EntityNotFoundException("debtor_detail not found with id: " + partyId));


        log.debug("replaceDebtorDetail:  pre-change debtor: {}", debtor);

        debtor.setVehicleMake(vehicle != null ? value(vehicle.getVehicleMakeAndModel()) : null);
        debtor.setVehicleRegistration(vehicle != null ? value(vehicle.getVehicleRegistration()) : null);

        if (employer != null) {
            debtor.setEmployerName(value(employer.getEmployerName()));
            debtor.setEmployeeReference(value(employer.getEmployerReference()));
            debtor.setEmployerEmail(value(employer.getEmployerEmailAddress()));
            debtor.setEmployerTelephone(value(employer.getEmployerTelephoneNumber()));

            AddressDetailsCommonStrict ea = employer.getEmployerAddress();
            if (ea != null) {
                debtor.setEmployerAddressLine1(ea.getAddressLine1());
                debtor.setEmployerAddressLine2(value(ea.getAddressLine2()));
                debtor.setEmployerAddressLine3(value(ea.getAddressLine3()));
                debtor.setEmployerAddressLine4(value(ea.getAddressLine4()));
                debtor.setEmployerAddressLine5(value(ea.getAddressLine5()));
                debtor.setEmployerPostcode(value(ea.getPostcode()));
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
            debtor.setDocumentLanguage(toLanguage(value(language.getDocumentLanguagePreference())));
            debtor.setHearingLanguage(toLanguage(value(language.getHearingLanguagePreference())));
            debtor.setDocumentLanguageDate(LocalDate.now());
            debtor.setHearingLanguageDate(LocalDate.now());
        } else {
            debtor.setDocumentLanguage(null);
            debtor.setHearingLanguage(null);
            debtor.setDocumentLanguageDate(null);
            debtor.setHearingLanguageDate(null);
        }

        log.debug("replaceDebtorDetail: post-change debtor: {}", debtor);

        debtorDetailRepositoryService.save(debtor);
    }

    private static Language toLanguage(LanguagePreferenceCommonStrict preference) {
        return preference == null ? null : Language.fromCode(preference.getLanguageCode().getValue());
    }

    private static <T> T value(JsonNullable<T> nullable) {
        return nullable == null ? null : nullable.orElse(null);
    }

    private void validateAccountExistsInBusinessUnit(DefendantAccountEntity account, String businessUnitId) {
        if (!account.isInBusinessUnit(businessUnitId)) {
            throw new EntityNotFoundException("Defendant Account not found in business unit " + businessUnitId);
        }
    }

}
