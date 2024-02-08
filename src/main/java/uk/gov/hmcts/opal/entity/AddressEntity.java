package uk.gov.hmcts.opal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

@Data
@MappedSuperclass
public abstract class AddressEntity {

    @Column(name = "name", length = 35, nullable = false)
    private String name;

    @Column(name = "address_line_1", length = 35, nullable = false)
    private String addressLine1;

    @Column(name = "address_line_2", length = 35)
    private String addressLine2;

    @Column(name = "address_line_3", length = 35)
    private String addressLine3;

    @Column(name = "postcode", length = 8)
    private String postcode;

}
