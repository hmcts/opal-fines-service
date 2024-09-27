package uk.gov.hmcts.opal.dto;

public interface DraftAccountRequestDto {

    String getAccount();

    Short getBusinessUnitId();

    String getSubmittedBy();

    String getAccountType();

    String getTimelineData();
}
