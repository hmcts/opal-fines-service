package uk.gov.hmcts.opal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultDto {

    private String resultId;
    private String resultTitle;
    private String resultTitleCy;
    private String resultType;
    private boolean active;

    private Short impositionAllocationPriority;
    private String impositionCreditor;
    private boolean imposition;
    private String impositionCategory;
    private Boolean impositionAccruing;

    private boolean enforcement;
    private boolean enforcementOverride;
    private boolean furtherEnforcementWarn;
    private boolean furtherEnforcementDisallow;
    private boolean enforcementHold;
    private boolean requiresEnforcer;
    private boolean generatesHearing;
    private boolean collectionOrder;

    private boolean extendTtpDisallow;
    private boolean extendTtpPreserveLastEnf;

    private boolean preventPaymentCard;
    private boolean listsMonies;

    private String resultParameters;
}
