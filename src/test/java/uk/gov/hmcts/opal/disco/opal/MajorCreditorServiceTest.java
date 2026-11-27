package uk.gov.hmcts.opal.disco.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
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
import org.springframework.data.jpa.repository.JpaSpecificationExecutor.SpecificationFluentQuery;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.dto.reference.MajorCreditorReferenceData;
import uk.gov.hmcts.opal.dto.search.MajorCreditorSearchDto;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorLegacyEntity;
import uk.gov.hmcts.opal.mapper.MajorCreditorMapper;
import uk.gov.hmcts.opal.repository.MajorCreditorRepository;
import uk.gov.hmcts.opal.service.opal.MajorCreditorService;
import uk.gov.hmcts.opal.util.FeatureFlags;

@ExtendWith(MockitoExtension.class)
class MajorCreditorServiceTest {

    @Mock
    private MajorCreditorRepository majorCreditorRepository;

    @Mock
    private MajorCreditorMapper majorCreditorMapper;

    @Mock
    private FeatureToggleApi featureToggleApi;

    @InjectMocks
    private MajorCreditorService majorCreditorService;

    @Test
    void testGetMajorCreditor() {
        // Arrange

        MajorCreditorLegacyEntity majorCreditorLegacyEntity = MajorCreditorLegacyEntity.builder().build();
        when(majorCreditorRepository.findById(any())).thenReturn(Optional.of(majorCreditorLegacyEntity));

        // Act
        MajorCreditorLegacyEntity result = majorCreditorService.getMajorCreditorById(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchMajorCreditors() {
        // Arrange
        SpecificationFluentQuery sfq = mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        MajorCreditorLegacyEntity majorCreditorLegacyEntity = MajorCreditorLegacyEntity.builder().build();
        Page<MajorCreditorLegacyEntity> mockPage =
            new PageImpl<>(List.of(majorCreditorLegacyEntity), Pageable.unpaged(), 999L);
        when(majorCreditorRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<MajorCreditorLegacyEntity> result = majorCreditorService.searchMajorCreditors(
            MajorCreditorSearchDto.builder().build());

        // Assert
        assertEquals(List.of(majorCreditorLegacyEntity), result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testMajorCreditorsReferenceDataRelease1bEnabled() {
        // Arrange
        SpecificationFluentQuery sfq = mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        MajorCreditorLegacyEntity majorCreditorLegacyEntity = MajorCreditorLegacyEntity.builder()
            .businessUnitId((short) 7)
            .creditorAccountEntity(
                CreditorAccountEntity.builder()
                    .creditorAccountId(8L)
                    .accountNumber("AC55K")
                    .creditorAccountType(CreditorAccountType.CF)
                    .prosecutionService(true)
                    .minorCreditorPartyId(555L)
                    .repayment(true)
                    .holdPayout(true)
                    .lastChangedDate(LocalDateTime.now())
                    .build())
            .build();

        MajorCreditorReferenceData referenceData = MajorCreditorReferenceData.builder()
            .majorCreditorId(majorCreditorLegacyEntity.getMajorCreditorId())
            .businessUnitId(majorCreditorLegacyEntity.getBusinessUnitId())
            .majorCreditorCode(majorCreditorLegacyEntity.getMajorCreditorCode())
            .name(majorCreditorLegacyEntity.getName())
            .postcode(majorCreditorLegacyEntity.getPostcode())
            .creditorAccountId(majorCreditorLegacyEntity.getCreditorAccountEntity().getCreditorAccountId())
            .accountNumber(majorCreditorLegacyEntity.getCreditorAccountEntity().getAccountNumber())
            .creditorAccountType(
                majorCreditorLegacyEntity.getCreditorAccountEntity().getCreditorAccountType().toString())
            .prosecutionService(majorCreditorLegacyEntity.getCreditorAccountEntity().isProsecutionService())
            .minorCreditorPartyId(majorCreditorLegacyEntity.getCreditorAccountEntity().getMinorCreditorPartyId())
            .repayment(majorCreditorLegacyEntity.getCreditorAccountEntity().isRepayment())
            .holdPayout(majorCreditorLegacyEntity.getCreditorAccountEntity().isHoldPayout())
            .lastChangedDate(majorCreditorLegacyEntity.getCreditorAccountEntity().getLastChangedDate())
            .build();

        Page<MajorCreditorLegacyEntity> mockPage =
            new PageImpl<>(List.of(majorCreditorLegacyEntity), Pageable.unpaged(), 999L);
        when(majorCreditorRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        when(majorCreditorMapper.toRefData(majorCreditorLegacyEntity)).thenReturn(referenceData);
        when(featureToggleApi.isFeatureEnabled(FeatureFlags.RELEASE_1B)).thenReturn(true);

        // Act
        List<MajorCreditorReferenceData> result = majorCreditorService.getReferenceData(
            Optional.empty(), Optional.empty());

        // Assert
        assertEquals(List.of(referenceData), result);
        Mockito.verify(majorCreditorMapper).toRefData(majorCreditorLegacyEntity);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testMajorCreditorsReferenceDataRelease1bDisabled() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        MajorCreditorLegacyEntity majorCreditorLegacyEntity = MajorCreditorLegacyEntity.builder()
            .businessUnitId((short) 7)
            .creditorAccountEntity(
                CreditorAccountEntity.builder()
                    .creditorAccountId(8L)
                    .accountNumber("AC55K")
                    .creditorAccountType(CreditorAccountType.CF)
                    .prosecutionService(true)
                    .minorCreditorPartyId(555L)
                    .repayment(true)
                    .holdPayout(true)
                    .lastChangedDate(LocalDateTime.now())
                    .build())
            .build();

        MajorCreditorReferenceData referenceData = MajorCreditorReferenceData.builder()
            .majorCreditorId(majorCreditorLegacyEntity.getMajorCreditorId())
            .businessUnitId(majorCreditorLegacyEntity.getBusinessUnitId())
            .majorCreditorCode(majorCreditorLegacyEntity.getMajorCreditorCode())
            .name(majorCreditorLegacyEntity.getName())
            .postcode(majorCreditorLegacyEntity.getPostcode())
            .creditorAccountId(majorCreditorLegacyEntity.getCreditorAccountEntity().getCreditorAccountId())
            .accountNumber(majorCreditorLegacyEntity.getCreditorAccountEntity().getAccountNumber())
            .creditorAccountType(
                majorCreditorLegacyEntity.getCreditorAccountEntity().getCreditorAccountType().toString())
            .prosecutionService(majorCreditorLegacyEntity.getCreditorAccountEntity().isProsecutionService())
            .minorCreditorPartyId(majorCreditorLegacyEntity.getCreditorAccountEntity().getMinorCreditorPartyId())
            .repayment(majorCreditorLegacyEntity.getCreditorAccountEntity().isRepayment())
            .holdPayout(majorCreditorLegacyEntity.getCreditorAccountEntity().isHoldPayout())
            .lastChangedDate(majorCreditorLegacyEntity.getCreditorAccountEntity().getLastChangedDate())
            .build();

        Page<MajorCreditorLegacyEntity> mockPage =
            new PageImpl<>(List.of(majorCreditorLegacyEntity), Pageable.unpaged(), 999L);
        when(majorCreditorRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        when(majorCreditorMapper.toRefData(majorCreditorLegacyEntity)).thenReturn(referenceData);
        when(featureToggleApi.isFeatureEnabled(FeatureFlags.RELEASE_1B)).thenReturn(false);

        // Act
        List<MajorCreditorReferenceData> result = majorCreditorService.getReferenceData(
            Optional.empty(), Optional.empty());

        // Assert
        assertEquals(List.of(referenceData), result);
        verify(majorCreditorMapper).toRefData(majorCreditorLegacyEntity);
    }
}
