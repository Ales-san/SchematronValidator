package itworks.group;

import itworks.group.models.*;
import itworks.group.repositories.SchematronDataRepository;
import itworks.group.repositories.SchematronInfoRepository;
import org.apache.commons.collections4.IterableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;

import name.dmaus.schxslt.Result;
import name.dmaus.schxslt.Schematron;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
public class SchematronValidationController {

    private final SchematronInfoRepository infoRepository;
    private final SchematronDataRepository dataRepository;
    private final Environment env;
    static final Logger logger = LoggerFactory.getLogger(SchematronValidationController.class);

    public SchematronValidationController(SchematronInfoRepository repository, SchematronDataRepository dataRepository, Environment env) {
        this.infoRepository = repository;
        this.dataRepository = dataRepository;
        this.env = env;
        logger.info("Controller initializing...");
    }

    //    @ResponseBody
    @PostMapping("/load/schematron")
//    @Transactional
    public ResponseEntity<Boolean> loadSchematron(
            @RequestParam(value = "medDocumentID") String documentID,
            @RequestParam(value = "commitHash") String commitHash,
            @RequestParam(value = "link") String link,
            @RequestParam(value = "regexPattern") String regexPattern,
            @RequestBody String documentData) {
        logger.info("Request to create or update schematron of medicine document with id: {} ", documentID);
        try {
            Optional<SchematronInfo> result = infoRepository.findByMedDocumentIDEquals(documentID);
            if (result.isPresent()) {
                Optional<SchematronData> dataObj = Optional.empty();
                if (result.get().getDataId() != null) {
                    dataObj = dataRepository.findById(result.get().getDataId());
                    if (dataObj.isPresent()) {
                        dataObj.get().setData(documentData);
                        dataRepository.save(dataObj.get());
                    }
                }
                if (result.get().getDataId() == null || dataObj.isEmpty())
                {
                    dataObj = Optional.of(new SchematronData(documentData));
                    dataRepository.save(dataObj.get());
                }
                result.get().setDataId(dataObj.get().getId());
                result.get().setMedDocumentUpdateDate(LocalDateTime.now());
                if (commitHash != null) result.get().setCommitHash(commitHash);
                if (link != null) result.get().setLink(link);
                if (regexPattern != null) result.get().setRegexPattern(regexPattern);
                infoRepository.save(result.get());
                logger.info("Updated schematron of medicine document with id: {} ", documentID);
            } else {
                Optional<SchematronData> dataObj = Optional.of(new SchematronData(documentData));
                dataRepository.save(dataObj.get());
                logger.info("Created new schematron of medicine document with id: {}", documentID);
                infoRepository.save(new SchematronInfo(documentID, commitHash, link, regexPattern, dataObj.get().getId()));
            }
            return ResponseEntity.status(HttpStatus.OK).body(true);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error while creating/updating schematron of medicine document with id: {}!\n Error message: {}", documentID, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    //    @ResponseBody
    @GetMapping("/get/schematron")
    public ResponseEntity<SchematronInfoWithData> getSchematron(
            @RequestParam(value = "medDocumentID") String documentID) {
        logger.info("Request to get schematron of medicine document with id: {} ", documentID);
        try {
            Optional<SchematronInfo> result = infoRepository.findByMedDocumentIDEquals(documentID);
            if (result.isEmpty() || result.get().getDataId() == null) {
                logger.info("Couldn't found schematron of medicine document with id: {} ", documentID);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            logger.info("Found schematron info of medicine document with id: {} ", documentID);
            Optional<SchematronData> dataObj = dataRepository.findById(result.get().getDataId());
            if (dataObj.isEmpty())
            {
                throw new NoSuchElementException(String.format("No schematron with such UUID: %s. Database was corrupted or internal error occurred!", result.get().getDataId().toString()));
            }
            return ResponseEntity.status(HttpStatus.OK).body(new SchematronInfoWithData(result.get(), dataObj.get().getData()));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error while getting schematron of medicine document with id: {}!\n Error message: {}", documentID, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/get/info")
    public ResponseEntity<SchematronInfo> getSchematronInfo(
            @RequestParam(value = "medDocumentID") String documentID) {
        logger.info("Request to get schematron of medicine document with id: {} ", documentID);
        try {
            Optional<SchematronInfo> result = infoRepository.findByMedDocumentIDEquals(documentID);
            if (result.isEmpty()) {
                logger.info("Couldn't found schematron of medicine document with id: {} ", documentID);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            logger.info("Found schematron info of medicine document with id: {} ", documentID);
            return ResponseEntity.status(HttpStatus.OK).body(result.get());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error while getting schematron of medicine document with id: {}!\n Error message: {}", documentID, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/get/info/standard")
    public ResponseEntity<StandardResultType> getSchematronInfoInStandardForm(
            @RequestParam(value = "medDocumentID") String documentID) {
        logger.info("Request to get schematron of medicine document with id: {} ", documentID);
        try {
            Optional<SchematronInfo> info = infoRepository.findByMedDocumentIDEquals(documentID);
            if (info.isEmpty()) {
                logger.info("Couldn't found schematron of medicine document with id: {} ", documentID);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            logger.info("Found schematron info of medicine document with id: {} ", documentID);
            List<List<StandardPropertyType>> schematronsStd = new ArrayList<>();
            schematronsStd.add(convertToStandardForm(info.get()));

            logger.info("Converted information to standard format!");
            StandardResultType result = new StandardResultType(
                    schematronsStd,
                    "true",
                    "200",
                    "success",
                    1);
            return ResponseEntity.status(HttpStatus.OK).body(result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error while getting schematron of medicine document with id: {}!\n Error message: {}", documentID, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }

    @GetMapping("/all/info")
    public ResponseEntity<Iterable<SchematronInfo>> getAllSchematronInfo() {
        logger.info("Request to get schematron info of all medicine documents");
        try {
            Iterable<SchematronInfo> result = infoRepository.findAll();

            logger.info("Found schematron info of {} medicine documents.", IterableUtils.size(result));
            return ResponseEntity.status(HttpStatus.OK).body(result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error while getting all schematron info of medicine documents!\n Error message: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/all/info/standard")
    public ResponseEntity<StandardResultType> getAllSchematronInfoInStandardForm() {
        logger.info("Request to get schematron info of all medicine documents");
        try {
            Iterable<SchematronInfo> schematrons = infoRepository.findAll();

            logger.info("Found schematron info of {} medicine documents.", IterableUtils.size(schematrons));
            List<List<StandardPropertyType>> schematronsStd = new ArrayList<>();
            for(SchematronInfo info: schematrons) {
                schematronsStd.add(convertToStandardForm(info));
            }
            logger.info("Converted information to standard format!");
            StandardResultType result = new StandardResultType(
                    schematronsStd,
                    "true",
                    "200",
                    "success",
                    IterableUtils.size(schematrons));
            return ResponseEntity.status(HttpStatus.OK).body(result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error while getting all schematron info of medicine documents!\n Error message: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private <T> List<StandardPropertyType> convertToStandardForm(T info) throws IllegalAccessException {
        Field[] fields = info.getClass().getDeclaredFields();
        List<StandardPropertyType> result = new ArrayList<>();
        for(Field f : fields) {
            f.setAccessible(true);
            Class<?> t = f.getType();
            Object v = f.get(info);
            result.add(new StandardPropertyType(f.getName(), v.toString()));
        }
        return result;
    }

    //    @ResponseBody
    @PostMapping("/validate")
    public ResponseEntity<ValidationResult> validateDocument(
            @RequestParam(value = "medDocumentID") String documentID,
            @RequestBody String documentData) {
        logger.info("Request to validate medicine document with id: {} ", documentID);
        try {
            Optional<SchematronInfo> schematronInfo = infoRepository.findByMedDocumentIDEquals(documentID);
            if (schematronInfo.isEmpty() || schematronInfo.get().getDataId() == null) {
                logger.info("Couldn't found schematron of medicine document with id: {} ", documentID);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            logger.info("Found schematron of medicine document with id: {} ", documentID);

            Optional<SchematronData> dataObj = dataRepository.findById(schematronInfo.get().getDataId());
            if (dataObj.isEmpty())
            {
                throw new NoSuchElementException(String.format("No schematron with such UUID: %s. Database was corrupted or internal error occurred!", schematronInfo.get().getDataId().toString()));
            }

            Schematron schematron = new Schematron(
                    new StreamSource(
                            new ByteArrayInputStream(dataObj.get().getData().getBytes(StandardCharsets.UTF_8))
                    )
            );
            logger.info("Loaded schematron of medicine document with id: {} ", documentID);
            // validate sample
            documentData = documentData.replace("xmlns=\"urn:hl7-org:v3\"", "xmlns:schsample=\"urn:hl7-org:v3\"");

            Result schematronOutput = schematron.validate(
                    new StreamSource(
                            new ByteArrayInputStream(documentData.getBytes(StandardCharsets.UTF_8))
                    )
            );
            logger.info("Validated medicine document with id: {} ", documentID);
            logger.info("Result of validation: {} ", schematronOutput.isValid());
            saveValidationResult(schematronOutput, documentID);

            ValidationResult validationResult = new ValidationResult(schematronOutput.isValid(), schematronOutput.getValidationMessages().toArray(new String[0]));

            return ResponseEntity.status(HttpStatus.OK).body(validationResult);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error while validating medicine document with id: {}!\n Error message: {}", documentID, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private void saveValidationResult(Result schematronOutput, String documentID) throws Exception {
        logger.info("Saving validation results of medicine document with id: {} ", documentID);
        String reportPath = env.getProperty("reportPath");
        if (reportPath == null || !(new File(reportPath).isDirectory())) {
//            logger.error("Неверно указан адрес директории для сохранения отчета о валидации");
            throw new Exception("Wrong directory of logs: " + reportPath);
        }
        if (!reportPath.endsWith(FileSystems.getDefault().getSeparator())) {
            reportPath += FileSystems.getDefault().getSeparator();
        }
        LocalDateTime nowTime = LocalDateTime.now();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        reportPath += "d" + documentID + "t" + dtf.format(nowTime);
        if (!schematronOutput.isValid()) {
            try (FileWriter fw = new FileWriter(reportPath + "validationMessages.txt", false)) {
                for (String message : schematronOutput.getValidationMessages()) {
                    fw.write(message + "\n");
                }
                fw.flush();
                logger.info("Validation messages of document with id: {} - are in file {}validationMessages.txt!", documentID, reportPath);
            }
        }

        // create and write validation report
        Document document = schematronOutput.getValidationReport();

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer();
        StringWriter sw = new StringWriter();
        try (FileWriter fw = new FileWriter(reportPath + "report.xml", false)) {
            trans.transform(new DOMSource(document), new StreamResult(fw));
            fw.flush();
            logger.info("Validation report of document with id: {} - is in file {}validationMessages.txt!", documentID, reportPath);

        }

    }
}



