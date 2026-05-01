package uk.gov.hmcts.opal.documents.docmosis;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/api")
public interface DocmosisClient {

    @PostExchange("/render")
    byte[] render(@RequestBody DocmosisRenderDto docmosisRenderDto);
}
