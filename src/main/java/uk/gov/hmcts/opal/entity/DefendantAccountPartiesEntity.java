package uk.gov.hmcts.opal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "defendant_account_parties")
public class DefendantAccountPartiesEntity {

    @Id
    @Column(name = "defendant_account_party_id")
    private Long defendantAccountPartyId;

    @ManyToOne
    @JoinColumn(name = "defendant_account_id", referencedColumnName = "defendant_account_id", nullable = false)
    private DefendantAccountEntity defendantAccount;

    @ManyToOne
    @JoinColumn(name = "party_id", referencedColumnName = "party_id", nullable = false)
    private PartyEntity party;

    @Column(name = "association_type", nullable = false)
    private String associationType;

    @Column(name = "debtor", nullable = false)
    private Boolean debtor;
}
