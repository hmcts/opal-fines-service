package uk.gov.hmcts.opal.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.AccountSearchDto;
import uk.gov.hmcts.opal.dto.PartyDto;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PartySummary;
import uk.gov.hmcts.opal.repository.PartyRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartyServiceTest {

    @Mock
    private PartyRepository partyRepository;

    @InjectMocks
    private PartyService partyService;

    @Test
    void testSaveParty() {

        PartyDto partyDto = buildPartyDto();

        PartyEntity partyEntity = buildPartyEntity();

        when(partyRepository.save(any(PartyEntity.class))).thenReturn(partyEntity);

        // Act
        PartyDto savedPartyDto = partyService.saveParty(partyDto);

        // Assert
        assertEquals(partyEntity.getPartyId(), savedPartyDto.getPartyId());
        verify(partyRepository, times(1)).save(any(PartyEntity.class));
    }

    @Test
    void testGetParty() {

        PartyEntity partyEntity = buildPartyEntity();

        when(partyRepository.getReferenceById(any(Long.class))).thenReturn(partyEntity);

        // Act
        PartyDto savedPartyDto = partyService.getParty(1L);

        // Assert
        assertEquals(partyEntity.getPartyId(), savedPartyDto.getPartyId());
        verify(partyRepository, times(1)).getReferenceById(any(Long.class));
    }

    @Test
    void testSearchForParty() {

        List<PartySummary> partyEntity = Collections.<PartySummary>emptyList();

        when(partyRepository.findBySurnameContaining(any())).thenReturn(partyEntity);

        // Act
        List<PartySummary> partySummaries = partyService.searchForParty(AccountSearchDto.builder().build());

        // Assert
        assertEquals(partyEntity.size(), partySummaries.size());
        verify(partyRepository, times(1)).findBySurnameContaining(any());
    }

    public static PartyDto buildPartyDto() {
        LocalDateTime.of(2023, 12, 5, 15, 45);
        return PartyDto.builder()
            .organisation(false)
            .surname("Smith")
            .forenames("John James")
            .initials("JJ")
            .title("Mr")
            .addressLine1("22 Acacia Avenue")
            .addressLine2("Hammersmith")
            .addressLine3("Birmingham")
            .addressLine4("Cornwall")
            .addressLine5("Scotland")
            .postcode("SN15 9TT")
            .accountType("TFO")  // TFO = Transfer. Could also be FP = Fixed Penalty
            .dateOfBirth(LocalDate.of(2001, 8, 16))
            .age((short)21)
            .niNumber("FF22446688")
            .lastChangedDate(LocalDateTime.of(2023, 12, 5, 15, 45))
            .build();
    }

    public static PartyEntity buildPartyEntity() {
        return PartyEntity.builder()
            .organisation(false)
            .surname("Smith")
            .forenames("John James")
            .initials("JJ")
            .title("Mr")
            .addressLine1("22 Acacia Avenue")
            .addressLine2("Hammersmith")
            .addressLine3("Birmingham")
            .addressLine4("Cornwall")
            .addressLine5("Scotland")
            .postcode("SN15 9TT")
            .accountType("TFO")  // TFO = Transfer. Could also be FP = Fixed Penalty
            .dateOfBirth(LocalDate.of(2001, 8, 16))
            .age((short)21)
            .niNumber("FF22446688")
            .lastChangedDate(LocalDateTime.of(2023, 12, 5, 15, 45))
            .build();
    }
}
