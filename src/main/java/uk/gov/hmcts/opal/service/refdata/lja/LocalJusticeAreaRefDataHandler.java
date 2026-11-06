package uk.gov.hmcts.opal.service.refdata.lja;

import static uk.gov.hmcts.opal.service.refdata.common.Address.MAX_ADDRESS_LINE_LENGTH;
import static uk.gov.hmcts.opal.service.refdata.common.ValidationUtils.invalid;
import static uk.gov.hmcts.opal.service.refdata.common.ValidationUtils.validateLength;
import static uk.gov.hmcts.opal.service.refdata.lja.LjaRecord.COURT_ADDRESS_TYPE;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.refdata.RefDataUpdateHandler;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.service.refdata.common.Address;

@Component
@RequiredArgsConstructor
public class LocalJusticeAreaRefDataHandler
    implements RefDataUpdateHandler<LjaRecord, LocalJusticeAreaEntity> {

    private final LocalJusticeAreaRepository repository;
    private final LocalJusticeAreaRefDataMapper mapper;

    @Override
    public String refDataType() {
        return "LJA";
    }

    @Override
    public Class<LjaRecord> payloadType() {
        return LjaRecord.class;
    }

    @Override
    public Optional<LocalJusticeAreaEntity> findEntity(LjaRecord dto) {
        Short localJusticeAreaId = Short.parseShort(dto.getLjaCode());
        return repository.findById(localJusticeAreaId);
    }

    @Override
    public LocalJusticeAreaEntity createEntityFromDto(LjaRecord dto) {
        LocalJusticeAreaEntity entity = new LocalJusticeAreaEntity();

        Short localJusticeAreaId;
        try {
            localJusticeAreaId = Short.valueOf(dto.getLjaCode());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid ljaCode received: " + dto.getLjaCode());
        }
        entity.setLocalJusticeAreaId(localJusticeAreaId);

        updateEntityFromDto(dto, entity);
        return entity;
    }

    @Override
    public LocalJusticeAreaEntity saveEntity(LocalJusticeAreaEntity entity) {
        return repository.save(entity);
    }

    @Override
    public void deleteEntity(String businessKey) {
        try {
            repository.deleteById(Short.parseShort(businessKey));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid business key received: " + businessKey, e);
        }
    }

    @Override
    public void updateEntityFromDto(LjaRecord dto, LocalJusticeAreaEntity entity) {
        mapper.updateEntityFromDto(dto, entity);
    }

    @Override
    public List<String> cachesToClear() {
        return List.of("ljaReferenceDataCache");
    }

    @Override
    public String getBusinessKey(LjaRecord dto) {
        return dto.getLjaCode();
    }

    @Override
    public void validateDto(LjaRecord dto) {

        if (!"LJA".equals(refDataType())) {
            return;
        }

        validateLength(dto.getLjaCode(), "lja_code", 4);
        validateLength(dto.getLjaName(), "lja_name", 100);

        if (dto.getLjaCode() != null
            && !dto.getLjaCode().matches("[0-9]{4}")) {
            throw invalid("lja_code", "must contain exactly four digits");
        }

        List<Address> addresses = dto.getAddresses();
        if (addresses != null) {
            long courtAddresses = addresses.stream()
                .filter(address -> COURT_ADDRESS_TYPE.equals(address.getAddressType()))
                .count();
            if (courtAddresses != 1) {
                throw invalid("addresses", "must contain exactly one Court Address");
            }
            addresses.forEach(address -> {
                validateLength(address.getAddressLine1(), "address_line_1", MAX_ADDRESS_LINE_LENGTH);
                validateLength(address.getAddressLine2(), "address_line_2", MAX_ADDRESS_LINE_LENGTH);
                validateLength(address.getAddressLine3(), "address_line_3", MAX_ADDRESS_LINE_LENGTH);
                validateLength(address.getAddressLine4(), "address_line_4", MAX_ADDRESS_LINE_LENGTH);
                validateLength(address.getAddressLine5(), "address_line_5", MAX_ADDRESS_LINE_LENGTH);
                validateLength(address.getPostCode(), "postcode", 8);
            });
        }
    }
}
