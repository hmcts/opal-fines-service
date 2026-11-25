package uk.gov.hmcts.opal.service.refdata.prison;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.service.refdata.common.Address;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@JiraStory("PO-11022")
@JiraEpic("PO-10769")
class PrisonRecordTest {

    @Test
    void prisonAddressReturnsFirstMatchingAddress() {
        Address first = Address.builder().addressType("PRIS").addressLine1("First").build();
        Address second = Address.builder().addressType("PRIS").addressLine1("Second").build();
        PrisonRecord record = PrisonRecord.builder().addresses(Arrays.asList(
            null, Address.builder().addressType("OTHER").build(), first, second)).build();

        assertThat(record.prisonAddress()).contains(first);
    }

    @Test
    void prisonAddressIsEmptyWithoutMatchingAddress() {
        PrisonRecord record = PrisonRecord.builder().build();
        assertThat(record.prisonAddress()).isEmpty();

        record.setAddresses(List.of(Address.builder().addressType("OTHER").build()));
        assertThat(record.prisonAddress()).isEmpty();
    }
}
