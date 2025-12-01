package uk.gov.hmcts.opal.dto.legacy.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToXmlString;
import uk.gov.hmcts.opal.util.LocalDateAdapter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CollectionOrder implements ToXmlString {

    private String collectionOrderCode;

    @JsonProperty("collection_order_flag")
    @XmlElement(name = "collection_order_flag")
    @NotNull
    private Boolean collectionOrderFlag;

    @JsonProperty("collection_order_date")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate collectionOrderDate;
}
