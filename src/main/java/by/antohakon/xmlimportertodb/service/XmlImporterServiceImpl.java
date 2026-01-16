package by.antohakon.xmlimportertodb.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
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
            return result;

        } catch (Exception e) {
            log.error("Ошибка при парсинге XML: {}", e.getMessage(), e);
            return Collections.emptyList();
        }

    }

    @Override
    public String getTableDDL(String tableName) {
        return "";
    }
}
