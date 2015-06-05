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
//import edu.phema.QdmKnime.MeasurePeriod;
import edu.phema.QdmKnime.MeasurePeriod_v2a;
import edu.phema.QdmKnime.MeasurePeriod_v2b;
import edu.phema.QdmKnime.QdmDataElement_v2;
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
public class HqmfJson2Knime_v2 {

	/**
	 * 
	 */
	
	private VsacConnector vsac;
	final static private int xSpace = 300;
	final static private int ySpace = 150;
	
	
	public HqmfJson2Knime_v2(VsacConnector vsac) {
		this.vsac = vsac;
	}
	
	public void translate(Path hqmfJsonPath, Path outputPath) 
			throws IOException, WrittenAlreadyException, SetUpIncompleteException, ParseException, JSONException {
		String jsonDoc = Toolkit.readFile(hqmfJsonPath.toString());
		final HqmfJson measure = new HqmfJson(jsonDoc);
		final KnimeProject kProject = new KnimeProject(outputPath.getParent(), outputPath.getFileName().toString());
		
		/* copying cypress_all_v3.table is done in KnimeProject class */
		
		kProject.SET_UP_LAYOUT = false;
		//final ArrayList<NodeInterface> nodes = new ArrayList<NodeInterface>();  // the index will the knime id
		//final ArrayList<ConnectionInterface> conns = new ArrayList<ConnectionInterface>();
		final HashMap <String, QdmDataElementInterface> sourceDataCriteriaNodes = new HashMap <String, QdmDataElementInterface> ();
		final HashMap <String, NodeInterface> dataCriteriaNodes = new HashMap <String, NodeInterface> ();
		final HashMap <RelayNode, String> relaysNeedBefore = new HashMap <RelayNode, String>();
		final HashMap <String, QdmDataElementInterface> dataCriteriaFindSource = new HashMap <String, QdmDataElementInterface>();
		final HashMap <String, ArrayList> dataCriteriaFindDataCriterias = new HashMap <String, ArrayList>(); // ArrayList<String>
		final HashMap <String, String> dataCriteria2Source = new HashMap<String, String>();
		
		final ArrayList<ArrayList> nodeMap1 = new ArrayList<ArrayList>();   // to lay out the nodes for human, source data
		final ArrayList<ArrayList> nodeMap2 = new ArrayList<ArrayList>();  // data criteria
		final ArrayList<ArrayList> nodeMap3 = new ArrayList<ArrayList>();  // population
		final ArrayList<ArrayList> nodeMap4 = new ArrayList<ArrayList>();  // postPop, real populations after population arithmetics
		int row = 0;  // variables
		int col = 0;
	
		
		/*
		 * Measure Period object need to be created and linked to sourceDataCriteria every time it is used
		 * It cannot be reused
		 * */
		String measureStart = measure.getMeasureStartDatetime();  // yyyyMMddHHmm
		String measureEnd = measure.getMeasureEndDatetime();   // yyyyMMddHHmm
		
		RelayNode databaseMasterConnectionNode = new RelayNode(DataType.Database);
		kProject.addKnimeNode(databaseMasterConnectionNode);
		databaseMasterConnectionNode.setComment("Plug your database connector here");
		addToMap(nodeMap1, databaseMasterConnectionNode, 0, 0);
		
		/*
		 * Create measure period node v2a (with date times)
		 * */
		
		MeasurePeriod_v2a measurePeriodStem = new MeasurePeriod_v2a(measureStart, measureEnd);
		kProject.addKnimeNode(measurePeriodStem);
		addToMap(nodeMap1, measurePeriodStem, 1, 0);
		
		/*
		 * source_data_criteria
		 * */
		HashMap <String, Integer> sourceDataCriteriaAccesses = 
				measure.getSourceDataCrtieriaAccesses();
		row = 0;
		col = 1;
		for (String sourceDataCriteriaName : sourceDataCriteriaAccesses.keySet()){
			Integer access = sourceDataCriteriaAccesses.get(sourceDataCriteriaName);
			QdmDataElementInterface element = new QdmDataElement_v2();
			kProject.addKnimeNode(element);
			addToMap(nodeMap1, element, row, col);
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
					e.printStackTrace();
				};
			}
			row ++;
		}
		row = 0;
		col = 0;
		
		
		
		/*
		 * Data Criteria
		 * use nodeMap2 for lay out
		 * */
		HashMap <String, Integer> dataCriteriaAccesses = 
				measure.getDataCrtieriaAccesses();
		for (String dataCriteriaName : dataCriteriaAccesses.keySet()){
			ArrayList<NodeInterface> rowLayOut = new ArrayList<NodeInterface>();
			nodeMap2.add(rowLayOut);
			Integer access = dataCriteriaAccesses.get(dataCriteriaName);
			String sourceDataCriteriaName = measure.getStringValue(access, "source_data_criteria");
			//ArrayList<QdmDataElementInterface> sourceElements = new ArrayList<QdmDataElementInterface>();
			QdmDataElementInterface singleSourceElement = sourceDataCriteriaNodes.get(sourceDataCriteriaName);

			dataCriteria2Source.put(dataCriteriaName, sourceDataCriteriaName);
			NodeInterface frontier = null;
			
			/*
			 *  CMS159v3 doesn't work here.
			 *  Check "GROUP_FIRST_129"
			 *  fixed? but still requires manual set up
			 * */
			
			/*
			 * make a relay node to label the source data element
			 * */
			if (singleSourceElement != null){ 
				dataCriteriaFindSource.put(dataCriteriaName, singleSourceElement);
				RelayNode relaySource = new RelayNode(DataType.Data);
				relaySource.setComment("Source Criterion: " + sourceDataCriteriaName);
				relaySource.setInputElement(0, singleSourceElement);
				kProject.addKnimeNode(relaySource);
				rowLayOut.add(relaySource);
				Connection connSource = new Connection();
				connSource.setSource(singleSourceElement, singleSourceElement.getGoodOutPorts()[0]);
				connSource.setDest(relaySource, 0);
				kProject.addKnimeConnection(connSource);
				
				frontier = relaySource; 
				//dataCriteriaFindSource.put(dataCriteriaName, sourceElement);
				/*
				 * Data Criteria/Attribute: value
				 * */
				Integer valueAccess = measure.getJsonObjectRegistry(access, "value");
				if (valueAccess != null){
					String text = "value:%%00010" + measure.getIVL_PQDescription(valueAccess);
					String columnType = "unknown";
					Attribute attr_value = new Attribute();
					kProject.addKnimeNode(attr_value);
					rowLayOut.add(attr_value);
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
					singleSourceElement.addQdmAttributes("value", columnType, text);
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
					rowLayOut.add(attr_value);
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
					singleSourceElement.addQdmAttributes(fieldName, columnType, text);
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
						singleSourceElement.addQdmAttributes("startDatetime", "DateAndTimeCell", "Start timestamp");
					}
					if (typeEnum.REQUIRE_LEFT_END){
						singleSourceElement.addQdmAttributes("stopDatetime", "DateAndTImeCell", "Stop/End timestamp");
					}
					TemporalRelationship temporalNode = new TemporalRelationship(typeEnum);
					kProject.addKnimeNode(temporalNode);
					temporalNode.setLeftElement(frontier);
					temporalNode.modifyAnnotateTexts("leftDataElement", dataCriteriaName);
					temporalNode.modifyAnnotateTexts("leftSourceDataElement", sourceDataCriteriaName);
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
						MeasurePeriod_v2b measurePeriodNode = new MeasurePeriod_v2b();
						kProject.addKnimeNode(measurePeriodNode);
						rowLayOut.add(measurePeriodNode);
						Connection connMeasurePeriodFromStem = new Connection();
						connMeasurePeriodFromStem.setSource(measurePeriodStem, 0);
						connMeasurePeriodFromStem.setDest(measurePeriodNode, 1);
						kProject.addKnimeConnection(connMeasurePeriodFromStem);
						Connection connMeasurePeriodIn = new Connection();
						kProject.addKnimeConnection(connMeasurePeriodIn);
						connMeasurePeriodIn.setSource(frontier, connLeftSourcePort);
						connMeasurePeriodIn.setDest(measurePeriodNode, 0);
						Connection connMeasurePeriodOut = new Connection();
						kProject.addKnimeConnection(connMeasurePeriodOut);
						connMeasurePeriodOut.setSource(measurePeriodNode, measurePeriodNode.getGoodOutPorts()[0]);
						connMeasurePeriodOut.setDest(temporalNode, 1);
						temporalNode.modifyAnnotateTexts("rightDataElement", "Measure Period");
						temporalNode.modifyAnnotateTexts("rightSourceDataElement", "Measure Period");
						temporalNode.setRightElement(frontier.getOutputElement(connLeftSourcePort));
					} else {
						RelayNode rightRelay = new RelayNode(DataType.Data);
						rightRelay.setComment(referenceString);
						kProject.addKnimeNode(rightRelay);
						rowLayOut.add(rightRelay);
						Connection connRightRelay = new Connection();
						connRightRelay.setDest(temporalNode, 1);
						connRightRelay.setSource(rightRelay, 0);
						kProject.addKnimeConnection(connRightRelay);
						relaysNeedBefore.put(rightRelay, referenceString);
						temporalNode.modifyAnnotateTexts("rightDataElement", referenceString);
						rightRelay.setTo(temporalNode);
					}
					rowLayOut.add(temporalNode);
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
					String ivlPqDesc = "";
					try {
						Integer ivlPqAccess = measure.getJsonObjectRegistry(operatorAccess, "value");
						ivlPqDesc = measure.getIVL_PQDescription(ivlPqAccess);
					} catch (NullPointerException e){
						// Do nothing
					}
					
					Aggregation aggrNode = new Aggregation();
					kProject.addKnimeNode(aggrNode);
					rowLayOut.add(aggrNode);
					aggrNode.setGroupByNodeText(typeString);
					aggrNode.setFilterNodeText(ivlPqDesc);
					Connection aggrConn = new Connection();
					kProject.addKnimeConnection(aggrConn);
					aggrConn.setSource(frontier, frontier.getGoodOutPorts()[0]);
					aggrConn.setDest(aggrNode, 0);
					frontier = aggrNode;
				}
			} else {
				/*
				 *  GROUP
				 * */
				dataCriteriaFindDataCriterias.put(dataCriteriaName, new ArrayList<String>());
				String[] childrenCriteriaReferences = measure.getStringArray(access, "children_criteria");
				for (String childCriterionStr : childrenCriteriaReferences){
					RelayNode referredCriterion = new RelayNode(DataType.Data);
					referredCriterion.setComment("Data Criterion: " + childCriterionStr);
					kProject.addKnimeNode(referredCriterion);
					rowLayOut.add(referredCriterion);
					relaysNeedBefore.put(referredCriterion, childCriterionStr);
					dataCriteriaFindDataCriterias.get(dataCriteriaName).add(childCriterionStr);
				}
				
			}
			
			
			
			RelayNode dataCriteriaLabel = new RelayNode(DataType.Data);
			rowLayOut.add(dataCriteriaLabel);
			kProject.addKnimeNode(dataCriteriaLabel);
			dataCriteriaLabel.setComment("Data Criterion: " + dataCriteriaName);
			if (singleSourceElement != null){
				dataCriteriaLabel.setInputElement(0, singleSourceElement);
			} else {
				dataCriteriaLabel.setInputElement(0, dataCriteriaLabel);
			}
			if(frontier != null) {
				Connection dataCriteriaLabelConn = new Connection();
				dataCriteriaLabelConn.setDest(dataCriteriaLabel, 0);
				dataCriteriaLabelConn.setSource(frontier, frontier.getGoodOutPorts()[0]);
				kProject.addKnimeConnection(dataCriteriaLabelConn);
			}
			frontier = dataCriteriaLabel;
			dataCriteriaNodes.put(dataCriteriaName, frontier);
		} 
		
		/*
		 * Temporal Step 2: after all the data criteria nodes are created, add the right side of the
		 * temporal nodes
		 * When build nodeTrace, select the highest level source. 
		 * */
		
		for (RelayNode relayReferred : 
			relaysNeedBefore.keySet()
			.toArray(new RelayNode[relaysNeedBefore.size()])) {
			String referenceString = relaysNeedBefore.get(relayReferred);
			//System.err.println(referenceString);
			NodeInterface beforeNode = dataCriteriaNodes.get(referenceString);
			NodeInterface maybeTemporalNode = relayReferred.getTo();

			if (beforeNode != null) {
				Connection connRight = new Connection();
				relayReferred.setInputElement(0, beforeNode.getOutputElement(beforeNode.getGoodOutPorts()[0]));
				connRight.setDest(relayReferred, 0);  // 0
				kProject.addKnimeConnection(connRight);
				
				
				int connRightSourcePort = beforeNode.getGoodOutPorts()[0];
				connRight.setSource(beforeNode, connRightSourcePort);
				
			}
			
			/*
			 * kinda handling GROUP
			 * */
			if (maybeTemporalNode != null && maybeTemporalNode instanceof TemporalRelationshipInterface){
				TemporalRelationshipInterface temporalNode = (TemporalRelationshipInterface) maybeTemporalNode;
				temporalNode.setRightElement(relayReferred.getOutputElement(relayReferred.getGoodOutPorts()[0])); // useless
			
				ArrayList<String> referenceStrings = new ArrayList<String>();
				referenceStrings.add(referenceString);
				ArrayList<String> daughterReferences = dataCriteriaFindDataCriterias.get(referenceString);
				if (daughterReferences != null){
					referenceStrings.addAll(daughterReferences);
				}
				for(String refStr : referenceStrings){
					QdmDataElementInterface rightSourceElement = dataCriteriaFindSource.get(refStr);
					if(rightSourceElement != null && temporalNode.getTemporalType().REQUIRE_RIGHT_START){
						rightSourceElement.addQdmAttributes("startDatetime", "DateAndTimeCell", "Start timestamp");
					}
					if (rightSourceElement != null && temporalNode.getTemporalType().REQUIRE_RIGHT_END){
						rightSourceElement.addQdmAttributes("stopDatetime", "DateAndTimeCell", "Stop/end timestamp");
					}
				}
			}
		}
		
		/*
		 * Population criteria
		 * */
		
		final HashMap<String, Integer> populationsAccesses = measure.getPopulationCriteriaAccesses();
		final HashMap<String, NodeInterface> populationCriteriaOut = 
				new HashMap<String, NodeInterface>();
		
		
		for (String population : populationsAccesses.keySet().toArray(
				new String[populationsAccesses.size()])){
			Integer access = populationsAccesses.get(population);
			
			ArrayList<NodeInterface> popNodes = new ArrayList<NodeInterface>();
			ArrayList<ConnectionInterface> popConns = new ArrayList<ConnectionInterface>(); 

			ArrayList<ArrayList> nodeMapPop = new ArrayList<ArrayList>();
			
			/*
			 * Explore the logical tree recursively
			 * */
			ArrayList<NodeInterface> nodes = new ArrayList<NodeInterface> ();
			ArrayList<ConnectionInterface> conns = new ArrayList<ConnectionInterface> ();
			NodeInterface populationOut = HqmfJson2Knime_v2.explorePopCriteriaTree(access, nodes, conns, dataCriteriaNodes, measure);
			popNodes.addAll(nodes);
			popConns.addAll(conns);
			populationCriteriaOut.put(population, populationOut);
			if (populationOut != null) {
				HqmfJson2Knime_v2.labelAPopulation(
						measure.getStringValue(access, "title"), populationOut, popNodes, popConns);
			}
			kProject.addKnimeNodes(popNodes);
			kProject.addKnimeConnections(popConns);
			
			mapFlows(nodeMapPop, popConns, popNodes);
			nodeMap3.addAll(nodeMapPop);
			
			nodeMap3.add(new ArrayList<NodeInterface>());
		}
		
		ArrayList<NodeInterface> postPopNodes = new ArrayList<NodeInterface>();
		ArrayList<ConnectionInterface> postPopConns = new ArrayList<ConnectionInterface>(); 

		
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
					HqmfJson2Knime_v2.logicTwoNodes(nodeDENEXCEP, nodeNUMER, 
						LogicalTypeCode.AND_NOT, postPopNodes, postPopConns);;
		}
		
		/*
		 * Population: Denominator (DENOM)
		 * */
		if (nodeDENOM != null && nodeIPP != null){
			nodeDENOM_modified = 
					HqmfJson2Knime_v2.logicTwoNodes(nodeDENOM, nodeIPP, 
						LogicalTypeCode.AND, postPopNodes, postPopConns);
		} else if (nodeIPP != null) {
			nodeDENOM_modified = nodeIPP;
		}
		
		if (nodeDENOM_modified != null && nodeDENEX != null) {
			nodeDENOM_modified = HqmfJson2Knime_v2.logicTwoNodes(
					nodeDENOM_modified, nodeDENEX, 
					LogicalTypeCode.AND_NOT, postPopNodes, postPopConns);
		}
		
		if (nodeDENOM_modified != null && nodeDENEXCEP != null) {
			nodeDENOM_modified = 
					HqmfJson2Knime_v2.logicTwoNodes(nodeDENOM_modified, nodeDENEXCEP_modified, 
							LogicalTypeCode.AND_NOT, postPopNodes, postPopConns);
		}
		
		if (nodeDENOM_modified != null) {
			HqmfJson2Knime_v2.labelAPopulation("Denominator (Real)", nodeDENOM_modified, postPopNodes, postPopConns);
		}
		
		
		/*
		 * Populations: Numerator (NUMER)
		 * */
		
		if (nodeNUMER != null && nodeDENOM_modified != null){
			nodeNUMER_modified = HqmfJson2Knime_v2.logicTwoNodes(nodeNUMER, nodeDENOM_modified, LogicalTypeCode.AND, postPopNodes, postPopConns);;
			HqmfJson2Knime_v2.labelAPopulation("Numerator (Real)", nodeNUMER_modified, postPopNodes, postPopConns);
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
					HqmfJson2Knime_v2.logicTwoNodes(nodeMSRPOPL, nodeIPP, LogicalTypeCode.AND, postPopNodes, postPopConns);
		} else {
			nodeMSRPOPL_modified = nodeMSRPOPL == null ? nodeMSRPOPL : nodeIPP;
		}
		if (nodeMSRPOPL_modified != null){
			HqmfJson2Knime_v2.labelAPopulation(
				"Measure Population (Real)", nodeMSRPOPL_modified, postPopNodes, postPopConns);
		}
		
		if (nodeOBSERV != null && nodeMSRPOPL_modified != null){
			nodeOBSERV_modified = HqmfJson2Knime_v2.logicTwoNodes(
					nodeOBSERV, nodeMSRPOPL_modified, LogicalTypeCode.AND, postPopNodes, postPopConns);
		} 
		if (nodeOBSERV_modified != null) {
			Integer observAccess = populationsAccesses.get("OBSERV");
			if (observAccess != null) {
				String aggregator = measure.getStringValue(observAccess, "aggregator");
				if (aggregator != null) {
					Aggregation aggr = new Aggregation();
					postPopNodes.add(aggr);
					aggr.setGroupByNodeText(aggregator);
					aggr.setFilterNodeText("Useless");
					Connection aggrConn = new Connection();
					postPopConns.add(aggrConn);
					aggrConn.setSource(nodeOBSERV_modified, 
							nodeOBSERV_modified.getGoodOutPorts()[0]);
					aggrConn.setDest(aggr, 0);
					aggr.setInputElement(nodeOBSERV_modified);
					nodeOBSERV_modified = aggr;
				}
			}
			HqmfJson2Knime_v2.labelAPopulation("Measure Observation (Real)", nodeOBSERV_modified, postPopNodes, postPopConns);
		}
		
		mapFlows(nodeMap4, postPopConns, postPopNodes);
		
		kProject.addKnimeNodes(postPopNodes);
		kProject.addKnimeConnections(postPopConns);

		for (String population : populationCriteriaOut.keySet()){
			NodeInterface outNode = populationCriteriaOut.get(population);
			if (outNode != null){
				System.err.println(population + ": Node " + outNode.getId());
			}
		}

		int nodeMap1Max = 0;
		
		for (int i = 0; i < nodeMap1.size(); i ++){
			ArrayList<NodeInterface> nodesRow = nodeMap1.get(i);
			for (int j = 0; j < nodesRow.size(); j ++){
				if (j > nodeMap1Max){
					nodeMap1Max = j;
				}
				NodeInterface node = nodesRow.get(j);
				if (node != null){
					node.setX(xSpace * (j + 1));
					node.setY(ySpace * (i + 1));
				}
			}
		}
		
		int nodeMap2Max = nodeMap1Max;
		
		for (int i = 0; i < nodeMap2.size(); i ++){
			ArrayList<NodeInterface> nodesRow = nodeMap2.get(i);
			for (int j = 0; j < nodesRow.size() ; j ++){
				int jReal = j + nodeMap1Max + 1;
				if (jReal > nodeMap2Max){
					nodeMap2Max = jReal;
				}
				NodeInterface node = nodesRow.get(j);
				if (node != null){
					node.setX(xSpace * (jReal + 1));
					node.setY(ySpace * (i + 1));
				}
			}
		}
		
		int nodeMap3Max = nodeMap2Max;
		
		for (int i = 0; i < nodeMap3.size(); i ++){
			ArrayList<NodeInterface> nodesRow = nodeMap3.get(i);
			for (int j = 0; j < nodesRow.size() ; j ++){
				int jReal = j + nodeMap2Max + 1;
				if (jReal > nodeMap3Max){
					nodeMap3Max = jReal;
				}
				NodeInterface node = nodesRow.get(j);
				if (node != null){
					node.setX(xSpace * (jReal + 1));
					node.setY(ySpace * (i + 1));
				}
			}
		}

		
		for (int i = 0; i < nodeMap4.size(); i ++){
			ArrayList<NodeInterface> nodesRow = nodeMap4.get(i);
			for (int j = 0; j < nodesRow.size() ; j ++){
				int jReal = j + nodeMap3Max + 1;
				NodeInterface node = nodesRow.get(j);
				if (node != null){
					node.setX(xSpace * (jReal + 1));
					node.setY(ySpace * (i + 1));
				}
			}
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
				temporalNode.setQuantity(Double.valueOf(valueRe));
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
		
		/*
		 * Prepare to connect to data criteria
		 * */
		String reference = measure.getStringValue(currentParentOfPreconditionsAccess, "reference");

		RelayNode relayDataCriteria = null;
		
		if (reference != null) {  
			NodeInterface dataCriteriaNode = dataCriteria.get(reference);
			int dataCriteriaNodeOutPort = dataCriteriaNode.getGoodOutPorts()[0];
			
			relayDataCriteria = new RelayNode(DataType.Data);
			relayDataCriteria.setComment("Data Criterion: " + reference);
			relayDataCriteria.setInputElement(0, dataCriteriaNode.getOutputElement(dataCriteriaNodeOutPort));
			Connection connRelay = new Connection();
			connRelay.setSource(dataCriteriaNode, dataCriteriaNodeOutPort);
			connRelay.setDest(relayDataCriteria, 0);
			frontier = relayDataCriteria;
			
			//nodes.add(relayDataCriteria);
			conns.add(connRelay);
			
			Boolean negated = measure.getBooleanValue(currentParentOfPreconditionsAccess, "negation");
			frontierNegated = negated != null && negated.booleanValue();
		}
		for (int access : preconditionsAccesses){
			NodeInterface leftNode = frontier;
			boolean leftNegated = frontierNegated;
//			int currentNewNodeLoc = nodes.size(); 
//			if(leftNode != null){   // match to if (leftNode == null)
//				nodes.add(null);   // try to get an earlier spot before the recursive
//			
//			}
			NodeInterface rightNode = explorePopCriteriaTree(
					access, nodes, conns, dataCriteria, measure);
			boolean rightNegated = measure.getBooleanValue(access, "negation") != null &&  
					measure.getBooleanValue(access, "negation").booleanValue() ;
			if (leftNode == null){          // in this situation, referred data criteria must also be null
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
		
		if (relayDataCriteria != null){
			nodes.add(relayDataCriteria);
		}
		
		return frontier;
	}
	
	private static NodeInterface labelAPopulation (String label, NodeInterface frontier, 
			ArrayList<NodeInterface> nodes, ArrayList<ConnectionInterface> conns) {
		
		RelayNode labelNode = new RelayNode(DataType.Data);
		//kProject.addKnimeNode(labelNode);
		labelNode.setComment(label);
		int frontierPort = frontier.getGoodOutPorts()[0];
		labelNode.setInputElement(0, frontier.getOutputElement(frontierPort));
		Connection labelConn = new Connection();
		labelConn.setSource(frontier, frontierPort);
		labelConn.setDest(labelNode, 0);
		//kProject.addKnimeConnection(labelConn);
		nodes.add(labelNode);
		conns.add(labelConn);
		return labelNode;
	}
	
	private static NodeInterface logicTwoNodes(
			NodeInterface leftNode, NodeInterface rightNode, LogicalTypeCode logic,
			ArrayList<NodeInterface> nodes, ArrayList<ConnectionInterface> conns){
		NodeInterface re = null;
		if (leftNode != null && rightNode != null){
			LogicalOperator logicNode = new LogicalOperator(logic);
			nodes.add(logicNode);
			int[] goodPorts = LogicalOperator.findGoodPortPair(leftNode, rightNode);
			logicNode.setLeftElement(leftNode.getOutputElement(goodPorts[0]));
			logicNode.setRightElement(rightNode.getOutputElement(goodPorts[1]));
			Connection connLeft = new Connection();
			conns.add(connLeft);
			connLeft.setSource(leftNode, goodPorts[0]);
			connLeft.setDest(logicNode, 0);
			Connection connRight = new Connection();
			conns.add(connRight);
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

	private static void addToMap(ArrayList<ArrayList> map, NodeInterface node, int row, int col){
		for (int i = map.size(); i <= row; i ++){
			map.add(new ArrayList<NodeInterface>());
		}
		ArrayList<NodeInterface> rowObj = map.get(row);
		for (int i = rowObj.size(); i <= col; i ++){
			rowObj.add(null);
		}
		rowObj.set(col, node);
	}
	
	private static void mapFlows(ArrayList<ArrayList> map, ArrayList<ConnectionInterface> conns, ArrayList<NodeInterface> nodes){
		
		HashMap<NodeInterface, NodeInterface[]> chase = new HashMap<NodeInterface, NodeInterface[]>();
		for (ConnectionInterface conn : conns){
			NodeInterface dest = conn.getDest();
			NodeInterface[] destArray = chase.get(dest);
			if(destArray == null){
				destArray = new NodeInterface[10];
				chase.put(dest, destArray);
			}
			NodeInterface source = conn.getSource();
			
			destArray[conn.getDestPort()] = source;
			
			
		}

		/*
		 * Recursively find levels
		 * 
		 * */
		HashMap<NodeInterface, Integer> nodeLevels = new HashMap<NodeInterface, Integer>();
		//ArrayList<NodeInterface> rootNodes = new ArrayList<NodeInterface>();
		int maxLevel = 0;
		
		for(NodeInterface node : nodes){
			int level = seekLevel(chase, node);
			nodeLevels.put(node, Integer.valueOf(level));
			if (level > maxLevel){
				maxLevel = level;
			}
			
			//boolean isRoot = true;
			//for(ConnectionInterface conn : conns){
			//	if(conn.getSource() == node){
			//		isRoot = false;
			//	}
			//}
			//if(isRoot){
			//	rootNodes.add(node);
			//}
		}
		
		//System.err.println(maxLevel);
		
		ArrayList[] flippedMap = new ArrayList[maxLevel + 1];
		for(NodeInterface node : nodeLevels.keySet()){
			int level = nodeLevels.get(node).intValue();
			if (flippedMap[level] == null){
				flippedMap[level] = new ArrayList<NodeInterface>();
			}
			ArrayList<NodeInterface> nodesOnLevel = flippedMap[level];
			nodesOnLevel.add(node);
		}
		for (int i = 0; i <= maxLevel; i ++){
			ArrayList<NodeInterface> nodesOnLevel = flippedMap[i];
			for (int j = 0; nodesOnLevel != null && j < nodesOnLevel.size(); j ++){
				addToMap(map, nodesOnLevel.get(j), j, i);
			}
		}
	}
	
	/*
	 * Recursively find levels
	 * for mapFlows
	 * */
	private static int seekLevel(HashMap<NodeInterface, NodeInterface[]> chase, NodeInterface current){
		NodeInterface[] sons = chase.get(current);
		int thisLevel = 0;
		if (sons != null){
			for (NodeInterface son : sons){
				int sonLevel = -1;
				if (son != null){
					sonLevel = seekLevel(chase, son);
					if(sonLevel >= thisLevel){
						thisLevel = sonLevel + 1;
					}
				}
			}
		}
		return thisLevel;
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
		// /Users/admin/Desktop
		String measureName = "CMS30v4";
		String measureType = "eh";
		Path hqmfJsonFile1 = Paths.get("src/test/resources/cypress-bundle-latest/sources/" + measureType + "/" + measureName + "/hqmf_model.json");
		Path outputDir1 = Paths.get("/Users/admin/Desktop/qdm2knime").resolve(measureType).resolve(measureName);
		
		//Path hqmfJsonFile2 = Paths.get("src/test/resources/cypress-bundle-latest/sources/ep/CMS179v3/hqmf_model.json");
		//Path outputDir2 = Paths.get(System.getProperty("java.io.tmpdir")).resolve("qdmKnime/CMS179v3");
		
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
		HqmfJson2Knime_v2 translator = new HqmfJson2Knime_v2(new VsacConnector("henryhmo", "2525WestEnd"));
		translator.translate(hqmfJsonFile, outputDir);
		System.out.println(outputDir.toString());
	}

}
