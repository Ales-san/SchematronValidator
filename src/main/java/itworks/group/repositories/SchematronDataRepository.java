package itworks.group.repositories;

import itworks.group.models.SchematronData;
import itworks.group.models.SchematronInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface SchematronDataRepository extends CrudRepository<SchematronData, UUID> {
//    SchematronInfo findByMedDocumentIDEquals(String medDocumentID);
}
