package uk.gov.hmcts.opal.entity.amendment;

import jakarta.persistence.Convert;
import org.hibernate.annotations.ColumnTransformer;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.converter.AssociatedRecordTypeConverter;
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "amendments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Immutable
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "amendment_id")
public class AmendmentEntity {

    @Id
    @Column(name = "amendment_id", nullable = false)
    @JsonProperty("amendment_id")
    private Long amendmentId;

    @Column(name = "business_unit_id", nullable = false)
    @JsonProperty("business_unit_id")
    private Short businessUnitId;

    @Convert(converter = AssociatedRecordTypeConverter.class)
    @ColumnTransformer(write = "?::t_associated_record_type_enum")
    @Column(name = "associated_record_type", length = 30, nullable = false,
        columnDefinition = "t_associated_record_type_enum")
    @JsonProperty("associated_record_type")
    private AssociatedRecordType associatedRecordType;

    @Column(name = "associated_record_id", length = 30, nullable = false)
    @JsonProperty("associated_record_id")
    private String associatedRecordId;

    @Column(name = "amended_date", nullable = false)
    @JsonProperty("amended_date")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime amendedDate;

    @Column(name = "amended_by", length = 20, nullable = false)
    @JsonProperty("amended_by")
    private String amendedBy;

    @Column(name = "amended_by_name", length = 100)
    @JsonProperty("amended_by_name")
    private String amendedByName;

    @Column(name = "field_code", nullable = false)
    @JsonProperty("field_code")
    private Short fieldCode;

    @Column(name = "old_value", length = 200)
    @JsonProperty("old_value")
    private String oldValue;

    @Column(name = "new_value", length = 200)
    @JsonProperty("new_value")
    private String newValue;

    @Column(name = "case_reference", length = 40)
    @JsonProperty("case_reference")
    private String caseReference;

    @Column(name = "function_code", length = 30)
    @JsonProperty("function_code")
    private String functionCode;

}
