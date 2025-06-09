package uk.gov.hmcts.opal.entity.businessunit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@MappedSuperclass
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public abstract class AbstractBusinessUnitEntity {

    @Id
    @Column(name = "business_unit_id")
    private Short businessUnitId;

    @Column(name = "business_unit_name", length = 200, nullable = false)
    private String businessUnitName;

    @Column(name = "business_unit_code", length = 4)
    private String businessUnitCode;

    @Column(name = "business_unit_type", length = 20, nullable = false)
    private String businessUnitType;

    @Column(name = "account_number_prefix", length = 2)
    private String accountNumberPrefix;

    @Column(name = "opal_domain", length = 30)
    private String opalDomain;

    @Column(name = "welsh_language")
    private Boolean welshLanguage;
}
