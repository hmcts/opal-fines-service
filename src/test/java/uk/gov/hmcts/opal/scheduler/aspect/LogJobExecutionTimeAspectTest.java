package uk.gov.hmcts.opal.scheduler.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;

import java.util.Date;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogJobExecutionTimeAspectTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private JobExecutionContext jobExecutionContext;

    @Mock
    private JobDetail jobDetail;

    @Mock
    private JobKey jobKey;

    @InjectMocks
    private LogJobExecutionTimeAspect logJobExecutionTimeAspect;

    @BeforeEach
    void setUp() {
        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
        when(jobDetail.getKey()).thenReturn(jobKey);
        when(jobKey.getName()).thenReturn("TestJob");
        when(jobExecutionContext.getFireTime()).thenReturn(new Date());
        when(jobExecutionContext.getNextFireTime()).thenReturn(new Date(System.currentTimeMillis() + 10000));
    }

    @Test
    void logExecutionTime_shouldLogExecutionTime() throws Throwable {
        // Given
        when(joinPoint.getArgs()).thenReturn(new Object[]{jobExecutionContext});
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            Thread.sleep(50); // Simulate job execution time
            return null;
        });

        // When
        logJobExecutionTimeAspect.logExecutionTime(joinPoint);

        // Then
        verify(joinPoint, times(1)).proceed();
    }
}
