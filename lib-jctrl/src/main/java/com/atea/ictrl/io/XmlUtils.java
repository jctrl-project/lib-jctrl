/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.io;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Martin
 */
public class XmlUtils {
    public static Document createDocument() {
        try {
            DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            Document doc = docBuilder.newDocument();            
            return doc;            
        } catch (ParserConfigurationException ex) {
            System.err.println(XmlUtils.class.getName() + ex);
        }
        return null;
    }
    public static String xmlToString(Document doc) {
        try {
            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperty(OutputKeys.INDENT, "no");            
            trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            //create string from xml tree
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            DOMSource source = new DOMSource(doc);
            trans.transform(source, result);
            return sw.toString();
        } catch (Exception ex) {
            System.err.println(XmlUtils.class.getName() + ex);
            ex.printStackTrace();
        }
        return "";
    }
    public static Element createElement(Document doc, int item, String parameter, String value) {
        Element element = doc.createElement(parameter);
        element.setAttribute("item", String.valueOf(item));
        element.appendChild(doc.createTextNode(value));
        return element;
    }
    public static Element createElement(Document doc, int item, String parameter, int value) {
        Element element = doc.createElement(parameter);
        element.setAttribute("item", String.valueOf(item));
        element.appendChild(doc.createTextNode(String.valueOf(value)));
        return element;
    }
    public static Element createElement(Document doc, int item, String parameter, double value) {
        Element element = doc.createElement(parameter);
        element.setAttribute("item", String.valueOf(item));
        element.appendChild(doc.createTextNode(String.valueOf(value)));
        return element;
    }
    public static Element createElement(Document doc, int item, String parameter, long value) {
        Element element = doc.createElement(parameter);
        element.setAttribute("item", String.valueOf(item));
        element.appendChild(doc.createTextNode(String.valueOf(value)));
        return element;
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
    public static boolean getBooleanValue(Element ele, String tagName) {
        //in production application you would catch the exception
        return Boolean.parseBoolean(getTextValue(ele, tagName));
    }
    
}
