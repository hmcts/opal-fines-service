package uk.gov.hmcts.opal.controllers.r1c;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.core.type.TypeReference;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.dto.AddDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.DraftAccountResponseDto;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountSource;
import uk.gov.hmcts.opal.entity.draft.DraftAccountType;
import uk.gov.hmcts.opal.repository.DraftAccountRepository;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration"})
@Sql(
    scripts = {
        "classpath:db/deleteData/delete_from_draft_account_reference_validation_data.sql",
        "classpath:db/insertData/insert_into_draft_account_reference_validation_data.sql",
        "classpath:db/deleteData/delete_from_draft_accounts_vpfpo.sql",
        "classpath:db/insertData/insert_into_draft_accounts_vpfpo.sql"
    },
    executionPhase = BEFORE_TEST_CLASS
)
@Sql(
    scripts = {
        "classpath:db/deleteData/delete_from_draft_accounts_vpfpo.sql",
        "classpath:db/deleteData/delete_from_draft_account_reference_validation_data.sql"
    },
    executionPhase = AFTER_TEST_CLASS
)
@Slf4j(topic = "opal.DraftAccountControllerPostIntegrationTest")
@DisplayName("DraftAccountVPFPOIntegrationTest")
public class DraftAccountVpfpoIntegrationTest extends AbstractIntegrationTest {

    static final String URL_BASE = "/draft-accounts";
    static final String RESTRICT_PARAM = "?restrict=counts";
    private static final int[] MAC_DRAFT_ACCOUNT_IDS = {11, 12};
    private static final int[] VPFPO_DRAFT_ACCOUNT_IDS = {21, 22};

    @Autowired
    protected DraftAccountRepository draftAccountRepository;

    @TestPropertySource(properties = {
        "launchdarkly.enabled=false",
        "launchdarkly.default-flag-values.release-1c-banking-interfaces=true"
    })
    @Nested
    class Release1CFlagOnTests {
        @Test
        @DisplayName("AC1: Get draft account filters on source=MAC")
        @JiraStory("PO-10308")
        @JiraEpic("PO-7459")
        void get_filtersSourceMAC() throws Exception {
            userStateStub.setupWithNoPermissions();
            userStateStub.addPermissions((short) 77,
                FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS,
                FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS);

            ResultActions resultActions = mockMvc.perform(get(getUrlBase("MAC"))
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .header("X-User-IP", "192.168.1.100")
                .contentType(MediaType.APPLICATION_JSON));

            resultActions.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.summaries.[0].draft_account_id").value(MAC_DRAFT_ACCOUNT_IDS[0]))
                .andExpect(jsonPath("$.summaries.[1].draft_account_id").value(MAC_DRAFT_ACCOUNT_IDS[1]));
        }

        @Test
        @DisplayName("AC2: Get draft account filters on source=VPFPO")
        @JiraStory("PO-10308")
        @JiraEpic("PO-7459")
        void get_filtersSourcevpfpo() throws Exception {
            userStateStub.setupWithNoPermissions();
            userStateStub.addPermissions((short) 77,
                FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS,
                FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS);

            ResultActions resultActions = mockMvc.perform(get(getUrlBase("VPFPO"))
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .header("X-User-IP", "192.168.1.100")
                .contentType(MediaType.APPLICATION_JSON));

            resultActions.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.summaries.[0].draft_account_id").value(VPFPO_DRAFT_ACCOUNT_IDS[0]))
                .andExpect(jsonPath("$.summaries.[1].draft_account_id").value(VPFPO_DRAFT_ACCOUNT_IDS[1]));
        }

