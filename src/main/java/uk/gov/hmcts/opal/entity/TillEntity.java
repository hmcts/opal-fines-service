package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

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

    @ManyToOne
    @JoinColumn(name = "business_unit_id", nullable = false)
    private BusinessUnitEntity businessUnit;

    @Column(name = "till_number", nullable = false)
    private Short tillNumber;

    @Column(name = "owned_by", length = 20, nullable = false)
    private String ownedBy;

    @Column(name = "source", columnDefinition = "t_interface_file_source_enum")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private InterfaceFileSourceEnum source;

    @Column(name = "status", columnDefinition = "t_till_status_enum")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private TillStatusEnum status;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interface_file_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private InterfaceFileEntity interfaceFile;

    @Column(name = "payments_count")
    private Short paymentsCount;

    @Column(name = "owned_by_name", length = 100)
    private String ownedByName;

    @Column(name = "auto_payment", nullable = false)
    private boolean autoPayment;

    @Column(name = "created_date", nullable = false)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime createdDate;

    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "tillEntity", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<PaymentInEntity> paymentInEntities = new ArrayList<>();
}
