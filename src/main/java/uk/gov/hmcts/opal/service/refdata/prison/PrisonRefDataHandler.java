package uk.gov.hmcts.opal.service.refdata.prison;

import static uk.gov.hmcts.opal.service.refdata.common.Address.MAX_ADDRESS_LINE_LENGTH;
import static uk.gov.hmcts.opal.service.refdata.common.ValidationUtils.invalid;
import static uk.gov.hmcts.opal.service.refdata.common.ValidationUtils.validateLength;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.entity.PrisonEntity;
import uk.gov.hmcts.opal.refdata.RefDataUpdateHandler;
import uk.gov.hmcts.opal.repository.PrisonRepository;
import uk.gov.hmcts.opal.service.refdata.common.Address;

@Component
@RequiredArgsConstructor
public class PrisonRefDataHandler implements RefDataUpdateHandler<PrisonRecord, PrisonEntity> {

    private final PrisonRepository repository;
    private final PrisonRefDataMapper mapper;

    @Override
    public String refDataType() {
        return "Prison";
    }

    @Override
    public Class<PrisonRecord> payloadType() {
        return PrisonRecord.class;
    }

    @Override
    public void validateDto(PrisonRecord dto) {
        if (dto == null) {
            throw invalid("prisonRecord", "must not be null");
        }
        if (dto.getName() == null) {
            throw invalid("name", "is required");
        }
        validateLength(dto.getName(), "name", 200);
        if (dto.getAddresses() != null) {
            dto.getAddresses().stream().filter(Objects::nonNull).forEach(this::validateAddress);
        }
    }

    private void validateAddress(Address address) {
        validateLength(address.getAddressType(), "address_type", 100);
        validateLength(address.getAddressLine1(), "address_line_1", MAX_ADDRESS_LINE_LENGTH);
        validateLength(address.getAddressLine2(), "address_line_2", MAX_ADDRESS_LINE_LENGTH);
        validateLength(address.getAddressLine3(), "address_line_3", MAX_ADDRESS_LINE_LENGTH);
        validateLength(address.getAddressLine4(), "address_line_4", MAX_ADDRESS_LINE_LENGTH);
        validateLength(address.getAddressLine5(), "address_line_5", MAX_ADDRESS_LINE_LENGTH);
        validateLength(address.getPostCode(), "postcode", 8);
    }

    @Override
    public Optional<PrisonEntity> findEntity(PrisonRecord dto) {
        return repository.findByName(dto.getName());
    }

    @Override
    public PrisonEntity createEntityFromDto(PrisonRecord dto) {
        PrisonEntity entity = new PrisonEntity();
        updateEntityFromDto(dto, entity);
        return entity;
    }

    @Override
    public void updateEntityFromDto(PrisonRecord dto, PrisonEntity entity) {
        mapper.updateEntityFromDto(dto, entity);
    }

    @Override
    public PrisonEntity saveEntity(PrisonEntity entity) {
        return repository.save(entity);
    }

    @Override
    public void deleteEntity(String businessKey) {
        repository.deleteByName(businessKey);
    }

    @Override
    public List<String> cachesToClear() {
        return List.of("prisonReferenceDataCache");
    }

    @Override
    public String getBusinessKey(PrisonRecord dto) {
        return dto.getName();
    }
}
