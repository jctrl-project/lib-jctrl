package com.atea.ictrl.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlParseHelper {
    private final DocumentBuilder builder;

    public XmlParseHelper() {
        // Document builder for the xml parsing
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            builder = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses the string to a sub tree of elements
     * @param xmlString
     * @return the root element
     */
    public Element parseXML(String xmlString) {
        try {
            // Retrieves and removes the first xml document in the queue
            // Converts the string to byte array
            ByteArrayInputStream xmlStream = new ByteArrayInputStream(xmlString.getBytes());

            // Document builder for the xml parsing
            Document xmlDocument = builder.parse(xmlStream);
            Node node = xmlDocument.getDocumentElement();

            // Check that the node is of the type element
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) node;
                return elem;
            } else {
                return null;
            }
        } catch (UnsupportedEncodingException e) {
            System.err.println("Ssh readXml: Unsupported encoding" + " used in readxml()");
            throw new RuntimeException(e);
        } catch (SAXException e) {
            System.err.println("Ssh readXml: UError in XML " + "document");
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.err.println("Ssh readXml: UError IO exception " + "from xml Document");
            throw new RuntimeException(e);
        }
    }
    /**
     * I take a xml element and the tag name, look for the tag and get
     * the text content 
     * i.e for <employee><name>John</name></employee> xml snippet if
     * the Element points to employee node and tagName is name I will return John  
     * @param ele
     * @param tagName
     * @return
     */
    public static String getTextValue(Element ele, String tagName) {
        String textVal = null;
        NodeList nl = ele.getElementsByTagName(tagName);
        if (nl != null && nl.getLength() > 0) {
            Element el = (Element) nl.item(0);
            textVal = el.getFirstChild().getNodeValue();
        }
        return textVal;
    }

    /**
     * Calls getTextValue and returns a int value
     * @param ele
     * @param tagName
     * @return
     */
    public static int getIntValue(Element ele, String tagName) {
        //in production application you would catch the exception
        return Integer.parseInt(getTextValue(ele, tagName));
    }


}
