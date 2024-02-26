package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EnforcerCourtBaseEntity extends AddressEntity {

    @Column(name = "business_unit_id", nullable = false)
    private Short businessUnitId;

    @Column(name = "name_cy", length = 35)
    private String nameCy;

    @Column(name = "address_line_1_cy", length = 35)
    private String addressLine1Cy;

    @Column(name = "address_line_2_cy", length = 35)
    private String addressLine2Cy;

    @Column(name = "address_line_3_cy", length = 35)
    private String addressLine3Cy;

}
