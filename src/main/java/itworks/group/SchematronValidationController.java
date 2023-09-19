package itworks.group;

import itworks.group.models.SchematronInfo;
import itworks.group.models.ValidationResult;
import itworks.group.repositories.SchematronRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
public class SchematronValidationController {

    private SchematronRepository repository;
    private Environment env;
    static final Logger logger = LoggerFactory.getLogger(SchematronValidationController.class);

    public SchematronValidationController(SchematronRepository repository, Environment env) {
        this.repository = repository;
        this.env = env;
        logger.info("Controller initializing...");
    }

    //    @ResponseBody
    @PostMapping("/create-update-schematron")
    public ResponseEntity<Boolean> createOrUpdateSchematron(
            @RequestParam(value = "medDocumentID") int documentID,
            @RequestBody String documentData) {
        logger.info("Request to create or update schematron of medicine document with id: {} ", documentID);
        try {
            SchematronInfo result = repository.findByMedDocumentIDEquals(documentID);
            if (result != null) {
                result.setData(documentData);
                result.setMedDocumentUpdateDate(LocalDateTime.now());
                repository.save(result);
                logger.info("Updated schematron of medicine document with id: {} ", documentID);
            } else {
                logger.info("Created new schematron of medicine document with id: {}", documentID);
                repository.save(new SchematronInfo(documentID, documentData));
            }
            return ResponseEntity.status(HttpStatus.OK).body(true);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error while creating/updating schematron of medicine document with id: {}!\n Error message: {}", documentID, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    //    @ResponseBody
    @GetMapping("/get-schematron")
    public ResponseEntity<SchematronInfo> getSchematron(
            @RequestParam(value = "medDocumentID") int documentID) {
        logger.info("Request to get schematron of medicine document with id: {} ", documentID);
        try {
            SchematronInfo result = repository.findByMedDocumentIDEquals(documentID);
            if (result == null) {
                logger.info("Couldn't found schematron of medicine document with id: {} ", documentID);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            logger.info("Found schematron of medicine document with id: {} ", documentID);
            return ResponseEntity.status(HttpStatus.OK).body(result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error while getting schematron of medicine document with id: {}!\n Error message: {}", documentID, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    //    @ResponseBody
    @PostMapping("/validate")
    public ResponseEntity<ValidationResult> validateDocument(
            @RequestParam(value = "medDocumentID") int documentID,
            @RequestBody String documentData) {
        logger.info("Request to validate medicine document with id: {} ", documentID);
        try {
            SchematronInfo schematronInfo = repository.findByMedDocumentIDEquals(documentID);
            if (schematronInfo == null) {
                logger.info("Couldn't found schematron of medicine document with id: {} ", documentID);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            logger.info("Found schematron of medicine document with id: {} ", documentID);
            Schematron schematron = new Schematron(
                    new StreamSource(
                            new StringReader(schematronInfo.getData())
                    )
            );
            logger.info("Loaded schematron of medicine document with id: {} ", documentID);
            // validate sample
            documentData = documentData.replace("xmlns=\"urn:hl7-org:v3\"", "xmlns:schsample=\"urn:hl7-org:v3\"");

            Result schematronOutput = schematron.validate(
                    new StreamSource(
                            new StringReader(documentData)
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

    private void saveValidationResult(Result schematronOutput, int documentID) throws Exception {
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



