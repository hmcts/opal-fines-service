package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "aliases")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "aliasId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AliasEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "alias_id_seq_generator")
    @SequenceGenerator(name = "alias_id_seq_generator", sequenceName = "alias_id_seq", allocationSize = 1)
    @Column(name = "alias_id", nullable = false)
    private Long aliasId;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id",  nullable = false)
    private PartyEntity party;

    @Column(name = "surname", length = 50, nullable = false)
    private String surname;

    @Column(name = "forenames", length = 50)
    private String forenames;

    @Column(name = "initials", length = 10)
    private String initials;

    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    @Column(name = "organisation_name", length = 50)
    private String organisationName;
}
