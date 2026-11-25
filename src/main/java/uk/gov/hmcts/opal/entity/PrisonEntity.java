package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@Entity
@Table(name = "prisons")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PrisonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "prison_id_seq_generator")
    @SequenceGenerator(name = "prison_id_seq_generator", sequenceName = "prison_id_seq", allocationSize = 1)
    @Column(name = "prison_id", nullable = false)
    private Long prisonId;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "address_line_1", length = 35)
    private String addressLine1;

    @Column(name = "address_line_2", length = 35)
    private String addressLine2;

    @Column(name = "address_line_3", length = 35)
    private String addressLine3;

    @Column(name = "address_line_4", length = 35)
    private String addressLine4;

    @Column(name = "address_line_5", length = 35)
    private String addressLine5;

    @Column(name = "postcode", length = 8)
    private String postcode;

}
