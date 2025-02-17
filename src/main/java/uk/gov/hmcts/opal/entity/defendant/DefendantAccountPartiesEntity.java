package uk.gov.hmcts.opal.entity.defendant;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.SequenceGenerator;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DefendantAccountPartiesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "defendant_account_party_id_seq")
    @SequenceGenerator(name = "defendant_account_party_id_seq", sequenceName = "defendant_account_party_id_seq",
        allocationSize = 1)
    @Column(name = "defendant_account_party_id")
    private Long defendantAccountPartyId;

    @Column(name = "defendant_account_id", insertable = false, updatable = false)
    private Long defendantAccountId;

    // @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // @JoinColumn(name = "defendant_account_id", nullable = false)
    // private DefendantAccount.Lite defendantAccount;

    @Column(name = "party_id", insertable = false, updatable = false)
    private Long partyId;

    // @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // @JoinColumn(name = "party_id",  nullable = false)
    // private PartyEntity party;

    @Column(name = "association_type", nullable = false, length = 30)
    private String associationType;

    @Column(name = "debtor", nullable = false)
    private Boolean debtor;

    // @Data
    // @Entity
    // @EqualsAndHashCode(callSuper = true)
    // @Table(name = "defendant_account_parties")
    // @SuperBuilder
    // @ToString(callSuper = true)
    // @AllArgsConstructor
    // @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    // @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "defendantAccountPartyId")
    // @XmlRootElement
    // @XmlAccessorType(XmlAccessType.FIELD)
    // @XmlType(name = "Offence")
    // public static class Lite extends DefendantAccountPartiesEntity {
    // }

}
