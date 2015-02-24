package edu.phema.QdmKnime.test;

import java.io.FileInputStream;
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

import edu.phema.QdmKnime.Toolkit;

public class XpathTry {

	public XpathTry() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
		// TODO Auto-generated method stub
		
		Path settings_xml = Paths.get("src/main/resources/metaNodeRepos/temporalRelationships/CONCURRENT/workflow.knime");
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(settings_xml.toFile());
		//System.out.print(Toolkit.readFile(settings_xml.toString()));
		
		XPathExpression xpath = XPathFactory.newInstance().newXPath().compile(
				"/config[@key=\"workflow.knime\"]/config[@key=\"annotations\"]/config[@key=\"annotation_0\"]/entry[@key=\"text\"]");
		Node nnode = (Node) xpath.evaluate(doc, XPathConstants.NODE);
		String text = nnode.getAttributes().getNamedItem("value").getTextContent();
		System.out.println(text);
	}

}
