package uk.gov.hmcts.opal.controllers.r1c;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

        private void assertRejectedRequestHasNoPersistenceSideEffects() {
            assertThat(findReferencesInBusinessUnit()).hasSize(3);
            assertThat(findFixtureAccountNotes()).isEmpty();
            assertThat(defendantAccountRepository.findAllByBusinessUnit_BusinessUnitId(BUSINESS_UNIT_ID))
                .extracting(DefendantAccountEntity::getVersionNumber)
                .containsExactlyInAnyOrder(0L, 0L, 0L);
        }

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
