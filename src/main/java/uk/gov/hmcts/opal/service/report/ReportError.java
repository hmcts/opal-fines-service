package uk.gov.hmcts.opal.service.report;

import lombok.Builder;

@Builder
public record ReportError(String operationId, String error) {

}
