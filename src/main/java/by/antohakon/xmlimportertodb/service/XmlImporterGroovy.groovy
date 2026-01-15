package by.antohakon.xmlimportertodb.service

import groovy.xml.slurpersupport.GPathResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.xml.sax.SAXException

import javax.xml.parsers.SAXParserFactory

class XmlImporterGroovy {

    private static final Logger log = LoggerFactory.getLogger(XmlParser.class);

    public List<String> getTableNames(String xmlUrl) {
        try {
            def xmlSlurper = new XmlSlurper(false, false)

            xmlSlurper.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
            xmlSlurper.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            xmlSlurper.setFeature("http://xml.org/sax/features/external-general-entities", false);
            xmlSlurper.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            def xml = xmlSlurper.parse(new URL(xmlUrl).openStream())

            // Получаем элемент <shop>
            def shop = xml.shop

            // Преобразуем в List<String>
            def result = []
            shop.children().each { node ->
                if (node.name() in ['currencies', 'categories', 'offers']) {
                    result << node.name()
                }
            }
            return result

        } catch (IOException | SAXException e) {
            log.error("Ошибка при чтении XML-файла: {}", e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Ошибка при парсинге XML: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

}
