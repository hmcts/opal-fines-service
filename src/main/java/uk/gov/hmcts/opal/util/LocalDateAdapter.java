package uk.gov.hmcts.opal.util;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

    @Override
    public LocalDate unmarshal(String v) throws Exception {
        if (v == null || v.isBlank()) {
            return null;
        }
        /*
        TODO: The below added code is a temporary fix and needs to be removed
         once PO-10822 is merged into master and before this is merged into master.
         */
        if (v.contains("T")) {
            return LocalDateTime.parse(v).toLocalDate();
        }

        return LocalDate.parse(v);
    }

    @Override
    public String marshal(LocalDate v) throws Exception {
        return v.toString();
    }
}
