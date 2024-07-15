package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.StandardLetterSearchDto;
import uk.gov.hmcts.opal.entity.StandardLetterEntity;
import uk.gov.hmcts.opal.repository.StandardLetterRepository;
import uk.gov.hmcts.opal.repository.jpa.StandardLetterSpecs;
import uk.gov.hmcts.opal.service.StandardLetterServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("standardLetterService")
public class StandardLetterService implements StandardLetterServiceInterface {

    private final StandardLetterRepository standardLetterRepository;

    private final StandardLetterSpecs specs = new StandardLetterSpecs();

    @Override
    public StandardLetterEntity getStandardLetter(long standardLetterId) {
        return standardLetterRepository.getReferenceById(standardLetterId);
    }

    @Override
    public List<StandardLetterEntity> searchStandardLetters(StandardLetterSearchDto criteria) {
        Page<StandardLetterEntity> page = standardLetterRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
