package uk.gov.hmcts.opal.techspike;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.Application;
import uk.gov.hmcts.opal.controllers.DraftAccountController;
import uk.gov.hmcts.opal.dto.AddDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.DraftAccountResponseDto;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;
import uk.gov.hmcts.opal.service.opal.DraftAccountService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j(topic = "TechSpike")
public class ActionMain extends TechSpikeAction {

    private static final Optional<Boolean> IGNORE = Optional.of(true);
    private static final Optional<Boolean> EMPTY = Optional.empty();
    private final DraftAccountController controller;

    private final ExecutorService execService;

    public ActionMain(DraftAccountService draftAccountService, DraftAccountController controller,
                      RestClient restClient) {
        super(draftAccountService, controller, restClient, 3000);
        this.controller = controller;
        // execService = Executors.newCachedThreadPool();
        execService = Executors.newFixedThreadPool(40);
    }

    @Override
    int getIndex() {
        return 0;
    }

    @Override
    public CallResponse doIt() {
        sleep(1.1f);
        log.info("{} Start - populate database", getLogName());
        sleep(1.4f);

        List<Long> draftIds = null;
        List<Callable<Long>> tasks = new ArrayList<>();
        float createTime = 0;

        try {
            for (long i = 0; i < 1; i++) {
                // tasks.add(addDraftAccountTask(i, 2));
                tasks.add(addDraftAccountHttpTask(i, 1L));
            }

            long start = System.currentTimeMillis();

            draftIds = invokeAll(tasks);
            createTime = (System.currentTimeMillis() - start) / 1000f;

            log.info("");
            log.info("{} Created {} Draft Accounts in {} secs", getLogName(), draftIds.size(), createTime);
            log.info("");
            log.info("");
            log.info("");
            log.info("");

            sleep(4.2f);

            // List<Callable<CallResponse>> actions = createOptimisticActions(draftIds);
            List<Callable<CallResponse>> actions = createPessimisticActions(draftIds);

            List<CallResponse> results = invokeAll(actions);

            sleep(4.7f);
            log.info("");
            log.info("Results Count: {}", results.size());
            log.info("");
            log.info("Invocation Results: \n\n\n{}\n\n\n", results.stream().sorted().map(Object::toString)
                .reduce("", (partialString, element) -> partialString + "\n\t" + element));
            log.info("");

            DraftAccountEntity entity = getDraftAccountEntity(draftIds.get(0), 4700);
            log.info("");
            log.info(".{} Entity '{}', accType: {}, valName: {}, version: {}", getLogName(), entity.getDraftAccountId(),
                     entity.getAccountType(), entity.getValidatedByName(), entity.getVersion()
            );
            log.info("");
            sleep(1.7f);

            log.info("");
            log.info("{} All sub-actions complete. About to delete data from the DB ..", getLogName());
            log.info("");

            sleep((3.7f));

        } catch (InterruptedException e) {
            log.error("INTERRUPTED!", e);
            throw new RuntimeException(e);
        } finally {
            sleep(1.5f);
            List<Callable<Object>> deletes = draftIds
                .stream()
                .map(this::deleteDraftAccountTask)
                .collect(Collectors.toList());
            try {
                long delStart = System.currentTimeMillis();
                List<Object> responses = invokeAll(deletes);
                float delTime = (System.currentTimeMillis() - delStart) / 1000f;
                log.info("");
                log.info("{} Created {} Draft Accounts in {} secs", getLogName(), draftIds.size(), createTime);
                log.info("{} Deleted {} Items from the DB in {} secs", getLogName(), responses.size(), delTime);
                log.info("");
                log.info("");
                sleep(1.3f);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                sleep(1.3f);
                log.info("");
                log.info("");
                log.info("");
                log.info("");
                log.info("Trigger SpringApplication exit");
                final int exit = SpringApplication.exit(Application.getContext(), () -> 0);
                log.info("Wait before System exit");
                sleep(1.3f);
                log.info("System exit");
                System.exit(exit);
                return new CallResponse("n/a", 0L);
            }
        }
    }

