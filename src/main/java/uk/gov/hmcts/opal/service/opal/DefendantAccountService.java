package uk.gov.hmcts.opal.service.opal;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.projection.DefendantAccountSummary;
import uk.gov.hmcts.opal.entity.projection.DefendantAccountSummary.PartyDefendantAccountSummary;
import uk.gov.hmcts.opal.entity.projection.DefendantAccountSummary.PartyLink;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.repository.CourtRepository;
import uk.gov.hmcts.opal.repository.DebtorDetailRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountPartiesRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.EnforcerRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.PaymentTermsRepository;
import uk.gov.hmcts.opal.repository.jpa.DefendantAccountSpecs;
import uk.gov.hmcts.opal.service.DefendantAccountServiceInterface;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.opal.dto.ToJsonString.getObjectMapper;

@Service
@Slf4j(topic = "opal.DefendantAccountService")
@RequiredArgsConstructor
@Qualifier("defendantAccountService")
public class DefendantAccountService implements DefendantAccountServiceInterface {

    private final DefendantAccountRepository defendantAccountRepository;

    private final DefendantAccountPartiesRepository defendantAccountPartiesRepository;

    private final PaymentTermsRepository paymentTermsRepository;

    private final DebtorDetailRepository debtorDetailRepository;

    private final EnforcerRepository enforcerRepository;

    private final NoteRepository noteRepository;

    private final BusinessUnitRepository businessRepository;

    private final CourtRepository courtRepository;

    private final DefendantAccountSpecs specs = new DefendantAccountSpecs();

    @Transactional(readOnly = true)
    public DefendantAccountEntity getDraftAccountById(long defAccountId) {
        return defendantAccountRepository.findById(defAccountId)
            .orElseThrow(() -> new EntityNotFoundException("Defendant Account not found with id: " + defAccountId));
    }

    @Override
    @Transactional
    public DefendantAccountEntity getDefendantAccount(AccountEnquiryDto request) {
        log.debug(":getDefendantAccount: request: {}", request);

        return defendantAccountRepository.findByBusinessUnit_BusinessUnitIdAndAccountNumber(
            request.getBusinessUnitId(), request.getAccountNumber());
    }

    @Override
    @Transactional
    public DefendantAccountEntity putDefendantAccount(DefendantAccountEntity defendantAccountEntity) {
        log.debug(":putDefendantAccount: account: {}", defendantAccountEntity);

        DefendantAccountEntity existing = getDraftAccountById(defendantAccountEntity.getDefendantAccountId());
        replaceBusinessUnit(defendantAccountEntity, existing);
        replaceCourts(defendantAccountEntity, existing);
        return defendantAccountRepository.save(existing);
    }

    private void replaceBusinessUnit(DefendantAccountEntity entity, DefendantAccountEntity existing) {
        Optional.ofNullable(entity.getBusinessUnit())
            .ifPresent(bu -> existing.setBusinessUnit(businessRepository.getReferenceById(bu.getBusinessUnitId())));
    }

    private void replaceCourts(DefendantAccountEntity entity, DefendantAccountEntity existing) {
        Optional.ofNullable(entity.getEnforcingCourt())
            .ifPresent(court -> existing.setEnforcingCourt(courtRepository.getReferenceById(court.getCourtId())));
        Optional.ofNullable(entity.getLastHearingCourt())
            .ifPresent(court -> existing.setLastHearingCourt(courtRepository.getReferenceById(court.getCourtId())));
    }

    @Override
    @Transactional
    public List<DefendantAccountEntity> getDefendantAccountsByBusinessUnit(Short businessUnitId) {

        log.debug(":getDefendantAccountsByBusinessUnit: busUnit: {}", businessUnitId);
        return defendantAccountRepository.findAllByBusinessUnit_BusinessUnitId(businessUnitId);
    }

