package uk.gov.hmcts.opal.service.opal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.FluentQuery;
import uk.gov.hmcts.opal.dto.reference.MajorCreditorReferenceData;
import uk.gov.hmcts.opal.dto.search.MajorCreditorSearchDto;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntityLite;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorEntity;
import uk.gov.hmcts.opal.mapper.MajorCreditorMapper;
import uk.gov.hmcts.opal.repository.MajorCreditorRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MajorCreditorServiceTest {

    @Mock
    private MajorCreditorRepository majorCreditorRepository;

    @Mock
    private MajorCreditorMapper majorCreditorMapper;

    @InjectMocks
    private MajorCreditorService majorCreditorService;

    @Test
    void testGetMajorCreditor() {
        // Arrange

        MajorCreditorEntity majorCreditorEntity = MajorCreditorEntity.builder().build();
        when(majorCreditorRepository.findById(any())).thenReturn(Optional.of(majorCreditorEntity));

        // Act
        MajorCreditorEntity result = majorCreditorService.getMajorCreditorById(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchMajorCreditors() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);
        when(ffq.sortBy(any())).thenReturn(ffq);

        MajorCreditorEntity majorCreditorEntity = MajorCreditorEntity.builder().build();
        Page<MajorCreditorEntity> mockPage = new PageImpl<>(List.of(majorCreditorEntity),
                                                            Pageable.unpaged(), 999L);
        when(majorCreditorRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<MajorCreditorEntity> result = majorCreditorService.searchMajorCreditors(
            MajorCreditorSearchDto.builder().build());

        // Assert
        assertEquals(List.of(majorCreditorEntity), result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testMajorCreditorsReferenceData() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);
        when(ffq.sortBy(any())).thenReturn(ffq);

        MajorCreditorEntity majorCreditorEntity = MajorCreditorEntity.builder()
            .businessUnitId((short) 7)
            .creditorAccountEntity(
                CreditorAccountEntityLite.builder()
                    .creditorAccountId(8L)
                    .accountNumber("AC55K")
                    .creditorAccountType("TYPE1")
                    .prosecutionService(true)
                    .minorCreditorPartyId(555L)
                    .fromSuspense(true)
                    .holdPayout(true)
                    .lastChangedDate(LocalDateTime.now())
                    .build())
            .build();

        MajorCreditorReferenceData referenceData = MajorCreditorReferenceData.builder()
            .majorCreditorId(majorCreditorEntity.getMajorCreditorId())
            .businessUnitId(majorCreditorEntity.getBusinessUnitId())
            .majorCreditorCode(majorCreditorEntity.getMajorCreditorCode())
            .name(majorCreditorEntity.getName())
            .postcode(majorCreditorEntity.getPostcode())
            .creditorAccountId(majorCreditorEntity.getCreditorAccountEntity().getCreditorAccountId())
            .accountNumber(majorCreditorEntity.getCreditorAccountEntity().getAccountNumber())
            .creditorAccountType(majorCreditorEntity.getCreditorAccountEntity().getCreditorAccountType())
            .prosecutionService(majorCreditorEntity.getCreditorAccountEntity().isProsecutionService())
            .minorCreditorPartyId(majorCreditorEntity.getCreditorAccountEntity().getMinorCreditorPartyId())
            .fromSuspense(majorCreditorEntity.getCreditorAccountEntity().isFromSuspense())
            .holdPayout(majorCreditorEntity.getCreditorAccountEntity().isHoldPayout())
            .lastChangedDate(majorCreditorEntity.getCreditorAccountEntity().getLastChangedDate())
            .build();

        Page<MajorCreditorEntity> mockPage = new PageImpl<>(List.of(majorCreditorEntity), Pageable.unpaged(), 999L);
        when(majorCreditorRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        when(majorCreditorMapper.toRefData(majorCreditorEntity)).thenReturn(referenceData);

        // Act
        List<MajorCreditorReferenceData> result = majorCreditorService.getReferenceData(
            Optional.empty(), Optional.empty());

        // Assert
        assertEquals(List.of(referenceData), result);
        Mockito.verify(majorCreditorMapper).toRefData(majorCreditorEntity);
    }
}
