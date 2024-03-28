package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.ApplicationFunctionSearchDto;
import uk.gov.hmcts.opal.entity.ApplicationFunctionEntity;
import uk.gov.hmcts.opal.repository.ApplicationFunctionRepository;
import uk.gov.hmcts.opal.repository.jpa.ApplicationFunctionSpecs;
import uk.gov.hmcts.opal.service.ApplicationFunctionServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("applicationFunctionService")
public class ApplicationFunctionService implements ApplicationFunctionServiceInterface {

    private final ApplicationFunctionRepository applicationFunctionRepository;

    private final ApplicationFunctionSpecs specs = new ApplicationFunctionSpecs();

    @Override
    public ApplicationFunctionEntity getApplicationFunction(long applicationFunctionId) {
        return applicationFunctionRepository.getReferenceById(applicationFunctionId);
    }

    @Override
    public List<ApplicationFunctionEntity> searchApplicationFunctions(ApplicationFunctionSearchDto criteria) {
        Page<ApplicationFunctionEntity> page = applicationFunctionRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
