package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "tills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "tillId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TillEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "till_id_seq_generator")
    @SequenceGenerator(name = "till_id_seq_generator", sequenceName = "till_id_seq", allocationSize = 1)
    @Column(name = "till_id", nullable = false)
    private Long tillId;

    @Column(name = "business_unit_id", nullable = false)
    private Short businessUnitId;

    @Column(name = "till_number", nullable = false)
    private Short tillNumber;

    @Column(name = "owned_by", length = 20, nullable = false)
    private String ownedBy;

}
