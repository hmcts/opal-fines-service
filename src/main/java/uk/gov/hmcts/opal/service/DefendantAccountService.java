package uk.gov.hmcts.opal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.opal.dto.ToJsonString.newObjectMapper;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class DefendantAccountService {

    private final DefendantAccountRepository defendantAccountRepository;

    private final DefendantAccountPartiesRepository defendantAccountPartiesRepository;

    private final PaymentTermsRepository paymentTermsRepository;

    private final DebtorDetailRepository debtorDetailRepository;

    private final EnforcersRepository enforcersRepository;

    private final NoteRepository noteRepository;

    public DefendantAccountEntity getDefendantAccount(AccountEnquiryDto request) {

        return defendantAccountRepository.findByBusinessUnitId_BusinessUnitIdAndAccountNumber(
            request.getBusinessUnitId(), request.getAccountNumber());
    }

    public DefendantAccountEntity putDefendantAccount(DefendantAccountEntity defendantAccountEntity) {

        return defendantAccountRepository.save(defendantAccountEntity);
    }

    public List<DefendantAccountEntity> getDefendantAccountsByBusinessUnit(Short businessUnitId) {

        log.info(":getDefendantAccountsByBusinessUnit: busUnit: {}", businessUnitId);
        return defendantAccountRepository.findAllByBusinessUnitId_BusinessUnitId(businessUnitId);
    }

    public AccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto) {

        if ("test".equalsIgnoreCase(accountSearchDto.getCourt())) {

            try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("tempSearchData.json")) {
                ObjectMapper mapper = newObjectMapper();
                AccountSearchResultsDto dto = mapper.readValue(in, AccountSearchResultsDto.class);
                log.info(":searchDefendantAccounts: temporary Hack for Front End testing. Read JSON file: \n{}",
                         dto.toPrettyJsonString());
                return dto;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        List<DefendantAccountSummary> summaries = defendantAccountRepository.findByOriginatorNameContaining(
            accountSearchDto.getSurname());

        return AccountSearchResultsDto.builder()
            .searchResults(List.of(AccountSummaryDto.builder().build()))
            .totalCount(999)
            .cursor(0)
            .build();
    }

    public AccountDetailsDto getAccountDetailsByDefendantAccountId(Long defendantAccountId) {


        if (defendantAccountId.equals(0L)) {


            try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("tempDetailsData.json")) {

                ObjectMapper mapper = newObjectMapper();
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
            .findByDefendantAccountId(defendantAccountId);

        //Extract unique defendantAccount and party entities
        final DefendantAccountEntity defendantAccountEntity = defendantAccountPartiesEntity.getDefendantAccount();
        final PartyEntity partyEntity = defendantAccountPartiesEntity.getParty();

        //query DB for PaymentTermsEntity
        PaymentTermsEntity paymentTermsEntity = paymentTermsRepository.findByDefendantAccount_DefendantAccountId(
            defendantAccountEntity);

        //query DB for EnforcementEntity
        EnforcersEntity enforcersEntity = enforcersRepository.findByEnforcerId(defendantAccountEntity
                                                                                   .getEnforcementOverrideEnforcerId());

        //query DB for NoteEntity by associatedRecordId (defendantAccountId) and noteType ("AC")
        List<NoteEntity> noteEntity = noteRepository.findByAssociatedRecordIdAndNoteType(
            defendantAccountEntity.getDefendantAccountId().toString(), "AC");


        //build fullAddress
        final String fullAddress = buildFullAddress(partyEntity);

        //build fullName
        final String fullName = buildFullName(partyEntity);

        //build paymentDetails
        final String paymentDetails = buildPaymentDetails(paymentTermsEntity);

        //build comments
        final List<String> comments = buildCommentsFromAssociatedNotes(noteEntity);

        //populate accountDetailsDto and return
        return AccountDetailsDto.builder()
            .accountNumber(defendantAccountEntity.getAccountNumber())
            .fullName(fullName)
            .accountCT(defendantAccountEntity.getBusinessUnitId().getBusinessUnitName())
            .accountType(defendantAccountEntity.getOriginatorType())
            .address(fullAddress)
            .postCode(partyEntity.getPostcode())
            .dob(partyEntity.getDateOfBirth())
            .detailsChanged(defendantAccountEntity.getLastChangedDate())
            .lastCourtAppAndCourtCode(defendantAccountEntity.getLastHearingDate().toString()
                                          + defendantAccountEntity.getLastHearingCourtId().getCourtCode())
            .lastMovement(defendantAccountEntity.getLastMovementDate())
            .commentField(comments)
            .pcr(defendantAccountEntity.getProsecutorCaseReference())
            .paymentDetails(paymentDetails)
            .lumpSum(paymentTermsEntity.getInstalmentLumpSum())
            .commencing(paymentTermsEntity.getEffectiveDate())
            .daysInDefault(paymentTermsEntity.getJailDays())
            .lastEnforcement(defendantAccountEntity.getLastEnforcement())
            .override(defendantAccountEntity.getEnforcementOverrideResultId())
            .enforcer(enforcersEntity.getEnforcerCode())
            .enforcementCourt(defendantAccountEntity.getEnforcingCourtId().getCourtCode())
            .imposed(defendantAccountEntity.getAmountImposed())
            .amountPaid(defendantAccountEntity.getAmountPaid())
            .arrears(defendantAccountEntity.getAccountBalance())
            .balance(defendantAccountEntity.getAccountBalance())
            .build();
    }

    private String buildFullAddress(PartyEntity partyEntity) {

        return partyEntity.getAddressLine1() + ", "
            + partyEntity.getAddressLine2() + ", "
            + partyEntity.getAddressLine3() + ", "
            + partyEntity.getAddressLine4() + ", "
            + partyEntity.getAddressLine5();
    }

    private String buildFullName(PartyEntity partyEntity) {

        return partyEntity.getOrganisationName() == null ? partyEntity.getTitle() + " "
            + partyEntity.getForenames() + " "
            + partyEntity.getInitials() + " "
            + partyEntity.getSurname() : partyEntity.getOrganisationName();
    }

    private String buildPaymentDetails(PaymentTermsEntity paymentTermsEntity) {
        final String paymentType = paymentTermsEntity.getTermsTypeCode();

        return switch (paymentType) {
            case "I" -> buildInstalmentDetails(paymentTermsEntity);
            case "B" -> buildByDateDetails(paymentTermsEntity);
            default -> "Paid";
        };
    }

    private String buildInstalmentDetails(PaymentTermsEntity paymentTermsEntity) {
        return paymentTermsEntity.getInstalmentAmount() + " / " + paymentTermsEntity.getInstalmentPeriod();
    }

    private String buildByDateDetails(PaymentTermsEntity paymentTermsEntity) {
        return paymentTermsEntity.getEffectiveDate() + " By Date";
    }

    private List<String> buildCommentsFromAssociatedNotes(List<NoteEntity> notes) {

        List<String> comments = new ArrayList<>();

        for (NoteEntity note : notes) {

            comments.add(note.getNoteText());
        }

        return comments;
    }
}
