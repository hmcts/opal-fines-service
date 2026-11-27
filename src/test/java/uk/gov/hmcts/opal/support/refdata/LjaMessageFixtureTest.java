package uk.gov.hmcts.opal.support.refdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LjaMessageFixtureTest {
    @Test
    void loadsRecordDefaultsFromFixture() {
        var record = LjaMessageFixture.fromFixture((short) 4321, "LJA owned");
        assertThat(record.code()).isEqualTo("4321");
        assertThat(record.name()).isEqualTo("LJA owned");
        assertThat(record.type()).isEqualTo("CRWCRT");
        assertThat(record.publishingStatus()).isEqualTo("Active");
        assertThat(record.startDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(record.endDate()).isNull();
        assertThat(record.addresses()).hasSize(2);
        var court = record.addresses().getFirst();
        assertThat(court.type()).isEqualTo("Court Address");
        assertThat(court.lines()).containsExactly("New address NovaChips 171", "New address line 2SR1910",
            "New address line 3", "New address line 4", null);
        assertThat(court.postcode()).isEqualTo("NE1 2BB");
        var financial = record.addresses().get(1);
        assertThat(financial.type()).isEqualTo("Financial Office Address");
        assertThat(financial.lines()).containsExactly("Secondary address line 1", null, null, null, null);
        assertThat(financial.postcode()).isEqualTo("NE1 2BB");
        var wire = wireRecord(record);
        assertThat(wire.get("cja_code")).isEqualTo("41");
        assertThat((List<?>) wire.get("contact_information")).isEmpty();
        assertThat(wire.containsKey("end_date")).isTrue();
        assertThat(wire.get("end_date")).isNull();
    }

    @Test
    void overridesOnlyRequestedFieldsWithoutMutatingFixture() {
        var baseline = LjaMessageFixture.fromFixture((short) 4321, "LJA owned");
        var changed = LjaMessageFixture.withOverrides(baseline, Map.of("lja_name", "LJA updated",
            "end_date", "2027-03-10", "address_line_1", "Changed Court address"));
        assertThat(changed.name()).isEqualTo("LJA updated");
        assertThat(changed.endDate()).isEqualTo(LocalDate.of(2027, 3, 10));
        assertThat(changed.addresses().getFirst().lines().getFirst()).isEqualTo("Changed Court address");
        assertThat(changed).usingRecursiveComparison().ignoringFields("name", "endDate", "addresses")
            .isEqualTo(baseline);
        assertThat(changed.addresses().getFirst().lines().subList(1, 5))
            .isEqualTo(baseline.addresses().getFirst().lines().subList(1, 5));
        assertThat(changed.addresses().getFirst().type()).isEqualTo(baseline.addresses().getFirst().type());
        assertThat(changed.addresses().getFirst().postcode()).isEqualTo(baseline.addresses().getFirst().postcode());
        assertThat(changed.addresses().get(1)).isEqualTo(baseline.addresses().get(1));
        changed.addresses().getFirst().lines().set(1, "Mutated");
        assertThat(baseline.addresses().getFirst().lines().get(1)).isEqualTo("New address line 2SR1910");
        assertThat(baseline).isEqualTo(LjaMessageFixture.fromFixture((short) 4321, "LJA owned"));
    }

    @Test
    void nullOverridesRemainPresentOnWire() {
        var baseline = LjaMessageFixture.fromFixture((short) 4321, "LJA owned");
        var changed = LjaMessageFixture.withOverrides(baseline,
            Map.of("lja_name", "<null>", "end_date", "<null>", "address_line_1", "<null>"));
        var wire = wireRecord(changed);
        assertThat(wire.containsKey("lja_name")).isTrue();
        assertThat(wire.get("lja_name")).isNull();
        assertThat(wire.containsKey("end_date")).isTrue();
        assertThat(wire.get("end_date")).isNull();
        var court = (Map<?, ?>) ((List<?>) wire.get("addresses")).getFirst();
        assertThat(court.containsKey("address_line_1")).isTrue();
        assertThat(court.get("address_line_1")).isNull();
    }

    @Test
    void rejectsUnknownOverrides() {
        var baseline = LjaMessageFixture.fromFixture((short) 4321, "LJA owned");
        assertThatThrownBy(() -> LjaMessageFixture.withOverrides(baseline, Map.of("lja_type", "INVALID")))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("lja_type");
    }

    @Test
    void fixtureCodeKeepsLeadingZeros() {
        assertThat(LjaMessageFixture.fromFixture((short) 42, "LJA owned").code()).isEqualTo("0042");
    }

    private static Map<?, ?> wireRecord(LjaMessageFixture.RecordData record) {
        var payload = (Map<?, ?>) LjaMessageFixture.create(List.of(record)).body().get("payload");
        return (Map<?, ?>) ((List<?>) payload.get("records")).getFirst();
    }

    @Test
    @SuppressWarnings("unchecked")
    void reloadsTemplateWithoutLosingDefaultsOrSharingMutableState() throws Exception {
        var mapper = new ObjectMapper();
        Map<String, Object> template;
        try (var input = getClass().getResourceAsStream("/refdata/lja/publication.json")) {
            assertThat(input).as("Reusable publication JSON template").isNotNull();
            template = mapper.readValue(input, new TypeReference<>() { });
        }
        var record = LjaMessageFixture.withOverrides(LjaMessageFixture.fromFixture((short) 42, "Owned"),
            Map.of("lja_name", "<null>"));
        var first = LjaMessageFixture.create(List.of(record));
        var header = (Map<String, Object>) first.body().get("header");
        var originalHeader = (Map<String, Object>) template.get("header");
        originalHeader.forEach((key, value) -> {
            if (!List.of("message_id", "created_date_time", "record_count").contains(key)) {
                assertThat(header.get(key)).as(key).isEqualTo(value);
            }
        });
        header.put("source_system", "Mutated");
        var second = LjaMessageFixture.create(List.of(record));
        assertThat(((Map<?, ?>) second.body().get("header")).get("source_system")).isEqualTo("Semarchy");
        var wire = (Map<?, ?>) ((List<?>) ((Map<?, ?>) first.body().get("payload")).get("records")).getFirst();
        assertThat(wire.containsKey("lja_name")).isTrue();
        assertThat(wire.get("lja_name")).isNull();
        assertThat(wire.get("lja_type")).isEqualTo("CRWCRT");
        assertThat(wire.get("end_date")).isNull();
        assertThat(wire.get("cja_code")).isEqualTo("41");
        assertThat((List<?>) wire.get("contact_information")).isEmpty();
        ((List<Object>) wire.get("contact_information")).add(Map.of("type", "Mutated"));
        var secondWire = (Map<?, ?>) ((List<?>) ((Map<?, ?>) second.body().get("payload"))
            .get("records")).getFirst();
        assertThat((List<?>) secondWire.get("contact_information")).isEmpty();
    }

    @Test
    void envelopeMatchesRecords() {
        var record = LjaMessageFixture.fromFixture((short) 42, "Owned");
        var first = LjaMessageFixture.create(List.of(record, record));
        var second = LjaMessageFixture.create(List.of(record));
        assertThat(first.body().get("header")).isInstanceOf(Map.class);
        var header = (Map<?, ?>) first.body().get("header");
        assertThat(header.get("record_count")).isEqualTo(2);
        assertThat(header.get("message_id")).isEqualTo(first.id().toString());
        assertThat(header.get("source_system")).isEqualTo("Semarchy");
        assertThat(header.get("data_product")).isEqualTo("LJA");
        assertThat(header.get("operation")).isEqualTo("PUBLISH");
        assertThat(header.get("message_type")).isEqualTo("ReferenceData");
        assertThat(header.get("release_package_id")).isEqualTo(1);
        assertThat(Instant.parse((String) header.get("created_date_time"))).isNotNull();
        assertThat(first.id()).isNotEqualTo(second.id());
    }

}
