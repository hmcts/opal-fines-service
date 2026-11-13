package uk.gov.hmcts.opal.controllers.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.core.type.TypeReference;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.common.user.authorisation.client.mapper.UserStateMapper;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;
import uk.gov.hmcts.opal.common.user.authorisation.model.Domain;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.controllers.shared.util.UserStateUtil;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateFileItemTestingSupport;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateItemTestingSupport;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateRequestTestingSupport;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateResponseTestingSupport;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsFileSourceTestingSupport;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsJobStatusTestingSupport;
import uk.gov.hmcts.opal.repository.AllocationRepository;
import uk.gov.hmcts.opal.repository.AmendmentRepository;
import uk.gov.hmcts.opal.repository.ChequeRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountPartiesRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.DefendantTransactionRepository;
import uk.gov.hmcts.opal.repository.DocumentInstanceRepository;
import uk.gov.hmcts.opal.repository.ImpositionRepository;
import uk.gov.hmcts.opal.repository.InterfaceFileRepository;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.PaymentTermsRepository;
import uk.gov.hmcts.opal.repository.ReportEntryRepository;
import uk.gov.hmcts.opal.repository.ReportRepository;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.TestingSupportControllerTest")
@SuppressWarnings("java:S1874")
class TestingSupportControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AllocationRepository allocationRepository;

    @Autowired
    private AmendmentRepository amendmentRepository;

    @Autowired
    private ChequeRepository chequeRepository;

    @Autowired
    private DefendantAccountPartiesRepository defendantAccountPartiesRepository;

    @Autowired
    private DefendantAccountRepository defendantAccountRepository;

    @Autowired
    private DefendantTransactionRepository defendantTransactionRepository;

    @Autowired
    private DocumentInstanceRepository documentInstanceRepository;

    @Autowired
    private ImpositionRepository impositionRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private PaymentTermsRepository paymentTermsRepository;

    @Autowired
    private ReportEntryRepository reportEntryRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private InterfaceJobRepository interfaceJobRepository;

    @Autowired
    private InterfaceFileRepository interfaceFileRepository;

    @MockitoBean
    private DynamicConfigService dynamicConfigService;

    @MockitoBean
    private FeatureToggleApi featureToggleApi;

    @MockitoBean
    private UserStateClientService userStateClientService;

    @MockitoBean
    private UserStateMapper userStateMapper;

    @Test
    @JiraStory("PO-256")
    @JiraEpic("PO-2233")
    @JiraTestKey("PO-6279")
    void testIsLegacyMode() throws Exception {
        when(dynamicConfigService.isLegacyMode()).thenReturn(true);

        mockMvc.perform(get("/testing-support/is-legacy-mode"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").value(true));
    }

    @Test
    @JiraStory("PO-256")
    @JiraEpic("PO-2233")
    @JiraTestKey("PO-6276")
    void testIsFeatureEnabled() throws Exception {
        when(featureToggleApi.isFeatureEnabled(anyString())).thenReturn(true);

        mockMvc.perform(get("/testing-support/launchdarkly/bool/testFeature"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isBoolean());
    }

    @Test
    @JiraStory("PO-256")
    @JiraEpic("PO-2233")
    @JiraTestKey("PO-6278")
    void testGetFeatureValue() throws Exception {
        String featureValue = "testValue";
        when(featureToggleApi.getFeatureValue(anyString(), anyString())).thenReturn(featureValue);

        mockMvc.perform(get("/testing-support/launchdarkly/string/testFeature"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(featureValue));
    }

    @Test
    @JiraStory("PO-256")
    @JiraEpic("PO-2233")
    @JiraTestKey("PO-6277")
    void testParseToken() throws Exception {
        mockMvc.perform(get("/testing-support/token/parse")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("Authorization", userStateStub.getBearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value("opal-test@HMCTS.NET"));
    }

    @Test
    @JiraStory("PO-256")
    @JiraEpic("PO-2233")
    @JiraTestKey("PO-6282")
    void testGetUserState() throws Exception {
        UserState userState = UserStateUtil.permissionUser((short) 5, FinesPermission.ACCOUNT_ENQUIRY);
        UserStateV2 userStateV2 = mock(UserStateV2.class);
        when(userStateClientService.getUserStateByAuthenticatedUser()).thenReturn(Optional.of(userStateV2));
        when(userStateMapper.toUserState(userStateV2, Domain.FINES)).thenReturn(userState);

        mockMvc.perform(get("/testing-support/user-client/0"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.user_name").value(userState.getUserName()))
            .andExpect(jsonPath("$.user_id").value(userState.getUserId()));
    }

    @Test
    @JiraStory("PO-256")
    @JiraEpic("PO-2233")
    @JiraTestKey("PO-6280")
    void testGetUserStateNotFound() throws Exception {
        mockMvc.perform(get("/testing-support/user-client/999"))
            .andExpect(status().isNotFound());
    }

    @Sql(
        scripts = "classpath:db/insertData/insert_into_defendants_for_deletion_test.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Test
    @JiraStory("PO-1772")
    @JiraStory("PO-1777")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6281")
    void shouldDeleteDefendantAccountAndAssociatedData() throws Exception {
        long defendantAccountId = 1001L;
        String associatedRecordId = String.valueOf(defendantAccountId);
        String impositionAssociatedRecordId = "9105";

        // Pre-check that data exists
        assertThat(defendantAccountRepository.existsById(defendantAccountId)).isTrue();
        assertThat(defendantAccountPartiesRepository.countByDefendantAccount_DefendantAccountId(defendantAccountId))
            .isPositive();
        assertThat(paymentTermsRepository.countByDefendantAccount_DefendantAccountId(defendantAccountId)).isPositive();
        assertThat(reportRepository.existsById("10001")).isTrue();
        assertThat(reportEntryRepository.countByAssociatedRecordId(associatedRecordId)).isPositive();
        assertThat(defendantTransactionRepository.countByDefendantAccountId(defendantAccountId)).isPositive();
        assertThat(impositionRepository.countByDefendantAccountId(defendantAccountId)).isPositive();
        assertThat(documentInstanceRepository.countByAssociatedRecordTypeAndAssociatedRecordId(
            AssociatedRecordType.DEFENDANT_ACCOUNTS.getLabel(),
            associatedRecordId
        )).isPositive();
        assertThat(documentInstanceRepository.countByAssociatedRecordTypeAndAssociatedRecordId(
            AssociatedRecordType.IMPOSITIONS.getLabel(),
            impositionAssociatedRecordId
        )).isPositive();
        assertThat(noteRepository.countByAssociatedRecordId(associatedRecordId)).isPositive();
        assertThat(amendmentRepository.countByAssociatedRecordId(associatedRecordId)).isPositive();
        assertThat(allocationRepository.countByImposition_DefendantAccountId(defendantAccountId)).isPositive();
        assertThat(countChequesByDefendantAccountId(defendantAccountId)).isPositive();

        // When: call the deletion endpoint
        ResultActions actions = mockMvc.perform(delete("/testing-support/defendant-accounts/1001"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":shouldDeleteDefendantAccountAndAssociatedData: Response body:\n"
                 + ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isNoContent());

        // Post-check that all related data is gone
        assertThat(defendantAccountRepository.existsById(defendantAccountId)).isFalse();
        assertThat(defendantAccountPartiesRepository.countByDefendantAccount_DefendantAccountId(defendantAccountId))
            .isZero();
        assertThat(paymentTermsRepository.countByDefendantAccount_DefendantAccountId(defendantAccountId)).isZero();
        assertThat(reportRepository.existsById("10001")).as("Rows in Reports should not be deleted").isTrue();
        assertThat(reportEntryRepository.countByAssociatedRecordId(associatedRecordId)).isZero();
        assertThat(defendantTransactionRepository.countByDefendantAccountId(defendantAccountId)).isZero();
        assertThat(impositionRepository.countByDefendantAccountId(defendantAccountId)).isZero();
        assertThat(documentInstanceRepository.countByAssociatedRecordTypeAndAssociatedRecordId(
            AssociatedRecordType.DEFENDANT_ACCOUNTS.getLabel(),
            associatedRecordId
        )).isZero();
        assertThat(documentInstanceRepository.countByAssociatedRecordTypeAndAssociatedRecordId(
            AssociatedRecordType.IMPOSITIONS.getLabel(),
            impositionAssociatedRecordId
        )).isZero();
        assertThat(noteRepository.countByAssociatedRecordId(associatedRecordId)).isZero();
        assertThat(amendmentRepository.countByAssociatedRecordId(associatedRecordId)).isZero();
        assertThat(allocationRepository.countByImposition_DefendantAccountId(defendantAccountId)).isZero();
        assertThat(countChequesByDefendantAccountId(defendantAccountId)).isZero();
    }

    private long countChequesByDefendantAccountId(long defendantAccountId) {
        List<Long> transactionIds = defendantTransactionRepository
            .findDefendantAccountTransactionIdsByDefendantAccountId(defendantAccountId);
        return transactionIds.isEmpty() ? 0 : chequeRepository.countByDefendantTransactionIdIn(transactionIds);
    }

    @Test
    @JiraStory("PO-11124")
    @JiraEpic("PO-3497")
    @DisplayName("PO-11124 - TESTING SUPPORT - Should successfully return IDS after adding interface job and file")
    void shouldCorrectlyReturnIdsWhenCreatingInterfaceJobs() throws Exception {
        // Arrange
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions((short) 77, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);

        InterfaceJobsCreateFileItemTestingSupport fileItem = InterfaceJobsCreateFileItemTestingSupport.builder()
            .fileName("file-name.dat")
            .overrideInhibits(true)
            .recordCount((short)4)
            .source(InterfaceJobsFileSourceTestingSupport.CDER)
            .totalAmount(BigDecimal.valueOf(200L))
            .build();

        InterfaceJobsCreateItemTestingSupport jobItem = InterfaceJobsCreateItemTestingSupport.builder()
            .businessUnitId((short)77)
            .interfaceFile(fileItem)
            .interfaceName("an interface")
            .status(InterfaceJobsJobStatusTestingSupport.CREATED)
            .createdDatetime(LocalDateTime.now())
            .build();

        InterfaceJobsCreateRequestTestingSupport request = InterfaceJobsCreateRequestTestingSupport.builder()
            .interfaceJobs(List.of(jobItem))
            .build();

        // Act
        ResultActions result = mockMvc.perform(post("/testing-support/interface-jobs")
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)));

        // Assert
        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        String body = result.andReturn().getResponse().getContentAsString();
        InterfaceJobsCreateResponseTestingSupport response = objectMapper.readValue(body, new TypeReference<>() {});
        assertThat(response.getInterfaceJobs()).singleElement();
        assertThat(response.getInterfaceJobs().getFirst().getInterfaceJobId()).isNotNull();
        assertThat(response.getInterfaceJobs().getFirst().getInterfaceFileId()).isNotNull();
        assertThat(interfaceJobRepository.existsById(response.getInterfaceJobs().getFirst().getInterfaceJobId()));
        assertThat(interfaceFileRepository.existsById(response.getInterfaceJobs().getFirst().getInterfaceFileId()));
    }

    @Test
    @JiraStory("PO-11124")
    @JiraEpic("PO-3497")
    @DisplayName("PO-11124 - TESTING SUPPORT - Should return 403 without allocate and process payments permission")
    void shouldReturnForbiddenWithoutProcessAndAllocatePaymentsPermission_403() throws Exception {
        // Arrange
        userStateStub.setupWithNoPermissions();

        InterfaceJobsCreateFileItemTestingSupport fileItem = InterfaceJobsCreateFileItemTestingSupport.builder()
            .fileName("file-name.dat")
            .overrideInhibits(true)
            .recordCount((short)4)
            .source(InterfaceJobsFileSourceTestingSupport.CDER)
            .totalAmount(BigDecimal.valueOf(200L))
            .build();

        InterfaceJobsCreateItemTestingSupport jobItem = InterfaceJobsCreateItemTestingSupport.builder()
            .businessUnitId((short)77)
            .interfaceFile(fileItem)
            .interfaceName("an interface")
            .status(InterfaceJobsJobStatusTestingSupport.CREATED)
            .createdDatetime(LocalDateTime.now())
            .build();

        InterfaceJobsCreateRequestTestingSupport request = InterfaceJobsCreateRequestTestingSupport.builder()
            .interfaceJobs(List.of(jobItem))
            .build();

        // Act
        ResultActions result = mockMvc.perform(post("/testing-support/interface-jobs")
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)));

        // Assert
        result.andExpect(status().isForbidden());
    }
}
