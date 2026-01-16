package by.antohakon.xmlimportertodb.service;

import by.antohakon.xmlimportertodb.dto.OfferDto;
import by.antohakon.xmlimportertodb.repository.XmlImporterRepositoryImpl;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class XmlImporterServiceImpl implements XmlImporterService {

    private final XmlImporterRepositoryImpl repository;

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
                    // Получаем вложенные элементы
                    NodeList childNodes = rowElement.getChildNodes();
                    for (int k = 0; k < childNodes.getLength(); k++) {
                        Node childNode = childNodes.item(k);
                        if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                            String childName = childNode.getNodeName();
                            String childValue = childNode.getTextContent();
                            if (!columns.contains(childName)) {
                                columns.add(childName);
                                String columnType = getColumnType(childValue); // Определяем тип данных
                                ddl.append(childName).append(" ").append(columnType).append(", ");
                            }
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

    public String updateTablesFromParam(String tableName, String xmlUrl) {
        if (tableName != null && !tableName.isEmpty()) {
            switch (tableName) {
                case "offers":
                    updateOffers(tableName, xmlUrl);
                    break;
                case "currencies":
                    updateCurrencies(tableName, xmlUrl);
                    break;
                case "categories":
                    updateCategories(tableName, xmlUrl);
                    break;
                default:
                    return "Таблица не найдена";
            }
            return "Данные в таблице " + tableName + " обновлены";
        } else {
            // Запускаем общий метод
            update(xmlUrl);
            return "Данные во всех таблицах обновлены";
        }
    }

    public void updateOffers(String tableName, String xmlUrl) {
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
                return;
            }
            Element shop = (Element) shopNodes.item(0);
            log.info("Элемент найден");

            // Получаем элемент по имени таблицы
            NodeList tableNodes = shop.getElementsByTagName(tableName);
            if (tableNodes.getLength() == 0) {
                log.error("Таблица $tableName не найдена в XML-файле.");
                return;
            }
            Element table = (Element) tableNodes.item(0);

            // Получаем все предложения
            NodeList offerNodes = table.getChildNodes();
            for (int i = 0; i < offerNodes.getLength(); i++) {
                Node offerNode = offerNodes.item(i);
                if (offerNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element offer = (Element) offerNode;
                    // Получаем атрибуты предложения
                    String id = offer.getAttribute("id");
                    String available = offer.getAttribute("available");

                    // Получаем вложенные элементы предложения
                    NodeList offerChildren = offer.getChildNodes();
                    String url = "";
                    String price = "";
                    String currencyId = "";
                    String categoryId = "";
                    String picture = "";
                    String name = "";
                    String vendor = "";
                    String vendorCode = "";
                    String description = "";
                    String param = "";
                    String count = "";

                    for (int j = 0; j < offerChildren.getLength(); j++) {
                        Node childNode = offerChildren.item(j);
                        if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element childElement = (Element) childNode;
                            switch (childElement.getNodeName()) {
                                case "url":
                                    url = childElement.getTextContent();
                                    break;
                                case "price":
                                    price = childElement.getTextContent();
                                    break;
                                case "currencyId":
                                    currencyId = childElement.getTextContent();
                                    break;
                                case "categoryId":
                                    categoryId = childElement.getTextContent();
                                    break;
                                case "picture":
                                    picture = childElement.getTextContent();
                                    break;
                                case "name":
                                    name = childElement.getTextContent();
                                    break;
                                case "vendor":
                                    vendor = childElement.getTextContent();
                                    break;
                                case "vendorCode":
                                    vendorCode = childElement.getTextContent();
                                    break;
                                case "description":
                                    description = childElement.getTextContent();
                                    break;
                                case "param":
                                    param = childElement.getTextContent();
                                    break;
                                case "count":
                                    count = childElement.getTextContent();
                                    break;
                            }
                        }
                    }

                    OfferDto offerDto = OfferDto.builder()
                            .id(id)
                            .url(url)
                            .price(price)
                            .currencyId(currencyId)
                            .categoryId(categoryId)
                            .picture(picture)
                            .name(name)
                            .vendor(vendor)
                            .vendorCode(vendorCode)
                            .description(description)
                            .param(param)
                            .count(count)
                            .build();

                    repository.updateOffersDb(offerDto);
                }
            }

        } catch (Exception e) {
            log.error("Ошибка при обновлении данных в таблице offers: {}", e.getMessage(), e);
        }
    }



    public void updateCurrencies(String tableName, String xmlUrl) {
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
                return;
            }
            Element shop = (Element) shopNodes.item(0);
            log.info("Элемент найден");

            // Получаем элемент по имени таблицы
            NodeList tableNodes = shop.getElementsByTagName(tableName);
            if (tableNodes.getLength() == 0) {
                log.error("Таблица $tableName не найдена в XML-файле.");
                return;
            }
            Element table = (Element) tableNodes.item(0);

            // Получаем все валюты
            NodeList currencyNodes = table.getChildNodes();
            for (int i = 0; i < currencyNodes.getLength(); i++) {
                Node currencyNode = currencyNodes.item(i);
                if (currencyNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element currency = (Element) currencyNode;
                    // Получаем атрибуты валюты
                    String id = currency.getAttribute("id");
                    String rate = currency.getAttribute("rate");

                    repository.updateCurrenciesDb(id, rate);
                }
            }

        } catch (Exception e) {
            log.error("Ошибка при обновлении данных в таблице currencies: {}", e.getMessage(), e);
        }
    }




    public void updateCategories(String tableName, String xmlUrl) {
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
                return;
            }
            Element shop = (Element) shopNodes.item(0);
            log.info("Элемент найден");

            // Получаем элемент по имени таблицы
            NodeList tableNodes = shop.getElementsByTagName(tableName);
            if (tableNodes.getLength() == 0) {
                log.error("Таблица $tableName не найдена в XML-файле.");
                return;
            }
            Element table = (Element) tableNodes.item(0);

            // Получаем все категории
            NodeList categoryNodes = table.getChildNodes();
            for (int i = 0; i < categoryNodes.getLength(); i++) {
                Node categoryNode = categoryNodes.item(i);
                if (categoryNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element category = (Element) categoryNode;
                    // Получаем атрибуты категории
                    String id = category.getAttribute("id");
                    String parentId = category.getAttribute("parentId");
                    String name = category.getTextContent();

                    repository.updateCategoriesDb(id, parentId, name);
                }
            }

        } catch (Exception e) {
            log.error("Ошибка при обновлении данных в таблице categories: {}", e.getMessage(), e);
        }
    }


    public void update(String xmlUrl) {
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
                return;
            }
            Element shop = (Element) shopNodes.item(0);
            log.info("Элемент найден");

            // Обновляем данные во всех таблицах
            NodeList children = shop.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    String childName = child.getNodeName();
                    if ("currencies".equals(childName)) {
                        updateCurrencies(childName, xmlUrl);
                    } else if ("categories".equals(childName)) {
                        updateCategories(childName, xmlUrl);
                    } else if ("offers".equals(childName)) {
                        updateOffers(childName, xmlUrl);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Ошибка при парсинге XML: {}", e.getMessage(), e);
        }
    }

}



