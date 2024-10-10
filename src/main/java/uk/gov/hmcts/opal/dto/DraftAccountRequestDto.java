package uk.gov.hmcts.opal.dto;

public interface DraftAccountRequestDto {

    String getAccount();

    String getSubmittedBy();

    String getAccountType();

    String getTimelineData();
}
