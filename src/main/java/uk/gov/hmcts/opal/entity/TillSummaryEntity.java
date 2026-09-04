package uk.gov.hmcts.opal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "v_till_summary")
@Immutable
@SuperBuilder
@NoArgsConstructor
public class TillSummaryEntity {

    @Id
    @Column(name = "till_id")
    private Long tillId;

    @Column(name = "till_number")
    private Short tillNumber;

    @Column(name = "errors")
    private Long errors;

    @Column(name = "interface_file_id")
    private Long interfaceFileId;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "source", columnDefinition = "t_interface_file_source_enum")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private InterfaceFileSourceEnum source;

    @Column(name = "amount", precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "business_unit_id")
    private Short businessUnitId;

    @Column(name = "business_unit_name")
    private String businessUnitName;

    @Column(name = "processed_by")
    private String processedBy;

    @Column(name = "date_processed")
    private LocalDateTime dateProcessed;

    @Column(name = "auto_payment")
    private Boolean autoPayment;

    @Column(name = "status", columnDefinition = "t_till_status_enum")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private TillStatusEnum status;
}
