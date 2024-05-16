package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "business_units")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "businessUnitId")
public class BusinessUnitEntity {

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

    @ManyToOne
    @JoinColumn(name = "parent_business_unit_id")
    private BusinessUnitEntity parentBusinessUnit;

    @Column(name = "opal_domain", length = 30)
    private String opalDomain;

}
