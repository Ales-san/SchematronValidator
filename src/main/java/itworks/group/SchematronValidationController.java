package itworks.group;

import itworks.group.models.SchematronInfo;
import itworks.group.repositories.SchematronRepository;
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
import java.util.List;

@RestController
public class SchematronValidationController {

    private SchematronRepository repository;
    public SchematronValidationController(SchematronRepository repository) {
        this.repository = repository;
    }

    @ResponseBody
    @PostMapping("/create-update-schematron")
    public boolean createOrUpdateSchematron(
            @RequestParam(value = "medDocumentID") int documentID,
            @RequestBody String documentData) {

        try {
            SchematronInfo result = repository.findByMedDocumentIDEquals(documentID);
            if (result != null)
            {
                result.setData(documentData);
                repository.save(result);
                System.out.println("Updated medicine document with id: " + documentID);
            } else {
                System.out.println("Created new  medicine document with id: " + documentID);
                repository.save(new SchematronInfo(documentID, documentData));
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @ResponseBody
    @GetMapping("/get-schematron")
    public SchematronInfo getSchematron(
            @RequestParam(value = "medDocumentID") int documentID) {

        try {
            return repository.findByMedDocumentIDEquals(documentID);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public void validate(String[] args) {
        System.out.println("Запуск приложения...");
        try {

//            String schemaPath = "file:\\D:\\Downloads\\1C_Docs\\СЭМД_(186)_Протокол_лабораторного исследования_(CDA)_Редакция_5\\1.2.643.5.1.13.13.15.18-main\\186_Схематрон_v122.sch";
//            String samplePath = "D:\\Downloads\\1C_Docs\\СЭМД_(186)_Протокол_лабораторного исследования_(CDA)_Редакция_5\\1.2.643.5.1.13.13.15.18-main\\Заключение_микробиологического_исследования.xml";

            // check arguments
            if (args.length < 2) {
                System.out.println("""
                        Недостаточно аргументов для запуска валидации.
                        Необходимо ввести:
                        1. полный адрес схематрона, в котором описаны правила для валидации документа,
                        2. полный адрес документа, для которого проводится валидация
                        3. (необязательный аргумент) адрес директории для отчета по валидации""");
                System.exit(1);
            }
            String schemaPath = args[0];
            String samplePath = args[1];
            String reportPath = ".";

            File smth = new File(schemaPath);
            if (!(new File(schemaPath).isFile())) {
                System.out.println("Неверно указан адрес схематрона");
                System.exit(1);
            }

            if (!(new File(samplePath).isFile())) {
                System.out.println("Неверно указан адрес валидируемого документа");
                System.exit(1);
            }

            if (args.length > 2) {
                reportPath = args[2];
                if (!(new File(reportPath).isDirectory())
                        && !((new File(reportPath.substring(0, reportPath.lastIndexOf(FileSystems.getDefault().getSeparator())))).exists()
                        && reportPath.endsWith(FileSystems.getDefault().getSeparator()))) {
                    System.out.println("Неверно указан адрес директории для сохранения отчета о валидации");
                    System.exit(1);
                }
            }

            if (!reportPath.endsWith(FileSystems.getDefault().getSeparator())) {
                reportPath += FileSystems.getDefault().getSeparator();
            }

            // modify sample: make urn:hl7-org:v3 not default namespace
            Path modifiedSample = Files.createTempFile("modifiedSample", ".xml");

//            String tmpdir = System.getProperty("java.io.tmpdir");
//            System.out.println("Temp file path: " + tmpdir);

            try (BufferedReader br = Files.newBufferedReader(Paths.get(samplePath));
                 BufferedWriter bw = Files.newBufferedWriter(modifiedSample)) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains("xmlns=\"urn:hl7-org:v3\""))
                        line = line.replace("xmlns=\"urn:hl7-org:v3\"", "xmlns:schsample=\"urn:hl7-org:v3\"");
                    bw.write(line);
//                    System.out.println(line);
                }
            }

            // create schematron
            schemaPath = "file:\\" + schemaPath;
            Schematron schematron = new Schematron(
                    new StreamSource(
                            SchematronValidator.class.getResourceAsStream(schemaPath),
                            schemaPath
                    )
            );
            // validate sample
            Result result = schematron.validate(
                    new StreamSource(
                            SchematronValidator.class.getResourceAsStream(modifiedSample.toString()),
                            modifiedSample.toString()
                    )
            );

            // remove redundant modified sample
            Files.delete(modifiedSample);

            // print result of validation
            if (result.isValid()) {
                System.out.println("Документ валиден!");
            } else {
                System.out.println("Документ не валиден!");
                // write validation messages
                List<String> validationMessages = result.getValidationMessages();
                System.out.println("Были найдены следующие ошибки валидации:");
                for (String message: validationMessages) {
                    System.out.println(message + "\n");
                }

                try (FileWriter fw = new FileWriter(reportPath + "validationMessages.txt", false)) {
                    for (String message: validationMessages) {
                        fw.write(message + "\n");
                    }
                    fw.flush();
                    System.out.println("Ошибки валидации перечислены в файле" + reportPath + "validationMessages.txt!");
                }
            }
            // create and write validation report
            Document document = result.getValidationReport();

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer trans = tf.newTransformer();
            StringWriter sw = new StringWriter();
            try (FileWriter fw = new FileWriter(reportPath + "report.xml", false)) {
                trans.transform(new DOMSource(document), new StreamResult(fw));
                fw.flush();
                System.out.println("Отчет о валидации находится в файле " + reportPath + "report.xml!");
            }

//            unused thing: path to modified document (usually is automatically deleted)
//            String separator = FileSystems.getDefault().getSeparator();
//            String absolutePath = modifiedSample.toAbsolutePath().toString();
//            String tempFilePath = absolutePath
//                    .substring(absolutePath.lastIndexOf(separator));
//
//            System.out.println("temp file path and name:");
//            System.out.println(modifiedSample.toString());
//            System.out.println(tempFilePath.toString());

            System.out.println("Работа приложения завершена.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
