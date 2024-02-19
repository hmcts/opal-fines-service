package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String>,
    JpaSpecificationExecutor<UserEntity> {
}
