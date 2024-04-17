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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "offenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "offenseId")
public class OffenseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "offense_id_seq_generator")
    @SequenceGenerator(name = "offense_id_seq_generator", sequenceName = "offense_id_seq", allocationSize = 1)
    @Column(name = "offense_id", nullable = false)
    private Short offenseId;

    @Column(name = "cjs_code", length = 10, nullable = false)
    private String cjsCode;

    @ManyToOne
    @JoinColumn(name = "business_unit_id", updatable = false)
    private BusinessUnitEntity businessUnit;

    @Column(name = "offense_title", length = 120, nullable = false)
    private String offenseTitle;

    @Column(name = "offense_title_cy", length = 120, nullable = false)
    private String offenseTitleCy;
}
