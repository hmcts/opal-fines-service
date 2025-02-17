package uk.gov.hmcts.opal.techspike;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.controllers.DraftAccountController;
import uk.gov.hmcts.opal.dto.AddDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.DraftAccountResponseDto;
import uk.gov.hmcts.opal.dto.ReplaceDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.UpdateDraftAccountRequestDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;
import uk.gov.hmcts.opal.service.opal.DraftAccountService;
import uk.gov.hmcts.opal.util.VersionUtils;
import uk.gov.hmcts.opal.util.Versioned;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j(topic = "TechSpike")
public abstract class TechSpikeAction implements Runnable, Callable<TechSpikeAction.CallResponse> {

    final DraftAccountService draftAccountService;

    final DraftAccountController controller;

    final RestClient restClient;

    final long rndSleep;

    TechSpikeAction(DraftAccountService service, DraftAccountController controller,
                    RestClient restClient, long rndSleep) {
        this.draftAccountService = service;
        this.controller = controller;
        this.restClient = restClient;
        this.rndSleep = rndSleep;
    }

    @Override
    public void run() {
        sleep(1.7f);
        doIt();
    }

    public CallResponse call() throws Exception {
        sleep(1.7f);
        try {
            CallResponse result = doIt();
            return new CallResponse(getName() + " - Fin : " + result.message(), result.slept);
        } catch (CallException ce) {
            return new CallResponse(getName() + " - PROBLEM - " +  ce.getMessage(), ce.getSlept());
        } catch (Throwable t) {
            return new CallResponse(getName() + " - PROBLEM - "
                                        + t.getClass().getSimpleName() + ": " +  t.getMessage(), Long.MAX_VALUE);
        }
    }

    String getName() {
        return String.format("%s%02d", this.getClass().getSimpleName(), getIndex());
    }

    public String getLogName() {
        return getName() + "-" + Thread.currentThread().getName();
    }

    public String getUpdateString(Versioned v) {
        return String.format("%s-%03d-v%s", getName(), ThreadLocalRandom.current().nextInt(999),
                             (Optional.ofNullable(v.getVersion()).orElse(0L) + 1));
    }

    abstract CallResponse doIt();

    abstract int getIndex();

    @SneakyThrows
    long sleep(long millis) {
        long half = millis >> 1;
        long quart = millis >> 2;
        long sleep = ThreadLocalRandom.current().nextLong(millis - half, millis + quart);
        Thread.sleep(sleep);
        return sleep;
    }

    @SneakyThrows
    void sleep(float secs) {
        Thread.sleep((long) (secs * 1000));
    }

    long timeMe(long start) {
        return System.currentTimeMillis() - start;
    }

    DraftAccountEntity getDraftAccountEntity(long id, long slept) {
        try {
            return draftAccountService.getDraftAccount(id);
        } catch (CallException ce) {
            throw ce;
        } catch (RuntimeException re) {
            throw new CallException(slept, re);
        }
    }

    DraftAccountEntity getDraftAccountEntityOLocked(long id, long slept) {
        try {
            return draftAccountService.getDraftAccountWithOLock(id);
        } catch (CallException ce) {
            throw ce;
        } catch (RuntimeException re) {
            throw new CallException(slept, re);
        }
    }

    DraftAccountEntity getDraftAccountEntityPLocked(long id, long slept) {
        try {
            return draftAccountService.getDraftAccountWithPLock(id);
        } catch (CallException ce) {
            throw ce;
        } catch (RuntimeException re) {
            throw new CallException(slept, re);
        }
    }

    DraftAccountEntity getDraftAccountEntityUserLocked(long id, long slept, String userId) {
        try {
            return draftAccountService.getDraftAccountWithUserLock(id, userId, 40);
        } catch (CallException ce) {
            throw ce;
        } catch (RuntimeException re) {
            throw new CallException(slept, re);
        }
    }

    DraftAccountEntity getDraftAccountEntityHttp(long id, long slept) {
        try {
            ResponseEntity<DraftAccountResponseDto> response = restClient
                .get()
                .uri("http://localhost:4550/draft-accounts/" + id)
                .retrieve()
                .toEntity(DraftAccountResponseDto.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new CallException(slept, "Could not get draft account entity, HTTP response: "
                                               + response.getStatusCode());
            }
            DraftAccountEntity entity = toEntity(response.getBody());

            return entity;

        } catch (CallException ce) {
            throw ce;
        } catch (RuntimeException re) {
            log.error(".{} :getHttp: ### error ### getting id: {}, error: {}", getName(), id, re.getMessage());
            log.error(":getHttp:", re);
            throw new CallException(slept, re);
        }
    }


