package by.antohakon.xmlimportertodb.service;

import java.util.List;

public interface XmlImporterService {

    // все имена таблиц какие есть в XML
    List<String> getAllTableNames(String xmlUrl);

    // возврат конкретно SQL текстом дял создания конкретной таблициы
    String getTableDDL(String tableName);

}
