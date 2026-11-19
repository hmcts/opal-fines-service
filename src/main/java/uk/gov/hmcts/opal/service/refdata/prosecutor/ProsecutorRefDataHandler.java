package uk.gov.hmcts.opal.service.refdata.prosecutor;

import static uk.gov.hmcts.opal.service.refdata.common.Address.MAX_ADDRESS_LINE_LENGTH;
import static uk.gov.hmcts.opal.service.refdata.common.ValidationUtils.invalid;
import static uk.gov.hmcts.opal.service.refdata.common.ValidationUtils.validateLength;
import static uk.gov.hmcts.opal.service.refdata.prosecutor.ProsecutorRecord.PROSECUTOR_ADDRESS_TYPE;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.entity.ProsecutorEntity;
import uk.gov.hmcts.opal.refdata.RefDataUpdateHandler;
import uk.gov.hmcts.opal.repository.ProsecutorRepository;
import uk.gov.hmcts.opal.service.refdata.common.Address;

@Component
@RequiredArgsConstructor
public class ProsecutorRefDataHandler implements RefDataUpdateHandler<ProsecutorRecord, ProsecutorEntity> {

    private static final int MAX_PROSECUTOR_CODE_LENGTH = 4;
    private static final int MAX_PROSECUTOR_NAME_LENGTH = 200;
    private static final int MAX_ADDRESS_LINE_1_LENGTH = 60;
    private static final int MAX_POSTCODE_LENGTH = 8;

    private final ProsecutorRepository repository;
    private final ProsecutorRefDataMapper mapper;

    @Override
    public String refDataType() {
        return "Prosecutor";
    }

    @Override
    public Class<ProsecutorRecord> payloadType() {
        return ProsecutorRecord.class;
    }

    @Override
    public Optional<ProsecutorEntity> findEntity(ProsecutorRecord dto) {
        return repository.findById(getBusinessKey(dto));
    }

    @Override
    public ProsecutorEntity createEntityFromDto(ProsecutorRecord dto) {
        ProsecutorEntity entity = new ProsecutorEntity();
        entity.setProsecutorCode(getBusinessKey(dto));
        updateEntityFromDto(dto, entity);
        return entity;
    }

    @Override
    public void updateEntityFromDto(ProsecutorRecord dto, ProsecutorEntity entity) {
        mapper.updateEntityFromDto(dto, entity);
    }

    @Override
    public ProsecutorEntity saveEntity(ProsecutorEntity entity) {
        return repository.save(entity);
    }

    @Override
    public void deleteEntity(String businessKey) {
        validateBusinessKey(businessKey);
        repository.deleteById(businessKey);
    }

    @Override
    public List<String> cachesToClear() {
        return List.of("opalProsecutorReferenceDataCache");
    }

    @Override
    public String getBusinessKey(ProsecutorRecord dto) {
        if (dto == null || dto.getProsecutorCode() == null) {
            throw invalid("prosecutor_code", "must not be null");
        }
        return dto.getProsecutorCode().toString();
    }

    @Override
    public void validateDto(ProsecutorRecord dto) {
        String businessKey = getBusinessKey(dto);
        validateBusinessKey(businessKey);

        if (dto.getName() == null) {
            throw invalid("name", "must not be null");
        }
        validateLength(dto.getName(), "name", MAX_PROSECUTOR_NAME_LENGTH);

        List<Address> addresses = dto.getAddresses();
        if (addresses == null) {
            return;
        }

        List<Address> prosecutorAddresses = addresses.stream()
            .filter(Objects::nonNull)
            .filter(address -> PROSECUTOR_ADDRESS_TYPE.equals(address.getAddressType()))
            .toList();
        if (prosecutorAddresses.size() != 1) {
            throw invalid("addresses", "must contain exactly one Prosecutor address");
        }

        Address address = prosecutorAddresses.getFirst();
        validateLength(address.getAddressLine1(), "address_line_1", MAX_ADDRESS_LINE_1_LENGTH);
        validateLength(address.getAddressLine2(), "address_line_2", MAX_ADDRESS_LINE_LENGTH);
        validateLength(address.getAddressLine3(), "address_line_3", MAX_ADDRESS_LINE_LENGTH);
        validateLength(address.getAddressLine4(), "address_line_4", MAX_ADDRESS_LINE_LENGTH);
        validateLength(address.getAddressLine5(), "address_line_5", MAX_ADDRESS_LINE_LENGTH);
        validateLength(address.getPostCode(), "postcode", MAX_POSTCODE_LENGTH);
    }

    private void validateBusinessKey(String businessKey) {
        if (businessKey == null) {
            throw invalid("prosecutor_code", "must not be null");
        }
        validateLength(businessKey, "prosecutor_code", MAX_PROSECUTOR_CODE_LENGTH);
    }
}
