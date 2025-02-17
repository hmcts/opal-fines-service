package uk.gov.hmcts.opal.entity.businessunit;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class BusinessUnit {

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

    @Column(name = "parent_business_unit_id")
    private Short parentBusinessUnitId;

    @Entity
    @Data
    @SuperBuilder
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    @Table(name = "business_units")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "businessUnitId")
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "BusinessUnit")
    public static class Lite extends BusinessUnit {
    }
}
