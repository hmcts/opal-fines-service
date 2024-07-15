package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.StandardLetterSearchDto;
import uk.gov.hmcts.opal.entity.StandardLetterEntity;

import java.util.List;

public interface StandardLetterServiceInterface {

    StandardLetterEntity getStandardLetter(long standardLetterId);

    List<StandardLetterEntity> searchStandardLetters(StandardLetterSearchDto criteria);
}
