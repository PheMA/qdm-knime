/**
 * 
 */
package edu.phema.QdmKnime;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import edu.phema.QdmKnimeInterfaces.ConnectionInterface;
import edu.phema.QdmKnimeInterfaces.NodeInterface;
import edu.phema.jaxb.knime.Config;
import edu.phema.jaxb.knime.ObjectFactory;
import edu.phema.knime.exceptions.SetUpIncompleteException;
import edu.phema.knime.exceptions.WrittenAlreadyException;

/**
 * @author Huan
 *
 */
public class KnimeProject {
	
	public boolean SET_UP_LAYOUT = false;

	private final Path workingDir;
	
	private final String projectName;
	
	private final ObjectFactory elementFactory = new ObjectFactory();
	
	private final Config rootConfig = elementFactory.createConfig();
	
	private final JAXBElement<Config> jaxbRoot = elementFactory.createConfig(rootConfig);
	
	private final ArrayList<NodeInterface> knimeNodes = new ArrayList<NodeInterface>();
	
	private final ArrayList<ConnectionInterface> knimeConnections = new ArrayList<ConnectionInterface>();
	
	/**
	 * @throws IOException 
	 * 
	 */
	public KnimeProject(Path workingDir, String projectName) throws IOException {
		// TODO Auto-generated constructor stub
		this.workingDir = workingDir;
		this.projectName = projectName;
		if(getProjectDir().toFile().exists() || getProjectZipPath().toFile().exists()){
			throw new IOException("Project dirctory or file of \"" + getProjectDir().toString() + "\" exists! ");			
		}
		getProjectDir().toFile().mkdirs();
		
		rootConfig.setKey("workflow.knime");
		Toolkit.addWorkflowOverHeads(rootConfig, elementFactory);
	}

	public synchronized void addKnimeNode (NodeInterface node){
		node.setId(knimeNodes.size());
		knimeNodes.add(node);
	}
	
	public synchronized void addKnimeNodes (ArrayList<NodeInterface> nodes){
		for (NodeInterface node : nodes){
			node.setId(knimeNodes.size());
			knimeNodes.add(node);
		}
	}
	
	public synchronized void addKnimeConnection (ConnectionInterface connection){
		connection.setId(knimeConnections.size());
		knimeConnections.add(connection);
	}
	
	public synchronized void addKnimeConnections (ArrayList<ConnectionInterface> connections){
		for (ConnectionInterface conn : connections){
			conn.setId(knimeConnections.size());
			knimeConnections.add(conn);
		}
	}
	
	public synchronized Path getProjectDir(){
		return workingDir.resolve(projectName);
	}
	
	public synchronized Path getWorkflowKnimePath(){
		return getProjectDir().resolve("workflow.knime");
	}
	
	public synchronized Path getProjectZipPath(){
		return workingDir.resolve(projectName + ".zip");
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

	
	public synchronized void buildProject() 
			throws WrittenAlreadyException, SetUpIncompleteException, 
			IOException, ZipException, JAXBException {
		
		/*
		 * nodeTrace is to count level of the nodes to make nodes graphically better arranged.
		 * */
		if (SET_UP_LAYOUT) {
			/*
			 * Build nodeTrace
			 * */
			HashMap<NodeInterface, NodeInterface> nodeTrace = 
					new HashMap<NodeInterface, NodeInterface>(); // destNode -> sourceNode
			for (ConnectionInterface conn : knimeConnections){
				NodeInterface sourceNode = conn.getSource();
				NodeInterface destNode = conn.getDest();
				NodeInterface previousSource = nodeTrace.get(destNode);
				/*
				 * A node should be located at its highest level
				 * */
				if (previousSource == null || 
						getNodeLevel(sourceNode, nodeTrace) > getNodeLevel(previousSource, nodeTrace)){
					nodeTrace.put(destNode, sourceNode);
				} 
			}
			
			/*
			 * Use a table to order the nodes in x and y axises.
			 * */
			ArrayList<ArrayList<NodeInterface>> nodesTable = new ArrayList<ArrayList<NodeInterface>> ();
			for (NodeInterface node : knimeNodes){
				/*
				 * Initiate columns
				 * */
				int nodeLevel = getNodeLevel(node, nodeTrace); 
				for (int i = nodesTable.size(); i <= nodeLevel; i ++){
					nodesTable.add(new ArrayList<NodeInterface>());
				}
				
				nodesTable.get(nodeLevel).add(node);
			}
			
			
			/*
			 * Assign physical location (x, y) of each nodes. 
			 * */
			for (int i = 0; i < nodesTable.size(); i ++){
				ArrayList<NodeInterface> column = nodesTable.get(i);
				for (int j = 0; j < column.size(); j ++){
					NodeInterface node = column.get(j);
					node.setX(300 * (i + 1));
					node.setY(150 * (j + 1));
				}
			}
			
		}
		
		Config nodesConfig = elementFactory.createConfig();
		nodesConfig.setKey("nodes");
		rootConfig.getEntryOrConfig().add(nodesConfig);
		
		Config connsConfig = elementFactory.createConfig();
		connsConfig.setKey("connections");
		rootConfig.getEntryOrConfig().add(connsConfig);
		
		for (NodeInterface node : knimeNodes.toArray(new NodeInterface[knimeNodes.size()])){
			nodesConfig.getEntryOrConfig().add(node.getKnimeWorkflowConfig(elementFactory));
			
			node.setWorkflowRoot(getProjectDir().toString());
			node.write();
		}
		
		for (ConnectionInterface conn : knimeConnections.toArray(new ConnectionInterface[knimeConnections.size()])){
			connsConfig.getEntryOrConfig().add(conn.getKnimeWorkflowConfig(elementFactory));
		}
		
		PrintWriter outStream = new PrintWriter(getWorkflowKnimePath().toFile());
		JAXBContext contextA = JAXBContext.newInstance(ObjectFactory.class);
		Marshaller mars = contextA.createMarshaller();
		mars.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		mars.marshal(jaxbRoot, outStream);
		outStream.close();
		
		/*
		 * Test data from Cypress
		 * */
		Files.copy(Paths.get("src/main/resources/cypress_data/cypress2.5_all_v4.table"), getProjectDir().resolve("cypress2.5_all_v4.table"), StandardCopyOption.REPLACE_EXISTING);
		
		ZipFile zipFile = new ZipFile(getProjectZipPath().toString());
		ZipParameters parameters = new ZipParameters();
		parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
		zipFile.addFolder(getProjectDir().toString(), parameters);
		
	}
	
	
}
