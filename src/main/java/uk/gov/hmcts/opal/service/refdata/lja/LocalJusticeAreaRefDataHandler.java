package uk.gov.hmcts.opal.service.refdata.lja;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.refdata.RefDataUpdateHandler;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;

@Component
@RequiredArgsConstructor
public class LocalJusticeAreaRefDataHandler
    implements RefDataUpdateHandler<LjaRecord, LocalJusticeAreaEntity> {

    private final LocalJusticeAreaRepository repository;
    private final LocalJusticeAreaMapper mapper;

    @Override
    public String refDataType() {
        return "LJA";
    }

    @Override
    public Class<LjaRecord> payloadType() {
        return LjaRecord.class;
    }

    @Override
    public void validateDto(LjaRecord dto) {
        //validation logic for LJA dtos goes here
    }

    @Override
    public Optional<LocalJusticeAreaEntity> findEntity(LjaRecord dto) {
        String ljaCode = dto.getLjaCode();
        Short ljaID = Short.parseShort(ljaCode);
        return repository.findById(ljaID);
    }

    @Override
    public LocalJusticeAreaEntity createEntityFromDto(LjaRecord dto) {
        LocalJusticeAreaEntity entity = new LocalJusticeAreaEntity();
        entity.setLjaCode(dto.getLjaCode());
        updateEntityFromDto(dto, entity);
        return entity;
    }

    @Override
    public LocalJusticeAreaEntity saveEntity(LocalJusticeAreaEntity entity) {
        return repository.save(entity);
    }

    @Override
    public void deleteEntity(LocalJusticeAreaEntity entity) {
        throw new UnsupportedOperationException("delete method not needed to test message processor");
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
        return String.valueOf(dto.getLjaCode());
    }
}
