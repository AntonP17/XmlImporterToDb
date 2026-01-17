package by.antohakon.xmlimportertodb.repository;

import java.util.List;

public interface XmlImporterRepository {

    // обновляет все таблицы в бд на основе изменений в XML
    void updateAllTables();

    // обноваляет конкретную таблицу из XML по имени
    void updateTableByName(String tableName);

    //дополнительно
    List<String> getColumnNames();

    boolean isColumnId(String tablName, String columnName);

    String getDDlChange(String tableName);

}