        @Test
        @DisplayName("AC5: Get draft account returns all when source is missing")
        @JiraStory("PO-10308")
        @JiraEpic("PO-7459")
        void get_filtersSourceDefault() throws Exception {
            userStateStub.setupWithNoPermissions();
            userStateStub.addPermissions((short) 77,
                FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS,
                FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS);

            ResultActions resultActions = mockMvc.perform(get(URL_BASE)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .header("X-User-IP", "192.168.1.100")
                .contentType(MediaType.APPLICATION_JSON));

            resultActions.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(4))
                .andExpect(jsonPath("$.summaries.[0].draft_account_id").value(MAC_DRAFT_ACCOUNT_IDS[0]))
                .andExpect(jsonPath("$.summaries.[1].draft_account_id").value(MAC_DRAFT_ACCOUNT_IDS[1]))
                .andExpect(jsonPath("$.summaries.[2].draft_account_id").value(VPFPO_DRAFT_ACCOUNT_IDS[0]))
                .andExpect(jsonPath("$.summaries.[3].draft_account_id").value(VPFPO_DRAFT_ACCOUNT_IDS[1]));
        }

        @Test
        @DisplayName("AC1: Get draft account counts filters on source=MAC")
        @JiraStory("PO-10308")
        @JiraEpic("PO-7459")
        void get_counts_filtersSourceMAC() throws Exception {
            userStateStub.setupWithNoPermissions();
            userStateStub.addPermissions((short) 77,
                FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS,
                FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS);

            ResultActions resultActions = mockMvc.perform(get(getCountsUrlBase("MAC"))
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .header("X-User-IP", "192.168.1.100")
                .contentType(MediaType.APPLICATION_JSON));

            resultActions.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.summaries").doesNotExist());
        }

        @Test
        @DisplayName("AC2: Get draft account counts filters on source=VPFPO")
        @JiraStory("PO-10308")
        @JiraEpic("PO-7459")
        void get_counts_filtersSourcevpfpo() throws Exception {
            userStateStub.setupWithNoPermissions();
            userStateStub.addPermissions((short) 77,
                FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS,
                FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS);

            ResultActions resultActions = mockMvc.perform(get(getCountsUrlBase("VPFPO"))
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .header("X-User-IP", "192.168.1.100")
                .contentType(MediaType.APPLICATION_JSON));

            resultActions.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.summaries").doesNotExist());
        }

