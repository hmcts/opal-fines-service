package uk.gov.hmcts.opal.controllers.till;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS;
import static uk.gov.hmcts.opal.entity.TillStatusEnum.Created;
import static uk.gov.hmcts.opal.entity.TillStatusEnum.Failed;
import static uk.gov.hmcts.opal.entity.TillStatusEnum.Processing;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_PAYMENT;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_PAYMENT_ENABLED_PROPERTY;

import jakarta.persistence.QueryTimeoutException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.jms.UncategorizedJmsException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationWithSecurityTest;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.TillStatusEnum;
import uk.gov.hmcts.opal.generated.model.TillsAllocateItem;
import uk.gov.hmcts.opal.generated.model.TillsAllocateRequest;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.repository.TillRepository;
import uk.gov.hmcts.opal.service.messaging.TillQueuePublisher;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1c-payment=true",
    "launchdarkly.default-flag-values.is-legacy-mode=false"
})
class TillAllocationIntegrationTest extends AbstractIntegrationWithSecurityTest {

    private static final String URL = "/tills/allocate";
    private static final short FIRST_BU = 78;
    private static final short SECOND_BU = 77;

    @MockitoSpyBean
    private TillRepository tillRepository;
    @Autowired
    private BusinessUnitRepository businessUnitRepository;
    @MockitoBean
    private TillQueuePublisher publisher;
    @MockitoSpyBean
    private FeatureToggleApi featureToggleApi;

    private final List<Long> tillIds = new ArrayList<>();
    private Long createdId;
    private Long failedId;

    @BeforeEach
    void setUp() {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions(FIRST_BU, PROCESS_AND_ALLOCATE_PAYMENTS);
        userStateStub.addPermissions(SECOND_BU, PROCESS_AND_ALLOCATE_PAYMENTS);
        createdId = createTill(FIRST_BU, Created);
        failedId = createTill(SECOND_BU, Failed);
    }

    @AfterEach
    void cleanUp() {
        tillRepository.deleteAllById(tillIds);
    }

    @Test
    @JiraStory("PO-3423")
    @JiraEpic("PO-2116")
    void allocatesCreatedAndFailedTills() throws Exception {
        allocate(item(createdId, FIRST_BU), item(failedId, SECOND_BU))
            .andExpect(status().isOk()).andExpect(content().string(""));

        assertStatus(createdId, Processing);
        assertStatus(failedId, Processing);
        verify(publisher).publish(List.of(createdId, failedId));
        verify(featureToggleApi).isFeatureEnabledWithPropertyValueDefault(
            RELEASE_1C_PAYMENT, RELEASE_1C_PAYMENT_ENABLED_PROPERTY, false);
    }

    @Test
    @JiraStory("PO-3423")
    @JiraEpic("PO-2116")
    void publicationFailureRollsBackStatuses() throws Exception {
        doThrow(new UncategorizedJmsException("Queue unavailable"))
            .when(publisher).publish(List.of(createdId, failedId));

        allocate(item(createdId, FIRST_BU), item(failedId, SECOND_BU))
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.retriable").value(true))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/message-queue-unavailable"));

        assertStatus(createdId, Created);
        assertStatus(failedId, Failed);
    }

    @Test
    @JiraStory("PO-3423")
    @JiraEpic("PO-2116")
    void repeatedRequestDoesNotPublishAgain() throws Exception {
        allocate(item(createdId, FIRST_BU)).andExpect(status().isOk());
        allocate(item(createdId, FIRST_BU)).andExpect(status().isConflict());

        assertStatus(createdId, Processing);
        verify(publisher).publish(List.of(createdId));
    }

    @ParameterizedTest
    @MethodSource("databaseFailures")
    @JiraStory("PO-3423")
    @JiraEpic("PO-2116")
    void databaseFailurePreventsPublication(RuntimeException failure, int expectedStatus) throws Exception {
        doThrow(failure).when(tillRepository).flush();

        allocate(item(createdId, FIRST_BU), item(failedId, SECOND_BU))
            .andExpect(status().is(expectedStatus))
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

        assertUnchanged();
    }

    private static Stream<Arguments> databaseFailures() {
        return Stream.of(
            Arguments.of(new QueryTimeoutException("Database timeout"), 408),
            Arguments.of(new DataAccessResourceFailureException("Database unavailable"), 503),
            Arguments.of(new JpaSystemException(new IllegalStateException("Persistence failure")), 500));
    }

    @Test
    @JiraStory("PO-3423")
    @JiraEpic("PO-2116")
    void requiresPermissionForEveryBusinessUnit() throws Exception {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions(FIRST_BU, PROCESS_AND_ALLOCATE_PAYMENTS);

        allocate(item(createdId, FIRST_BU), item(failedId, SECOND_BU)).andExpect(status().isForbidden());

        assertUnchanged();
    }

    @Test
    @JiraStory("PO-3423")
    @JiraEpic("PO-2116")
    void rejectsMissingToken() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(request(item(createdId, FIRST_BU))))
            .andExpect(status().isUnauthorized());

        assertUnchanged();
    }

    @Test
    @JiraStory("PO-3423")
    @JiraEpic("PO-2116")
    void rejectsExpiredToken() throws Exception {
        mockMvc.perform(post(URL).header("Authorization", "Bearer " + expiredToken)
                .contentType(MediaType.APPLICATION_JSON).content(request(item(createdId, FIRST_BU))))
            .andExpect(status().isUnauthorized());

        assertUnchanged();
    }

    private Long createTill(short businessUnitId, TillStatusEnum initialStatus) {
        TillEntity till = tillRepository.saveAndFlush(TillEntity.builder()
            .businessUnit(businessUnitRepository.findById(businessUnitId).orElseThrow())
            .tillNumber((short) (3423 + tillIds.size()))
            .ownedBy("L078JG")
            .status(initialStatus)
            .createdDate(LocalDateTime.of(2026, 9, 30, 10, 0))
            .build());
        tillIds.add(till.getTillId());
        return till.getTillId();
    }

    private ResultActions allocate(TillsAllocateItem... items) throws Exception {
        return mockMvc.perform(post(URL).with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .contentType(MediaType.APPLICATION_JSON).content(request(items)));
    }

    private String request(TillsAllocateItem... items) {
        return objectMapper.writeValueAsString(TillsAllocateRequest.builder().tills(List.of(items)).build());
    }

    private static TillsAllocateItem item(Long id, short businessUnitId) {
        return TillsAllocateItem.builder().tillId(id).businessUnitId(businessUnitId).build();
    }

    private void assertStatus(Long id, TillStatusEnum expected) {
        assertThat(tillRepository.findById(id).orElseThrow().getStatus()).isEqualTo(expected);
    }

    private void assertUnchanged() {
        assertStatus(createdId, Created);
        assertStatus(failedId, Failed);
        verifyNoInteractions(publisher);
    }
}
