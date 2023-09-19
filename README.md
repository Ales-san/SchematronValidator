[[en]](#schematron-validator) [[ru]](#валидатор-схематрона)
# Schematron Validator
[![Run in Postman](https://run.pstmn.io/button.svg)](https://god.gw.postman.com/run-collection/23609182-cada9b5b-1bd8-428b-8cbc-f6e263cb4228?action=collection%2Ffork&source=rip_markdown&collection-url=entityId%3D23609182-cada9b5b-1bd8-428b-8cbc-f6e263cb4228%26entityType%3Dcollection%26workspaceId%3D9d5a770d-f0b9-433a-877c-a8e36d7f46af) <br>
Small web-service application to validate xml file with schematron. <br>
It doesn't check xml file validity with xsd schemas and doesn't need them to work with schematron validation. <br>

For packaging to one jar with all dependencies use command:
``` 
mvn clean package
```

Execution example:
```
mvn spring-boot:run
```
or
```
java -jar .\target\schematron-validator-1.0-SNAPSHOT.jar
```
Path to directory for validation results saving is in file application.properties: reportPath. <br>
Examples of requests could be found in postman collection.

---

# Валидатор схематрона
Небольшой веб-сервис для валидации xml-файлов при помощи схематрона. <br>
Проверка на соответствие структуре XSD не осуществляется, соответственно xml схемы для работы приложения не требуются.  <br>

Для сборки приложения со всеми зависимостями в один файл используется следующая команда:
``` 
mvn clean package
```

Пример консольной команды для запуска приложения (если установлены все зависимости):
```
mvn spring-boot:run
```
или
```
java -jar .\target\schematron-validator-1.0-SNAPSHOT.jar
```

Адрес директории, в которую будет сохранен отчет по валидации
и файл с перечнем ошибок, возникших в процессе валидации, 
задается в файле application.properties в поле reportPath <br>
Примеры запросов приведены в коллекции Postman.
