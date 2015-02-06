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
import org.json.JSONObject;

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
			throws IOException, JSONException, WrittenAlreadyException, SetUpIncompleteException, ParseException {
		String jsonDoc = Toolkit.readFile(hqmfJsonPath.toString());
		final HqmfJson measure = new HqmfJson(jsonDoc);
		final KnimeProject kProject = new KnimeProject(outputPath.getParent(), outputPath.getFileName().toString());
		kProject.SET_UP_LAYOUT = true;
		final ArrayList<NodeInterface> nodes = new ArrayList<NodeInterface>();  // the index will the knime id
		final ArrayList<ConnectionInterface> conns = new ArrayList<ConnectionInterface>();
		final HashMap <String, QdmDataElementInterface> sourceDataCriteriaNodes = new HashMap <String, QdmDataElementInterface> ();
		final HashMap <String, NodeInterface> dataCriteriaNodes = new HashMap <String, NodeInterface> ();
		final HashMap <TemporalRelationshipInterface, String> rightSideOfTemporals = 
				new HashMap <TemporalRelationshipInterface, String>();
		
		/*
		 * Measure Period object need to be created and linked to sourceDataCriteria every time it is used
		 * It cannot be reused
		 * */
		String measureStart = measure.getMeasureStartDatetime();  // yyyyMMddHHmm
		String measureEnd = measure.getMeasureEndDatetime();   // yyyyMMddHHmm
		
		RelayNode databaseMasterConnectionNode = new RelayNode(nodes.size(), DataType.Database);
		nodes.add(databaseMasterConnectionNode);
		databaseMasterConnectionNode.setComment("Plug your database connector here");
		
		for (String sourceDataCriteriaName : measure.getSourceDataCrtieriaList()){
			QdmDataElement element = new QdmDataElement(nodes.size());
			nodes.add(element);
			Connection conn = new Connection(conns.size());
			conn.setDest(element, 0);
			conn.setSource(databaseMasterConnectionNode, 
					databaseMasterConnectionNode.getGoodOutPorts()[0]);
			conns.add(conn);
			sourceDataCriteriaNodes.put(sourceDataCriteriaName, element);
			String description = measure.getSourceDataCriteriaInfo(sourceDataCriteriaName, "description");
			element.setQdmDataElementText(description);
			int endTypeIndex = description.matches(":") ? description.indexOf(":") - 1 : description.length() - 1;
			element.setQdmDataType(description.substring(0, endTypeIndex));
			String valueSet_oid = measure.getSourceDataCriteriaInfo(sourceDataCriteriaName, "code_list_id");
			if (valueSet_oid != null) {
				try {
					element.setValueSet(vsac.getValueSetXml(valueSet_oid));
				} catch (JAXBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				};
			}
		}
		
		for (String dataCriteriaName : measure.getDataCrtieriaList()){
			QdmDataElementInterface sourceElement = sourceDataCriteriaNodes.get(
					measure.getDataCriteriaInfo(dataCriteriaName, "source_data_criteria"));
			NodeInterface frontier = sourceElement; 
			if (! measure.typeOfValueInDataCriteria(dataCriteriaName).equals("")){
				String text = measure.getTextOfValueInDataCriteria(dataCriteriaName);
				String columnType = "unknown";
				Attribute attr_value = new Attribute(nodes.size());
				nodes.add(attr_value);
				Connection conn = new Connection(conns.size());
				conns.add(conn);
				conn.setSource(frontier, frontier.getGoodOutPorts()[0]);
				conn.setDest(attr_value, 1);  // Port for native nodes start from 1
				attr_value.setInputElementId(0, frontier.getOutputElementId(0));
				//nodeTrace.put(attr_value, frontier);
				attr_value.setAttributeName("value");
				if (measure.typeOfValueInDataCriteria(dataCriteriaName).equals("IVL_PQ")){
					columnType = "Double";
					Double[] high_low = measure.getValueIVL_PQInDataCriteriaHL(dataCriteriaName);
					attr_value.setMode_Comparison(high_low[0], high_low[1]);
				} else if (measure.typeOfValueInDataCriteria(dataCriteriaName).equals("CD")){
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
			 * field_values
			 * */
			for (String fieldName : measure.getDataCriteriaFieldsList(dataCriteriaName)){
				String text = measure.getTextOfFieldValuesInDataCriteria(dataCriteriaName, fieldName);
				String columnType = "unknown";
				Attribute attr_value = new Attribute(nodes.size());
				nodes.add(attr_value);
				Connection conn = new Connection(conns.size());
				conns.add(conn);
				conn.setSource(frontier, frontier.getGoodOutPorts()[0]);
				conn.setDest(attr_value, 1);  // Port for native nodes start from 1
				attr_value.setInputElementId(0, frontier.getOutputElementId(0));
				//nodeTrace.put(attr_value, frontier);
				attr_value.setAttributeName(fieldName);
				if (measure.typeOfFieldValueInDataCriteria(dataCriteriaName, fieldName).equals("IVL_PQ")){
					columnType = "Double";
					Double[] high_low = measure.getFieldValues_IVL_PQInDataCriteriaHL(dataCriteriaName, fieldName);
					attr_value.setMode_Comparison(high_low[0], high_low[1]);
				} else if (measure.typeOfFieldValueInDataCriteria(dataCriteriaName, fieldName).equals("CD")){
					columnType = "String";
					ArrayList<String> codes = new ArrayList<String>();
					ArrayList<String> displayNames = new ArrayList<String>();
					HashMap<String, String> cd = measure.getFieldValuesCDInDataCriteria(dataCriteriaName, fieldName);
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
				sourceElement.addQdmAttributes(fieldName, columnType, text);
				attr_value.setAnnotationText(text);
				frontier = attr_value; 
				
				
			}
			/*
			 * temporal_references
			 * */
			int numberOfTemporals = measure.getNumberOfTemporalRefsInDataCriteria(dataCriteriaName);
			//System.err.println(dataCriteriaName + " Numbers of temporals: " + numberOfTemporals);
			/*
			 * Temporal Step 1: make temporal nodes, and connect the left side, update trace
			 * */
			if (numberOfTemporals > 0) {
				HashMap <String, String> temporalReference = 
						measure.getTemporalRefInDataCritieria(dataCriteriaName, 0);
				String typeString = temporalReference.get("type");
				String referenceString = temporalReference.get("reference");
				TemporalTypeCode typeEnum = TemporalTypeCode.SBS;
				try{
					typeEnum = Enum.valueOf(TemporalTypeCode.class, typeString);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					System.err.println("Unrecognized temporal reference "
							+ typeString + " at " + dataCriteriaName);
					System.err.println("Please look at the SBS node. ");
				}
				
				TemporalRelationship temporalNode = new TemporalRelationship(
						nodes.size(), typeEnum);
				nodes.add(temporalNode);
				temporalNode.setLeftId(frontier.getId());
				JSONObject ivlPq = measure.getTemporalRange_IVL_PQInDataCritieria(dataCriteriaName, 0);
				if (ivlPq != null){
					ivlPqInTemporal(ivlPq, temporalNode);
				}
				
				Connection connLeft = new Connection(conns.size());
				int connLeftSourcePort = frontier.getGoodOutPorts()[0];
				connLeft.setSource(frontier, connLeftSourcePort);
				connLeft.setDest(temporalNode, 0);
				conns.add(connLeft);
				if (referenceString.equals("MeasurePeriod")){
					MeasurePeriod measurePeriodNode = new MeasurePeriod(
								nodes.size(), measureStart, measureEnd);
					nodes.add(measurePeriodNode);
					Connection connMeasurePeriodIn = new Connection(conns.size());
					conns.add(connMeasurePeriodIn);
					connMeasurePeriodIn.setSource(frontier, connLeftSourcePort);
					connMeasurePeriodIn.setDest(measurePeriodNode, 0);
					Connection connMeasurePeriodOut = new Connection(conns.size());
					conns.add(connMeasurePeriodOut);
					connMeasurePeriodOut.setSource(measurePeriodNode, measurePeriodNode.getGoodOutPorts()[0]);
					connMeasurePeriodOut.setDest(temporalNode, 1);
					temporalNode.setRightId(frontier.getOutputElementId(connLeftSourcePort));
				} else {
					rightSideOfTemporals.put(temporalNode, referenceString);
				}
				frontier = temporalNode;
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
				Connection connRight = new Connection (conns.size());
				conns.add(connRight);
				int connRightSourcePort = rightNode.getGoodOutPorts()[0];
				connRight.setSource(rightNode, connRightSourcePort);
				connRight.setDest(temporalNode, 1);
				temporalNode.setRightId(rightNode.getOutputElementId(rightNode.getGoodOutPorts()[0]));
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
			populationCriteriaOut.put(population, 
					HqmfJson2Knime.explorePopCriteriaTree(access, nodes, conns, dataCriteriaNodes, measure));
			
			
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
		 * */
		if (nodeIPP != null){
			RelayNode labelIPP = new RelayNode(nodes.size(), DataType.Data);
			nodes.add(labelIPP);
			labelIPP.setComment("Initial Population");
			int nodeIPPOutPort = nodeIPP.getGoodOutPorts()[0];
			labelIPP.setInputElementId(0, nodeIPP.getOutputElementId(nodeIPPOutPort));
			Connection labelIPPConn = new Connection(conns.size());
			labelIPPConn.setSource(nodeIPP, nodeIPPOutPort);
			labelIPPConn.setDest(labelIPP, 0);
			conns.add(labelIPPConn);
		}
		
		/*
		 * Population: Denominator Exclusion (DENEX)
		 * */
		if (nodeDENEX != null){
			RelayNode labelDENEX = new RelayNode(nodes.size(), DataType.Data);
			nodes.add(labelDENEX);
			labelDENEX.setComment("Denominator Exclusion");
			int nodeDENEXOutPort = nodeDENEX.getGoodOutPorts()[0];
			labelDENEX.setInputElementId(0, nodeDENEX.getOutputElementId(nodeDENEXOutPort));
			Connection labelDENEXConn = new Connection(conns.size());
			labelDENEXConn.setSource(nodeDENEX, nodeDENEXOutPort);
			labelDENEXConn.setDest(labelDENEX, 0);
			conns.add(labelDENEXConn);
		}
		
		/*
		 * Population: DENEXCEP (Denominator Exception)
		 * */
		if (nodeDENEXCEP != null){
			RelayNode labelDENEXCEP = new RelayNode(nodes.size(), DataType.Data);
			nodes.add(labelDENEXCEP);
			labelDENEXCEP.setComment("Denominator Exception");
			int nodeDENEXCEPOutPort = nodeDENEXCEP.getGoodOutPorts()[0];
			labelDENEXCEP.setInputElementId(0, nodeDENEXCEP.getOutputElementId(nodeDENEXCEPOutPort));
			Connection labelDENEXCEPConn = new Connection(conns.size());
			labelDENEXCEPConn.setSource(nodeDENEXCEP, nodeDENEXCEPOutPort);
			labelDENEXCEPConn.setDest(labelDENEXCEP, 0);
			conns.add(labelDENEXCEPConn);
			if (nodeNUMER != null) {
				LogicalOperator andNotNode = new LogicalOperator(nodes.size(), LogicalTypeCode.AND_NOT);
				nodes.add(andNotNode);
				NodeInterface leftNode = nodeDENEXCEP;
				NodeInterface rightNode = nodeNUMER;
				int[] goodPorts = LogicalOperator.findGoodPortPair(leftNode, rightNode);
				andNotNode.setLeftId(leftNode.getOutputElementId(goodPorts[0]));
				andNotNode.setRightId(rightNode.getOutputElementId(goodPorts[1]));
				Connection connLeft = new Connection(conns.size());
				conns.add(connLeft);
				connLeft.setSource(leftNode, goodPorts[0]);
				connLeft.setDest(andNotNode, 0);
				Connection connRight = new Connection(conns.size());
				conns.add(connRight);
				connRight.setSource(rightNode, goodPorts[1]);
				connRight.setDest(andNotNode, 1);
				nodeDENEXCEP_modified = andNotNode;
			}
		}
		
		/*
		 * Population: Denominator (DENOM)
		 * */
		if (nodeDENOM != null){
			RelayNode labelDENOM = new RelayNode(nodes.size(), DataType.Data);
			nodes.add(labelDENOM);
			labelDENOM.setComment("Denominator (Originial)");
			int nodeDENOMOutPort = nodeDENOM.getGoodOutPorts()[0];
			labelDENOM.setInputElementId(0, nodeDENOM.getOutputElementId(nodeDENOMOutPort));
			Connection labelDENOMConn = new Connection(conns.size());
			labelDENOMConn.setSource(nodeDENOM, nodeDENOMOutPort);
			labelDENOMConn.setDest(labelDENOM, 0);
			conns.add(labelDENOMConn);
			if (nodeIPP != null){
				LogicalOperator andNode = new LogicalOperator(nodes.size(), LogicalTypeCode.AND);
				nodes.add(andNode);
				NodeInterface leftNode = nodeIPP;
				NodeInterface rightNode = nodeDENOM_modified; 
				int[] goodPorts = LogicalOperator.findGoodPortPair(leftNode, rightNode);
				andNode.setLeftId(leftNode.getOutputElementId(goodPorts[0]));
				andNode.setRightId(rightNode.getOutputElementId(goodPorts[1]));
				Connection connLeft = new Connection(conns.size());
				conns.add(connLeft);
				connLeft.setSource(leftNode, goodPorts[0]);
				connLeft.setDest(andNode, 0);
				Connection connRight = new Connection(conns.size());
				conns.add(connRight);
				connRight.setSource(rightNode, goodPorts[1]);
				connRight.setDest(andNode, 1);
				nodeDENOM_modified = andNode;
			}
		} else if (nodeIPP != null) {
			nodeDENOM_modified = nodeIPP;
		}
		
		if (nodeDENOM_modified != null && nodeDENEX != null) {
			LogicalOperator andNotNode = new LogicalOperator(nodes.size(), LogicalTypeCode.AND_NOT);
			nodes.add(andNotNode);
			NodeInterface leftNode = nodeDENOM_modified;
			NodeInterface rightNode = nodeDENEX;
			int[] goodPorts = LogicalOperator.findGoodPortPair(leftNode, rightNode);
			andNotNode.setLeftId(leftNode.getOutputElementId(goodPorts[0]));
			andNotNode.setRightId(rightNode.getOutputElementId(goodPorts[1]));
			Connection connLeft = new Connection(conns.size());
			conns.add(connLeft);
			connLeft.setSource(leftNode, goodPorts[0]);
			connLeft.setDest(andNotNode, 0);
			Connection connRight = new Connection(conns.size());
			conns.add(connRight);
			connRight.setSource(rightNode, goodPorts[1]);
			connRight.setDest(andNotNode, 1);
			nodeDENOM_modified = andNotNode;
		}
		if (nodeDENOM_modified != null && nodeDENEXCEP != null) {
			LogicalOperator andNotNode = new LogicalOperator(nodes.size(), LogicalTypeCode.AND_NOT);
			nodes.add(andNotNode);
			NodeInterface leftNode = nodeDENOM_modified;
			NodeInterface rightNode = nodeDENEXCEP_modified;
			int[] goodPorts = LogicalOperator.findGoodPortPair(leftNode, rightNode);
			andNotNode.setLeftId(leftNode.getOutputElementId(goodPorts[0]));
			andNotNode.setRightId(rightNode.getOutputElementId(goodPorts[1]));
			Connection connLeft = new Connection(conns.size());
			conns.add(connLeft);
			connLeft.setSource(leftNode, goodPorts[0]);
			connLeft.setDest(andNotNode, 0);
			Connection connRight = new Connection(conns.size());
			conns.add(connRight);
			connRight.setSource(rightNode, goodPorts[1]);
			connRight.setDest(andNotNode, 1);
			nodeDENOM_modified = andNotNode;
		}
		
		if (nodeDENOM_modified != null) {
			RelayNode labelDENOM_modified = new RelayNode(nodes.size(), DataType.Data);
			nodes.add(labelDENOM_modified);
			labelDENOM_modified.setComment("Denominator (Real)");
			int nodeOutPort = nodeDENOM_modified.getGoodOutPorts()[0];
			labelDENOM_modified.setInputElementId(0, nodeDENOM_modified.getOutputElementId(nodeOutPort));
			Connection labelDENOM_modifiedConn = new Connection(conns.size());
			labelDENOM_modifiedConn.setSource(nodeDENOM_modified, nodeOutPort);
			labelDENOM_modifiedConn.setDest(labelDENOM_modified, 0);
			conns.add(labelDENOM_modifiedConn);
		}
		
		/*
		 * Populations: Numerator (NUMER)
		 * */
		if (nodeNUMER != null) {
			RelayNode labelNUMER = new RelayNode(nodes.size(), DataType.Data);
			nodes.add(labelNUMER);
			labelNUMER.setComment("Numerator (Original)");
			int nodeOutPort = nodeNUMER.getGoodOutPorts()[0];
			labelNUMER.setInputElementId(0, nodeNUMER.getOutputElementId(nodeOutPort));
			Connection labelNUMERConn = new Connection(conns.size());
			labelNUMERConn.setSource(nodeNUMER, nodeOutPort);
			labelNUMERConn.setDest(labelNUMER, 0);
			conns.add(labelNUMERConn);
		}
		if (nodeDENOM_modified != null){
			LogicalOperator andNode = new LogicalOperator(nodes.size(), LogicalTypeCode.AND);
			nodes.add(andNode);
			NodeInterface leftNode = nodeNUMER;
			NodeInterface rightNode = nodeDENOM_modified; 
			int[] goodPorts = LogicalOperator.findGoodPortPair(leftNode, rightNode);
			andNode.setLeftId(leftNode.getOutputElementId(goodPorts[0]));
			andNode.setRightId(rightNode.getOutputElementId(goodPorts[1]));
			Connection connLeft = new Connection(conns.size());
			conns.add(connLeft);
			connLeft.setSource(leftNode, goodPorts[0]);
			connLeft.setDest(andNode, 0);
			Connection connRight = new Connection(conns.size());
			conns.add(connRight);
			connRight.setSource(rightNode, goodPorts[1]);
			connRight.setDest(andNode, 1);
			nodeNUMER_modified = andNode;
			
			RelayNode labelNUMER_modified = new RelayNode(nodes.size(), DataType.Data);
			nodes.add(labelNUMER_modified);
			labelNUMER_modified.setComment("Numerator (Real)");
			int nodeOutPort = nodeNUMER_modified.getGoodOutPorts()[0];
			labelNUMER_modified.setInputElementId(0, nodeNUMER_modified.getOutputElementId(nodeOutPort));
			Connection labelNUMER_modifiedConn = new Connection(conns.size());
			labelNUMER_modifiedConn.setSource(nodeNUMER_modified, nodeOutPort);
			labelNUMER_modifiedConn.setDest(labelNUMER_modified, 0);
			conns.add(labelNUMER_modifiedConn);
			
		}
		
		
		
		
		/*
		 * Build Knime project
		 * */
		kProject.addKnimeNodes(nodes);
		kProject.addKnimeConnections(conns);
		
		try {
			kProject.buildProject();
		} catch (ZipException | JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void ivlPqInTemporal (JSONObject ivl_pq, TemporalRelationshipInterface temporalNode) throws JSONException{
		JSONObject pq = null;
		if (ivl_pq.has("high")){
			pq = ivl_pq.getJSONObject("high");
			if (pq.getBoolean("inclusive?")){
				temporalNode.setOperator(Operator.lessThanOrEqualTo);
			} else {
				temporalNode.setOperator(Operator.lessThan);
			}
			
			
		} else if (ivl_pq.has("low")){
			pq = ivl_pq.getJSONObject("low");
			if (pq.getBoolean("inclusive?")){
				temporalNode.setOperator(Operator.greaterThanOrEqualTo);
			} else {
				temporalNode.setOperator(Operator.greaterThan);
			}
			
		} 
		if (pq != null) {
			try {
				
				temporalNode.setQuantity(Integer.valueOf(pq.getString("value")));
				
				Unit unitEnum = Toolkit.timeUnits(pq.getString("unit"));
				
				temporalNode.setUnit(unitEnum);
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
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
							new LogicalOperator(nodes.size(), LogicalTypeCode.AND_NOT);
				} else {
					newNode = 
							new LogicalOperator(nodes.size(), logicType);
				}
				nodes.add(newNode);
				Connection connLeft = new Connection(conns.size());
				connLeft.setDest(newNode, 0);
				conns.add(connLeft);
				Connection connRight = new Connection(conns.size());
				connRight.setDest(newNode, 1);
				conns.add(connRight);
				int[] sourcePorts = LogicalOperator.findGoodPortPair(leftNode, rightNode);
				if (leftNegated && ! rightNegated){
					/*
					 * flip left and right
					 * */
					connRight.setSource(leftNode, sourcePorts[0]);
					newNode.setRightId(leftNode.getOutputElementId(sourcePorts[0]));
					connLeft.setSource(rightNode, sourcePorts[1]);
					newNode.setLeftId(rightNode.getOutputElementId(sourcePorts[1]));
				} else {
					connLeft.setSource(leftNode, sourcePorts[0]);
					newNode.setLeftId(leftNode.getOutputElementId(sourcePorts[0]));
					connRight.setSource(rightNode, sourcePorts[1]);
					newNode.setRightId(rightNode.getOutputElementId(sourcePorts[1]));
				}
				frontier = newNode;
			}
			
			
		}
		
		
		
		return frontier;
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
	public static void main(String[] args) throws IOException, JSONException, WrittenAlreadyException, SetUpIncompleteException, ParseException {
		// TODO Auto-generated method stub
		Path hqmfJsonFile = Paths.get("src/test/resources/cypress-bundle-latest/sources/eh/CMS30v4/hqmf_model.json");
		Path outputDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("qdmKnime/CMS30v4");
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
