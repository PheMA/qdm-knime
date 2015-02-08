/**
 * 
 */
package edu.phema.readers;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;

import edu.phema.QdmKnime.Aggregation;
import edu.phema.QdmKnime.Attribute;
import edu.phema.QdmKnime.Connection;
import edu.phema.QdmKnime.KnimeProject;
import edu.phema.QdmKnime.LogicalOperator;
import edu.phema.QdmKnime.MeasurePeriod;
import edu.phema.QdmKnime.QdmDataElement;
import edu.phema.QdmKnime.RelayNode;
import edu.phema.QdmKnime.TemporalRelationship;
import edu.phema.QdmKnime.Toolkit;
import edu.phema.QdmKnimeInterfaces.ConnectionInterface;
import edu.phema.QdmKnimeInterfaces.LogicalRelationshipInterface.LogicalTypeCode;
import edu.phema.QdmKnimeInterfaces.NodeInterface;
import edu.phema.QdmKnimeInterfaces.QdmDataElementInterface;
import edu.phema.QdmKnimeInterfaces.RelayNodeInterface.DataType;
import edu.phema.QdmKnimeInterfaces.TemporalRelationshipInterface;
import edu.phema.QdmKnimeInterfaces.TemporalRelationshipInterface.Operator;
import edu.phema.QdmKnimeInterfaces.TemporalRelationshipInterface.TemporalTypeCode;
import edu.phema.QdmKnimeInterfaces.TemporalRelationshipInterface.Unit;
import edu.phema.jaxb.ihe.svs.ConceptListType;
import edu.phema.jaxb.ihe.svs.RetrieveValueSetResponseType;
import edu.phema.knime.exceptions.SetUpIncompleteException;
import edu.phema.knime.exceptions.WrittenAlreadyException;
import edu.phema.vsac.VsacConnector;

/**
 * @author Huan Mo
 *
 */
public class HqmfJson2Knime {

	/**
	 * 
	 */
	
	private VsacConnector vsac;
	
	public HqmfJson2Knime(VsacConnector vsac) {
		// TODO Auto-generated constructor stub
		this.vsac = vsac;
	}
	
