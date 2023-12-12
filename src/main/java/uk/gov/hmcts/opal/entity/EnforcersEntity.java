package uk.gov.hmcts.opal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "enforcers")
public class EnforcersEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enforcer_id", nullable = false)
    private Long enforcerId;

    @Column(name = "business_unit_id", nullable = false)
    private Short businessUnitId;

    @Column(name = "enforcer_code", nullable = false)
    private Short enforcerCode;

    @Column(name = "name", nullable = false, length = 35)
    private String name;

    @Column(name = "name_cy", length = 35)
    private String nameCy;

    @Column(name = "address_line_1", length = 35)
    private String addressLine1;

    @Column(name = "address_line_2", length = 35)
    private String addressLine2;

    @Column(name = "address_line_3", length = 35)
    private String addressLine3;

    @Column(name = "address_line_1_cy", length = 35)
    private String addressLine1Cy;

    @Column(name = "address_line_2_cy", length = 35)
    private String addressLine2Cy;

    @Column(name = "address_line_3_cy", length = 35)
    private String addressLine3Cy;

    @Column(name = "postcode", length = 8)
    private String postcode;

    @Column(name = "warrant_reference_sequence", length = 20)
    private String warrantReferenceSequence;

    @Column(name = "warrant_register_sequence")
    private Integer warrantRegisterSequence;
}
