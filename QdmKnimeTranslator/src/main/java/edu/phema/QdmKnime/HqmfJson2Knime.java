/**
 * 
 */
package edu.phema.QdmKnime;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.JAXBException;

import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;

import edu.phema.QdmKnimeInterfaces.ConnectionInterface;
import edu.phema.QdmKnimeInterfaces.NodeInterface;
import edu.phema.QdmKnimeInterfaces.QdmDataElementInterface;
import edu.phema.knime.exceptions.SetUpIncompleteException;
import edu.phema.knime.exceptions.WrittenAlreadyException;
import edu.phema.readers.HqmfJson;
import edu.phema.vsac.VsacConnector;

/**
 * @author Huan Mo
 *
 */
public class HqmfJson2Knime {

	/**
	 * 
	 */
	
	VsacConnector vsac;
	
	public HqmfJson2Knime(VsacConnector vsac) {
		// TODO Auto-generated constructor stub
		this.vsac = vsac;
	}
	
	public void translate(Path hqmfJsonPath, Path outputPath) 
			throws IOException, JSONException, WrittenAlreadyException, SetUpIncompleteException {
		String jsonDoc = Toolkit.readFile(hqmfJsonPath.toString());
		HqmfJson measure = new HqmfJson(jsonDoc);
		KnimeProject kProject = new KnimeProject(outputPath.getParent(), outputPath.getFileName().toString());
		ArrayList<NodeInterface> nodes = new ArrayList<NodeInterface>();  // the index will the knime id
		ArrayList<ConnectionInterface> conns = new ArrayList<ConnectionInterface>();
		HashMap <String, QdmDataElementInterface> sourceDataCriteriaNodes = new HashMap <String, QdmDataElementInterface> ();
		HashMap <NodeInterface, Integer> nodeLevels = new HashMap <NodeInterface, Integer> (); // Start with 0
		
		for (String sourceDataCriteriaName : measure.getSourceDataCrtieriaList()){
			QdmDataElement element = new QdmDataElement(nodes.size());
			nodes.add(element);
			sourceDataCriteriaNodes.put(sourceDataCriteriaName, element);
			nodeLevels.put(element, new Integer(0));
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
		
		ArrayList<ArrayList<NodeInterface>> nodesTable = new ArrayList<ArrayList<NodeInterface>> ();
		for (NodeInterface node : nodes){
			/*
			 * Initiate columns
			 * */
			for (int i = nodesTable.size(); i <= nodeLevels.get(node).intValue(); i ++){
				nodesTable.add(new ArrayList<NodeInterface>());
			}
			
			nodesTable.get(nodeLevels.get(node).intValue()).add(node);
		}
		
		for (int i = 0; i < nodesTable.size(); i ++){
			ArrayList<NodeInterface> column = nodesTable.get(i);
			for (int j = 0; j < column.size(); j ++){
				NodeInterface node = column.get(j);
				node.setX(200 * (i + 1));
				node.setY(100 * (j + 1));
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

	/**
	 * @param args
	 * @throws IOException 
	 * @throws JSONException 
	 */
	public static void main(String[] args) throws IOException, JSONException {
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