        @Test
        @DisplayName("AC5: Get draft account counts returns all when source is missing")
        @JiraStory("PO-10308")
        @JiraEpic("PO-7459")
        void get_counts_filtersSourceDefault() throws Exception {
            userStateStub.setupWithNoPermissions();
            userStateStub.addPermissions((short) 77,
                FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS,
                FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS);

            ResultActions resultActions = mockMvc.perform(get(URL_BASE + RESTRICT_PARAM)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .header("X-User-IP", "192.168.1.100")
                .contentType(MediaType.APPLICATION_JSON));

            resultActions.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(4))
                .andExpect(jsonPath("$.summaries").doesNotExist());
        }

        @Test
        @DisplayName("AC1: POST draft account allows null source_reference when source=MAC")
        @JiraStory("PO-10351")
        @JiraEpic("PO-7459")
        void post_allowsNullSourceRefWhenSourceIsMac() throws Exception {
            String validRequestBody = validRawJsonCreateRequestBody(DraftAccountSource.MAC, null);

            ResultActions resultActions = mockMvc.perform(post(URL_BASE)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequestBody));

            String body = resultActions.andReturn().getResponse().getContentAsString();
            DraftAccountResponseDto response = objectMapper.readValue(body, new TypeReference<>() {
            });

            resultActions.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            DraftAccountEntity createdEntity = getDraftAccount(response.getDraftAccountId());
            assertThat(createdEntity.getSource()).isEqualTo(DraftAccountSource.MAC);
            assertThat(createdEntity.getSourceReference()).isNull();

            // Cleanup
            draftAccountRepository.deleteById(response.getDraftAccountId());
        }

        @Test
        @DisplayName("AC2: POST draft account allows source_reference when source=VPFPO")
        @JiraStory("PO-10351")
        @JiraEpic("PO-7459")
        void post_acceptsSourceAndSourceRef() throws Exception {
            String validRequestBody = validRawJsonCreateRequestBody(DraftAccountSource.VPFPO, 101L);

            ResultActions resultActions = mockMvc.perform(post(URL_BASE)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequestBody));

            String body = resultActions.andReturn().getResponse().getContentAsString();
            DraftAccountResponseDto response = objectMapper.readValue(body, new TypeReference<>() {
            });

            resultActions.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            DraftAccountEntity createdEntity = getDraftAccount(response.getDraftAccountId());
            assertThat(createdEntity.getSource()).isEqualTo(DraftAccountSource.VPFPO);
            assertThat(createdEntity.getSourceReference()).isEqualTo(101L);

            // Cleanup
            draftAccountRepository.deleteById(response.getDraftAccountId());
        }

        @Test
        @DisplayName("AC7: POST draft account fails when source is null")
        @JiraStory("PO-10351")
        @JiraEpic("PO-7459")
        void post_failsWhenSourceIsNull() throws Exception {
            String validRequestBody = validRawJsonCreateRequestBody(null, 101L);

            ResultActions resultActions = mockMvc.perform(post(URL_BASE)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequestBody));

            resultActions.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("The request does not conform to the required JSON schema"));
        }

        @Test
        @DisplayName("AC2: POST draft account fails when source=VPFPO and source_ref is blank")
        @JiraStory("PO-10351")
        @JiraEpic("PO-7459")
        void post_failsonBlackSourceRefAndvpfo() throws Exception {
            String validRequestBody = validRawJsonCreateRequestBody(DraftAccountSource.VPFPO, null);

            ResultActions resultActions = mockMvc.perform(post(URL_BASE)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequestBody));

            resultActions.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail")
                    .value("source_reference cannot be blank when source is VPFPO"));
        }
    }

    @TestPropertySource(properties = {
        "launchdarkly.enabled=false",
        "launchdarkly.default-flag-values.release-1c-banking-interfaces=false"
    })
    @Nested
    class Release1CFlagOffTests {
        @Test
        @DisplayName("AC4: Get draft account returns only MAC accounts when source is missing")
        @JiraStory("PO-10308")
        @JiraEpic("PO-7459")
        void get_filtersSourceDefault() throws Exception {
            userStateStub.setupWithNoPermissions();
            userStateStub.addPermissions((short) 77,
                FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS,
                FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS);

            ResultActions resultActions = mockMvc.perform(get(URL_BASE)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .header("X-User-IP", "192.168.1.100")
                .contentType(MediaType.APPLICATION_JSON));

            resultActions.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.summaries.[0].draft_account_id").value(MAC_DRAFT_ACCOUNT_IDS[0]))
                .andExpect(jsonPath("$.summaries.[1].draft_account_id").value(MAC_DRAFT_ACCOUNT_IDS[1]));
        }

        @Test
        @DisplayName("AC4: Get draft account counts returns only MAC accounts when source is missing")
        @JiraStory("PO-10308")
        @JiraEpic("PO-7459")
        void get_counts_filtersSourceDefault() throws Exception {
            userStateStub.setupWithNoPermissions();
            userStateStub.addPermissions((short) 77,
                FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS,
                FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS);

            ResultActions resultActions = mockMvc.perform(get(URL_BASE + RESTRICT_PARAM)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .header("X-User-IP", "192.168.1.100")
                .contentType(MediaType.APPLICATION_JSON));

            resultActions.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.summaries").doesNotExist());
        }

        @Test
        @DisplayName("AC4: POST draft account defaults to source = MAC")
        @JiraStory("PO-10351")
        @JiraEpic("PO-7459")
        void post_defaultsToSourceMac() throws Exception {
            String validRequestBody = validRawJsonCreateRequestBody(null, null);

            ResultActions resultActions = mockMvc.perform(post(URL_BASE)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequestBody));

            String body = resultActions.andReturn().getResponse().getContentAsString();
            DraftAccountResponseDto response = objectMapper.readValue(body, new TypeReference<>() {
            });

            resultActions.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            DraftAccountEntity createdEntity = getDraftAccount(response.getDraftAccountId());
            assertThat(createdEntity.getSource()).isEqualTo(DraftAccountSource.MAC);
            assertThat(createdEntity.getSourceReference()).isNull();

            // Cleanup
            draftAccountRepository.deleteById(response.getDraftAccountId());
        }

        @Test
        @DisplayName("AC5: POST draft account accepts source and not source_ref")
        @JiraStory("PO-10351")
        @JiraEpic("PO-7459")
        void post_acceptsSourceOnly() throws Exception {
            String validRequestBody = validRawJsonCreateRequestBody(DraftAccountSource.MAC, null);

            ResultActions resultActions = mockMvc.perform(post(URL_BASE)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequestBody));

            String body = resultActions.andReturn().getResponse().getContentAsString();
            DraftAccountResponseDto response = objectMapper.readValue(body, new TypeReference<>() {
            });

            resultActions.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            DraftAccountEntity createdEntity = getDraftAccount(response.getDraftAccountId());
            assertThat(createdEntity.getSource()).isEqualTo(DraftAccountSource.MAC);
            assertThat(createdEntity.getSourceReference()).isNull();

            // Cleanup
            draftAccountRepository.deleteById(response.getDraftAccountId());
        }

        @Test
        @DisplayName("AC6: POST draft account accepts source and source_ref")
        @JiraStory("PO-10351")
        @JiraEpic("PO-7459")
        void post_acceptsSourceRefAndSource() throws Exception {
            String validRequestBody = validRawJsonCreateRequestBody(DraftAccountSource.VPFPO, 101L);

            ResultActions resultActions = mockMvc.perform(post(URL_BASE)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequestBody));

            String body = resultActions.andReturn().getResponse().getContentAsString();
            DraftAccountResponseDto response = objectMapper.readValue(body, new TypeReference<>() {
            });

            resultActions.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            DraftAccountEntity createdEntity = getDraftAccount(response.getDraftAccountId());
            assertThat(createdEntity.getSource()).isEqualTo(DraftAccountSource.VPFPO);
            assertThat(createdEntity.getSourceReference()).isEqualTo(101L);

            // Cleanup
            draftAccountRepository.deleteById(response.getDraftAccountId());
        }

        @Test
        @DisplayName("AC2: POST draft account fails when source=VPFPO and source_ref is blank")
        @JiraStory("PO-10351")
        @JiraEpic("PO-7459")
        void post_failsonBlackSourceRefAndvpfpo() throws Exception {
            String validRequestBody = validRawJsonCreateRequestBody(DraftAccountSource.VPFPO, null);

            ResultActions resultActions = mockMvc.perform(post(URL_BASE)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequestBody));

            resultActions.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail")
                    .value("source_reference cannot be blank when source is VPFPO"));
        }
    }

    private static String getUrlBase(String source) {
        return String.format("%s?source=%s", URL_BASE, source);
    }

    private static String getCountsUrlBase(String source) {
        return String.format("%s%s&source=%s", URL_BASE, RESTRICT_PARAM, source);
    }

    private DraftAccountEntity getDraftAccount(long draftAccountId) {
        return draftAccountRepository.findById(draftAccountId).orElseThrow();
    }

    private String validRawJsonCreateRequestBody(DraftAccountSource source, Long sourceReference) {
        AddDraftAccountRequestDto dto = AddDraftAccountRequestDto.builder()
            .businessUnitId((short) 78)
            .account(validAccountJsonString())
            .accountType(DraftAccountType.FINE)
            .source(source)
            .sourceReference(sourceReference)
            .build();

        try {
            return objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize AddDraftAccountRequestDto", e);
        }
    }

    private static String validAccountJsonString() {
        return """
            {
              "account_type": "Fine",
              "defendant_type": "Adult",
              "originator_name": "Draft Account Validation LJA",
              "originator_id": 32001,
              "originator_type": "NEW",
              "enforcement_court_id": 260000000048,
              "payment_card_request": true,
              "account_sentence_date": "2023-12-01",
              "defendant": {
                "company_flag": false,
                "surname": "LNAME",
                "address_line_1": "123 Main Street"
              },
              "offences": [],
              "payment_terms": {
                "payment_terms_type_code": "P",
                "effective_date": "2023-11-01",
                "instalment_period": "M",
                "lump_sum_amount": 1000.00,
                "instalment_amount": 200.00,
                "default_days_in_jail": 5
              }
            }
            """;
    }

}
