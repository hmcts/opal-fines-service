package uk.gov.hmcts.opal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

@Data
@MappedSuperclass
public class EnforcersCourtsBaseEntity {

    @Column(name = "business_unit_id", nullable = false)
    private Short businessUnitId;

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

}
