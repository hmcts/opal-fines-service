package uk.gov.hmcts.opal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "business_units")
@Data
public class BusinessUnitsEntity {

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
