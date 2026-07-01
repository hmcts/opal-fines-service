package uk.gov.hmcts.opal.entity.auditamendmentfield;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "audit_amendment_fields")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Immutable
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "field_code")
public class AuditAmendmentFieldEntity {

    @Id
    @Column(name = "field_code", nullable = false)
    @JsonProperty("field_code")
    private Short fieldCode;

    @Column(name = "data_item", length = 50, nullable = false)
    @JsonProperty("data_item")
    private String dataItem;
}
