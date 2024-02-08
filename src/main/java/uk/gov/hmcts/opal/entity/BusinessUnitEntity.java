package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

    @Column(name = "business_unit_name", nullable = false)
    private String businessUnitName;

    @Column(name = "business_unit_code")
    private String businessUnitCode;

    @Column(name = "business_unit_type", nullable = false)
    private String businessUnitType;

    @Column(name = "account_number_prefix")
    private String accountNumberPrefix;

    @Column(name = "parent_business_unit_id")
    private Short parentBusinessUnitId;
}
