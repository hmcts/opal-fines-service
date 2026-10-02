package uk.gov.hmcts.opal.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InterfaceFileProcessorService {

    public void process(Long interfaceFileId) {

        log.info(
            "PO-6460 processor not yet implemented. interfaceFileId={}",
            interfaceFileId
        );
    }
}
