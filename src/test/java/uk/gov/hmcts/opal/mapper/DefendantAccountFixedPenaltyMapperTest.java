package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.generated.model.VehicleFixedPenaltyDetailsCommon;

class DefendantAccountFixedPenaltyMapperTest {

    private final DefendantAccountFixedPenaltyMapper mapper =
        Mappers.getMapper(DefendantAccountFixedPenaltyMapper.class);

    @Nested
    class ToResponse {

        @Test
        void whenVehicleRegistrationIsPresent_thenMapsVehicleFixedPenaltyResponse() {
            DefendantAccountEntity account = buildAccount();
            FixedPenaltyOffenceEntity offence = buildOffence("AB12 CDE");

            GetDefendantAccountFixedPenaltyResponse response = mapper.toResponse(account, offence);
            VehicleFixedPenaltyDetailsCommon vehicleDetails = response.getVehicleFixedPenaltyDetails().get();

            assertAll(
                () -> assertTrue(response.getVehicleFixedPenaltyFlag()),
                () -> assertEquals(BigInteger.valueOf(12), response.getVersion()),
                () -> assertEquals("Kingston-upon-Thames Mags Court",
                    response.getFixedPenaltyTicketDetails().getIssuingAuthority()),
                () -> assertEquals("888", response.getFixedPenaltyTicketDetails().getTicketNumber()),
                () -> assertEquals("12:34", response.getFixedPenaltyTicketDetails().getTimeOfOffence()),
                () -> assertEquals("London", response.getFixedPenaltyTicketDetails().getPlaceOfOffence()),
                () -> assertTrue(response.getVehicleFixedPenaltyDetails().isPresent()),
                () -> assertEquals("AB12 CDE", vehicleDetails.getVehicleRegistrationNumber().get()),
                () -> assertEquals("DOE1234567", vehicleDetails.getVehicleDriversLicense().get()),
                () -> assertEquals("PN98765", vehicleDetails.getNoticeNumber().get()),
                () -> assertEquals(LocalDate.of(2024, 1, 1), vehicleDetails.getDateNoticeIssued().get())
            );
        }

        @Test
        void whenVehicleRegistrationIsNv_thenMapsNonVehicleFixedPenaltyResponse() {
            DefendantAccountEntity account = buildAccount();
            FixedPenaltyOffenceEntity offence = buildOffence("nv");

            GetDefendantAccountFixedPenaltyResponse response = mapper.toResponse(account, offence);

            assertAll(
                () -> assertFalse(response.getVehicleFixedPenaltyFlag()),
                () -> assertEquals("Kingston-upon-Thames Mags Court",
                    response.getFixedPenaltyTicketDetails().getIssuingAuthority()),
                () -> assertEquals("888", response.getFixedPenaltyTicketDetails().getTicketNumber()),
                () -> assertEquals("12:34", response.getFixedPenaltyTicketDetails().getTimeOfOffence()),
                () -> assertEquals("London", response.getFixedPenaltyTicketDetails().getPlaceOfOffence()),
                () -> assertTrue(response.getVehicleFixedPenaltyDetails().isPresent()),
                () -> assertNull(response.getVehicleFixedPenaltyDetails().get())
            );
        }
    }

    private DefendantAccountEntity buildAccount() {
        return DefendantAccountEntity.builder()
            .originatorName("Kingston-upon-Thames Mags Court")
            .versionNumber(12L)
            .build();
    }

    private FixedPenaltyOffenceEntity buildOffence(String vehicleRegistration) {
        return FixedPenaltyOffenceEntity.builder()
            .ticketNumber("888")
            .vehicleRegistration(vehicleRegistration)
            .offenceLocation("London")
            .noticeNumber("PN98765")
            .issuedDate(LocalDate.of(2024, 1, 1))
            .licenceNumber("DOE1234567")
            .timeOfOffence(LocalTime.of(12, 34))
            .build();
    }
}
