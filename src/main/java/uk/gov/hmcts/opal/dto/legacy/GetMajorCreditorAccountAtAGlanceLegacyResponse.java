package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.opal.common.legacy.model.ErrorResponse;
import uk.gov.hmcts.opal.common.legacy.model.HasErrorResponse;
import uk.gov.hmcts.opal.dto.ToXmlString;

@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetMajorCreditorAccountAtAGlanceLegacyResponse implements ToXmlString, HasErrorResponse {

    @XmlElement(name = "major_creditor")
    private MajorCreditorLegacy majorCreditor;

    @XmlElement(name = "error_response")
    private ErrorResponse errorResponse;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Jacksonized
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class MajorCreditorLegacy {

        @XmlElement(name = "creditor_account_id")
        private Long creditorAccountId;

        @XmlElement(name = "creditor_account_version")
        private BigInteger creditorAccountVersion;

        @XmlElement(name = "name")
        private String name;

        @XmlElement(name = "code")
        private String code;

        @XmlElement(name = "address")
        private MajorCreditorAddressLegacy address;

        @XmlElement(name = "pay_by_bacs")
        private Boolean payByBacs;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Jacksonized
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class MajorCreditorAddressLegacy {

        @XmlElement(name = "line_1")
        private String line1;

        @XmlElement(name = "line_2")
        private String line2;

        @XmlElement(name = "line_3")
        private String line3;

        @XmlElement(name = "postcode")
        private String postcode;
    }
}
