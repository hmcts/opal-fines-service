package uk.gov.hmcts.opal.controllers;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.entity.AliasEntity;
import uk.gov.hmcts.opal.entity.amendment.AmendmentEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.AssociationType;
import uk.gov.hmcts.opal.entity.debtordetail.DebtorDetailEntity;
import uk.gov.hmcts.opal.repository.AliasRepository;
import uk.gov.hmcts.opal.repository.AmendmentRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountPartiesRepository;
import uk.gov.hmcts.opal.repository.DebtorDetailRepository;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

@ActiveProfiles({"integration", "opal"})
@Sql(
    scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS
)
@Sql(
    scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql",
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS
)
@Slf4j
abstract class AbstractOpalDefendantsIntegrationTest extends AbstractIntegrationTest {

    protected static final String URL_BASE = "/defendant-accounts";
    protected static final String DEFENDANT_GLANCE_RESPONSE_SCHEMA = SchemaPaths.DEFENDANT_ACCOUNT
        + "/getDefendantAccountAtAGlanceResponse.json";
    protected static final String DEFENDANT_PARTY_RESPONSE_SCHEMA = SchemaPaths.DEFENDANT_ACCOUNT
        + "/getDefendantAccountPartyResponse.json";
    protected static final String DEFENDANT_FIXED_PENALTY_RESPONSE_SCHEMA =
        SchemaPaths.DEFENDANT_ACCOUNT + "/getDefendantAccountFixedPenaltyResponse.json";
    protected static final LocalDate ACCOUNT_77_BIRTH_DATE = LocalDate.of(1980, 2, 3);

    @MockitoSpyBean
    protected JsonSchemaValidationService jsonSchemaValidationService;

    @Autowired
    protected DebtorDetailRepository debtorDetailRepository;

    @Autowired
    protected DefendantAccountPartiesRepository defendantAccountPartiesRepository;

    @Autowired
    protected AliasRepository aliasRepository;

    @Autowired
    protected AmendmentRepository amendmentRepository;

    protected @NonNull HttpHeaders buildHttpHeaders(String number, String currentVersion) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", number);
        headers.add(HttpHeaders.IF_MATCH, currentVersion);
        return headers;
    }

    protected static String commentAndNotesPayload(String accountComment) {
        return commentAndNotesPayload(accountComment, null, null, null);
    }

    protected static String commentAndNotesPayload(String accountComment, String note1, String note2, String note3) {
        return """
            {
              "comment_and_notes": {
                "account_comment": %s,
                "free_text_note_1": %s,
                "free_text_note_2": %s,
                "free_text_note_3": %s
              }
            }
            """.formatted(jsonValue(accountComment), jsonValue(note1), jsonValue(note2), jsonValue(note3));
    }

    protected static String jsonValue(String s) {
        if (s == null) {
            return "null";
        }
        return "\"" + s.replace("\"", "\\\"") + "\"";
    }

    protected static String expectedAge() {
        return String.valueOf(Period.between(ACCOUNT_77_BIRTH_DATE, LocalDate.now()).getYears());
    }

    protected HttpHeaders authorisedHeaders(String bearerToken, String businessUnitId, String ifMatch) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, bearerToken);
        headers.add("Business-Unit-Id", businessUnitId);
        if (ifMatch != null) {
            headers.add(HttpHeaders.IF_MATCH, ifMatch);
        }
        return headers;
    }

    protected Integer versionFor(long defendantAccountId) {
        return defendantAccountVersionFor(defendantAccountId);
    }

    protected String lastEnforcementFor(long defendantAccountId) {
        return defendantAccountRepository.findById(defendantAccountId).orElseThrow().getLastEnforcement();
    }

    protected int partyAssociationCountFor(long defendantAccountId, long defendantAccountPartyId) {
        return defendantAccountPartiesRepository.countByDefendantAccount_DefendantAccountIdAndDefendantAccountPartyId(
            defendantAccountId, defendantAccountPartyId);
    }

    protected int partyAssociationCountFor(long defendantAccountId, AssociationType associationType) {
        return defendantAccountPartiesRepository.countByDefendantAccount_DefendantAccountIdAndAssociationType(
            defendantAccountId, associationType);
    }

    protected long partyCountForOrganisations(long defendantAccountId, String... organisationNames) {
        return defendantAccountPartiesRepository.countByDefendantAccount_DefendantAccountIdAndParty_OrganisationNameIn(
            defendantAccountId, Set.of(organisationNames));
    }

    protected DebtorDetailEntity debtorDetailFor(long partyId) {
        return debtorDetailRepository.findByPartyId(partyId).orElseThrow();
    }

    protected List<AliasEntity> aliasesForParty(long partyId) {
        return aliasRepository.findByParty_PartyIdOrderBySequenceNumberAsc(partyId);
    }

    protected int aliasCountFor(long partyId, long aliasId) {
        return aliasRepository.countByParty_PartyIdAndAliasId(partyId, aliasId);
    }

    protected List<AmendmentEntity> defendantAccountAmendmentsFor(long defendantAccountId) {
        return amendmentRepository.findByAssociatedRecordIdOrderByAmendmentIdAsc(String.valueOf(defendantAccountId));
    }

    protected LocalDate lastChangedDateFor(long defendantAccountId) {
        return defendantAccountRepository.findById(defendantAccountId).orElseThrow().getLastChangedDate();
    }

    protected void authorise(short businessUnitId, FinesPermission permission) {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions(businessUnitId, permission);
    }
}
