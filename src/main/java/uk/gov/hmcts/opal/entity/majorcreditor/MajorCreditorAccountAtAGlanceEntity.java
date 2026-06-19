package uk.gov.hmcts.opal.entity.majorcreditor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Immutable;

@Getter
@Entity
@Table(name = "v_major_creditor_account_at_a_glance")
@Immutable
@SuperBuilder
@NoArgsConstructor
public class MajorCreditorAccountAtAGlanceEntity {

    @Id
    @Column(name = "creditor_account_id")
    private Long creditorAccountId;

    @Column(name = "bacs_details")
    private String bacsDetails;

    @Column(name = "name")
    private String name;

    @Column(name = "address_line_1")
    private String addressLine1;

    @Column(name = "address_line_2")
    private String addressLine2;

    @Column(name = "address_line_3")
    private String addressLine3;

    @Column(name = "postcode")
    private String postcode;
}
