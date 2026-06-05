package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorAccountHeaderEntity;

@Repository
public interface MajorCreditorAccountHeaderRepository
    extends JpaRepository<MajorCreditorAccountHeaderEntity, Long> {
}
