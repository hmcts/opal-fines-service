package uk.gov.hmcts.opal.dto;


import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ImpositionTotalsDto {

    private BigDecimal fineImpositions;
    private BigDecimal costImpositions;
    private BigDecimal compensationImpositions;
    private BigDecimal criminalCourtsChargeImpositions;
    private BigDecimal victimSurchargeImpositions;
    private BigDecimal otherImpositions;

}
