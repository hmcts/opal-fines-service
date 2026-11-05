package uk.gov.hmcts.opal.service.persistence;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.repository.SuspenseItemSummaryRepository;

@Service
@Slf4j(topic = "opal.SuspenseAccountSummaryRepositoryService")
@RequiredArgsConstructor
public class SuspenseAccountSummaryRepositoryService {

    private final SuspenseItemSummaryRepository suspenseAccountItemSummaryRepository;

    private final EntityManager entityManager;

}
