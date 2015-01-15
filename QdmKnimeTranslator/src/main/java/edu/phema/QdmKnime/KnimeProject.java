/**
 * 
 */
package edu.phema.QdmKnime;

import java.nio.file.Path;
import java.util.ArrayList;
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

	private final Path workingDir;
	
	private final String projectName;
	
	private final ObjectFactory elementFactory = new ObjectFactory();
	
	private final Config rootConfig = elementFactory.createConfig();
	
	private final JAXBElement<Config> jaxbRoot = elementFactory.createConfig(rootConfig);
	
	public final ArrayList<NodeInterface> knimeNodes = new ArrayList<NodeInterface>();
	
	public final ArrayList<ConnectionInterface> knimeConnections = new ArrayList<ConnectionInterface>();
	
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
		knimeNodes.add(node);
	}
	
	public synchronized void addKnimeNodes (ArrayList<NodeInterface> nodes){
		knimeNodes.addAll(nodes);
	}
	
	public synchronized void addKnimeConnection (ConnectionInterface connection){
		knimeConnections.add(connection);
	}
	
	public synchronized void addKnimeConnections (ArrayList<ConnectionInterface> connections){
		knimeConnections.addAll(connections);
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
	
	public synchronized void buildProject() 
			throws WrittenAlreadyException, SetUpIncompleteException, 
			IOException, ZipException, JAXBException {
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
		
		ZipFile zipFile = new ZipFile(getProjectZipPath().toString());
		ZipParameters parameters = new ZipParameters();
		parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
		zipFile.addFolder(getProjectDir().toString(), parameters);
		
	}
	
	
}
