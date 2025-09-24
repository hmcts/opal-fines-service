package uk.gov.hmcts.opal.repository;

import uk.gov.hmcts.opal.entity.PartyEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyRepository extends JpaRepository<PartyEntity, Long>, JpaSpecificationExecutor<PartyEntity> {

}
