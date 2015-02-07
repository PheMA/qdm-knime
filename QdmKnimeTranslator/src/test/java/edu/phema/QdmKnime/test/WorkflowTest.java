/**
 * 
 */
package edu.phema.QdmKnime.test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.bind.JAXBException;

import net.lingala.zip4j.exception.ZipException;

import org.junit.Test;

import edu.phema.QdmKnime.Aggregation;
import edu.phema.QdmKnime.Attribute;
import edu.phema.QdmKnime.Connection;
import edu.phema.QdmKnime.KnimeProject;
import edu.phema.QdmKnime.LogicalOperator;
import edu.phema.QdmKnime.QdmDataElement;
import edu.phema.QdmKnime.TemporalRelationship;
import edu.phema.QdmKnime.Toolkit;
import edu.phema.QdmKnimeInterfaces.LogicalRelationshipInterface.LogicalTypeCode;
import edu.phema.QdmKnimeInterfaces.TemporalRelationshipInterface.Operator;
import edu.phema.QdmKnimeInterfaces.TemporalRelationshipInterface.TemporalTypeCode;
import edu.phema.QdmKnimeInterfaces.TemporalRelationshipInterface.Unit;
import edu.phema.knime.exceptions.SetUpIncompleteException;
import edu.phema.knime.exceptions.WrittenAlreadyException;
import edu.phema.vsac.VsacConnector;

/**
 * @author Huan
 *
 */
public class WorkflowTest {

//	int currentNodeId = 0;
//	int currentConnectionId = 0;
	
//	private int newNode(){
//		return currentNodeId++;
//	}
	
//	private int newConnection(){
//		return currentConnectionId++;
//	}

	@Test
	public void smokeTest() throws IOException, WrittenAlreadyException, SetUpIncompleteException, ZipException, JAXBException {
		Path testPath = Paths.get(System.getProperty("java.io.tmpdir")).resolve("qdmKnime");
		testPath.toFile().mkdirs();

		String projectName = "foo";

		KnimeProject project = new KnimeProject (testPath, projectName);
		
		
		/*
		 * Set up the nodes
		 * */
				
//		int nodeAId = newNode();
		
		LogicalOperator nodeA = new LogicalOperator(LogicalTypeCode.AND);

		project.addKnimeNode(nodeA);
		
//		nodeA.setX(20);
//		nodeA.setY(20);
		
		
//		int nodeBId = newNode();
		LogicalOperator nodeB = new LogicalOperator(LogicalTypeCode.OR);
		
		project.addKnimeNode(nodeB);
		
//		nodeB.setX(150);
//		nodeB.setY(150);
		
		nodeB.setLeftElement(nodeA);
		
		
//		int nodeCId = newNode();
		TemporalRelationship nodeC = new TemporalRelationship(TemporalTypeCode.SBS);
		nodeC.setOperator(Operator.lessThanOrEqualTo);
		nodeC.setQuantity(120);
		nodeC.setUnit(Unit.days);
		
		project.addKnimeNode(nodeC);
		
//		nodeB.setX(150);
//		nodeB.setY(150);
		
		nodeB.setLeftElement(nodeA);
		
//		int nodeDId = newNode();
		QdmDataElement nodeD = new QdmDataElement();
		nodeD.setQdmDataType("Diagnosis Active");
		nodeD.setX(100);
		nodeD.setY(100);
		nodeD.setValueSet(Toolkit.readFile("src/test/resources/valueSet_infection.xml", 
				Charset.defaultCharset()));
		nodeD.setQdmDataElementText("Text Description from QDM");
		nodeD.addQdmAttributes("startDatetime", 
				"DateAndTimeCell", 
				"Start/admitting date (time). Please make sure data type is correct");
		nodeD.addQdmAttributes("stopDatetime", 
				"DateAndTimeCell", 
				"Stop/discharging date (time). Please make sure data type is correct");
		
		// Need to add configurations
		
		project.addKnimeNode(nodeD);
		
		//VsacConnector vsac = new VsacConnector("", ""); // UMLS username, passoword
		
//		int nodeEId = newNode();
		QdmDataElement nodeE = new QdmDataElement();
		nodeE.setQdmDataType("Medication Active");
		nodeE.setX(100);
		nodeE.setY(300);
		//nodeE.setValueSet(vsac.getValueSetJaxb("2.16.840.1.113883.3.117.1.7.1.824"));
		nodeE.setQdmDataElementText("check rxnorm");
		project.addKnimeNode(nodeE);
		
//		int nodeFId = newNode();
		Attribute nodeF = new Attribute();
		nodeF.setAttributeName("dose");
		nodeF.setInputElement(0, nodeE);
		nodeF.setMode_Comparison(null, 20.0);
		//nodeF.setMode_isPresent();
		
//		int nodeGId = newNode();
		Aggregation nodeG = new Aggregation();
		nodeG.setNodeText("COUNT >= 5");
		nodeG.setGroupByNodeText("COUNT");
		nodeG.setFilterNodeText(">= 5");
		project.addKnimeNode(nodeG);
		
		project.addKnimeNode(nodeF);
		
		/*
		 *  Now I am going to set up a connection
		 * */
		

//		int connAId = newConnection();
		Connection connA = new Connection();
		project.addKnimeConnection(connA);
		connA.setSource(nodeA, 1);
		connA.setDest(nodeB, 1);
//		connA.addBendpoint(100, 100);
		
//		int connBId = newConnection();
		Connection connB = new Connection();
		project.addKnimeConnection(connB);
		connB.setSource(nodeA, 1);
		connB.setDest(nodeC, 1);

//		int connCId = newConnection();
		Connection connC = new Connection();
		project.addKnimeConnection(connC);
		connC.setSource(nodeD, 0);
		connC.setDest(nodeA, 0);
		
//		int connDId = newConnection();
		Connection connD = new Connection();
		project.addKnimeConnection(connD);
		connD.setSource(nodeE, 0);
		connD.setDest(nodeF, 1);
		
		
		
		project.buildProject();
		
		System.out.println(project.getProjectDir().toString());
		
	}
}