	public void translate(Path hqmfJsonPath, Path outputPath) 
			throws IOException, WrittenAlreadyException, SetUpIncompleteException, ParseException, JSONException {
		String jsonDoc = Toolkit.readFile(hqmfJsonPath.toString());
		final HqmfJson measure = new HqmfJson(jsonDoc);
		final KnimeProject kProject = new KnimeProject(outputPath.getParent(), outputPath.getFileName().toString());
		kProject.SET_UP_LAYOUT = true;
		//final ArrayList<NodeInterface> nodes = new ArrayList<NodeInterface>();  // the index will the knime id
		//final ArrayList<ConnectionInterface> conns = new ArrayList<ConnectionInterface>();
		final HashMap <String, QdmDataElementInterface> sourceDataCriteriaNodes = new HashMap <String, QdmDataElementInterface> ();
		final HashMap <String, NodeInterface> dataCriteriaNodes = new HashMap <String, NodeInterface> ();
		final HashMap <TemporalRelationshipInterface, String> rightSideOfTemporals = 
				new HashMap <TemporalRelationshipInterface, String>();
		final HashMap <String, QdmDataElementInterface> dataCriteriaFindSource = new HashMap <String, QdmDataElementInterface>();
		
		/*
		 * Measure Period object need to be created and linked to sourceDataCriteria every time it is used
		 * It cannot be reused
		 * */
		String measureStart = measure.getMeasureStartDatetime();  // yyyyMMddHHmm
		String measureEnd = measure.getMeasureEndDatetime();   // yyyyMMddHHmm
		
		RelayNode databaseMasterConnectionNode = new RelayNode(DataType.Database);
		kProject.addKnimeNode(databaseMasterConnectionNode);
		databaseMasterConnectionNode.setComment("Plug your database connector here");
		
		
		/*
		 * source_data_criteria
		 * */
		HashMap <String, Integer> sourceDataCriteriaAccesses = 
				measure.getSourceDataCrtieriaAccesses();
		for (String sourceDataCriteriaName : sourceDataCriteriaAccesses.keySet()){
			Integer access = sourceDataCriteriaAccesses.get(sourceDataCriteriaName);
			QdmDataElement element = new QdmDataElement();
			kProject.addKnimeNode(element);
			Connection conn = new Connection();
			conn.setDest(element, 0);
			conn.setSource(databaseMasterConnectionNode, 
					databaseMasterConnectionNode.getGoodOutPorts()[0]);
			kProject.addKnimeConnection(conn);
			sourceDataCriteriaNodes.put(sourceDataCriteriaName, element);
			String description = measure.getStringValue(access, "description");
			element.setQdmDataElementText(description);
			int endTypeIndex = description.matches(":") ? description.indexOf(":") - 1 : description.length() - 1;
			element.setQdmDataType(description.substring(0, endTypeIndex));
			String valueSet_oid = measure.getStringValue(access, "code_list_id");
			if (valueSet_oid != null) {
				try {
					element.setValueSet(vsac.getValueSetXml(valueSet_oid));
				} catch (JAXBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				};
			}
		}
		
		
		/*
		 * Data Criteria
		 * */
		HashMap <String, Integer> dataCriteriaAccesses = 
				measure.getDataCrtieriaAccesses();
		for (String dataCriteriaName : dataCriteriaAccesses.keySet()){
			Integer access = dataCriteriaAccesses.get(dataCriteriaName);
			QdmDataElementInterface sourceElement = sourceDataCriteriaNodes.get(
					measure.getStringValue(access, "source_data_criteria"));
			NodeInterface frontier = sourceElement; 
			dataCriteriaFindSource.put(dataCriteriaName, sourceElement);
			/*
			 * Data Criteria/Attribute: value
			 * */
			Integer valueAccess = measure.getJsonObjectRegistry(access, "value");
			if (valueAccess != null){
				String text = "value:%%00010" + measure.getIVL_PQDescription(valueAccess);
				String columnType = "unknown";
				Attribute attr_value = new Attribute();
				kProject.addKnimeNode(attr_value);
				Connection conn = new Connection();
				kProject.addKnimeConnection(conn);
				conn.setSource(frontier, frontier.getGoodOutPorts()[0]);
				conn.setDest(attr_value, 1);  // Port for native nodes start from 1
				attr_value.setInputElement(0, frontier.getOutputElement(0));
				attr_value.setAttributeName("value");
				if (measure.getStringValue(valueAccess, "type").equals("IVL_PQ")){
					columnType = "Double";
					Double[] high_low = measure.getHighLowOfIVL_PQ(valueAccess);
					attr_value.setMode_Comparison(high_low[0], high_low[1]);
				} else if (measure.getStringValue(valueAccess, "type").equals("CD")){
					columnType = "String";
					ArrayList<String> codes = new ArrayList<String>();
					ArrayList<String> displayNames = new ArrayList<String>();
					HashMap<String, String> cd = measure.getValueCDInDataCriteria(dataCriteriaName);
					String oid = cd.get("code_list_id");
					String code = cd.get("code");
					if (oid != null){
						JAXBElement<RetrieveValueSetResponseType> returnedValueSet = vsac.getValueSetJaxb(oid);
						codes.addAll(getCodes(returnedValueSet));
						displayNames.addAll(getCodeDisplayNames(returnedValueSet));
					} 
					if (code != null){
						codes.add(code);
					}
					ArrayList<String> allStrings = new ArrayList<String>();
					allStrings.addAll(codes);
					allStrings.addAll(displayNames);
					String regExp = "\\b(" + StringUtils.join(allStrings, '|') + ")\\b";
					attr_value.setMode_textTool(regExp, false, false, true);

				}
				sourceElement.addQdmAttributes("value", columnType, text);
				attr_value.setAnnotationText(text);
				frontier = attr_value; 
			}
			
			/*
			 * Data Criteria/Attributes: field_values
			 * */
			Integer fieldValueAccess = measure.getJsonObjectRegistry(access, "field_values");
			HashMap <String, Integer> fieldValuesAccesses = fieldValueAccess != null ?
					measure.getChildrenJSONObjectAccesses(fieldValueAccess) :
					new HashMap <String, Integer>();
			for (String fieldName : fieldValuesAccesses.keySet()){
				Integer fieldAccess = fieldValuesAccesses.get(fieldName);
				String text = fieldName + ":%%00010" + 
						measure.getIVL_PQDescription(fieldAccess);
				String columnType = "unknown";
				Attribute attr_value = new Attribute();
				kProject.addKnimeNode(attr_value);
				Connection conn = new Connection();
				kProject.addKnimeConnection(conn);
				conn.setSource(frontier, frontier.getGoodOutPorts()[0]);
				conn.setDest(attr_value, 1);  // Port for native nodes start from 1
				attr_value.setInputElement(0, frontier.getOutputElement(0));
				attr_value.setAttributeName(fieldName);
				if (measure.getStringValue(fieldAccess, "type").equals("IVL_PQ")){
					columnType = "Double";
					Double[] high_low = measure.getHighLowOfIVL_PQ(fieldAccess);
					attr_value.setMode_Comparison(high_low[0], high_low[1]);
				} else if (measure.getStringValue(fieldAccess, "type").equals("CD")){
					columnType = "String";
					ArrayList<String> codes = new ArrayList<String>();
					ArrayList<String> displayNames = new ArrayList<String>();
					//HashMap<String, String> cd = measure.getFieldValuesCDInDataCriteria(dataCriteriaName, fieldName);
					String oid = measure.getStringValue(fieldAccess, "code_list_id");
					String code = measure.getStringValue(fieldAccess, "code");
					if (oid != null){
						JAXBElement<RetrieveValueSetResponseType> returnedValueSet = vsac.getValueSetJaxb(oid);
						codes.addAll(getCodes(returnedValueSet));
						displayNames.addAll(getCodeDisplayNames(returnedValueSet));
					} 
					if (code != null){
						codes.add(code);
					}
					ArrayList<String> allStrings = new ArrayList<String>();
					allStrings.addAll(codes);
					allStrings.addAll(displayNames);
					String regExp = "\\b(" + StringUtils.join(allStrings, '|') + ")\\b";
					attr_value.setMode_textTool(regExp, false, false, true);
				}
				sourceElement.addQdmAttributes(fieldName, columnType, text);
				attr_value.setAnnotationText(text);
				frontier = attr_value; 
				
				
			}
			/*
			 * Data Criteria/temporal_references
			 * */
			int[] temporalsAccesses = measure.getJsonArrayRegestries(access, "temporal_references");
			//System.err.println(dataCriteriaName + " Numbers of temporals: " + numberOfTemporals);
			/*
			 * Temporal Step 1: make temporal nodes, and connect the left side
			 * 
			 * */
			for (int i = 0; temporalsAccesses != null && i < temporalsAccesses.length; i ++) {
				int temporalAcc = temporalsAccesses[i];
				String typeString = measure.getStringValue(temporalAcc, "type");
				String referenceString = measure.getStringValue(temporalAcc, "reference");
				TemporalTypeCode typeEnum = TemporalTypeCode.SBS;
				try{
					typeEnum = Enum.valueOf(TemporalTypeCode.class, typeString);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					System.err.println("Unrecognized temporal reference "
							+ typeString + " at " + dataCriteriaName);
					System.err.println("Please look at the SBS node. ");
				}
				if (typeEnum.REQUIRE_LEFT_START) {
					sourceElement.addQdmAttributes("startDatetime", "DateAndTimeCell", "Start timestamp");
				}
				if (typeEnum.REQUIRE_LEFT_END){
					sourceElement.addQdmAttributes("stopDatetime", "DateAndTImeCell", "Stop/End timestamp");
				}
				TemporalRelationship temporalNode = new TemporalRelationship(typeEnum);
				kProject.addKnimeNode(temporalNode);
				temporalNode.setLeftElement(frontier);
				Integer ivlPqAccess = measure.getTemporalRange_IVL_PQInDataCritieria(new Integer(temporalAcc));
				if (ivlPqAccess != null){
					ivlPqInTemporal(ivlPqAccess.intValue(), measure, temporalNode);
				}
				
				Connection connLeft = new Connection();
				int connLeftSourcePort = frontier.getGoodOutPorts()[0];
				connLeft.setSource(frontier, connLeftSourcePort);
				connLeft.setDest(temporalNode, 0);
				kProject.addKnimeConnection(connLeft);
				if (referenceString.equals("MeasurePeriod")){
					MeasurePeriod measurePeriodNode = new MeasurePeriod(measureStart, measureEnd);
					kProject.addKnimeNode(measurePeriodNode);
					Connection connMeasurePeriodIn = new Connection();
					kProject.addKnimeConnection(connMeasurePeriodIn);
					connMeasurePeriodIn.setSource(frontier, connLeftSourcePort);
					connMeasurePeriodIn.setDest(measurePeriodNode, 0);
					Connection connMeasurePeriodOut = new Connection();
					kProject.addKnimeConnection(connMeasurePeriodOut);
					connMeasurePeriodOut.setSource(measurePeriodNode, measurePeriodNode.getGoodOutPorts()[0]);
					connMeasurePeriodOut.setDest(temporalNode, 1);
					temporalNode.setRightElement(frontier.getOutputElement(connLeftSourcePort));
				} else {
					rightSideOfTemporals.put(temporalNode, referenceString);
				}
				frontier = temporalNode;
			}
			
			/*
			 * Data Criteria/subset_operators (aggregative)
			 * 
			 * */
			int[] subsetOperatorsAccesses = measure.getJsonArrayRegestries(access, "subset_operators");
			if (subsetOperatorsAccesses == null) {
				subsetOperatorsAccesses = new int[] {};
			}
			for (int operatorAccess : subsetOperatorsAccesses){
				String typeString = measure.getStringValue(operatorAccess, "type");
				Integer ivlPqAccess = measure.getJsonObjectRegistry(operatorAccess, "value");
				String ivlPqDesc = measure.getIVL_PQDescription(ivlPqAccess);
				Aggregation aggrNode = new Aggregation();
				kProject.addKnimeNode(aggrNode);
				aggrNode.setGroupByNodeText(typeString);
				aggrNode.setFilterNodeText(ivlPqDesc);
				Connection aggrConn = new Connection();
				kProject.addKnimeConnection(aggrConn);
				aggrConn.setSource(frontier, frontier.getGoodOutPorts()[0]);
				aggrConn.setDest(aggrNode, 0);
				frontier = aggrNode;
			}
			
			dataCriteriaNodes.put(dataCriteriaName, frontier);
		}
		
		/*
		 * Temporal Step 2: after all the data criteria nodes are created, add the right side of the
		 * temporal nodes
		 * When build nodeTrace, select the highest level source. 
		 * */
		
		for (TemporalRelationshipInterface temporalNode : 
			rightSideOfTemporals.keySet()
			.toArray(new TemporalRelationshipInterface[rightSideOfTemporals.size()])) {
			String referenceString = rightSideOfTemporals.get(temporalNode);
			//System.err.println(referenceString);
			NodeInterface rightNode = dataCriteriaNodes.get(referenceString);
			if (rightNode != null) {
				Connection connRight = new Connection();
				kProject.addKnimeConnection(connRight);
				int connRightSourcePort = rightNode.getGoodOutPorts()[0];
				connRight.setSource(rightNode, connRightSourcePort);
				connRight.setDest(temporalNode, 1);
				temporalNode.setRightElement(rightNode.getOutputElement(rightNode.getGoodOutPorts()[0]));
			}
			QdmDataElementInterface rightSourceElement = dataCriteriaFindSource.get(referenceString);
			if(temporalNode.getTemporalType().REQUIRE_RIGHT_START){
				rightSourceElement.addQdmAttributes("startDatetime", "DateAndTimeCell", "Start timestamp");
			}
			if (temporalNode.getTemporalType().REQUIRE_RIGHT_END){
				rightSourceElement.addQdmAttributes("stopDatetime", "DateAndTimeCell", "Stop/end timestamp");
			}
		}
		
		final HashMap<String, Integer> populationsAccesses = measure.getPopulationCriteriaAccesses();
		final HashMap<String, NodeInterface> populationCriteriaOut = 
				new HashMap<String, NodeInterface>();
		
		for (String population : populationsAccesses.keySet().toArray(
				new String[populationsAccesses.size()])){
			Integer access = populationsAccesses.get(population);
			
			/*
			 * Explore the logical tree recursively
			 * */
			ArrayList<NodeInterface> nodes = new ArrayList<NodeInterface> ();
			ArrayList<ConnectionInterface> conns = new ArrayList<ConnectionInterface> ();
			NodeInterface populationOut = HqmfJson2Knime.explorePopCriteriaTree(access, nodes, conns, dataCriteriaNodes, measure);
			kProject.addKnimeNodes(nodes);
			kProject.addKnimeConnections(conns);

			populationCriteriaOut.put(population, populationOut);
			if (populationOut != null) {
				HqmfJson2Knime.labelAPopulation(
						measure.getStringValue(access, "title"), populationOut, kProject);
			}
		}
		for (String population : populationCriteriaOut.keySet()){
			NodeInterface outNode = populationCriteriaOut.get(population);
			if (outNode != null){
				System.err.println(population + ": Node " + outNode.getId());
			}
		}
		
		NodeInterface nodeIPP = populationCriteriaOut.get("IPP");
		NodeInterface nodeDENOM = populationCriteriaOut.get("DENOM");
		NodeInterface nodeDENEX = populationCriteriaOut.get("DENEX");
		NodeInterface nodeDENEXCEP = populationCriteriaOut.get("DENEXCEP");
		NodeInterface nodeNUMER = populationCriteriaOut.get("NUMER");
		NodeInterface nodeDENEXCEP_modified = null;
		NodeInterface nodeDENOM_modified = nodeDENOM;
		NodeInterface nodeNUMER_modified = nodeNUMER;
		
		/*
		 * Population: IPP (initial population)
		 * 
		 * No actions are proposed
		 * 
		 * */

		
		/*
		 * Population: Denominator Exclusion (DENEX)
		 * 
		 * No actions are proposed
		 * 
		 * */
		
		/*
		 * Population: DENEXCEP (Denominator Exception)
		 * */
		if (nodeDENEXCEP != null && nodeNUMER != null){
			nodeDENEXCEP_modified = 
					HqmfJson2Knime.logicTwoNodes(nodeDENEXCEP, nodeNUMER, 
						LogicalTypeCode.AND_NOT, kProject);;
		}
		
		/*
		 * Population: Denominator (DENOM)
		 * */
		if (nodeDENOM != null && nodeIPP != null){
			nodeDENOM_modified = 
					HqmfJson2Knime.logicTwoNodes(nodeIPP, nodeDENOM_modified, 
						LogicalTypeCode.AND, kProject);
		} else if (nodeIPP != null) {
			nodeDENOM_modified = nodeIPP;
		}
		
		if (nodeDENOM_modified != null && nodeDENEX != null) {
			nodeDENOM_modified = HqmfJson2Knime.logicTwoNodes(
					nodeDENOM_modified, nodeDENEX, 
					LogicalTypeCode.AND_NOT, kProject);
		}
		
		if (nodeDENOM_modified != null && nodeDENEXCEP != null) {
			nodeDENOM_modified = 
					HqmfJson2Knime.logicTwoNodes(nodeDENOM_modified, nodeDENEXCEP_modified, 
							LogicalTypeCode.AND_NOT, kProject);
		}
		
		if (nodeDENOM_modified != null) {
			HqmfJson2Knime.labelAPopulation("Denominator (Real)", nodeDENOM_modified, kProject);
		}
		
		
		/*
		 * Populations: Numerator (NUMER)
		 * */
		
		if (nodeNUMER != null && nodeDENOM_modified != null){
			nodeNUMER_modified = HqmfJson2Knime.logicTwoNodes(nodeNUMER, nodeDENOM_modified, LogicalTypeCode.AND, kProject);;
			HqmfJson2Knime.labelAPopulation("Numerator (Real)", nodeNUMER_modified, kProject);
		}
		
		/*
		 * eMeasures with Continuous Variables
		 * */
		
		NodeInterface nodeMSRPOPL = populationCriteriaOut.get("MSRPOPL"); // Measure Population
		NodeInterface nodeMSRPOPL_modified = null;
		NodeInterface nodeOBSERV = populationCriteriaOut.get("OBSERV"); // Meassure Observation
		NodeInterface nodeOBSERV_modified = null;

		
		/*
		 * Population: Measure Population (MSRPOPL)
		 * */
		if (nodeMSRPOPL != null && nodeIPP != null) {
			nodeMSRPOPL_modified = 
					HqmfJson2Knime.logicTwoNodes(nodeMSRPOPL, nodeIPP, LogicalTypeCode.AND, kProject);
		} else {
			nodeMSRPOPL_modified = nodeMSRPOPL == null ? nodeMSRPOPL : nodeIPP;
		}
		if (nodeMSRPOPL_modified != null){
			HqmfJson2Knime.labelAPopulation(
				"Measure Population (Real)", nodeMSRPOPL_modified, kProject);
		}
		
		if (nodeOBSERV != null && nodeMSRPOPL_modified != null){
			nodeOBSERV_modified = HqmfJson2Knime.logicTwoNodes(
					nodeOBSERV, nodeMSRPOPL_modified, LogicalTypeCode.AND, kProject);
		} 
		if (nodeOBSERV_modified != null) {
			Integer observAccess = populationsAccesses.get("OBSERV");
			if (observAccess != null) {
				String aggregator = measure.getStringValue(observAccess, "aggregator");
				if (aggregator != null) {
					Aggregation aggr = new Aggregation();
					kProject.addKnimeNode(aggr);
					aggr.setGroupByNodeText(aggregator);
					aggr.setFilterNodeText("Useless");
					Connection aggrConn = new Connection();
					kProject.addKnimeConnection(aggrConn);
					aggrConn.setSource(nodeOBSERV_modified, 
							nodeOBSERV_modified.getGoodOutPorts()[0]);
					aggrConn.setDest(aggr, 0);
					aggr.setInputElement(nodeOBSERV_modified);
					nodeOBSERV_modified = aggr;
				}
			}
			HqmfJson2Knime.labelAPopulation("Measure Observation (Real)", nodeOBSERV_modified, kProject);
		}

		/*
		 * Population: Measure Observation
		 * 
		 * */
		
		/*
		 * Build Knime project
		 * */
		try {
			kProject.buildProject();
		} catch (ZipException | JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void ivlPqInTemporal (Integer ivlPqAccess, HqmfJson measure, TemporalRelationshipInterface temporalNode) {
		Integer highPqAccess = measure.getJsonObjectRegistry(ivlPqAccess, "high");
		Integer lowPqAccess = measure.getJsonObjectRegistry(ivlPqAccess, "low");
		Integer pqAccess = null;
		
		if(highPqAccess != null){
			if (measure.getBooleanValue(highPqAccess, "inclusive?")){
				temporalNode.setOperator(Operator.lessThanOrEqualTo);
			} else {
				temporalNode.setOperator(Operator.lessThan);
			}
			pqAccess = highPqAccess;
			
		} else if (lowPqAccess != null){
			if (measure.getBooleanValue(lowPqAccess, "inclusive?")){
				temporalNode.setOperator(Operator.greaterThanOrEqualTo);
			} else {
				temporalNode.setOperator(Operator.greaterThan);
			}
			pqAccess = lowPqAccess;
		} 
		if (pqAccess != null) {
			
			String valueRe = measure.getStringValue(pqAccess, "value");
			if (valueRe != null){
				temporalNode.setQuantity(Integer.valueOf(valueRe));
			}
			
			Unit unitEnum = Toolkit.timeUnits(measure.getStringValue(pqAccess, "unit"));
			
			if (unitEnum != null) {
				temporalNode.setUnit(unitEnum);
			}
			
		}
		
	}
	
	private static NodeInterface explorePopCriteriaTree(
			int currentParentOfPreconditionsAccess,
			ArrayList<NodeInterface> nodes, 
			ArrayList<ConnectionInterface> conns,
			HashMap<String, NodeInterface> dataCriteria, 
			HqmfJson measure){
		NodeInterface frontier = null;
		boolean frontierNegated = false;
		String conjunctionCode = measure.getStringValue(
				currentParentOfPreconditionsAccess, "conjunction_code");
		LogicalTypeCode logicType = conjunctionCode != null && conjunctionCode.equals("allTrue") ? 
				LogicalTypeCode.AND : LogicalTypeCode.OR;
		int[] preconditionsAccesses = measure.getJsonArrayRegistries(
				currentParentOfPreconditionsAccess, "preconditions");
		if (preconditionsAccesses == null){
			preconditionsAccesses = new int[]{};
		}
		String reference = measure.getStringValue(currentParentOfPreconditionsAccess, "reference");
		if (reference != null) {
			frontier = dataCriteria.get(reference);
			Boolean negated = measure.getBooleanValue(currentParentOfPreconditionsAccess, "negation");
			frontierNegated = negated != null && negated.booleanValue();
		}
		for (int access : preconditionsAccesses){
			NodeInterface leftNode = frontier;
			boolean leftNegated = frontierNegated;
			NodeInterface rightNode = explorePopCriteriaTree(
					access, nodes, conns, dataCriteria, measure);
			boolean rightNegated = measure.getBooleanValue(access, "negation") != null &&  
					measure.getBooleanValue(access, "negation").booleanValue() ;
			if (leftNode == null){
				frontier = rightNode;
				frontierNegated = rightNegated;
			} else {
				LogicalOperator newNode;
				if (leftNegated || rightNegated) {
					newNode = 
							new LogicalOperator(LogicalTypeCode.AND_NOT);
				} else {
					newNode = 
							new LogicalOperator(logicType);
				}
				nodes.add(newNode);
				Connection connLeft = new Connection();
				connLeft.setDest(newNode, 0);
				conns.add(connLeft);
				Connection connRight = new Connection();
				connRight.setDest(newNode, 1);
				conns.add(connRight);
				int[] sourcePorts = LogicalOperator.findGoodPortPair(leftNode, rightNode);
				if (leftNegated && ! rightNegated){
					/*
					 * flip left and right
					 * */
					connRight.setSource(leftNode, sourcePorts[0]);
					newNode.setRightElement(leftNode.getOutputElement(sourcePorts[0]));
					connLeft.setSource(rightNode, sourcePorts[1]);
					newNode.setLeftElement(rightNode.getOutputElement(sourcePorts[1]));
				} else {
					connLeft.setSource(leftNode, sourcePorts[0]);
					newNode.setLeftElement(leftNode.getOutputElement(sourcePorts[0]));
					connRight.setSource(rightNode, sourcePorts[1]);
					newNode.setRightElement(rightNode.getOutputElement(sourcePorts[1]));
				}
				frontier = newNode;
			}
			
			
		}
		
		
		
		return frontier;
	}
	
	private static void labelAPopulation (String label, NodeInterface frontier, KnimeProject kProject) {
		RelayNode labelNode = new RelayNode(DataType.Data);
		kProject.addKnimeNode(labelNode);
		labelNode.setComment(label);
		int frontierPort = frontier.getGoodOutPorts()[0];
		labelNode.setInputElement(0, frontier.getOutputElement(frontierPort));
		Connection labelConn = new Connection();
		labelConn.setSource(frontier, frontierPort);
		labelConn.setDest(labelNode, 0);
		kProject.addKnimeConnection(labelConn);
	}
	
	private static NodeInterface logicTwoNodes(
			NodeInterface leftNode, NodeInterface rightNode, LogicalTypeCode logic,
			KnimeProject kProject){
		NodeInterface re = null;
		if (leftNode != null && rightNode != null){
			LogicalOperator logicNode = new LogicalOperator(logic);
			kProject.addKnimeNode(logicNode);
			int[] goodPorts = LogicalOperator.findGoodPortPair(leftNode, rightNode);
			logicNode.setLeftElement(leftNode.getOutputElement(goodPorts[0]));
			logicNode.setRightElement(rightNode.getOutputElement(goodPorts[1]));
			Connection connLeft = new Connection();
			kProject.addKnimeConnection(connLeft);
			connLeft.setSource(leftNode, goodPorts[0]);
			connLeft.setDest(logicNode, 0);
			Connection connRight = new Connection();
			kProject.addKnimeConnection(connRight);
			connRight.setSource(rightNode, goodPorts[1]);
			connRight.setDest(logicNode, 1);
			re = logicNode;
		} else {
			re = leftNode != null ? leftNode : rightNode;
		}
		return re;
	}
	
	private static ArrayList<String> getCodeDisplayNames(JAXBElement<RetrieveValueSetResponseType> vsacXmlJaxb){
		ArrayList<String> re = new ArrayList<String>();
		
		for(int i = 0; 
				i < vsacXmlJaxb.getValue().getValueSet().getConceptList().size(); 
				i++){
			ConceptListType concepts = 
					vsacXmlJaxb.getValue().getValueSet().getConceptList().get(i);
			for (int j = 0; j < concepts.getConcept().size(); j ++){
				re.add(concepts.getConcept().get(j).getDisplayName());
			}
		}
		return re;
	}
	
	private static ArrayList<String> getCodes(JAXBElement<RetrieveValueSetResponseType> vsacXmlJaxb){
		ArrayList<String> re = new ArrayList<String>();
		
		for(int i = 0; 
				i < vsacXmlJaxb.getValue().getValueSet().getConceptList().size(); 
				i++){
			ConceptListType concepts = 
					vsacXmlJaxb.getValue().getValueSet().getConceptList().get(i);
			for (int j = 0; j < concepts.getConcept().size(); j ++){
				re.add(concepts.getConcept().get(j).getCode());
			}
		}
		return re;
	}

	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws JSONException 
	 * @throws ParseException 
	 * @throws SetUpIncompleteException 
	 * @throws WrittenAlreadyException 
	 */
	public static void main(String[] args) throws IOException, WrittenAlreadyException, SetUpIncompleteException, ParseException, JSONException {
		// TODO Auto-generated method stub
		Path hqmfJsonFile1 = Paths.get("src/test/resources/cypress-bundle-latest/sources/eh/CMS30v4/hqmf_model.json");
		Path outputDir1 = Paths.get(System.getProperty("java.io.tmpdir")).resolve("qdmKnime/CMS30v4");
		
		Path hqmfJsonFile2 = Paths.get("src/test/resources/cypress-bundle-latest/sources/ep/CMS179v3/hqmf_model.json");
		Path outputDir2 = Paths.get(System.getProperty("java.io.tmpdir")).resolve("qdmKnime/CMS179v3");
		
		Path hqmfJsonFile = hqmfJsonFile1;
		Path outputDir = outputDir1;
		
		if (args.length == 2) {
			hqmfJsonFile = Paths.get(args[0]);
			outputDir = Paths.get(args[1]);
		}
		if(outputDir.toFile().exists()){
			FileUtils.deleteDirectory(outputDir.toFile());
		}
		
		if (Paths.get(outputDir.toString() + ".zip").toFile().exists()) {
			Paths.get(outputDir.toString() + ".zip").toFile().delete();
		}
		HqmfJson2Knime translator = new HqmfJson2Knime(new VsacConnector("henryhmo", "2525WestEnd"));
		translator.translate(hqmfJsonFile, outputDir);
		System.out.println(outputDir.toString());
	}

}
