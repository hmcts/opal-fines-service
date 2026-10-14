package uk.gov.hmcts.opal.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.Category;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.Relationship;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlternatePaymentReferenceDto {

    private Long alternatePaymentReferenceId;

    private Long defendantAccountId;

    private Relationship relationship;

    private Category category;

    private String businessUnitCode;

    private String aprText;

    private LocalDateTime createdDatetime;

    private LocalDateTime updatedDatetime;
}