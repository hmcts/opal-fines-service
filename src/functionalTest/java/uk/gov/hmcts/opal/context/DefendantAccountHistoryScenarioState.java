package uk.gov.hmcts.opal.context;

import io.restassured.response.Response;

import java.time.LocalDate;
import java.util.Map;

/**
 * Holds the mutable history-related state for a single defendant-account scenario.
 */
@ScenarioScoped
public class DefendantAccountHistoryScenarioState {

    private Response firstResponse;
    private Response secondResponse;
    private Response latestHistoryResponse;
    private LocalDate rememberedDateFrom;
    private LocalDate rememberedDateTo;
    private Long lastRequestedAccountId;
    private Map<String, Long> minimumHistoryItemCounts;

    public Map<String, Long> getMinimumHistoryItemCounts() {
        return minimumHistoryItemCounts;
    }

    public void setMinimumHistoryItemCounts(Map<String, Long> minimumHistoryItemCounts) {
        this.minimumHistoryItemCounts = minimumHistoryItemCounts;
    }

    public Response getFirstResponse() {
        return firstResponse;
    }

    public void setFirstResponse(Response firstResponse) {
        this.firstResponse = firstResponse;
    }

    public Response getSecondResponse() {
        return secondResponse;
    }

    public void setSecondResponse(Response secondResponse) {
        this.secondResponse = secondResponse;
    }

    public Response getLatestHistoryResponse() {
        return latestHistoryResponse;
    }

    public void setLatestHistoryResponse(Response latestHistoryResponse) {
        this.latestHistoryResponse = latestHistoryResponse;
    }

    public LocalDate getRememberedDateFrom() {
        return rememberedDateFrom;
    }

    public void setRememberedDateFrom(LocalDate rememberedDateFrom) {
        this.rememberedDateFrom = rememberedDateFrom;
    }

    public LocalDate getRememberedDateTo() {
        return rememberedDateTo;
    }

    public void setRememberedDateTo(LocalDate rememberedDateTo) {
        this.rememberedDateTo = rememberedDateTo;
    }

    public Long getLastRequestedAccountId() {
        return lastRequestedAccountId;
    }

    public void setLastRequestedAccountId(Long lastRequestedAccountId) {
        this.lastRequestedAccountId = lastRequestedAccountId;
    }
}
