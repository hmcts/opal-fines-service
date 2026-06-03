package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
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
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.MajorCreditorAccountAtAGlanceRepository;
import uk.gov.hmcts.opal.util.VersionUtils;

@ExtendWith(MockitoExtension.class)
class OpalMajorCreditorAccountServiceTest {

    @Mock
    private CreditorAccountRepository creditorAccountRepository;

    @Mock
    private MajorCreditorAccountAtAGlanceRepository majorCreditorAccountAtAGlanceRepository;

    @InjectMocks
    private OpalMajorCreditorAccountService service;

    @Test
    void getAtAGlance_mapsMajorCreditorAccount() {
        Long accountId = 123L;
        CreditorAccountEntity creditorAccount = CreditorAccountEntity.builder()
            .creditorAccountId(accountId)
            .creditorAccountType(CreditorAccountType.MJ)
            .payByBacs(true)
            .versionNumber(4L)
            .majorCreditor(MajorCreditorEntity.builder().majorCreditorCode("ABCD").build())
            .build();
        MajorCreditorAccountAtAGlanceEntity entity = MajorCreditorAccountAtAGlanceEntity.builder()
            .creditorAccountId(accountId)
            .name("Major Creditor Ltd")
            .addressLine1("1 Main Street")
            .addressLine2("Town")
            .addressLine3("County")
            .postcode("AB1 2CD")
            .build();

        when(creditorAccountRepository.findFullByCreditorAccountId(accountId)).thenReturn(Optional.of(creditorAccount));
        when(majorCreditorAccountAtAGlanceRepository.findById(accountId)).thenReturn(Optional.of(entity));

        GetMajorCreditorAccountAtAGlanceResponse result = service.getAtAGlance(accountId);

        assertEquals(accountId, result.getMajorCreditor().getCreditorAccountId());
        assertEquals("Major Creditor Ltd", result.getMajorCreditor().getName());
        assertEquals("ABCD", result.getMajorCreditor().getCode());
        assertEquals(true, result.getMajorCreditor().getPayByBacs());
        assertEquals("1 Main Street", result.getMajorCreditor().getAddress().getAddressLine1());
        assertEquals("\"4\"", VersionUtils.createETag(result));
    }

    @Test
    void getAtAGlance_mapsCentralFundAccountWithoutMajorCreditorSpecificFields() {
        Long accountId = 456L;
        CreditorAccountEntity creditorAccount = CreditorAccountEntity.builder()
            .creditorAccountId(accountId)
            .creditorAccountType(CreditorAccountType.CF)
            .payByBacs(false)
            .versionNumber(2L)
            .build();
        MajorCreditorAccountAtAGlanceEntity entity = MajorCreditorAccountAtAGlanceEntity.builder()
            .creditorAccountId(accountId)
            .name("HM Courts & Tribunals Service")
            .addressLine1("HMCS add 1")
            .addressLine2("HMCS add 2")
            .addressLine3("HMCS add 3")
            .build();

        when(creditorAccountRepository.findFullByCreditorAccountId(accountId)).thenReturn(Optional.of(creditorAccount));
        when(majorCreditorAccountAtAGlanceRepository.findById(accountId)).thenReturn(Optional.of(entity));

        GetMajorCreditorAccountAtAGlanceResponse result = service.getAtAGlance(accountId);

        assertEquals("HM Courts & Tribunals Service", result.getMajorCreditor().getName());
        assertNull(result.getMajorCreditor().getCode());
        assertNull(result.getMajorCreditor().getPayByBacs());
        assertNull(result.getMajorCreditor().getAddress().getPostcode());
    }

    @Test
    void getAtAGlance_throwsWhenAccountTypeIsUnsupported() {
        Long accountId = 789L;
        CreditorAccountEntity creditorAccount = CreditorAccountEntity.builder()
            .creditorAccountId(accountId)
            .creditorAccountType(CreditorAccountType.MN)
            .build();

        when(creditorAccountRepository.findFullByCreditorAccountId(accountId)).thenReturn(Optional.of(creditorAccount));

        assertThrows(EntityNotFoundException.class, () -> service.getAtAGlance(accountId));
    }
}
