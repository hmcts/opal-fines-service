package uk.gov.hmcts.opal.service.opal;

import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.orm.jpa.JpaSystemException;
import uk.gov.hmcts.opal.common.logging.LogUtil;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.entity.draft.TimelineData;
import uk.gov.hmcts.opal.service.opal.jpa.DraftAccountTransactional;

@ExtendWith(MockitoExtension.class)
class DraftAccountPublishTest {

    @Mock
    private DraftAccountTransactional draftAccountTransactional;

    @Mock
    private BusinessUnitUser unitUser;

    private Clock clock;
    private DraftAccountPublish draftAccountPublish;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-06-25T00:00:00Z"), ZoneOffset.UTC);
        draftAccountPublish = new DraftAccountPublish(draftAccountTransactional, clock);
    }

    @Test
    void publishDefendantAccount_success_delegatesAndReturnsTransactionalResult() {
        DraftAccountEntity publishEntity = createPendingDraft();
        DraftAccountEntity transactionalResult = createPublishedDraft();
        when(draftAccountTransactional.publishDefendantAccount(publishEntity)).thenReturn(transactionalResult);

        DraftAccountEntity result = draftAccountPublish.publishDefendantAccount(publishEntity, unitUser);

        assertSame(transactionalResult, result);
        verify(draftAccountTransactional).publishDefendantAccount(publishEntity);
        verify(draftAccountTransactional, never()).updateStatus(any(), any(), any());
    }

    @ParameterizedTest
    @MethodSource("publishDefendantAccountFailureCases")
    void publishDefendantAccount_failure_updatesFailureState(
        RuntimeException thrown,
        String operationId,
        String expectedUserId
    ) {
        when(unitUser.getBusinessUnitUserId()).thenReturn(expectedUserId);
        when(draftAccountTransactional.publishDefendantAccount(any(DraftAccountEntity.class)))
            .thenThrow(thrown);
        when(draftAccountTransactional.updateStatus(
            any(DraftAccountEntity.class),
            eq(DraftAccountStatus.PUBLISHING_FAILED),
            same(draftAccountTransactional)
        )).thenAnswer(invocation -> invocation.getArgument(0));
        DraftAccountEntity pending = createPendingDraft();
        try (MockedStatic<LogUtil> logUtilMock = Mockito.mockStatic(LogUtil.class)) {
            logUtilMock.when(LogUtil::getOrCreateOpalOperationId).thenReturn(operationId);

            DraftAccountEntity result = draftAccountPublish.publishDefendantAccount(pending, unitUser);

            ArgumentCaptor<DraftAccountEntity> captor = ArgumentCaptor.forClass(DraftAccountEntity.class);
            verify(draftAccountTransactional).updateStatus(
                captor.capture(),
                eq(DraftAccountStatus.PUBLISHING_FAILED),
                same(draftAccountTransactional)
            );
            DraftAccountEntity failedUpdate = captor.getValue();
            assertEquals(failedUpdate, result);
            assertEquals(pending.getDraftAccountId(), failedUpdate.getDraftAccountId());
            assertEquals(pending.getVersionNumber(), failedUpdate.getVersionNumber());
            assertEquals(LogUtil.ERRMSG_STORED_PROC_FAILURE, failedUpdate.getStatusMessage());
            TimelineData expectedTimeline = new TimelineData(pending.getTimelineData());
            assertEquals(expectedTimeline.toJson(), failedUpdate.getTimelineData());
            expectedTimeline.insertEntry(
                expectedUserId,
                DraftAccountStatus.PUBLISHING_FAILED.getLabel(),
                LocalDate.now(clock),
                LogUtil.ERRMSG_STORED_PROC_FAILURE + " Error code: [" + operationId + "]"
            );
        }
    }

    static Stream<Arguments> publishDefendantAccountFailureCases() {
        return Stream.of(
            Arguments.of(
                new JpaSystemException(new RuntimeException("SQL Stored Procedure Error.")),
                "1234",
                "Dave"
            ),
            Arguments.of(
                new InvalidDataAccessApiUsageException("bad request"),
                "5678",
                "Dave"
            )
        );
    }

    private DraftAccountEntity createPendingDraft() {
        TimelineData timelineData = new TimelineData();
        timelineData.insertEntry(
            "SUBMITTER",
            DraftAccountStatus.SUBMITTED.getLabel(),
            LocalDate.of(2026, 6, 22),
            null
        );

        DraftAccountEntity entity = new DraftAccountEntity();
        entity.setDraftAccountId(9_999_901L);
        entity.setVersionNumber(0L);
        entity.setTimelineData(timelineData.toJson());
        entity.setAccountStatus(DraftAccountStatus.PUBLISHING_PENDING);
        entity.setStatusMessage(null);
        entity.setAccountId(null);
        entity.setAccountNumber(null);
        return entity;
    }

    private DraftAccountEntity createPublishedDraft() {
        DraftAccountEntity entity = createPendingDraft();
        entity.setVersionNumber(1L);
        entity.setAccountStatus(DraftAccountStatus.PUBLISHED);
        entity.setAccountId(12345L);
        entity.setAccountNumber("DEF-12345");
        return entity;
    }

}
