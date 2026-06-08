package uk.gov.hmcts.opal.entity.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountType;

public record DefendantAccountImpositionData(
    Long defendantAccountId,
    Long accountVersionNumber,
    DefendantAccountType defendantAccountType,
    Long impositionId,
    LocalDateTime postedDate,
    String resultId,
    String resultTitle,
    Long creditorAccountId,
    CreditorAccountType creditorAccountType,
    Long majorCreditorId,
    String majorCreditorName,
    Long minorCreditorPartyId,
    Boolean minorCreditorOrganisation,
    String minorCreditorOrganisationName,
    String minorCreditorTitle,
    String minorCreditorForenames,
    String minorCreditorSurname,
    LocalDateTime imposedDate,
    BigDecimal imposedAmount,
    BigDecimal paidAmount,
    Long offenceId,
    String impositionOffenceCode,
    String impositionOffenceTitle,
    String offenceCode,
    String offenceTitle,
    Long imposingCourtId,
    Short imposingCourtCode,
    String imposingCourtName
) {
}
