package uk.gov.hmcts.opal.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_entitlements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntitlementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_entitlement_id_seq_generator")
    @SequenceGenerator(name = "user_entitlement_id_seq_generator", sequenceName = "user_entitlement_id_seq",
        allocationSize = 1)
    @Column(name = "user_entitlement_id")
    private Long userEntitlementId;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "business_unit_user_id", nullable = false)
    private BusinessUnitUserEntity businessUnitUser;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "application_function_id", nullable = false)
    private ApplicationFunctionEntity applicationFunction;
}
