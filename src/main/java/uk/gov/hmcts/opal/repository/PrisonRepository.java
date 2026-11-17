package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.PrisonEntity;

@Repository
public interface PrisonRepository extends JpaRepository<PrisonEntity, Long> {

    java.util.Optional<PrisonEntity> findByName(String name);

    @Transactional
    void deleteByName(String name);
}
