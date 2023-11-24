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
or for launch in the background
```
javaw -jar .\target\schematron-validator-1.0-SNAPSHOT.jar
```
Command 
```
taskkill /F /PID {applicationPID}
```
can be used to stop application with PID from logs. <br>
Example line from logs: <br>
2023-09-28T11:20:23.124+03:00  INFO 20128 --- [main] itworks.group.SchematronValidator        : Starting SchematronValidator v1.0-SNAPSHOT using Java 17 with PID 20128 <br>

Jar-file and files with properties: application.properties and persistence.properties are required to run an application. <br>

In file application.properties: <br>
Field reportPath - path to directory for validation results saving; <br>
Fields logging.* - parameters for saving logs: path to directory for logs saving, file name for saving the newest logs, pattern for saving old logs, max number of log files; <br>
в поле server.port - port used by this application; <br>

Examples of requests could be found in postman collection (link in the beginning of description or in file Validator.postman_collection.json in this repository). <br>

Sometimes it may be necessary to check reportsPath directory size, because these reports will not be deleted automatically. 

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
или для запуска в фоне
```
javaw -jar .\target\schematron-validator-1.0-SNAPSHOT.jar
```
Команда 
```
taskkill /F /PID {applicationPID}
```
может быть использована для остановки приложения с PID из логов. <br>
Пример строки из логов с указанием PID: <br>
2023-09-28T11:20:23.124+03:00  INFO 20128 --- [main] itworks.group.SchematronValidator        : Starting SchematronValidator v1.0-SNAPSHOT using Java 17 with PID 20128 <br>

Для запуска приложения требуется скачать jar-файл и файлы с настройками: application.properties и persistence.properties.

В файле application.properties: <br>
в поле reportPath - адрес директории, в которую будет сохранен отчет по валидации
и файл с перечнем ошибок, возникших в процессе валидации; <br>
в полях logging.* - параметры для сохранения логов: папка для сохранения логов, имя файла для сохранения последних логов, паттерн для сохранения старых логов, максимальное количество логов в папке; <br>
в поле server.port - порт, на котором будет доступен сервис валидации; <br>

Примеры запросов приведены в коллекции Postman (ссылка в начале описания или файл Validator.postman_collection.json в репозитории). <br>
Также стоит следить за размером папки с данными отчетов о валидации (переменная reportPath), поскольку данные отчеты не удаляются автоматически.
