package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "court_fees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "court_feeId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CourtFeeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "court_fee_id_seq_generator")
    @SequenceGenerator(name = "court_fee_id_seq_generator", sequenceName = "court_fee_id_seq", allocationSize = 1)
    @Column(name = "court_fee_id", nullable = false)
    private Long courtFeeId;

    @ManyToOne
    @JoinColumn(name = "business_unit_id", updatable = false)
    private BusinessUnitEntity businessUnit;

    @Column(name = "court_fee_code", length = 10, nullable = false)
    private String courtFeeCode;

    @Column(name = "description", length = 50, nullable = false)
    private String description;

    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "stats_code", length = 10, nullable = false)
    private String statsCode;
}
