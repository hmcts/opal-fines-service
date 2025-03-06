package uk.gov.hmcts.opal.authentication.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.authentication.exception.MissingRequestHeaderException;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddLogAuditDetailDto;
import uk.gov.hmcts.opal.service.opal.LogAuditDetailService;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LogAuditDetailsAspect {

    private final UserStateAspectService userStateAspectService;
    private final LogAuditDetailService logAuditDetailService;

    @Around("@annotation(logAuditDetail)")
    public Object writeLogAuditDetail(ProceedingJoinPoint joinPoint,
                                      LogAuditDetail logAuditDetail
    ) throws Throwable {
        writeAuditLog(joinPoint, logAuditDetail);
        return joinPoint.proceed();
    }

    public void writeAuditLog(ProceedingJoinPoint joinPoint, LogAuditDetail logAuditDetail) {
        try {
            UserState userState = userStateAspectService.getUserState(joinPoint);

            AddLogAuditDetailDto logAuditDetailDto = AddLogAuditDetailDto.builder()
                .logAction(logAuditDetail.action())
                .userId(userState.getUserId())
                .jsonRequest(logAuditDetail.defaultJsonRequest())
                .build();

            logAuditDetailService.writeLogAuditDetail(logAuditDetailDto);
            log.debug("LogAuditDetails logged action {} for user id {}",
                      logAuditDetail.action(), userState.getUserId());
        } catch (MissingRequestHeaderException exception) {
            log.warn("Can't log action {} details as missing JWT access token in parameters", logAuditDetail.action());
        } catch (Exception exception) {
            log.error("Error writing audit log action:: {}", logAuditDetail.action(), exception);
        }
    }

}
