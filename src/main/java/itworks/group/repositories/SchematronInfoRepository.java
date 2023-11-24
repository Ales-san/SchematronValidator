package itworks.group.repositories;

import itworks.group.models.SchematronInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface SchematronInfoRepository extends CrudRepository<SchematronInfo, UUID> {
    Optional<SchematronInfo> findByMedDocumentIDEquals(String medDocumentID);
}
