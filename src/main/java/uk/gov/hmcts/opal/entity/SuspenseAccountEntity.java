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
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnit;

@Entity
@Table(name = "suspense_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "suspense_accountId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SuspenseAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "suspense_account_id_seq_generator")
    @SequenceGenerator(name = "suspense_account_id_seq_generator", sequenceName = "suspense_account_id_seq",
        allocationSize = 1)
    @Column(name = "suspense_account_id", nullable = false)
    private Long suspenseAccountId;

    @ManyToOne
    @JoinColumn(name = "business_unit_id", updatable = false)
    private BusinessUnit.Lite businessUnit;

    @Column(name = "account_number", length = 20, nullable = false)
    private String accountNumber;
}
