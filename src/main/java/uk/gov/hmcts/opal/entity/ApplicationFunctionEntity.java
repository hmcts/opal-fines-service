package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "application_functions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ApplicationFunctionEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "application_function_id_seq_generator")
    @SequenceGenerator(name = "application_function_id_seq_generator", sequenceName = "application_function_id_seq",
        allocationSize = 1)
    @Column(name = "application_function_id")
    private Long applicationFunctionId;

    @Column(name = "function_name", length = 200)
    private String functionName;

}
