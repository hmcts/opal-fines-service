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
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "suspense_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "suspense_transactionId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SuspenseTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "suspense_transaction_id_seq_generator")
    @SequenceGenerator(name = "suspense_transaction_id_seq_generator", sequenceName = "suspense_transaction_id_seq",
        allocationSize = 1)
    @Column(name = "suspense_transaction_id", nullable = false)
    private Long id;

    @Column(name = "suspense_item_id",nullable = false)
    private Long suspenseItemId;

    @Column(name = "posted_date")
    private LocalDate postedDate;

    @Column(name = "posted_by")
    private Long postedBy;

    @Column(name = "posted_by_name")
    private String postedByName;

    @Column(name = "transaction_type",nullable = false)
    private String transactionType;

    @Column(name = "amount",nullable = false)
    private Double amount;

    @Column(name = "associated_record_type",nullable = false)
    private String associatedRecordType;

    @Column(name = "associated_record_id",nullable = false)
    private String associatedRecordId;

    @Column(name = "text",nullable = false)
    private String text;

    @Column(name = "reversed",nullable = false)
    private String reversed;

}
