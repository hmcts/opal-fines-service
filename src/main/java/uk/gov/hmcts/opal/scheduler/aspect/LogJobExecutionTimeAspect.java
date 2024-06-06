package uk.gov.hmcts.opal.scheduler.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
public class LogJobExecutionTimeAspect {

    @Around("@annotation(LogJobExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        JobExecutionContext context = (JobExecutionContext) joinPoint.getArgs()[0];
        String jobName = context.getJobDetail().getKey().getName();

        log.info("Job '{}' started as per fire time {}", jobName, context.getFireTime());

        long startTime = System.nanoTime();
        Object result = joinPoint.proceed();
        long endTime = System.nanoTime();

        long executionTime = endTime - startTime;

        log.info("Job '{}' executed in {} ms, next fire time: {}",
                 jobName, TimeUnit.NANOSECONDS.toMillis(executionTime), context.getNextFireTime()
        );

        return result;
    }
}