    DraftAccountEntity createDraftAccountEntityHttp(AddDraftAccountRequestDto dto, long slept) {
        try {
            DraftAccountResponseDto responseDto = restClient
                .post()
                .uri("http://localhost:4550/draft-accounts")
                .body(dto)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(DraftAccountResponseDto.class).getBody();

            return toEntity(responseDto);

        } catch (CallException ce) {
            throw ce;
        } catch (RuntimeException re) {
            log.error(".{} :createDraft..: ### error ### creating entity, error: {}",
                      getName(), re.getMessage());
            log.error(":createDraft..:", re);
            throw new CallException(slept, re);
        }
    }

    DraftAccountEntity replaceDraftAccount(long id, DraftAccountEntity entity, long slept) {
        try {
            return replaceDraftAccount(id, toReplaceDto(entity), slept);
        } catch (CallException ce) {
            throw ce;
        } catch (RuntimeException re) {
            throw new CallException(slept, re);
        }
    }

    DraftAccountEntity replaceDraftAccount(long id, ReplaceDraftAccountRequestDto dto, long slept) {
        log.info(".{} :replaceDA: id: {}, accType: {}, version: {}", getName(), id,
                 dto.getAccountType(), dto.getVersion());

        try {
            DraftAccountEntity replaced = draftAccountService.replaceDraftAccount(id, dto, draftAccountService);
            VersionUtils.verifyUpdated(replaced, dto, id, "Spike.replaceDraftAccount");
            return replaced;
        } catch (ObjectOptimisticLockingFailureException re) {
            log.warn(".{} :replaceDA: ### warn ### id: {}, error: {}", getName(), id, re.getMessage());
            throw new CallException(slept, re);
        }
    }

    DraftAccountEntity replaceDraftAccountHttp(long id, DraftAccountEntity entity, long slept) {
        try {
            return replaceDraftAccountHttp(id, toReplaceDto(entity), slept);
        } catch (CallException ce) {
            throw ce;
        } catch (RuntimeException re) {
            throw new CallException(slept, re);
        }
    }

    DraftAccountEntity replaceDraftAccountHttp(long id, ReplaceDraftAccountRequestDto dto, long slept) {
        log.info(".{} :replaceHTTP: id: {}, accType: {}, version: {}", getName(), id,
                 dto.getAccountType(), dto.getVersion());
        try {
            DraftAccountResponseDto outDto = restClient
                .put()
                .uri("http://localhost:4550/draft-accounts/" + id)
                .body(dto.toJson())
                .contentType(MediaType.APPLICATION_JSON)
                .exchange((request, response) -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        return response.bodyTo(DraftAccountResponseDto.class);
                    } else {
                        throw new CallException(slept, "[" + response.getStatusCode() + "] - "
                            + response.bodyTo(String.class));
                    }
                });

