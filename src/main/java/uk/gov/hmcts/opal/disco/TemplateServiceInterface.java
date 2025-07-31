package uk.gov.hmcts.opal.disco;

import uk.gov.hmcts.opal.dto.search.TemplateSearchDto;
import uk.gov.hmcts.opal.entity.TemplateEntity;

import java.util.List;

public interface TemplateServiceInterface {

    TemplateEntity getTemplate(long templateId);

    List<TemplateEntity> searchTemplates(TemplateSearchDto criteria);
}
