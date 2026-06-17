package uk.gov.hmcts.opal;

import com.azure.core.util.BinaryData;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    public BinaryData runTest(String testName) {
        if (testName.equalsIgnoreCase("runJsonTest")) {

        } else if (testName.equalsIgnoreCase("runPdfTest")) {

        }

        return null;
    }
}
