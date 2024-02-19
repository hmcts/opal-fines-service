package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.TemplateMappingEntity;
import uk.gov.hmcts.opal.entity.TemplateMappingEntity.MappingId;

@Repository
public interface TemplateMappingRepository extends JpaRepository<TemplateMappingEntity,
    MappingId>, JpaSpecificationExecutor<TemplateMappingEntity> {

    TemplateMappingEntity findDistinctByTemplate_TemplateIdAndApplicationFunction_ApplicationFunctionId(
        Long templateId, Long applicationFunctionId);

}
