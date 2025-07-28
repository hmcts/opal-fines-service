package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.StandardLetterSearchDto;
import uk.gov.hmcts.opal.entity.StandardLetterEntity;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.StandardLetterServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyStandardLetterService;
import uk.gov.hmcts.opal.disco.opal.StandardLetterService;

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
