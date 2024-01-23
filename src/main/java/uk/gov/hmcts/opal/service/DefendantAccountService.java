package uk.gov.hmcts.opal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.AccountSearchDto;
import uk.gov.hmcts.opal.dto.AccountSearchResultsDto;
import uk.gov.hmcts.opal.dto.AccountSummaryDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountSummary;
import uk.gov.hmcts.opal.entity.EnforcersEntity;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.repository.DebtorDetailRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountPartiesRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.EnforcersRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.PaymentTermsRepository;
import uk.gov.hmcts.opal.repository.jpa.DefendantAccountSpecs;

import uk.gov.hmcts.opal.entity.DefendantAccountSummary.PartyLink;
import uk.gov.hmcts.opal.entity.DefendantAccountSummary.PartyDefendantAccountSummary;

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
@Transactional
@Slf4j(topic = "DefendantAccountService")
@RequiredArgsConstructor
public class DefendantAccountService implements DefendantAccountServiceInterface {

    private final DefendantAccountRepository defendantAccountRepository;

    private final DefendantAccountPartiesRepository defendantAccountPartiesRepository;

    private final PaymentTermsRepository paymentTermsRepository;

    private final DebtorDetailRepository debtorDetailRepository;

    private final EnforcersRepository enforcersRepository;

    private final NoteRepository noteRepository;

    @Override
    public DefendantAccountEntity getDefendantAccount(AccountEnquiryDto request) {

        return defendantAccountRepository.findByBusinessUnitId_BusinessUnitIdAndAccountNumber(
            request.getBusinessUnitId(), request.getAccountNumber());
    }

    @Override
    public DefendantAccountEntity putDefendantAccount(DefendantAccountEntity defendantAccountEntity) {

        return defendantAccountRepository.save(defendantAccountEntity);
    }

    @Override
    public List<DefendantAccountEntity> getDefendantAccountsByBusinessUnit(Short businessUnitId) {

        log.info(":getDefendantAccountsByBusinessUnit: busUnit: {}", businessUnitId);
        return defendantAccountRepository.findAllByBusinessUnitId_BusinessUnitId(businessUnitId);
    }

    @Override
    public AccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto) {
        log.info(":searchDefendantAccounts: criteria: {}", accountSearchDto.toJson());

        if ("test".equalsIgnoreCase(accountSearchDto.getCourt())) {

            try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("tempSearchData.json")) {
                ObjectMapper mapper = getObjectMapper();
                AccountSearchResultsDto dto = mapper.readValue(in, AccountSearchResultsDto.class);
                log.info(":searchDefendantAccounts: temporary Hack for Front End testing. Read JSON file: \n{}",
                         dto.toPrettyJsonString());
                return dto;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        Page<DefendantAccountSummary> summariesPage = defendantAccountRepository
            .findBy(DefendantAccountSpecs.findByAccountSearch(accountSearchDto),
                    ffq -> ffq.as(DefendantAccountSummary.class).page(Pageable.unpaged()));

        List<AccountSummaryDto> dtos = summariesPage.getContent().stream()
            .map(this::toDto)
            .collect(Collectors.toList());

        return AccountSearchResultsDto.builder()
            .searchResults(dtos)
            .totalCount(summariesPage.getTotalElements())
            .cursor(summariesPage.getNumber())
            .build();
    }

    public AccountDetailsDto getAccountDetailsByDefendantAccountId(Long defendantAccountId) {


        if (defendantAccountId.equals(0L)) {


            try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("tempDetailsData.json")) {

                ObjectMapper mapper = getObjectMapper();
                AccountDetailsDto dto = mapper.readValue(in, AccountDetailsDto.class);
                log.info(
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
            .findByDefendantAccountDefendantAccountId(defendantAccountId);

        //Extract unique defendantAccount and party entities
        final DefendantAccountEntity defendantAccountEntity = defendantAccountPartiesEntity.getDefendantAccount();
        final PartyEntity partyEntity = defendantAccountPartiesEntity.getParty();


        //query DB for PaymentTermsEntity
        PaymentTermsEntity paymentTermsEntity = paymentTermsRepository.findByDefendantAccount_DefendantAccountId(
            defendantAccountEntity.getDefendantAccountId());

        //query DB for EnforcementEntity
        EnforcersEntity enforcersEntity = enforcersRepository.findByEnforcerId(defendantAccountEntity
                                                                                   .getEnforcementOverrideEnforcerId());

        //query DB for NoteEntity by associatedRecordId (defendantAccountId) and noteType ("AC")
        List<NoteEntity> noteEntity = noteRepository.findByAssociatedRecordIdAndNoteType(
            defendantAccountEntity.getDefendantAccountId().toString(), "AC");


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
        final List<String> comments = buildCommentsFromAssociatedNotes(noteEntity);

        //populate accountDetailsDto and return
        return AccountDetailsDto.builder()
            .defendantAccountId(defendantAccountEntity.getDefendantAccountId())
            .accountNumber(defendantAccountEntity.getAccountNumber())
            .fullName(partyEntity.getOrganisationName() == null
                          ? partyEntity.getFullName()
                          : partyEntity.getOrganisationName())
            .accountCT(defendantAccountEntity.getBusinessUnitId().getBusinessUnitName())
            .address(fullAddress)
            .postCode(partyEntity.getPostcode())
            .dob(partyEntity.getDateOfBirth())
            .detailsChanged(defendantAccountEntity.getLastChangedDate())
            .lastCourtAppAndCourtCode(defendantAccountEntity.getLastHearingDate().toString()
                                          + " " + defendantAccountEntity.getLastHearingCourtId().getCourtCode())
            .lastMovement(defendantAccountEntity.getLastMovementDate())
            .commentField(comments)
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
            .enforcer(enforcersEntity.getEnforcerCode())
            .enforcementCourt(defendantAccountEntity.getEnforcingCourtId().getCourtCode())
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

    public AccountSummaryDto toDto(DefendantAccountSummary summary) {
        Optional<PartyDefendantAccountSummary> party = summary.getParties().stream().findAny().map(PartyLink::getParty);
        return AccountSummaryDto.builder()
            .defendantAccountId(summary.getDefendantAccountId())
            .accountNo(summary.getAccountNumber())
            .court(summary.getImposingCourtId())
            .balance(summary.getAccountBalance())
            .name(party.map(PartyDefendantAccountSummary::getFullName).orElse(""))
            .addressLine1(party.map(PartyDefendantAccountSummary::getAddressLine1).orElse(""))
            .dateOfBirth(party.map(PartyDefendantAccountSummary::getDateOfBirth).orElse(null))
            .build();
    }
}
