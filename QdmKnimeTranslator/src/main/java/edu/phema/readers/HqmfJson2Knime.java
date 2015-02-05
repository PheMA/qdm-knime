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
import edu.phema.QdmKnime.MeasurePeriod;
import edu.phema.QdmKnime.QdmDataElement;
import edu.phema.QdmKnime.TemporalRelationship;
import edu.phema.QdmKnime.Toolkit;
import edu.phema.QdmKnimeInterfaces.ConnectionInterface;
import edu.phema.QdmKnimeInterfaces.NodeInterface;
import edu.phema.QdmKnimeInterfaces.QdmDataElementInterface;
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
		final ArrayList<NodeInterface> nodes = new ArrayList<NodeInterface>();  // the index will the knime id
		final ArrayList<ConnectionInterface> conns = new ArrayList<ConnectionInterface>();
		final HashMap <String, QdmDataElementInterface> sourceDataCriteriaNodes = new HashMap <String, QdmDataElementInterface> ();
		// HashMap <NodeInterface, Integer> nodeLevels = new HashMap <NodeInterface, Integer> (); // Start with 0
		final HashMap <String, NodeInterface> dataCriteriaNodes = new HashMap <String, NodeInterface> ();
		final HashMap <TemporalRelationshipInterface, String> rightSideOfTemporals = 
				new HashMap <TemporalRelationshipInterface, String>();
		/*
		 * nodeTrace is to count level of the nodes to make nodes graphically better arranged.
		 * */
		final HashMap<NodeInterface, NodeInterface> nodeTrace = new HashMap<NodeInterface, NodeInterface>(); 
		
		/*
		 * Measure Period object need to be created and linked to sourceDataCriteria every time it is used
		 * It cannot be reused
		 * */
		String measureStart = measure.getMeasureStartDatetime();  // yyyyMMddHHmm
		String measureEnd = measure.getMeasureEndDatetime();   // yyyyMMddHHmm
		
		for (String sourceDataCriteriaName : measure.getSourceDataCrtieriaList()){
			QdmDataElement element = new QdmDataElement(nodes.size());
			nodes.add(element);
			sourceDataCriteriaNodes.put(sourceDataCriteriaName, element);
			// nodeLevels.put(element, new Integer(0));
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
			// int frontierLevel = nodeLevels.get(frontier);   // for nodeLevels
			if (! measure.typeOfValueInDataCriteria(dataCriteriaName).equals("")){
				String text = measure.getTextOfValueInDataCriteria(dataCriteriaName);
				String columnType = "unknown";
				Attribute attr_value = new Attribute(nodes.size());
				nodes.add(attr_value);
				Connection conn = new Connection(conns.size());
				conns.add(conn);
				conn.setSource(frontier.getId(), frontier.getGoodOutPorts()[0]);
				conn.setDest(attr_value.getId(), 1);  // Port for native nodes start from 1
				attr_value.setInputElementId(0, frontier.getOutputElementId(0));
				nodeTrace.put(attr_value, frontier);
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
				conn.setSource(frontier.getId(), frontier.getGoodOutPorts()[0]);
				conn.setDest(attr_value.getId(), 1);  // Port for native nodes start from 1
				attr_value.setInputElementId(0, frontier.getOutputElementId(0));
				nodeTrace.put(attr_value, frontier);
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
				connLeft.setSource(frontier.getId(), connLeftSourcePort);
				connLeft.setDest(temporalNode.getId(), 0);
				conns.add(connLeft);
				nodeTrace.put(temporalNode, frontier);
				if (referenceString.equals("MeasurePeriod")){
					MeasurePeriod measurePeriodNode = new MeasurePeriod(
								nodes.size(), measureStart, measureEnd);
					nodes.add(measurePeriodNode);
					Connection connMeasurePeriodIn = new Connection(conns.size());
					conns.add(connMeasurePeriodIn);
					connMeasurePeriodIn.setSource(frontier.getId(), connLeftSourcePort);
					connMeasurePeriodIn.setDest(measurePeriodNode.getId(), 0);
					nodeTrace.put(measurePeriodNode, frontier);
					Connection connMeasurePeriodOut = new Connection(conns.size());
					conns.add(connMeasurePeriodOut);
					connMeasurePeriodOut.setSource(measurePeriodNode.getId(), measurePeriodNode.getGoodOutPorts()[0]);
					connMeasurePeriodOut.setDest(temporalNode.getId(), 1);
					temporalNode.setRightId(frontier.getOutputElementId(connLeftSourcePort));
					nodeTrace.put(temporalNode, measurePeriodNode);
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
				connRight.setSource(rightNode.getId(), connRightSourcePort);
				connRight.setDest(temporalNode.getId(), 1);
				temporalNode.setRightId(rightNode.getOutputElementId(rightNode.getGoodOutPorts()[0]));
				int leftLevel = getNodeLevel(nodeTrace.get(temporalNode), nodeTrace);
				int rightLevel = getNodeLevel(rightNode, nodeTrace);
				if (rightLevel > leftLevel){
					nodeTrace.put(temporalNode, rightNode);
				}
			}
		}
		
		
		ArrayList<ArrayList<NodeInterface>> nodesTable = new ArrayList<ArrayList<NodeInterface>> ();
		for (NodeInterface node : nodes){
			/*
			 * Initiate columns
			 * */
			int nodeLevel = getNodeLevel(node, nodeTrace); 
			for (int i = nodesTable.size(); i <= nodeLevel; i ++){
				nodesTable.add(new ArrayList<NodeInterface>());
			}
			
			nodesTable.get(nodeLevel).add(node);
		}
		
		for (int i = 0; i < nodesTable.size(); i ++){
			ArrayList<NodeInterface> column = nodesTable.get(i);
			for (int j = 0; j < column.size(); j ++){
				NodeInterface node = column.get(j);
				node.setX(300 * (i + 1));
				node.setY(150 * (j + 1));
			}
		}
		
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

	private static int getNodeLevel (NodeInterface node, HashMap <NodeInterface, NodeInterface> nodeTrace){
		int nodeLevel = 0;
		for (NodeInterface current = node; 
				nodeTrace.containsKey(current); 
				current = nodeTrace.get(current)){
			nodeLevel ++;
		}
		return nodeLevel;
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
