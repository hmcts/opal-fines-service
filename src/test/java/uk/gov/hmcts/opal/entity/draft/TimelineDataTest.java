package uk.gov.hmcts.opal.entity.draft;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimelineDataTest {

    @Test
    void deserializiSerialize() {
        String json = getTimelineJson();
        TimelineData data = new TimelineData(json);
        String output = data.toJson();
        assertEquals(json.stripIndent(), output.stripIndent());
    }

    @Test
    void addEntry() {
        String json = getTimelineJson();
        TimelineData data = new TimelineData(json);
        data.insertEntry("qq", "done", LocalDate.of(2025, 6, 18), "testing");
        assertTrue(data.toJson().startsWith(getStartsWith()));
    }

    private String getStartsWith() {
        return """
[ {
  "username" : "qq",
  "status" : "done",
  "status_date" : "2025-06-18",
  "reason_text" : "testing"
}, {
  "username" : "johndoe123",""";
    }

    private String getTimelineJson() {
        return """
[ {
  "username" : "johndoe123",
  "status" : "Active",
  "status_date" : "2023-11-01",
  "reason_text" : "Account successfully activated after review."
}, {
  "username" : "janedoe456",
  "status" : "Pending",
  "status_date" : "2023-12-05",
  "reason_text" : "Awaiting additional documentation for verification."
}, {
  "username" : "mikebrown789",
  "status" : "Suspended",
  "status_date" : "2023-10-15",
  "reason_text" : "Violation of terms of service."
} ]""";
    }
}
