package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@Entity
@EqualsAndHashCode()
@Table(name = "prosecutors")
@SuperBuilder
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "prosecutor_id")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProsecutorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prosecutor_id", nullable = false)
    @JsonProperty("prosecutor_id")
    private Long prosecutorId;

    @Column(name = "prosecutor_code", length = 4)
    @JsonProperty("prosecutor_code")
    private String prosecutorCode;

    @Column(name = "name", length = 200)
    private String name;

    @Column(name = "address_line_1", length = 60)
    @JsonProperty("address_line_1")
    private String addressLine1;

    @Column(name = "address_line_2", length = 35)
    @JsonProperty("address_line_2")
    private String addressLine2;

    @Column(name = "address_line_3", length = 35)
    @JsonProperty("address_line_3")
    private String addressLine3;

    @Column(name = "address_line_4", length = 35)
    @JsonProperty("address_line_4")
    private String addressLine4;

    @Column(name = "address_line_5", length = 35)
    @JsonProperty("address_line_5")
    private String addressLine5;

    @Column(name = "postcode", length = 8)
    private String postcode;

    @Column(name = "end_date")
    @JsonProperty("end_date")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime endDate;
}