    private Callable<Long> addDraftAccountTask(long counter, float sleep) {
        return new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                sleep(sleep);
                ResponseEntity<DraftAccountResponseDto> response = controller.postDraftAccount(
                    createAddRequest(),
                    null
                );
                log.debug("{} creating Draft Account {} response: {}", getLogName(), counter, response);

                if (!response.getStatusCode().is2xxSuccessful()) {
                    throw new CallException(0L, response.toString());
                }

                DraftAccountResponseDto dto = response.getBody();
                Long draftId = dto.getDraftAccountId();
                log.info("{} created Draft Account {} with id: {}", getLogName(), counter, draftId);
                return draftId;
            }
        };
    }

    private Callable<Long> addDraftAccountHttpTask(long counter, long slept) {
        return new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                DraftAccountEntity entity = createDraftAccountEntityHttp(createAddRequest(), slept);
                return entity.getDraftAccountId();
            }
        };
    }

    private Callable<Object> deleteDraftAccountTask(Long id) {
        return () -> {
            ResponseEntity<String> deleted = controller.deleteDraftAccountById(id, null, EMPTY);
            log.debug("{} deleting Draft Account response: {}", getLogName(), deleted);
            return deleted.getBody();
        };
    }

    private <T> List<T> invokeAll(List<Callable<T>> tasks) throws InterruptedException {
        List<Future<T>> actFutures = execService.invokeAll(tasks);
        return actFutures.stream().parallel().map(fut -> {
            try {
                return fut.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

    private List<Callable<CallResponse>> createOptimisticActions(List<Long> draftIds) {
        List<Callable<CallResponse>> actions = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            actions.add(new SrvcReplace(draftAccountService, controller, restClient, draftIds));
            actions.add(new HttpReplace(draftAccountService, controller, restClient, draftIds));
            actions.add(new SrvcUpdate(draftAccountService, controller, restClient, draftIds));
            actions.add(new HttpUpdate(draftAccountService, controller, restClient, draftIds));
        }
        return actions;
    }

    private List<Callable<CallResponse>> createPessimisticActions(List<Long> draftIds) {
        List<Callable<CallResponse>> actions = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            actions.add(new SrvcGetOoooLocked(draftAccountService, controller, restClient, draftIds));
            actions.add(new SrvcGetPppLocked(draftAccountService, controller, restClient, draftIds));
            actions.add(new SrvcGetUnlocked(draftAccountService, controller, restClient, draftIds));
        }
        actions.add(new SrvcGetUserLocked(draftAccountService, controller, restClient, draftIds, 60));
        actions.add(new SrvcGetUserLocked(draftAccountService, controller, restClient, draftIds, 260));
        return actions;
    }

    private AddDraftAccountRequestDto createAddRequest() {
        return AddDraftAccountRequestDto.builder()
            .businessUnitId((short) 47)
            .accountType("SPIKE_TEST")
            .submittedBy("T_SPIKE")
            .submittedByName("Techy McSpike")
            .account(getAccountJson())
            .timelineData(getTimelineJson())
            .build();
    }

    private String getAccountJson() {
        return """
               {
                 "account_type": "fine",
                 "defendant_type": "company",
                 "originator_name": "Asylum & Immigration Tribunal",
                 "originator_id": 3865,
                 "prosecutor_case_reference": "AB123456",
                 "enforcement_court_id": 6255,
                 "collection_order_made": true,
                 "collection_order_made_today": true,
                 "collection_order_date": null,
                 "suspended_committal_date": null,
                 "payment_card_request": true,
                 "account_sentence_date": "2025-01-01",
                 "defendant": {
                     "company_flag": true,
                     "title": null,
                     "surname": null,
                     "forenames": null,
                     "company_name": "Acme Co Ltd",
                     "dob": null,
                     "address_line_1": "1 Test Lane",
                     "address_line_2": null,
                     "address_line_3": null,
                     "address_line_4": null,
                     "address_line_5": null,
                     "post_code": null,
                     "telephone_number_home": null,
                     "telephone_number_business": null,
                     "telephone_number_mobile": null,
                     "email_address_1": null,
                     "email_address_2": null,
                     "national_insurance_number": null,
                     "driving_licence_number": null,
                     "pnc_id": null,
                     "nationality_1": null,
                     "nationality_2": null,
                     "ethnicity_self_defined": null,
                     "ethnicity_observed": null,
                     "cro_number": null,
                     "occupation": null,
                     "gender": null,
                     "custody_status": null,
                     "prison_number": null,
                     "interpreter_lang": null,
                     "debtor_detail": {
                         "vehicle_make": null,
                         "vehicle_registration_mark": null,
                         "document_language": "EN",
                         "hearing_language": "EN",
                         "employee_reference": null,
                         "employer_company_name": null,
                         "employer_address_line_1": null,
                         "employer_address_line_2": null,
                         "employer_address_line_3": null,
                         "employer_address_line_4": null,
                         "employer_address_line_5": null,
                         "employer_post_code": null,
                         "employer_telephone_number": null,
                         "employer_email_address": null,
                         "aliases": null
                     },
                     "parent_guardian": null
                 },
                 "offences": [
                     {
                         "date_of_sentence": "01/01/2025",
                         "imposing_court_id": 6255,
                         "offence_id": 35014,
                         "impositions": [
                             {
                                 "result_id": "100",
                                 "amount_imposed": 100,
                                 "amount_paid": 0,
                                 "major_creditor_id": null,
                                 "minor_creditor": null
                             }
                         ]
                     }
                 ],
                 "fp_ticket_detail": null,
                 "payment_terms": {
                     "payment_terms_type_code": "B",
                     "effective_date": "2025-01-31",
                     "instalment_period": null,
                     "lump_sum_amount": null,
                     "instalment_amount": null,
                     "default_days_in_jail": null,
                     "enforcements": null
                 },
                 "account_notes": null
            }""";
    }

    private String getTimelineJson() {
        return """
            [
                 {
                     "username": "johndoe123",
                     "status": "Active",
                     "status_date": "2023-11-01",
                     "reason_text": "Account successfully activated after review."
                 },
                 {
                     "username": "janedoe456",
                     "status": "Pending",
                     "status_date": "2023-12-05",
                     "reason_text": "Awaiting additional documentation for verification."
                 },
                 {
                     "username": "mikebrown789",
                     "status": "Suspended",
                     "status_date": "2023-10-15",
                     "reason_text": "Violation of terms of service."
                 }
             ]""";
    }
}