            return toEntity(outDto);

        } catch (ObjectOptimisticLockingFailureException re) {
            log.warn(".{} :replaceHTTP: ### warn ### id: {}, error: {}", getName(), id, re.getMessage());
            throw new CallException(slept, re);
        }
    }

    DraftAccountEntity updateDraftAccount(long id, DraftAccountEntity entity, long slept) {
        try {
            return updateDraftAccount(id, toUpdateDto(entity), slept);
        } catch (CallException ce) {
            throw ce;
        } catch (RuntimeException re) {
            throw new CallException(slept, re);
        }
    }

    DraftAccountEntity updateDraftAccount(long id, UpdateDraftAccountRequestDto dto, long slept) {
        log.info(".{} :updateDA: id: {}, valName: {}, version: {}", getName(), id,
                 dto.getValidatedByName(), dto.getVersion());

        try {
            DraftAccountEntity replaced = draftAccountService.updateDraftAccount(id, dto, draftAccountService);
            VersionUtils.verifyUpdated(replaced, dto, id, "Tech.updateDraftAccount");
            return replaced;

        } catch (ObjectOptimisticLockingFailureException re) {
            log.warn("{} :updateDA: ### warn ### id: {}, error: {}", getName(), id, re.getMessage());
            throw new CallException(slept, re);
        }
    }

    DraftAccountEntity updateDraftAccountHttp(long id, DraftAccountEntity entity, long slept) {
        try {
            return updateDraftAccountHttp(id, toUpdateDto(entity), slept);
        } catch (CallException ce) {
            throw ce;
        } catch (RuntimeException re) {
            throw new CallException(slept, re);
        }
    }

    DraftAccountEntity updateDraftAccountHttp(long id, UpdateDraftAccountRequestDto dto, long slept) {
        log.info(".{} :updateHttp: id: {}, valName: {}, version: {}", getName(), id,
                 dto.getValidatedByName(), dto.getVersion());
        try {
            DraftAccountResponseDto outDto = restClient
                .patch()
                .uri("http://localhost:4550/draft-accounts/" + id)
                .body(dto.toJson())
                .contentType(MediaType.APPLICATION_JSON)
                .exchange((request, response) -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        return response.bodyTo(DraftAccountResponseDto.class);
                    } else {
                        throw new CallException(slept, "[" + response.getStatusCode() + "] - "
                            + response.bodyTo(String.class));
                    }
                });

            return toEntity(outDto);

        } catch (ObjectOptimisticLockingFailureException re) {
            log.warn(".{} :replaceHTTP: ### warn ### id: {}, error: {}", getName(), id, re.getMessage());
            throw new CallException(slept, re);
        }
    }

    ReplaceDraftAccountRequestDto toReplaceDto(DraftAccountEntity entity) {
        return ReplaceDraftAccountRequestDto.builder()
            .account(entity.getAccount())
            .accountStatus(entity.getAccountStatus().getLabel())
            .accountType(entity.getAccountType())
            .businessUnitId(entity.getBusinessUnit().getBusinessUnitId())
            .submittedBy(entity.getSubmittedBy())
            .submittedByName(entity.getSubmittedByName())
            .timelineData(entity.getTimelineData())
            .version(entity.getVersion())
            .build();
    }

    UpdateDraftAccountRequestDto toUpdateDto(DraftAccountEntity entity) {
        return UpdateDraftAccountRequestDto.builder()
            .accountStatus(entity.getAccountStatus().getLabel())
            .businessUnitId(entity.getBusinessUnit().getBusinessUnitId())
            .validatedBy(entity.getValidatedBy())
            .validatedByName(entity.getValidatedByName())
            .timelineData(entity.getTimelineData())
            .version(entity.getVersion())
            .build();
    }

    DraftAccountEntity toEntity(DraftAccountResponseDto dto) {
        return DraftAccountEntity.builder()
            .draftAccountId(dto.getDraftAccountId())
            .businessUnit(BusinessUnitEntity.builder().businessUnitId(dto.getBusinessUnitId()).build())
            .createdDate(dto.getCreatedDate().toLocalDateTime())
            .submittedBy(dto.getSubmittedBy())
            .submittedByName(dto.getSubmittedByName())
            .validatedDate(Optional.ofNullable(dto.getValidatedDate()).map(d -> d.toLocalDateTime()).orElse(null))
            .validatedBy(dto.getValidatedBy())
            .validatedByName(dto.getValidatedByName())
            .account(dto.getAccount())
            .accountSnapshot(dto.getAccountSnapshot())
            .accountType(dto.getAccountType())
            .accountStatus(dto.getAccountStatus())
            .accountStatusDate(dto.getAccountStatusDate().toLocalDateTime())
            .statusMessage(dto.getStatusMessage())
            .timelineData(dto.getTimelineData())
            .accountNumber(dto.getAccountNumber())
            .accountId(dto.getAccountId())
            .version(dto.getVersion())
            .build();
    }

    public CallResponse successCallResponse(String serviceMsg, DraftAccountEntity entity, long delta, long slept) {
        return new CallResponse(
            serviceMsg + entity.getDraftAccountId() + "', DB query in " + delta + "ms"
                + ", updated: " + nullToEmpty(entity.getValidatedByName())
                + "-" + nullToEmpty(entity.getAccountType())
                + ", lock id: '" + nullToEmpty(entity.getLockIdData())
                + "-" + Optional.ofNullable(entity.getLockTimeout()).map(Object::toString).orElse("")
                + "', version: " + entity.getVersion(), slept);
    }

    private String nullToEmpty(String s) {
        return Optional.ofNullable(s).orElse("");
    }

    public record CallResponse(String message, Long slept) implements Comparable<CallResponse> {
        @Override
        public int compareTo(CallResponse other) {
            return Long.compare(slept, other.slept);
        }

        @Override
        public String toString() {
            return String.format("%4sms - %s", slept, message);
        }
    }

    public static class CallException extends RuntimeException {
        private final long slept;

        public CallException(long slept, String message) {
            super(message);
            this.slept = slept;
        }

        public CallException(long slept, Throwable t) {
            super(getTCause(t).getClass().getSimpleName()
                      + "[" + getStackElement(getTCause(t))
                      + "] - " + t.getMessage());
            this.slept = slept;
        }

        public long getSlept() {
            return slept;
        }
    }

    public static Throwable getTCause(Throwable t) {
        return Optional.ofNullable(t.getCause()).orElse(t);
    }

    public static String getStackElement(Throwable t) {
        StackTraceElement[] traces = t.getStackTrace();
        Optional<StackTraceElement> failPoint = Arrays.stream(traces)
            .filter(s -> s.getClassName().contains("DraftAccountService"))
            .findFirst();
        return failPoint.map(s -> s.getMethodName() + ":" + s.getLineNumber()).orElse("n/a");
    }

}
