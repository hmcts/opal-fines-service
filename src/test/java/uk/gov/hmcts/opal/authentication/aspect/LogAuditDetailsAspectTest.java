package uk.gov.hmcts.opal.authentication.aspect;


import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authentication.exception.MissingRequestHeaderException;
import uk.gov.hmcts.opal.authorisation.model.LogActions;
import uk.gov.hmcts.opal.authorisation.model.Permission;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddLogAuditDetailDto;
import uk.gov.hmcts.opal.disco.opal.LogAuditDetailService;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogAuditDetailsAspectTest {

    private static final UserState USER_STATE = UserState.builder()
        .userName("name")
        .userId(123L)
        .businessUnitUser(Set.of(BusinessUnitUser.builder()
                          .businessUnitId((short) 123)
                          .businessUnitUserId("BU123")
                          .permissions(Set.of(
                              Permission.builder()
                                  .permissionId(1L)
                                  .permissionName("Notes")
                                  .build()))
                          .build()))
        .build();

    @Mock
    private UserStateAspectService userStateAspectService;

    @Mock
    private LogAuditDetailService logAuditDetailService;

    @InjectMocks
    private LogAuditDetailsAspect logAuditDetailsAspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private LogAuditDetail logAuditDetail;

    private Object[] args;

    @BeforeEach
    void setUp() {
        args = new Object[]{};
    }

    @Nested
    class WriteLogAuditDetail {


        @Test
        void writeLogAuditDetail_shouldProceedAndLogAuditDetails() throws Throwable {

            when(userStateAspectService.getUserState(any())).thenReturn(USER_STATE);
            when(logAuditDetail.action()).thenReturn(LogActions.LOG_IN);
            when(logAuditDetail.defaultJsonRequest()).thenReturn("{}");

            Object expectedReturnValue = new Object();
            when(joinPoint.proceed()).thenReturn(expectedReturnValue);

            Object returnValue = logAuditDetailsAspect.writeLogAuditDetail(joinPoint, logAuditDetail);

            verify(logAuditDetailService).writeLogAuditDetail(any(AddLogAuditDetailDto.class));
            assertEquals(expectedReturnValue, returnValue);
        }

        @Test
        void writeLogAuditDetail_shouldHandleMissingRequestHeaderExceptionGracefully() throws Throwable {
            when(userStateAspectService.getUserState(any()))
                .thenThrow(new MissingRequestHeaderException("Authorization"));
            when(logAuditDetail.action()).thenReturn(LogActions.LOG_IN);

            Object expectedReturnValue = new Object();
            when(joinPoint.proceed()).thenReturn(expectedReturnValue);

            Object returnValue = logAuditDetailsAspect.writeLogAuditDetail(joinPoint, logAuditDetail);

            verify(logAuditDetailService, never()).writeLogAuditDetail(any(AddLogAuditDetailDto.class));
            verify(joinPoint).proceed();
            assertEquals(expectedReturnValue, returnValue);
        }

        @Test
        void writeLogAuditDetail_shouldHandleGeneralExceptionGracefully() throws Throwable {
            when(userStateAspectService.getUserState(joinPoint)).thenThrow(new RuntimeException("Test Exception"));
            when(logAuditDetail.action()).thenReturn(LogActions.LOG_OUT);

            Object expectedReturnValue = new Object();
            when(joinPoint.proceed()).thenReturn(expectedReturnValue);

            Object returnValue = logAuditDetailsAspect.writeLogAuditDetail(joinPoint, logAuditDetail);

            verify(logAuditDetailService, never()).writeLogAuditDetail(any(AddLogAuditDetailDto.class));
            verify(joinPoint).proceed();
            assertEquals(expectedReturnValue, returnValue);
        }

    }

    @Nested
    class WriteAuditLog {


        @Test
        void writeAuditLog_shouldWriteAuditLog() {

            when(userStateAspectService.getUserState(joinPoint)).thenReturn(USER_STATE);
            when(logAuditDetail.action()).thenReturn(LogActions.LOG_IN);
            when(logAuditDetail.defaultJsonRequest()).thenReturn("{}");

            logAuditDetailsAspect.writeAuditLog(joinPoint, logAuditDetail);

            ArgumentCaptor<AddLogAuditDetailDto> captor = ArgumentCaptor.forClass(AddLogAuditDetailDto.class);
            verify(logAuditDetailService).writeLogAuditDetail(captor.capture());

            AddLogAuditDetailDto capturedDto = captor.getValue();
            assertEquals(LogActions.LOG_IN, capturedDto.getLogAction());
            assertEquals(123, capturedDto.getUserId());
            assertEquals("{}", capturedDto.getJsonRequest());
        }

        @Test
        void writeAuditLog_shouldHandleMissingRequestHeaderExceptionGracefully() {
            when(userStateAspectService.getUserState(joinPoint))
                .thenThrow(new MissingRequestHeaderException("Authorization"));
            when(logAuditDetail.action()).thenReturn(LogActions.LOG_IN);

            logAuditDetailsAspect.writeAuditLog(joinPoint, logAuditDetail);

            verify(logAuditDetailService, never()).writeLogAuditDetail(any(AddLogAuditDetailDto.class));
        }

        @Test
        void writeAuditLog_shouldHandleGeneralExceptionGracefully() {
            when(userStateAspectService.getUserState(joinPoint)).thenThrow(new RuntimeException("Test Exception"));
            when(logAuditDetail.action()).thenReturn(LogActions.LOG_IN);

            logAuditDetailsAspect.writeAuditLog(joinPoint, logAuditDetail);

            verify(logAuditDetailService, never()).writeLogAuditDetail(any(AddLogAuditDetailDto.class));
        }
    }
}
