package uk.gov.hmcts.opal.entity.draft;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;


public final class TimelineData {

    private final ArrayList<Entry> entries;

    public TimelineData() {
        entries = new ArrayList<>();
    }

    public TimelineData(String json) {
        entries = new ArrayList<>(Arrays.asList(fromJson(json)));
    }

    public void insertEntry(String username, String status, LocalDate timestamp, String reason) {
        Entry entry = Entry.builder()
            .username(username)
            .status(status)
            .statusDate(timestamp)
            .reasonText(reason)
            .build();
        entries.addFirst(entry);
    }

    public String toJson() {
        return ToJsonString.objectToPrettyJson(entries.toArray(new Entry[0]));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Entry implements ToJsonString {

        @JsonProperty("username")
        private String username;

        @JsonProperty("status")
        private String status;

        @JsonProperty("status_date")
        private LocalDate statusDate;

        @JsonProperty("reason_text")
        private String reasonText;
    }

    public static Entry[] fromJson(String json) {
        return ToJsonString.toClassInstance(json, Entry[].class);
    }
}
