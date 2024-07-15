package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.StandardLetterSearchDto;
import uk.gov.hmcts.opal.entity.StandardLetterEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.StandardLetterServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyStandardLetterService;
import uk.gov.hmcts.opal.service.opal.StandardLetterService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("standardLetterServiceProxy")
public class StandardLetterServiceProxy implements StandardLetterServiceInterface, ProxyInterface {

    private final StandardLetterService opalStandardLetterService;
    private final LegacyStandardLetterService legacyStandardLetterService;
    private final DynamicConfigService dynamicConfigService;

    private StandardLetterServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyStandardLetterService : opalStandardLetterService;
    }

    @Override
    public StandardLetterEntity getStandardLetter(long standardLetterId) {
        return getCurrentModeService().getStandardLetter(standardLetterId);
    }

    @Override
    public List<StandardLetterEntity> searchStandardLetters(StandardLetterSearchDto criteria) {
        return getCurrentModeService().searchStandardLetters(criteria);
    }
}
