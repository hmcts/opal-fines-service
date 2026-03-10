package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.entity.draft.StoredProcedureNames.DEF_ACC_ID;
import static uk.gov.hmcts.opal.entity.draft.StoredProcedureNames.DEF_ACC_NO;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.jpa.JpaSystemException;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.entity.draft.TimelineData;
import uk.gov.hmcts.opal.mapper.DraftAccountMapper;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.repository.DraftAccountRepository;
import uk.gov.hmcts.opal.service.opal.jpa.DraftAccountTransactional;
import uk.gov.hmcts.opal.util.LogUtil;

@ExtendWith(MockitoExtension.class)
class DraftAccountPublishTest {

    DraftAccountMapper mapper = Mappers.getMapper(DraftAccountMapper.class);

    @Mock
    DraftAccountRepository draftRepository;

    @Mock
    BusinessUnitRepository businessRepository;

    private DraftAccountTransactional draftAccountTransactional;

    @InjectMocks
    private DraftAccountPublish draftAccountPublish;

    @BeforeEach
    void openMocks() throws Exception {
        draftAccountTransactional = spy(new DraftAccountTransactional(draftRepository, businessRepository));
        injectDraftTransactionsService(draftAccountPublish, draftAccountTransactional);
    }

    private void injectDraftTransactionsService(
        DraftAccountPublish draftAccountPublish, DraftAccountTransactional draftAccountTransactional)
        throws NoSuchFieldException, IllegalAccessException {

        Field field = DraftAccountPublish.class.getDeclaredField("draftAccountTransactional");
        field.setAccessible(true);
        field.set(draftAccountPublish, draftAccountTransactional);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testPublishDefendantAccount_success() {
        DraftAccountEntity existingFromDB = createPendingDraft();
        when(draftRepository.findById(any())).thenReturn(Optional.of(existingFromDB));
        when(draftRepository.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);

        Map<String, Object> procResponse = Map.of(DEF_ACC_NO, "ACC-008", DEF_ACC_ID, Long.valueOf(8));
        when(draftRepository.createDefendantAccount(anyLong(), anyShort(), any(), any()))
            .thenReturn(procResponse);

        BusinessUnitUser buu = createBUU();
        DraftAccountEntity pending = cloneAndModify(existingFromDB, null);
        DraftAccountEntity published = draftAccountPublish.publishDefendantAccount(pending, buu);

        assertEquals(8L, published.getAccountId());
        assertEquals("ACC-008", published.getAccountNumber());
        assertEquals(DraftAccountStatus.PUBLISHED, published.getAccountStatus());

        DraftAccountEntity expected = cloneAndModify(existingFromDB, DraftAccountStatus.PUBLISHED);
        expected.setAccountId(8L);
        expected.setAccountNumber("ACC-008");
        assertEquals(expected, published);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testPublishDefendantAccount_procFailure() {
        DraftAccountEntity existingFromDB = createPendingDraft();
        when(draftRepository.findById(any())).thenReturn(Optional.of(existingFromDB));
        when(draftRepository.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);

        when(draftRepository.createDefendantAccount(anyLong(), anyShort(), any(), any()))
            .thenThrow(new JpaSystemException(new RuntimeException("SQL Stored Procedure Error.")));

        BusinessUnitUser buu = createBUU();
        DraftAccountEntity pending = cloneAndModify(existingFromDB, null);
        DraftAccountEntity published = draftAccountPublish.publishDefendantAccount(pending, buu);

        assertNull(published.getAccountId());
        assertNull(published.getAccountNumber());
        assertEquals(DraftAccountStatus.PUBLISHING_FAILED, published.getAccountStatus());
        assertEquals(LogUtil.ERRMSG_STORED_PROC_FAILURE, published.getStatusMessage());

        TimelineData timelineData = new TimelineData();
        timelineData.insertEntry("Dave", DraftAccountStatus.PUBLISHING_FAILED.getLabel(),
            LocalDate.now(), LogUtil.ERRMSG_STORED_PROC_FAILURE);
        assertEquals(timelineData.toJson(), published.getTimelineData());

        DraftAccountEntity expected = cloneAndModify(existingFromDB, DraftAccountStatus.PUBLISHING_FAILED);
        assertEquals(expected, published);
    }

    private String emptyTimelineData() {
        return new TimelineData().toJson();
    }

    private BusinessUnitUser createBUU() {
        return BusinessUnitUser.builder()
            .businessUnitId((short)7)
            .businessUnitUserId("Dave")
            .build();
    }

    private DraftAccountEntity createPendingDraft() {
        return DraftAccountEntity.builder()
            .draftAccountId(1L)
            .businessUnit(
                BusinessUnitFullEntity.builder()
                    .businessUnitId((short)6)
                    .build())
            .timelineData(emptyTimelineData())
            .accountStatus(DraftAccountStatus.PUBLISHING_PENDING)
            .submittedBy("Dave")
            .versionNumber(6L)
            .build();
    }

    private DraftAccountEntity cloneAndModify(DraftAccountEntity original, DraftAccountStatus status) {
        DraftAccountEntity clone =  mapper.clone(original);
        Optional.ofNullable(status).ifPresent(clone::setAccountStatus);
        return clone;
    }

}
