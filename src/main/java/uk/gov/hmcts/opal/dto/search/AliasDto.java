package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AliasDto {
    private Integer aliasNumber;
    private String organisationName;
    private String surname;
    private String forenames;
}
