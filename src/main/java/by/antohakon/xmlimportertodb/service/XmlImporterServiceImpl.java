package by.antohakon.xmlimportertodb.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Service
@Slf4j
public class XmlImporterServiceImpl implements XmlImporterService {


    @Override
    public List<String> getAllTableNames(String xmlUrl) {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new URL(xmlUrl).openStream());
            document.getDocumentElement().normalize();

            // Получаем корневой элемент <yml_catalog>
            Element root = document.getDocumentElement();

            // Получаем элемент <shop>
            NodeList shopNodes = root.getElementsByTagName("shop");
            if (shopNodes.getLength() == 0) {
                log.error("Элемент <shop> не найден в XML-файле.");
                return Collections.emptyList();
            }
            Element shop = (Element) shopNodes.item(0);
            log.info("элемент найден ");

            List<String> result = new ArrayList<>();
            NodeList children = shop.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    String childName = child.getNodeName();
                    if ("currencies".equals(childName) || "categories".equals(childName) || "offers".equals(childName)) {
                        result.add(childName);
                    }
                }
            }
            log.info("таблицы={}", result);
            return result;

        } catch (Exception e) {
            log.error("Ошибка при парсинге XML: {}", e.getMessage(), e);
            return Collections.emptyList();
        }

    }

    @Override
    public String getTableDDL(String xmlUrl, String tableName) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new URL(xmlUrl).openStream());
            document.getDocumentElement().normalize();

            // Получаем корневой элемент <yml_catalog>
            Element root = document.getDocumentElement();

            // Получаем элемент <shop>
            NodeList shopNodes = root.getElementsByTagName("shop");
            if (shopNodes.getLength() == 0) {
                log.error("Элемент <shop> не найден в XML-файле.");
                return null;
            }
            Element shop = (Element) shopNodes.item(0);
            log.info("Элемент найден");

            // Получаем элемент по имени таблицы
            NodeList tableNodes = shop.getElementsByTagName(tableName);
            if (tableNodes.getLength() == 0) {
                log.error("Таблица $tableName не найдена в XML-файле.");
                return null;
            }
            Element table = (Element) tableNodes.item(0);

            // Создаем SQL для создания таблицы
            StringBuilder ddl = new StringBuilder();
            ddl.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");

            // Получаем вложенные элементы
            NodeList rows = table.getChildNodes();
            HashSet<String> columns = new HashSet<>();
            for (int i = 0; i < rows.getLength(); i++) {
                Node row = rows.item(i);
                if (row.getNodeType() == Node.ELEMENT_NODE) {
                    Element rowElement = (Element) row;
                    // Получаем атрибуты элемента
                    NamedNodeMap attributes = rowElement.getAttributes();
                    for (int j = 0; j < attributes.getLength(); j++) {
                        Node attribute = attributes.item(j);
                        String columnName = attribute.getNodeName();
                        String columnValue = attribute.getNodeValue();
                        if (!columns.contains(columnName)) {
                            columns.add(columnName);
                            String columnType = getColumnType(columnValue); // Определяем тип данных
                            ddl.append(columnName).append(" ").append(columnType).append(", ");
                        }
                    }
                }
            }
            ddl.setLength(ddl.length() - 2); // Удаляем последнюю запятую
            ddl.append(");");

            log.info("DDL={}", ddl.toString());
            return ddl.toString();

        } catch (Exception e) {
            log.error("Ошибка при парсинге XML: {}", e.getMessage(), e);
            return null;
        }
    }


    private String getColumnType(String value) {
        try {
            // Попытка преобразовать значение в число
            Integer.parseInt(value);
            return "INT";
        } catch (NumberFormatException e) {
            // Попытка преобразовать значение в логическое значение
            if ("true".equals(value) || "false".equals(value)) {
                return "BOOLEAN";
            }
            // Если значение не является числом или логическим значением, то оно текстовое
            return "VARCHAR(255)";
        }
    }
}

