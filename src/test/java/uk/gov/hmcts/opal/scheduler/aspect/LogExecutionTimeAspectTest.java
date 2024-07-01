package uk.gov.hmcts.opal.scheduler.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LogExecutionTimeAspectTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    @InjectMocks
    private LogExecutionTimeAspect logExecutionTimeAspect;

    @Test
    void logExecutionTime_shouldLogExecutionTime() throws Throwable {

        logExecutionTimeAspect.logExecutionTime(joinPoint);

        verify(joinPoint, times(1)).proceed();
    }
}
