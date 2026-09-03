package uk.gov.hmcts.opal.service.refdata.lja;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.service.refdata.framework.RefDataUpdateHandler;

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
        return repository.findByLjaCode(dto.getLjaCode());
    }

    @Override
    public LocalJusticeAreaEntity createEntity(LjaRecord dto) {
        return LocalJusticeAreaEntity.builder().build();
    }

    @Override
    public LocalJusticeAreaEntity saveEntity(LocalJusticeAreaEntity entity) {
        return repository.save(entity);
    }

    @Override
    public LocalJusticeAreaMapper mapper() {
        return mapper;
    }
}
