package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigInteger;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorAccountAtAGlanceEntity;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorEntity;
import uk.gov.hmcts.opal.mapper.MajorCreditorAccountHeaderEntityMapper;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.MajorCreditorAccountAtAGlanceRepository;
import uk.gov.hmcts.opal.repository.MajorCreditorAccountHeaderRepository;

@ExtendWith(MockitoExtension.class)
class OpalMajorCreditorAccountServiceTest {

    @Mock
    private CreditorAccountRepository creditorAccountRepository;

    @Mock
    private MajorCreditorAccountAtAGlanceRepository majorCreditorAccountAtAGlanceRepository;

    @Mock
    private MajorCreditorAccountHeaderRepository majorCreditorAccountHeaderRepository;

    @Mock
    private MajorCreditorAccountHeaderEntityMapper majorCreditorAccountHeaderEntityMapper;

    @InjectMocks
    private OpalMajorCreditorAccountService service;

    @Test
    void getAtAGlance_mapsMajorCreditorAccount() {
        Long creditorAccountId = 10770000000041L;
        when(creditorAccountRepository.findFullByCreditorAccountId(creditorAccountId))
            .thenReturn(Optional.of(CreditorAccountEntity.builder()
                .creditorAccountId(creditorAccountId)
                .creditorAccountType(CreditorAccountType.MJ)
                .versionNumber(4L)
                .majorCreditor(MajorCreditorEntity.builder().majorCreditorCode("TFL2").build())
                .build()));
        when(majorCreditorAccountAtAGlanceRepository.findById(creditorAccountId))
            .thenReturn(Optional.of(MajorCreditorAccountAtAGlanceEntity.builder()
                .creditorAccountId(creditorAccountId)
                .bacsDetails("PROVIDED")
                .name("TFL2 ATCM Testing")
                .addressLine1("1 ATCM Lane")
                .addressLine2("London")
                .addressLine3("NW1")
                .postcode("AA1 1AA")
                .build()));

        GetMajorCreditorAccountAtAGlanceResponse response = service.getAtAGlance(creditorAccountId);

        assertEquals(BigInteger.valueOf(4), response.getVersion());
        assertEquals(creditorAccountId, response.getMajorCreditor().getCreditorAccountId());
        assertEquals("TFL2 ATCM Testing", response.getMajorCreditor().getName());
        assertEquals("TFL2", response.getMajorCreditor().getCode());
        assertEquals(true, response.getMajorCreditor().getPayByBacs());
        assertEquals("1 ATCM Lane", response.getMajorCreditor().getAddress().getLine1());
        assertEquals("London", response.getMajorCreditor().getAddress().getLine2());
        assertEquals("NW1", response.getMajorCreditor().getAddress().getLine3());
        assertEquals("AA1 1AA", response.getMajorCreditor().getAddress().getPostcode());
    }

    @Test
    void getAtAGlance_mapsCentralFundWithoutMjOnlyFields() {
        Long creditorAccountId = 77L;
        when(creditorAccountRepository.findFullByCreditorAccountId(creditorAccountId))
            .thenReturn(Optional.of(CreditorAccountEntity.builder()
                .creditorAccountId(creditorAccountId)
                .creditorAccountType(CreditorAccountType.CF)
                .versionNumber(1L)
                .build()));
        when(majorCreditorAccountAtAGlanceRepository.findById(creditorAccountId))
            .thenReturn(Optional.of(MajorCreditorAccountAtAGlanceEntity.builder()
                .creditorAccountId(creditorAccountId)
                .bacsDetails("NOT PROVIDED")
                .name("West London Central Fund")
                .addressLine1("1 HMCTS Way")
                .addressLine2("London")
                .addressLine3("Westminster")
                .build()));

        GetMajorCreditorAccountAtAGlanceResponse response = service.getAtAGlance(creditorAccountId);

        assertEquals(BigInteger.ONE, response.getVersion());
        assertEquals("West London Central Fund", response.getMajorCreditor().getName());
        assertNull(response.getMajorCreditor().getCode());
        assertNull(response.getMajorCreditor().getPayByBacs());
        assertEquals("1 HMCTS Way", response.getMajorCreditor().getAddress().getLine1());
        assertNull(response.getMajorCreditor().getAddress().getPostcode());
    }

    @Test
    void getAtAGlance_omitsAddressWhenAllAddressFieldsAreNull() {
        Long creditorAccountId = 10770000000041L;
        when(creditorAccountRepository.findFullByCreditorAccountId(creditorAccountId))
            .thenReturn(Optional.of(CreditorAccountEntity.builder()
                .creditorAccountId(creditorAccountId)
                .creditorAccountType(CreditorAccountType.MJ)
                .versionNumber(1L)
                .majorCreditor(MajorCreditorEntity.builder().majorCreditorCode("TFL2").build())
                .build()));
        when(majorCreditorAccountAtAGlanceRepository.findById(creditorAccountId))
            .thenReturn(Optional.of(MajorCreditorAccountAtAGlanceEntity.builder()
                .creditorAccountId(creditorAccountId)
                .bacsDetails("PROVIDED")
                .name("TFL2 ATCM Testing")
                .build()));

        GetMajorCreditorAccountAtAGlanceResponse response = service.getAtAGlance(creditorAccountId);

        assertNull(response.getMajorCreditor().getAddress());
    }

    @Test
    void getAtAGlance_mapsBacsFromViewWhenNotProvided() {
        Long creditorAccountId = 10770000000041L;
        when(creditorAccountRepository.findFullByCreditorAccountId(creditorAccountId))
            .thenReturn(Optional.of(CreditorAccountEntity.builder()
                .creditorAccountId(creditorAccountId)
                .creditorAccountType(CreditorAccountType.MJ)
                .versionNumber(2L)
                .majorCreditor(MajorCreditorEntity.builder().majorCreditorCode("TFL2").build())
                .build()));
        when(majorCreditorAccountAtAGlanceRepository.findById(creditorAccountId))
            .thenReturn(Optional.of(MajorCreditorAccountAtAGlanceEntity.builder()
                .creditorAccountId(creditorAccountId)
                .bacsDetails("NOT PROVIDED")
                .name("TFL2 ATCM Testing")
                .build()));

        GetMajorCreditorAccountAtAGlanceResponse response = service.getAtAGlance(creditorAccountId);

        assertEquals(false, response.getMajorCreditor().getPayByBacs());
    }

    @Test
    void getAtAGlance_throwsWhenAccountTypeUnsupported() {
        when(creditorAccountRepository.findFullByCreditorAccountId(123L))
            .thenReturn(Optional.of(CreditorAccountEntity.builder()
                .creditorAccountId(123L)
                .creditorAccountType(CreditorAccountType.MN)
                .build()));

        assertThrows(EntityNotFoundException.class, () -> service.getAtAGlance(123L));
    }
}
