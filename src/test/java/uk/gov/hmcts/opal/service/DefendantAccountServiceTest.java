package uk.gov.hmcts.opal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefendantAccountServiceTest {

    @Mock
    private DefendantAccountRepository defendantAccountRepository;

    @InjectMocks
    private DefendantAccountService defendantAccountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testGetDefendantAccount() {
        // Arrange
        AccountEnquiryDto request = AccountEnquiryDto.builder().accountNumber("12345").businessUnitId(
            Short.valueOf("123")).build();

        DefendantAccountEntity mockEntity = new DefendantAccountEntity();
        when(defendantAccountRepository.findByBusinessUnitIdAndAccountNumber(Short.valueOf("123"), "12345"))
            .thenReturn(mockEntity);

        // Act
        DefendantAccountEntity result = defendantAccountService.getDefendantAccount(request);

        // Assert
        assertEquals(mockEntity, result);
        verify(defendantAccountRepository, times(1)).findByBusinessUnitIdAndAccountNumber(
            Short.valueOf("123"), "12345");
    }

    @Test
    void testPutDefendantAccount() {
        // Arrange
        DefendantAccountEntity mockEntity = new DefendantAccountEntity();
        when(defendantAccountRepository.save(any(DefendantAccountEntity.class)))
            .thenReturn(mockEntity);

        // Act
        DefendantAccountEntity result = defendantAccountService.putDefendantAccount(mockEntity);

        // Assert
        assertEquals(mockEntity, result);
        verify(defendantAccountRepository, times(1)).save(mockEntity);
    }

    @Test
    void testToString() {
        DefendantAccountService service = new DefendantAccountService();
        assertNotNull(service.toString());
    }

    @Test
    void testEqualsAndHashCode() {
        DefendantAccountService service1 = new DefendantAccountService();
        DefendantAccountService service2 = new DefendantAccountService();

        assertEquals(service1, service2);
        assertEquals(service1.hashCode(), service2.hashCode());
    }
}