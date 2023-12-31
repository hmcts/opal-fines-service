package uk.gov.hmcts.opal.entity;

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
import lombok.Builder;
import lombok.Data;


@Entity
@Table(name = "defendant_account_parties")
@Data
@Builder
public class DefendantAccountPartiesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "defendant_account_party_id_seq")
    @SequenceGenerator(name = "defendant_account_party_id_seq", sequenceName = "defendant_account_party_id_seq",
        allocationSize = 1)
    @Column(name = "defendant_account_party_id")
    private Long defendantAccountPartyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defendant_account_id", referencedColumnName = "defendant_account_id", nullable = false)
    private DefendantAccountEntity defendantAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", referencedColumnName = "party_id", nullable = false)
    private PartyEntity party;

    @Column(name = "association_type", nullable = false, length = 30)
    private String associationType;

    @Column(name = "debtor", nullable = false)
    private Boolean debtor;
}
