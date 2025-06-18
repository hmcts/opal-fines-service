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
@Table(name = "payment_card_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCardRequestEntity {

    @Id
    @Column(name = "defendant_account_id")
    private Long defendantAccountId;
}
