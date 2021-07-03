package fun.lula.flomo.util;


import fun.lula.flomo.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class XmlUtil {

    public static Map<String, String> parseXmlToMap(String message) {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = builderFactory.newDocumentBuilder();
            Document document = builder.parse(IOUtils.toInputStream(message, Charset.defaultCharset()));
            Element element = document.getDocumentElement();
            NodeList childNodes = element.getChildNodes();

            Map<String, String> map = new LinkedHashMap<>();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node instanceof Element) {
                    map.put(node.getNodeName(), node.getTextContent());
                }
            }
            return map;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            throw new ServiceException("xml 解析错误");
        }
    }
}
