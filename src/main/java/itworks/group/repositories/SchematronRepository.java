package itworks.group.repositories;

import itworks.group.models.SchematronInfo;
import org.springframework.data.repository.CrudRepository;

public interface SchematronRepository extends CrudRepository<SchematronInfo, Integer> {
    SchematronInfo findByMedDocumentIDEquals(int medDocumentID);
}
