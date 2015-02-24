package edu.phema.QdmKnime.test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XpathTry {

	public XpathTry() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
		// TODO Auto-generated method stub
		
		Path settings_xml = Paths.get("src/main/resources/metaNodeRepos/temporalRelationships/SAE/Time Difference (#68)/settings.xml");
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(settings_xml.toFile());
		XPathExpression granularity = XPathFactory.newInstance().newXPath().compile("/config/config[@key=\"model\"]/entry[@key=\"granularity\"]");
		Node nl = (Node) granularity.evaluate(doc, XPathConstants.NODE);
		nl.getAttributes().getNamedItem("value").setTextContent("Week");;
		System.out.println(nl.getAttributes().getNamedItem("value").getNodeValue());
	}

}
