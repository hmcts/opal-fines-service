package uk.gov.hmcts.opal.controllers.r1c;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.MANAGE_ALTERNATE_PAYMENT_REFERENCES;
import static uk.gov.hmcts.opal.controllers.shared.util.OpenApiContractAssertions.assertGet200JsonResponseMatchesBundledSpec;
import static uk.gov.hmcts.opal.testutil.JsonErrorAssertions.expectEntityNotFound;
import static uk.gov.hmcts.opal.testutil.JsonErrorAssertions.expectError;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.JsonNode;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.NoteType;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.AlternatePaymentReferenceEntity;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.Category;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.Relationship;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.repository.AlternatePaymentReferenceRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@TestPropertySource(properties = "launchdarkly.default-flag-values.release-1c-banking-interfaces=true")
@Sql(scripts = "classpath:db/insertData/insert_into_alternate_payment_references.sql",
    executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_alternate_payment_references.sql",
    executionPhase = AFTER_TEST_METHOD)
@DisplayName("Alternate payment reference controller integration tests")
class AlternatePaymentReferenceControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String ENDPOINT = "/defendant-accounts/{defendantAccountId}/alternate-payment-references";
    private static final short BUSINESS_UNIT_ID = 10877;
    private static final long ACCOUNT_WITH_REFERENCES = 1087701L;
    private static final long ACCOUNT_WITHOUT_REFERENCES = 1087702L;

    @Autowired
    private AlternatePaymentReferenceRepository alternatePaymentReferenceRepository;

    @Autowired
    private NoteRepository noteRepository;

    @BeforeEach
    void setUpPermissions() {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions(BUSINESS_UNIT_ID, MANAGE_ALTERNATE_PAYMENT_REFERENCES);
    }

    @Nested
    @DisplayName("Get alternate payment references for a defendant account")
    class GetAlternatePaymentReferences {

        @Test
        @JiraStory("PO-10877")
        @JiraEpic("PO-7465")
        @DisplayName("Returns multiple APRs with all mapped fields and excludes other accounts")
        void returnsMultipleReferencesForRequestedAccountOnly() throws Exception {
            ResultActions result = getReferences(ACCOUNT_WITH_REFERENCES)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(content().json("""
                    [
                      {
                        "alternate_payment_reference_id": 10877001,
                        "defendant_account_id": 1087701,
                        "relationship": "APR",
                        "category": "APR",
                        "business_unit_id": 10877,
                        "apr_text": "APR-REFERENCE-1",
                        "created_at": "2026-10-20T09:15:30",
                        "updated_at": "2026-10-21T10:20:40",
                        "version": 2
                      },
                      {
                        "alternate_payment_reference_id": 10877002,
                        "defendant_account_id": 1087701,
                        "relationship": "CONSOLIDATED",
                        "category": "ACC",
                        "business_unit_id": 10877,
                        "apr_text": "APR-REFERENCE-2",
                        "created_at": "2026-10-18T11:25:35",
                        "updated_at": "2026-10-19T12:30:45",
                        "version": 4
                      }
                    ]
                    """));

            assertGet200JsonResponseMatchesBundledSpec(
                objectMapper.readTree(result.andReturn().getResponse().getContentAsString()), ENDPOINT);
        }

        @Test
        @JiraStory("PO-10877")
        @JiraEpic("PO-7465")
        @DisplayName("Returns an empty array when the defendant account has no APRs")
        void returnsEmptyArrayWhenNoReferencesExist() throws Exception {
            getReferences(ACCOUNT_WITHOUT_REFERENCES)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @JiraStory("PO-10877")
        @JiraEpic("PO-7465")
        @DisplayName("Returns not found when the defendant account does not exist")
        void returnsNotFoundWhenDefendantAccountDoesNotExist() throws Exception {
            getReferences(1087799L)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(expectEntityNotFound());
        }

        @Test
        @JiraStory("PO-10877")
        @JiraEpic("PO-7465")
        @DisplayName("Returns forbidden when the authenticated user has no permissions")
        void returnsForbiddenWhenUserHasNoPermissions() throws Exception {
            userStateStub.setupWithNoPermissions();

            assertForbidden(getReferences(ACCOUNT_WITH_REFERENCES));
        }

        @Test
        @JiraStory("PO-10877")
        @JiraEpic("PO-7465")
        @DisplayName("Returns forbidden when the user has APR permission only in another business unit")
        void returnsForbiddenWhenPermissionIsForAnotherBusinessUnit() throws Exception {
            userStateStub.setupWithNoPermissions();
            userStateStub.addPermissions((short) 78, MANAGE_ALTERNATE_PAYMENT_REFERENCES);

            assertForbidden(getReferences(ACCOUNT_WITH_REFERENCES));
        }
    }

    @Nested
    @DisplayName("Create an alternate payment reference for a defendant account")
    class CreateAlternatePaymentReference {

        @Test
        @JiraStory("PO-10878")
        @JiraEpic("PO-7465")
        @DisplayName("Creates an APR, adds an activity note, and increments the defendant account version")
        void createsReferenceAndActivityNoteAndIncrementsDefendantAccountVersion() throws Exception {
            String aprText = "NEW-APR-REFERENCE";
            LocalDateTime beforeRequest = LocalDateTime.now(ZoneOffset.UTC);

            ResultActions result = createReference(ACCOUNT_WITHOUT_REFERENCES, aprText, "\"0\"")
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string(HttpHeaders.ETAG, "\"0\""))
                .andExpect(jsonPath("$.alternate_payment_reference_id").isNumber())
                .andExpect(jsonPath("$.defendant_account_id").value(ACCOUNT_WITHOUT_REFERENCES))
                .andExpect(jsonPath("$.relationship").value("APR"))
                .andExpect(jsonPath("$.category").value("APR"))
                //Cast to int because jsonPath returns a number as int, but the business unit id is a short
                .andExpect(jsonPath("$.business_unit_id").value((int) BUSINESS_UNIT_ID))
                .andExpect(jsonPath("$.apr_text").value(aprText))
                .andExpect(jsonPath("$.version").value(0));

            LocalDateTime afterRequest = LocalDateTime.now(ZoneOffset.UTC);
            JsonNode response = objectMapper.readTree(result.andReturn().getResponse().getContentAsString());
            long referenceId = response.get("alternate_payment_reference_id").asLong();
            assertThat(referenceId).isPositive();
            LocalDateTime createdAt = LocalDateTime.parse(response.get("created_at").asString());
            LocalDateTime updatedAt = LocalDateTime.parse(response.get("updated_at").asString());
            assertThat(createdAt).isBetween(beforeRequest, afterRequest);
            assertThat(updatedAt).isBetween(createdAt, afterRequest);

            AlternatePaymentReferenceEntity savedReference =
                alternatePaymentReferenceRepository.findById(referenceId).orElseThrow();

            assertThat(savedReference.getDefendantAccount().getDefendantAccountId())
                .isEqualTo(ACCOUNT_WITHOUT_REFERENCES);
            assertThat(savedReference.getRelationship()).isEqualTo(Relationship.APR);
            assertThat(savedReference.getCategory()).isEqualTo(Category.APR);
            assertThat(savedReference.getBusinessUnit().getBusinessUnitId()).isEqualTo(BUSINESS_UNIT_ID);
            assertThat(savedReference.getAprText()).isEqualTo(aprText);
            assertThat(savedReference.getVersionNumber()).isZero();
            assertThat(savedReference.getCreatedDatetime())
                .isCloseTo(createdAt, within(1, ChronoUnit.MICROS));
            assertThat(savedReference.getUpdatedDatetime())
                .isCloseTo(updatedAt, within(1, ChronoUnit.MICROS));
            assertThat(findReferencesInBusinessUnit()).hasSize(4);

            assertThat(findFixtureAccountNotes()).singleElement().satisfies(savedNote -> {
                assertThat(savedNote.getNoteId()).isPositive();
                assertThat(savedNote.getAssociatedRecordId()).isEqualTo(String.valueOf(ACCOUNT_WITHOUT_REFERENCES));
                assertThat(savedNote.getAssociatedRecordType()).isEqualTo(AssociatedRecordType.DEFENDANT_ACCOUNTS);
                assertThat(savedNote.getNoteType()).isEqualTo(NoteType.AA);
                assertThat(savedNote.getNoteText()).isEqualTo("Alternate payment reference added | " + aprText);
                assertThat(savedNote.getBusinessUnitUserId()).isEqualTo("L010877JG");
                assertThat(savedNote.getPostedByUsername()).isEqualTo(userStateStub.getUserState().getUsername());
                assertThat(savedNote.getPostedDate()).isBetween(beforeRequest, afterRequest);
            });
            assertThat(defendantAccountVersionFor(ACCOUNT_WITHOUT_REFERENCES)).isEqualTo(1);
        }

        @Test
        @JiraStory("PO-10878")
        @JiraEpic("PO-7465")
        @DisplayName("Returns not found when the defendant account does not exist")
        void returnsNotFoundWhenDefendantAccountDoesNotExist() throws Exception {
            createReference(1087799L, "NEW-APR-REFERENCE", "\"0\"")
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(expectEntityNotFound());
            assertRejectedRequestHasNoPersistenceSideEffects();
        }

        @Test
        @JiraStory("PO-10878")
        @JiraEpic("PO-7465")
        @DisplayName("Returns forbidden when the authenticated user has no APR permissions")
        void returnsForbiddenWhenUserHasNoPermissions() throws Exception {
            userStateStub.setupWithNoPermissions();

            assertForbidden(createReference(ACCOUNT_WITHOUT_REFERENCES, "NEW-APR-REFERENCE", "\"0\""));
            assertRejectedRequestHasNoPersistenceSideEffects();
        }

        @Test
        @JiraStory("PO-10878")
        @JiraEpic("PO-7465")
        @DisplayName("Rejects reference text already used by another defendant account in the same business unit")
        void returnsUnprocessableContentWhenReferenceTextAlreadyExistsInBusinessUnit() throws Exception {
            createReference(ACCOUNT_WITHOUT_REFERENCES, "APR-REFERENCE-1", "\"0\"")
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.title")
                    .value("Alternate Payment Reference text already exists in business unit"))
                .andExpect(jsonPath("$.detail").value(
                    "Alternate Payment Reference text: APR-REFERENCE-1 already exists in business unit: 10877"));
            assertRejectedRequestHasNoPersistenceSideEffects();
        }

        @Test
        @JiraStory("PO-10878")
        @JiraEpic("PO-7465")
        @DisplayName("Rejects a second APR relationship for the same defendant account even when the text is unique")
        void returnsUnprocessableContentWhenDefendantAccountAlreadyHasAprRelationship() throws Exception {
            createReference(ACCOUNT_WITH_REFERENCES, "NEW-APR-REFERENCE", "\"0\"")
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.title")
                    .value("Alternate Payment Reference already exists for defendant account"))
                .andExpect(jsonPath("$.detail").value(
                    "Alternate Payment Reference with relationship APR already exists for defendant account: 1087701"));
            assertRejectedRequestHasNoPersistenceSideEffects();
        }

        @Test
        @JiraStory("PO-10878")
        @JiraEpic("PO-7465")
        @DisplayName("Returns conflict failed when If-Match differs from the defendant account version")
        void returnsConflictFailedWhenDefendantAccountVersionDoesNotMatch() throws Exception {
            createReference(ACCOUNT_WITHOUT_REFERENCES, "NEW-APR-REFERENCE", "\"1\"")
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/optimistic-locking"));
            assertRejectedRequestHasNoPersistenceSideEffects();
        }

        @Test
        @JiraStory("PO-10878")
        @JiraEpic("PO-7465")
        @DisplayName("Returns bad request when APR text exceeds eighteen characters")
        void returnsBadRequestWhenReferenceTextExceedsEighteenCharacters() throws Exception {
            createReference(ACCOUNT_WITHOUT_REFERENCES, "1234567890123456789", "\"0\"")
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(expectError(HttpStatus.BAD_REQUEST, "Bad Request",
                    "The request does not conform to the required JSON schema",
                    "https://hmcts.gov.uk/problems/json-schema-validation"));
            assertRejectedRequestHasNoPersistenceSideEffects();
        }
    }

    @Nested
    @DisplayName("Update an alternate payment reference for a defendant account")
    class UpdateAlternatePaymentReference {

        private static final long REFERENCE_ID = 10877001L;
        private static final String UPDATED_TEXT = "UPDATED-REFERENCE";

        @Test
        @JiraStory("PO-10879")
        @JiraEpic("PO-7465")
        @DisplayName("Updates APR text and version, returns the new ETag, and adds an activity note")
        void updatesReferenceVersionAndAddsActivityNote() throws Exception {
            LocalDateTime beforeRequest = LocalDateTime.now(ZoneOffset.UTC);

            updateReference(ACCOUNT_WITH_REFERENCES, REFERENCE_ID, UPDATED_TEXT, "\"2\"")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string(HttpHeaders.ETAG, "\"3\""))
                .andExpect(jsonPath("$.alternate_payment_reference_id").value(REFERENCE_ID))
                .andExpect(jsonPath("$.defendant_account_id").value(ACCOUNT_WITH_REFERENCES))
                .andExpect(jsonPath("$.business_unit_id").value((int) BUSINESS_UNIT_ID))
                .andExpect(jsonPath("$.apr_text").value(UPDATED_TEXT))
                .andExpect(jsonPath("$.relationship").value("APR"))
                .andExpect(jsonPath("$.category").value("APR"))
                .andExpect(jsonPath("$.version").value(3));

            LocalDateTime afterRequest = LocalDateTime.now(ZoneOffset.UTC);
            AlternatePaymentReferenceEntity saved = alternatePaymentReferenceRepository.findById(REFERENCE_ID)
                .orElseThrow();
            assertThat(saved.getAprText()).isEqualTo(UPDATED_TEXT);
            assertThat(saved.getVersionNumber()).isEqualTo(3L);
            assertThat(saved.getRelationship()).isEqualTo(Relationship.APR);
            assertThat(saved.getCategory()).isEqualTo(Category.APR);
            assertThat(saved.getCreatedDatetime()).isEqualTo(LocalDateTime.of(2026, 10, 20, 9, 15, 30));
            assertThat(findReferencesInBusinessUnit()).hasSize(3);
            assertThat(alternatePaymentReferenceRepository.findById(10877002L).orElseThrow().getAprText())
                .isEqualTo("APR-REFERENCE-2");
            assertThat(alternatePaymentReferenceRepository.findById(10877003L).orElseThrow().getAprText())
                .isEqualTo("APR-OTHER-ACCOUNT");

            assertThat(findFixtureAccountNotes()).singleElement().satisfies(note -> {
                assertThat(note.getNoteId()).isPositive();
                assertThat(note.getAssociatedRecordId()).isEqualTo(String.valueOf(ACCOUNT_WITH_REFERENCES));
                assertThat(note.getAssociatedRecordType()).isEqualTo(AssociatedRecordType.DEFENDANT_ACCOUNTS);
                assertThat(note.getNoteType()).isEqualTo(NoteType.AA);
                assertThat(note.getNoteText()).isEqualTo("Alternate payment reference updated | " + UPDATED_TEXT);
                assertThat(note.getBusinessUnitUserId()).isEqualTo("L010877JG");
                assertThat(note.getPostedByUsername()).isEqualTo(userStateStub.getUserState().getUsername());
                assertThat(note.getPostedDate()).isBetween(beforeRequest, afterRequest);
            });
            assertThat(defendantAccountVersionFor(ACCOUNT_WITH_REFERENCES)).isEqualTo(1);
        }

        @Test
        @JiraStory("PO-10879")
        @JiraEpic("PO-7465")
        @DisplayName("Returns not found when the defendant account does not exist")
        void returnsNotFoundWhenDefendantAccountDoesNotExist() throws Exception {
            updateReference(1087799L, REFERENCE_ID, UPDATED_TEXT, "\"2\"")
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(expectEntityNotFound());
            assertRejectedRequestHasNoPersistenceSideEffects();
        }

        @Test
        @JiraStory("PO-10879")
        @JiraEpic("PO-7465")
        @DisplayName("Returns forbidden when the authenticated user has no APR permission")
        void returnsForbiddenWhenUserHasNoPermissions() throws Exception {
            userStateStub.setupWithNoPermissions();

            assertForbidden(updateReference(ACCOUNT_WITH_REFERENCES, REFERENCE_ID, UPDATED_TEXT, "\"2\""));
            assertRejectedRequestHasNoPersistenceSideEffects();
        }

        @Test
        @JiraStory("PO-10879")
        @JiraEpic("PO-7465")
        @DisplayName("Rejects text already used by another APR in the same business unit")
        void rejectsReferenceTextAlreadyUsedInBusinessUnit() throws Exception {
            updateReference(ACCOUNT_WITH_REFERENCES, REFERENCE_ID, "APR-OTHER-ACCOUNT", "\"2\"")
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title")
                    .value("Alternate Payment Reference text already exists in business unit"))
                .andExpect(jsonPath("$.detail").value(
                    "Alternate Payment Reference text: APR-OTHER-ACCOUNT already exists in business unit: 10877"));
            assertRejectedRequestHasNoPersistenceSideEffects();
        }

        @Test
        @JiraStory("PO-10879")
        @JiraEpic("PO-7465")
        @DisplayName("Rejects an APR version mismatch even when If-Match matches the defendant account version")
        void rejectsAprVersionMismatch() throws Exception {
            updateReference(ACCOUNT_WITH_REFERENCES, REFERENCE_ID, UPDATED_TEXT, "\"0\"")
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/optimistic-locking"));
            assertRejectedRequestHasNoPersistenceSideEffects();
        }

        @Test
        @JiraStory("PO-10879")
        @JiraEpic("PO-7465")
        @DisplayName("Returns bad request when updated APR text exceeds eighteen characters")
        void rejectsReferenceTextLongerThanEighteenCharacters() throws Exception {
            updateReference(ACCOUNT_WITH_REFERENCES, REFERENCE_ID, "1234567890123456789", "\"2\"")
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(expectError(HttpStatus.BAD_REQUEST, "Bad Request",
                    "The request does not conform to the required JSON schema",
                    "https://hmcts.gov.uk/problems/json-schema-validation"));
            assertRejectedRequestHasNoPersistenceSideEffects();
        }

        @ParameterizedTest
        @CsvSource({"1087701, 10877002, CONSOLIDATED, 4", "1087703, 10877003, AMALGAMATED, 1"})
        @JiraStory("PO-10879")
        @JiraEpic("PO-7465")
        @DisplayName("Rejects references with a consolidated or amalgamated relationship")
        void rejectsNonAprRelationships(long accountId, long referenceId, Relationship relationship, long version)
            throws Exception {
            updateReference(accountId, referenceId, UPDATED_TEXT, "\"" + version + "\"")
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title")
                    .value("Alternate Payment Reference relationship does not have a relationship of APR"))
                .andExpect(jsonPath("$.detail").value("Alternate Payment Reference with relationship: " + relationship
                    + " cannot be updated for defendant account: " + accountId));
            assertRejectedRequestHasNoPersistenceSideEffects();
        }

        @ParameterizedTest
        @CsvSource({"1087701, 10877999", "1087702, 10877001"})
        @JiraStory("PO-10879")
        @JiraEpic("PO-7465")
        @DisplayName("Returns not found when the APR is missing or belongs to another defendant account")
        void rejectsMissingOrUnrelatedReference(long accountId, long referenceId) throws Exception {
            updateReference(accountId, referenceId, UPDATED_TEXT, "\"2\"")
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Alternate Payment Reference not found"))
                .andExpect(jsonPath("$.detail").value("Alternate Payment Reference not found for defendantAccountId: "
                    + accountId + " and alternatePaymentReferenceId: " + referenceId));
            assertRejectedRequestHasNoPersistenceSideEffects();
        }
    }

    @Nested
    @DisplayName("Delete an alternate payment reference for a defendant account")
    class DeleteAlternatePaymentReference {

        private static final long REFERENCE_ID = 10877001L;

        @Test
        @JiraStory("PO-10880")
        @JiraEpic("PO-7465")
        @DisplayName("Deletes the APR, returns its details and ETag, and adds an activity note")
        void deletesReferenceAndAddsActivityNote() throws Exception {
            LocalDateTime beforeRequest = LocalDateTime.now(ZoneOffset.UTC);

            deleteReference(ACCOUNT_WITH_REFERENCES, REFERENCE_ID, "\"2\"")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string(HttpHeaders.ETAG, "\"2\""))
                .andExpect(jsonPath("$.alternate_payment_reference_id").value(REFERENCE_ID))
                .andExpect(jsonPath("$.defendant_account_id").value(ACCOUNT_WITH_REFERENCES))
                .andExpect(jsonPath("$.business_unit_id").value((int) BUSINESS_UNIT_ID))
                .andExpect(jsonPath("$.apr_text").value("APR-REFERENCE-1"))
                .andExpect(jsonPath("$.relationship").value("APR"))
                .andExpect(jsonPath("$.category").value("APR"))
                .andExpect(jsonPath("$.created_at").value("2026-10-20T09:15:30"))
                .andExpect(jsonPath("$.updated_at").value("2026-10-21T10:20:40"))
                .andExpect(jsonPath("$.version").value(2));

            LocalDateTime afterRequest = LocalDateTime.now(ZoneOffset.UTC);
            assertThat(alternatePaymentReferenceRepository.existsById(REFERENCE_ID)).isFalse();
            assertThat(findReferencesInBusinessUnit())
                .extracting(AlternatePaymentReferenceEntity::getAlternatePaymentReferenceId,
                    AlternatePaymentReferenceEntity::getAprText, AlternatePaymentReferenceEntity::getVersionNumber)
                .containsExactlyInAnyOrder(
                    tuple(10877002L, "APR-REFERENCE-2", 4L),
                    tuple(10877003L, "APR-OTHER-ACCOUNT", 1L));
            assertThat(findFixtureAccountNotes()).singleElement().satisfies(note -> {
                assertThat(note.getNoteId()).isPositive();
                assertThat(note.getAssociatedRecordId()).isEqualTo(String.valueOf(ACCOUNT_WITH_REFERENCES));
                assertThat(note.getAssociatedRecordType()).isEqualTo(AssociatedRecordType.DEFENDANT_ACCOUNTS);
                assertThat(note.getNoteType()).isEqualTo(NoteType.AA);
                assertThat(note.getNoteText()).isEqualTo("Alternate payment reference deleted | APR-REFERENCE-1");
                assertThat(note.getBusinessUnitUserId()).isEqualTo("L010877JG");
                assertThat(note.getPostedByUsername()).isEqualTo(userStateStub.getUserState().getUsername());
                assertThat(note.getPostedDate()).isBetween(beforeRequest, afterRequest);
            });
            assertThat(defendantAccountVersionFor(ACCOUNT_WITH_REFERENCES)).isEqualTo(1);
            assertThat(defendantAccountVersionFor(ACCOUNT_WITHOUT_REFERENCES)).isZero();
            assertThat(defendantAccountVersionFor(1087703L)).isZero();
        }

        @Test
        @JiraStory("PO-10880")
        @JiraEpic("PO-7465")
        @DisplayName("Returns not found when the defendant account does not exist")
        void returnsNotFoundWhenDefendantAccountDoesNotExist() throws Exception {
            deleteReference(1087799L, REFERENCE_ID, "\"2\"")
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(expectEntityNotFound());
            assertRejectedRequestHasNoPersistenceSideEffects();
        }

        @ParameterizedTest
        @CsvSource({"1087701, 10877999", "1087702, 10877001"})
        @JiraStory("PO-10880")
        @JiraEpic("PO-7465")
        @DisplayName("Returns not found when the APR is missing or belongs to another defendant account")
        void rejectsMissingOrUnrelatedReference(long accountId, long referenceId) throws Exception {
            deleteReference(accountId, referenceId, "\"2\"")
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Alternate Payment Reference not found"))
                .andExpect(jsonPath("$.detail").value("Alternate Payment Reference not found for defendantAccountId: "
                    + accountId + " and alternatePaymentReferenceId: " + referenceId));
            assertRejectedRequestHasNoPersistenceSideEffects();
        }

        @Test
        @JiraStory("PO-10880")
        @JiraEpic("PO-7465")
        @DisplayName("Returns forbidden when the authenticated user has no APR permission")
        void returnsForbiddenWhenUserHasNoPermissions() throws Exception {
            userStateStub.setupWithNoPermissions();

            assertForbidden(deleteReference(ACCOUNT_WITH_REFERENCES, REFERENCE_ID, "\"2\""));
            assertRejectedRequestHasNoPersistenceSideEffects();
        }

        @Test
        @JiraStory("PO-10880")
        @JiraEpic("PO-7465")
        @DisplayName("Returns forbidden when the user has APR permission only in another business unit")
        void returnsForbiddenWhenPermissionIsForAnotherBusinessUnit() throws Exception {
            userStateStub.setupWithNoPermissions();
            userStateStub.addPermissions((short) 78, MANAGE_ALTERNATE_PAYMENT_REFERENCES);

            assertForbidden(deleteReference(ACCOUNT_WITH_REFERENCES, REFERENCE_ID, "\"2\""));
            assertRejectedRequestHasNoPersistenceSideEffects();
        }

        @Test
        @JiraStory("PO-10880")
        @JiraEpic("PO-7465")
        @DisplayName("Rejects an APR version mismatch even when If-Match matches the defendant account version")
        void rejectsAprVersionMismatch() throws Exception {
            deleteReference(ACCOUNT_WITH_REFERENCES, REFERENCE_ID, "\"0\"")
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/optimistic-locking"));
            assertRejectedRequestHasNoPersistenceSideEffects();
        }
    }

    private ResultActions deleteReference(long accountId, long referenceId, String ifMatch) throws Exception {
        return mockMvc.perform(delete(ENDPOINT + "/{alternatePaymentReferenceId}", accountId, referenceId)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(HttpHeaders.IF_MATCH, ifMatch)
            .accept(MediaType.APPLICATION_JSON));
    }

    private void assertRejectedRequestHasNoPersistenceSideEffects() {
        assertThat(findReferencesInBusinessUnit())
            .extracting(AlternatePaymentReferenceEntity::getAlternatePaymentReferenceId,
                AlternatePaymentReferenceEntity::getAprText, AlternatePaymentReferenceEntity::getVersionNumber)
            .containsExactlyInAnyOrder(
                tuple(10877001L, "APR-REFERENCE-1", 2L),
                tuple(10877002L, "APR-REFERENCE-2", 4L),
                tuple(10877003L, "APR-OTHER-ACCOUNT", 1L));
        assertThat(findFixtureAccountNotes()).isEmpty();
        assertThat(defendantAccountRepository.findAllByBusinessUnit_BusinessUnitId(BUSINESS_UNIT_ID))
            .extracting(DefendantAccountEntity::getVersionNumber)
            .containsExactlyInAnyOrder(0L, 0L, 0L);
    }

    private ResultActions updateReference(long accountId, long referenceId, String aprText, String ifMatch)
        throws Exception {
        return mockMvc.perform(patch(ENDPOINT + "/{alternatePaymentReferenceId}", accountId, referenceId)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(HttpHeaders.IF_MATCH, ifMatch)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Map.of("apr_text", aprText))));
    }

    private List<AlternatePaymentReferenceEntity> findReferencesInBusinessUnit() {
        return alternatePaymentReferenceRepository.findAll((root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("businessUnit").get("businessUnitId"), BUSINESS_UNIT_ID));
    }

    private List<NoteEntity> findFixtureAccountNotes() {
        return noteRepository.findAll((root, query, criteriaBuilder) -> criteriaBuilder.and(
            root.get("associatedRecordId").in("1087701", "1087702", "1087703"),
            criteriaBuilder.equal(root.get("associatedRecordType"), AssociatedRecordType.DEFENDANT_ACCOUNTS)));
    }

    private ResultActions createReference(long defendantAccountId, String aprText, String ifMatch) throws Exception {
        return mockMvc.perform(post(ENDPOINT, defendantAccountId)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(HttpHeaders.IF_MATCH, ifMatch)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Map.of("apr_text", aprText))));
    }

    private ResultActions getReferences(long defendantAccountId) throws Exception {
        return mockMvc.perform(get(ENDPOINT, defendantAccountId)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .accept(MediaType.APPLICATION_JSON));
    }

    private void assertForbidden(ResultActions result) throws Exception {
        result.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(expectError(HttpStatus.FORBIDDEN, "Forbidden",
                "You do not have permission to access this resource", "https://hmcts.gov.uk/problems/forbidden"))
            .andExpect(jsonPath("$.retriable").value(false));
    }
}
