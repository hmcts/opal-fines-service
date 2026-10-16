package uk.gov.hmcts.opal.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalJusticeAreaDto {

    private Short localJusticeAreaId;
    private String ljaCode;
    private LocalJusticeAreaType ljaType;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String addressLine4;
    private String addressLine5;
    private String postcode;
    private LocalDateTime endDate;
    private String name;
}