    @Override
    @Transactional
    public DefendantAccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto) {
        log.debug(":searchDefendantAccounts: criteria: {}", accountSearchDto.toJson());

        // TODO - 25/06/2024 - remove this Disco+ 'test' code soon?
        if ("test".equalsIgnoreCase(accountSearchDto.getCourt())) {

            try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("tempSearchData.json")) {
                ObjectMapper mapper = getObjectMapper();
                DefendantAccountSearchResultsDto dto = mapper.readValue(in, DefendantAccountSearchResultsDto.class);
                log.debug(":searchDefendantAccounts: temporary Hack for Front End testing. Read JSON file: \n{}",
                         dto.toPrettyJsonString());
                return dto;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        Page<DefendantAccountSummary> summariesPage = defendantAccountRepository
            .findBy(specs.findByAccountSearch(accountSearchDto),
                    ffq -> ffq.as(DefendantAccountSummary.class).page(Pageable.unpaged()));

        List<DefendantAccountSummaryDto> dtos = summariesPage.getContent().stream()
            .map(this::toDto)
            .collect(Collectors.toList());

        return DefendantAccountSearchResultsDto.builder()
            .defendantAccounts(dtos)
            .build();
    }

    @Transactional
    public AccountDetailsDto getAccountDetailsByDefendantAccountId(Long defendantAccountId) {

        // TODO - 25/06/2024 - remove this Disco+ 'test' code soon?
        if (defendantAccountId.equals(0L)) {


            try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("tempDetailsData.json")) {

                ObjectMapper mapper = getObjectMapper();
                AccountDetailsDto dto = mapper.readValue(in, AccountDetailsDto.class);
                log.debug(
                        """
                        :getAccountDetailsByDefendantAccountId:
                        " temporary Hack for Front End testing. Read JSON file: \n{}
                        """,
                         dto.toPrettyJsonString());
                return dto;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


        //query db for defendantAccountPartiesEntity
        DefendantAccountPartiesEntity defendantAccountPartiesEntity = defendantAccountPartiesRepository
            .findByDefendantAccount_DefendantAccountId(defendantAccountId);

        if (defendantAccountPartiesEntity == null) {
            throw new EntityNotFoundException("Defendant Account & Party not found with id: " + defendantAccountId);
        }

        //Extract unique defendantAccount and party entities
        final DefendantAccountEntity defendantAccountEntity = defendantAccountPartiesEntity.getDefendantAccount();
        final PartyEntity partyEntity = defendantAccountPartiesEntity.getParty();


        //query DB for PaymentTermsEntity
        PaymentTermsEntity paymentTermsEntity = paymentTermsRepository.findByDefendantAccount_DefendantAccountId(
            defendantAccountEntity.getDefendantAccountId());

        //query DB for EnforcementEntity
        EnforcerEntity enforcerEntity = enforcerRepository.findByEnforcerId(defendantAccountEntity
                                                                                   .getEnforcementOverrideEnforcerId());

        //query DB for NoteEntity by associatedRecordId (defendantAccountId) and noteType ("AC")
        List<NoteEntity> noteEntityAC = noteRepository.findByAssociatedRecordIdAndNoteType(
            defendantAccountEntity.getDefendantAccountId().toString(), "AC");

        //query DB for NoteEntity by associatedRecordId (defendantAccountId) and noteType ("AA")
        // returning only latest (postedDate)
        Optional<NoteEntity> noteEntityAA = Optional.ofNullable(
            noteRepository.findTopByAssociatedRecordIdAndNoteTypeOrderByPostedDateDesc(
            defendantAccountEntity.getDefendantAccountId().toString(), "AA"));

        //build fullAddress
        final String fullAddress = buildFullAddress(partyEntity.getAddressLine1(),
                                                    partyEntity.getAddressLine2(),
                                                    partyEntity.getAddressLine3(),
                                                    partyEntity.getAddressLine4(),
                                                    partyEntity.getAddressLine5());

        //build paymentDetails
        final String paymentDetails = buildPaymentDetails(paymentTermsEntity.getTermsTypeCode(),
                                                          paymentTermsEntity.getInstalmentAmount(),
                                                          paymentTermsEntity.getInstalmentPeriod(),
                                                          paymentTermsEntity.getEffectiveDate());

        //build comments
        final List<String> comments = buildCommentsFromAssociatedNotes(noteEntityAC);


        //populate accountDetailsDto and return
        return AccountDetailsDto.builder()
            .defendantAccountId(defendantAccountEntity.getDefendantAccountId())
            .accountNumber(defendantAccountEntity.getAccountNumber())
            .fullName(partyEntity.getOrganisationName() == null
                          ? partyEntity.getFullName()
                          : partyEntity.getOrganisationName())
            .accountCT(defendantAccountEntity.getBusinessUnit().getBusinessUnitName())
            .businessUnitId(defendantAccountEntity.getBusinessUnit().getBusinessUnitId())
            .address(fullAddress)
            .postCode(partyEntity.getPostcode())
            .dob(partyEntity.getDateOfBirth())
            .detailsChanged(defendantAccountEntity.getLastChangedDate())
            .lastCourtAppAndCourtCode(defendantAccountEntity.getLastHearingDate().toString()
                                          + " " + defendantAccountEntity.getLastHearingCourt().getCourtCode())
            .lastMovement(defendantAccountEntity.getLastMovementDate())
            .commentField(comments)
            .accountNotes(noteEntityAA.map(NoteEntity::getNoteText).orElse(null))
            .pcr(defendantAccountEntity.getProsecutorCaseReference())
            .paymentDetails(paymentDetails)
            .lumpSum(paymentTermsEntity.getInstalmentLumpSum())
            .commencing(paymentTermsEntity.getTermsTypeCode().equals("I")
                            ? paymentTermsEntity.getEffectiveDate()
                            : null)
            .daysInDefault(paymentTermsEntity.getJailDays())
            .sentencedDate(defendantAccountEntity.getImposedHearingDate())
            .lastEnforcement(defendantAccountEntity.getLastEnforcement())
            .override(defendantAccountEntity.getEnforcementOverrideResultId())
            .enforcer(enforcerEntity.getEnforcerCode())
            .enforcementCourt(defendantAccountEntity.getEnforcingCourt().getCourtCode())
            .imposed(defendantAccountEntity.getAmountImposed())
            .amountPaid(defendantAccountEntity.getAmountPaid())
            .balance(defendantAccountEntity.getAccountBalance())
            .build();
    }

    public static String buildFullAddress(String addressLine1,
                                          String addressLine2,
                                          String addressLine3,
                                          String addressLine4,
                                          String addressLine5) {

        List<String> addressLines = new ArrayList<>();

        addressLines.add(addressLine1);
        addressLines.add(addressLine2);
        addressLines.add(addressLine3);
        addressLines.add(addressLine4);
        addressLines.add(addressLine5);

        addressLines.removeIf(Objects::isNull);

        return String.join(", ", addressLines);
    }

    public static String buildPaymentDetails(String termsTypeCode,
                                             BigDecimal installmentAmount,
                                             String instalmentPeriod,
                                             LocalDate effectiveDate) {

        return switch (termsTypeCode) {
            case "I" -> buildInstalmentDetails(installmentAmount, instalmentPeriod);
            case "B" -> buildByDateDetails(effectiveDate);
            default -> "Paid";
        };
    }

    public static String buildInstalmentDetails(BigDecimal installmentAmount, String installmentPeriod) {
        return installmentAmount + " / " + installmentPeriod;
    }

    public static String buildByDateDetails(LocalDate effectiveDate) {
        return effectiveDate + " By Date";
    }

    public static List<String> buildCommentsFromAssociatedNotes(List<NoteEntity> notes) {

        List<String> comments = new ArrayList<>();

        for (NoteEntity note : notes) {

            comments.add(note.getNoteText());
        }

        return comments;
    }

    public DefendantAccountSummaryDto toDto(DefendantAccountSummary summary) {
        Optional<PartyDefendantAccountSummary> party = summary.getParties().stream().findAny().map(PartyLink::getParty);
        return DefendantAccountSummaryDto.builder()
            .defendantAccountId(String.valueOf(summary.getDefendantAccountId()))
            .accountNumber(summary.getAccountNumber())
            .accountBalance(summary.getAccountBalance() != null ? summary.getAccountBalance().doubleValue() : null)
            .defendantTitle("Ms")
            .defendantFirstnames("Anna")
            .defendantSurname("Graham")
            .organisation(false)
            .organisationName(null)
            .aliases(null)
            .postcode(null)
            .businessUnitName("CT")
            .businessUnitId("78")
            .prosecutorCaseReference(null)
            .lastEnforcementAction(null)
            .nationalInsuranceNumber(null)
            .parentGuardianSurname(null)
            .parentGuardianFirstnames(null)
            .addressLine1(party.map(PartyDefendantAccountSummary::getAddressLine1).orElse(""))
            .birthDate(party.map(PartyDefendantAccountSummary::getDateOfBirth).map(LocalDate::toString).orElse(null))
            .build();
    }
}
