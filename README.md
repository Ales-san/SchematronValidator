[[en]](#schematron-validator) [[ru]](#валидатор-схематрона)
# Schematron Validator
Lightweight small application to validate xml file with schematron. <br>
It doesn't check xml file validity with xsd schemas and doesn't need them to work with schematron validation. <br>

For packaging to one jar with all dependencies use command:
``` 
mvn clean compile assembly:single
```

Execution example:
```
java -jar .\target\schematron-validator-1.0-SNAPSHOT-jar-with-dependencies.jar `
 "D:\Downloads\186 Schematron_v122.sch" `
 "D:\Downloads\186_sample.xml" `
 "D:\Downloads"
```
First argument is path to schematron file. <br>
Second argument is path to document that should be validated. <br>
Third argument is unnecessary, it is path to directory for validation report saving. <br>

---

# Валидатор схематрона
Небольшое легковесное приложение для валидации xml-файлов при помощи схематрона. <br>
Проверка на соответствие структуре XSD не осуществляется, соответственно xml схемы для работы приложения не требуются.  <br>

Для сборки приложения со всеми зависимостями в один файл используется следующая команда:
``` 
mvn clean compile assembly:single
```

Пример консольной команды для запуска приложения (если установлена java):
```
java -jar .\target\schematron-validator-1.0-SNAPSHOT-jar-with-dependencies.jar `
 "D:\Downloads\186 Schematron_v122.sch" `
 "D:\Downloads\186_sample.xml" `
 "D:\Downloads"
```
Первый аргумент отвечает за адрес файла схематрона, в котором описаны правила для валидации документа. <br>
Второй аргумент отвечает за адрес документа, для которого проводится валидация. <br>
Третий аргумент не обязателен и отвечает за адрес директории, в которую будет сохранен отчет по валидации
и файл с перечнем ошибок, возникших в процессе валидации. <br>
