package uk.gov.hmcts.opal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "business_unit_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessUnitUserEntity {

    @Id
    @Column(name = "business_unit_user_id", length = 6)
    private String businessUnitUserId;

    @Column(name = "business_unit_id")
    private Short businessUnitId;

    @Column(name = "user_id", length = 100)
    private String userId;

}
