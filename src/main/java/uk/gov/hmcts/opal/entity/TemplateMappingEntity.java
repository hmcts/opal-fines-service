package uk.gov.hmcts.opal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@IdClass(TemplateMappingEntity.MappingId.class)
@Table(name = "template_mappings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateMappingEntity {

    @Id
    @Column(name = "template_id")
    private Long templateId;

    @Id
    @Column(name = "application_function_id")
    private Long applicationFunctionId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MappingId implements Serializable {

        public Long templateId;

        public Long applicationFunctionId;
    }

}
