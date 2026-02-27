package uk.gov.hmcts.opal.dto.legacy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.util.LocalDateAdapter;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "response")
public class AddPaymentTermsLegacyResponse {

    @XmlElement(name = "defendant_account_id")
    @NotBlank
    private String defendantAccountId;

    @XmlElement(name = "version")
    @NotNull
    private Integer version;

    @XmlElement(name = "payment_terms")
    @NotNull
    private LegacyPaymentTerms paymentTerms;

    @XmlElement(name = "payment_card_last_requested")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate paymentCardLastRequested;

    @XmlElement(name = "last_enforcement")
    private String lastEnforcement;
}
