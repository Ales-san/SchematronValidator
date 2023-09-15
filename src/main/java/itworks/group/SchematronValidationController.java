package itworks.group;

import itworks.group.models.SchematronInfo;
import itworks.group.models.ValidationResult;
import itworks.group.repositories.SchematronRepository;
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

    public SchematronValidationController(SchematronRepository repository, Environment env) {
        this.repository = repository;
        this.env = env;
    }

    //    @ResponseBody
    @PostMapping("/create-update-schematron")
    public ResponseEntity<Boolean> createOrUpdateSchematron(
            @RequestParam(value = "medDocumentID") int documentID,
            @RequestBody String documentData) {

        try {
            SchematronInfo result = repository.findByMedDocumentIDEquals(documentID);
            if (result != null) {
                result.setData(documentData);
                repository.save(result);
                System.out.println("Updated medicine document with id: " + documentID);
            } else {
                System.out.println("Created new  medicine document with id: " + documentID);
                repository.save(new SchematronInfo(documentID, documentData));
            }
            return ResponseEntity.status(HttpStatus.OK).body(true);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    //    @ResponseBody
    @GetMapping("/get-schematron")
    public ResponseEntity<SchematronInfo> getSchematron(
            @RequestParam(value = "medDocumentID") int documentID) {

        try {
            SchematronInfo result = repository.findByMedDocumentIDEquals(documentID);
            if (result == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.status(HttpStatus.OK).body(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    //    @ResponseBody
    @PostMapping("/validate")
    public ResponseEntity<ValidationResult> validateDocument(
            @RequestParam(value = "medDocumentID") int documentID,
            @RequestBody String documentData) {

        try {
            SchematronInfo schematronInfo = repository.findByMedDocumentIDEquals(documentID);
            if (schematronInfo == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            Schematron schematron = new Schematron(
                    new StreamSource(
                            new StringReader(schematronInfo.getData())
                    )
            );
            // validate sample
            documentData = documentData.replace("xmlns=\"urn:hl7-org:v3\"", "xmlns:schsample=\"urn:hl7-org:v3\"");

            Result schematronOutput = schematron.validate(
                    new StreamSource(
                            new StringReader(documentData)
                    )
            );

            logValidationResult(schematronOutput, documentID);

            ValidationResult validationResult = new ValidationResult(schematronOutput.isValid(), schematronOutput.getValidationMessages().toArray(new String[0]));

            return ResponseEntity.status(HttpStatus.OK).body(validationResult);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private void logValidationResult(Result schematronOutput, int documentId) {
        try {
            String reportPath = env.getProperty("reportPath");
            if (reportPath == null || !(new File(reportPath).isDirectory())) {
//                System.out.println("Неверно указан адрес директории для сохранения отчета о валидации");
                throw new Exception("Wrong directory for logs: " + reportPath);
            }
            if (!reportPath.endsWith(FileSystems.getDefault().getSeparator())) {
                reportPath += FileSystems.getDefault().getSeparator();
            }
            LocalDateTime nowTime = LocalDateTime.now();

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

            reportPath += "d" + documentId + "t" + dtf.format(nowTime);
            try (FileWriter fw = new FileWriter(reportPath + "validationMessages.txt", false)) {
                for (String message : schematronOutput.getValidationMessages()) {
                    fw.write(message + "\n");
                }
                fw.flush();
                System.out.println("Ошибки валидации перечислены в файле" + reportPath + "validationMessages.txt!");
            }

            // create and write validation report
            Document document = schematronOutput.getValidationReport();

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer trans = tf.newTransformer();
            StringWriter sw = new StringWriter();
            try (FileWriter fw = new FileWriter(reportPath + "report.xml", false)) {
                trans.transform(new DOMSource(document), new StreamResult(fw));
                fw.flush();
                System.out.println("Отчет о валидации находится в файле " + reportPath + "report.xml!");

            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}



