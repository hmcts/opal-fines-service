package uk.gov.hmcts.opal.controllers.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.entity.DraftAccountStatus;

@Component
public class DraftAccountStatusConverter implements Converter<String, DraftAccountStatus> {

    @Override
    public DraftAccountStatus convert(String value) {
        try {
            return DraftAccountStatus.valueOf(value);
        } catch (IllegalArgumentException iae) {
            return DraftAccountStatus.fromLabel(value);
        }
    }
}
