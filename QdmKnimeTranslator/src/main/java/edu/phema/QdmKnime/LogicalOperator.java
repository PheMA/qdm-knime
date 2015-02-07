/**
 * 
 */
package edu.phema.QdmKnime;

//import java.awt.Point;
import java.io.IOException;
//import java.io.PrintWriter;
//import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.io.FileUtils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import edu.phema.QdmKnimeInterfaces.LogicalRelationshipInterface;
import edu.phema.QdmKnimeInterfaces.NodeInterface;
import edu.phema.knime.exceptions.SetUpIncompleteException;
import edu.phema.knime.exceptions.WrittenAlreadyException;

/**
 * @author Huan
 *
 */
public class LogicalOperator extends MetaNode implements LogicalRelationshipInterface {

	/**
	 * 
	 */
	
	// private Path workflowRoot;  // moved to MetaNode
	
	private Path tempFolder = Paths.get("");
	
	private final LogicalTypeCode logic; 
		
	// private int id = Integer.MIN_VALUE;  // serial number of nodes, moved to MetaNode
	
	// private final Point nodeLocation = new Point(0, 0);  // moved to MetaNode
	
	
	private final int NUM_INPORTS = 2;
	
	/*
	 * You don't actually need to set them up.  
	 * I try to calculate them through charachtors/lines of names
	 * moved to MetaNode
	 */
	// private int nodeWidth = 130;
	// private int nodeHeight = 67;
	
	private NodeInterface leftElementNode  = this;
	private NodeInterface rightElementNode = this;
	
	private final ArrayList<m_OutPort> myOutPorts;   // Not sure if it is a good design
	
	// private String folderName;   // end folder name for the node "AND (#3)", defined in method write
	
	private Path sourceFolder = Paths.get("src/main/resources/metaNodeRepos/logicalOperators");
	
	private Random randMachine = new Random();
	

	/*  // LogicalType should be set up at constructor
	public LogicalOperator() {
		// TODO Auto-generated constructor stub
		super();
		super.setX(500 + randMachine.nextInt(6) * 250);
		super.setY(200 + randMachine.nextInt(6) * 150);
	}
	*/

	public LogicalOperator(LogicalTypeCode typeCode){
		super();
		logic = typeCode;
		myOutPorts = m_getOutPorts();
//		super.setX(300 + randMachine.nextInt(5) * 50);
//		super.setY(100 + randMachine.nextInt(5) * 25);
//		folderName = m_makeFolderName();  // #Unknown
	}
	
//	public LogicalOperator(int id, LogicalTypeCode typeCode){
//		super(id);
//		logic = typeCode;
//		myOutPorts = m_getOutPorts();
//		leftElementNodeId = id;
//		rightElementNodeId = id;
//		super.setX(300 + randMachine.nextInt(5) * 50);
//		super.setY(100 + randMachine.nextInt(5) * 25);
//		folderName = m_makeFolderName();
//	}

	
	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.NodeInterface#setRoot(java.lang.String)
	 */
	@Override
	public synchronized void setWorkflowRoot(String dir) {
		// TODO Auto-generated method stub

		super.setWorkflowRoot(dir);
		tempFolder = getWorkflowRoot().resolve("temp");		
	}


	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.NodeInterface#write()
	 */
	@Override
	public synchronized void write() throws WrittenAlreadyException,
			SetUpIncompleteException, IOException, ZipException {
		// TODO Auto-generated method stub
		
		
		Path workflowRoot = super.getWorkflowRoot();
		//folderName = m_makeFolderName();
		Path nodeFolderPath = workflowRoot.resolve(this.getFolderName());
		if (nodeFolderPath.toFile().exists()) {
			throw new WrittenAlreadyException(nodeFolderPath.toString() + " exists already! ");
		}
		if (workflowRoot.getNameCount() == 0){
			throw new SetUpIncompleteException("Workflow root is not set up for Node" + super.getId());
		}
		
		Path tempZipPath = tempFolder.resolve(m_getZipFileName());
		tempZipPath.toFile().mkdirs();  // To make sure the "temp" folder is there. Do I need to check success? 
		
		Path sourceZipPath = sourceFolder.resolve(m_getZipFileName());
		
		Files.copy(sourceZipPath, tempZipPath, StandardCopyOption.REPLACE_EXISTING); // throws IOException
		
		Path tempFolderForUnzip = tempFolder.resolve("unzip");

		if (Files.exists(tempFolderForUnzip)){
			FileUtils.deleteDirectory(tempFolderForUnzip.toFile());		
		}

		tempFolderForUnzip.toFile().mkdir();
		
		ZipFile zipFile = new ZipFile(tempZipPath.toString());
		zipFile.extractAll(tempFolderForUnzip.toString());
		
		/*
		 *  Set up $%{customDescription}%$
		 *  no use actually
		 * 
		
		Path workflowTemplate = tempFolderForUnzip.resolve(m_getInZipFolderName()).resolve("workflow.knime.template");
		Path workflowInTemp = tempFolderForUnzip.resolve(m_getInZipFolderName()).resolve("workflow.knime");
		
		String workflowTemplateContent = Toolkit.readFile(
				workflowTemplate.toString(), 
				Charset.defaultCharset());
		
		String workflowOutContent = workflowTemplateContent.replace(
				"$%{customDescription}%$", 
				customDescription.isEmpty() ? getNodeName() : customDescription);
		
		PrintWriter outStream = new PrintWriter(workflowInTemp.toFile());
		
		outStream.print(workflowOutContent);
		
		outStream.close();
		
		workflowTemplate.toFile().delete();
		*/
		
		/*
		 * Set up final target folder name, and make copy from temp
		 * */
		// String nodeName = getNodeName();
		 
		
		Files.move(tempFolderForUnzip.resolve(m_getInZipFolderName()), 
				nodeFolderPath, StandardCopyOption.REPLACE_EXISTING);
		
		
	}
	
	
	/*
	 *  After unzip, the folder name
	 * */
	private synchronized String m_getInZipFolderName(){
		String name = "";
		switch (logic) {
			case AND: name = "AND"; break;
			case OR: name = "OR"; break;
			case AND_NOT: name = "AND_NOT"; break;
		}
		return name;
	}
	
	private synchronized String m_getZipFileName(){
		String name = "";
		switch (logic) {
			case AND: name = "AND.zip"; break;
			case OR: name = "OR.zip"; break;
			case AND_NOT: name = "AND_NOT.zip"; break;
		}
		return name;
	}
	
//	private synchronized String m_makeFolderName(){
//		String logicName = logic.name();
//		String sn = super.getId() == super.UNKNOWN_ID ? "Unknown" : String.valueOf(super.getId());
//		String fn = logicName.substring(0, Math.min(logicName.length(), 12))
//				+ " (#" + sn + ")"; 
//		return fn;
//	}

	
	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.NodeInterface#getNumberOfInPorts()
	 */
	@Override
	public synchronized int getNumberOfInPorts() {
		// TODO Auto-generated method stub
		return NUM_INPORTS;
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.NodeInterface#getNumberOfOutPorts()
	 */
	@Override
	public synchronized int getNumberOfOutPorts() {
		// TODO Auto-generated method stub
		return myOutPorts.size();
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.NodeInterface#getNoteName()
	 * Useless?
	 */
	@Override
	public synchronized String getNodeName() {
		// TODO Auto-generated method stub
		String nodeName = "";
		switch (logic){
			case AND:
				nodeName = "Logical: AND";
				break;
			case OR:
				nodeName = "Logical: OR";
				break;
			case AND_NOT:
				nodeName = "Logical: AND NOT";
				break;
		}
		return nodeName;
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.LogicalRelationshipInterface#setLeftElement(java.lang.String)
	 */
	@Override
	public synchronized void setLeftElement(NodeInterface node) {
		// TODO Auto-generated method stub
		leftElementNode = node;
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.LogicalRelationshipInterface#setRightElement(java.lang.String)
	 */
	@Override
	public synchronized void setRightElement(NodeInterface node) {
		// TODO Auto-generated method stub
		rightElementNode = node;
	}
	
	
	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.LogicalRelationshipInterface#getLogicalTypeCode()
	 */
	@Override
	public synchronized LogicalTypeCode getLogicalTypeCode() {
		// TODO Auto-generated method stub
		return logic;
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.LogicalRelationshipInterface#getOutputElementId(int)
	 */
	@Override
	public synchronized NodeInterface getOutputElement(int port) throws IndexOutOfBoundsException {
		// TODO Auto-generated method stub
		return myOutPorts.get(port).getElementNode();
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.LogicalRelationshipInterface#getOutputEntityLevel(int)
	 */
	@Override
	public synchronized EntityLevel getOutputEntityLevel(int port) throws IndexOutOfBoundsException {
		// TODO Auto-generated method stub
		return myOutPorts.get(port).getEventsOrPatients();
	}

	private synchronized ArrayList<m_OutPort> m_getOutPorts(){
		NodeInterface currentNode = this;
		ArrayList<m_OutPort> outPorts = new ArrayList<m_OutPort>();
		switch (logic) {
			case OR:
				outPorts.add(new m_OutPort(currentNode, EntityLevel.patient));
				outPorts.add(new m_OutPort(
						leftElementNode == rightElementNode ? leftElementNode : currentNode, 
						EntityLevel.event));
				break;
			case AND:
				outPorts.add(new m_OutPort(leftElementNode, EntityLevel.event));
				outPorts.add(new m_OutPort(leftElementNode, EntityLevel.event));
				outPorts.add(new m_OutPort(currentNode, EntityLevel.patient));
				outPorts.add(new m_OutPort(rightElementNode, EntityLevel.event));
				outPorts.add(new m_OutPort(rightElementNode, EntityLevel.event));
				break;
			case AND_NOT:
				outPorts.add(new m_OutPort(leftElementNode, EntityLevel.event));
				outPorts.add(new m_OutPort(leftElementNode, EntityLevel.event));
				break;
		}
		return outPorts;
	}
	
	private class m_OutPort {
		NodeInterface elementNode;
		EntityLevel level;
		m_OutPort(NodeInterface elementNode, EntityLevel eventsOrPatients){
			this.elementNode = elementNode;
			level = eventsOrPatients;
		}
		public synchronized NodeInterface getElementNode(){
			return elementNode;
		}
		public synchronized EntityLevel getEventsOrPatients (){
			return level;
		}
	}

	@Override
	public synchronized String getFolderName() {
		// TODO Auto-generated method stub
		String logicName = logic.name();
		String sn = super.getId() == super.UNKNOWN_ID ? "Unknown" : String.valueOf(super.getId());
		String fn = logicName.substring(0, Math.min(logicName.length(), 12))
				+ " (#" + sn + ")"; 
		return fn;

	}

	@Override
	public synchronized int[] getGoodOutPorts() {
		// TODO Auto-generated method stub
		
		int[] GoodOutPorts;
		
		switch (logic) {
			case AND:
				GoodOutPorts = 
					leftElementNode == rightElementNode ? new int[]{0, 2, 4} : new int[]{1, 2, 3};
				break;
			case OR:
				GoodOutPorts = 
					leftElementNode == rightElementNode ? new int[]{1} : new int[]{0};
				break;
			case AND_NOT:
				GoodOutPorts = 
					leftElementNode == rightElementNode ? new int[]{0} : new int[]{1};
				break;
			default:
				GoodOutPorts = new int[]{};
				break;
		}
		
		return GoodOutPorts;
	}

	@Override
	public void setInputElement(int port, NodeInterface node)
			throws IndexOutOfBoundsException {
		// TODO Auto-generated method stub
		if (port == 0) {
			this.setLeftElement(node);
		} else if (port == 1){
			this.setRightElement(node);
		} else
			throw new IndexOutOfBoundsException();
		
	}

	public static int[] findGoodPortPair(NodeInterface leftNode, NodeInterface rightNode){
		int[] re = new int[2];
		int[] leftPorts = leftNode.getGoodOutPorts();
		int[] rightPorts = rightNode.getGoodOutPorts();
		re[0] = leftPorts[0];
		re[1] = rightPorts[0];
		boolean found = false;
		for (int i = 0; i < leftPorts.length && ! found; i++){
			for (int j = 0; j < rightPorts.length && ! found; j++){
				if (leftNode.getOutputElement(leftPorts[i]) == 
						rightNode.getOutputElement(rightPorts[j])){
					re[0] = leftPorts[i];
					re[1] = rightPorts[j];
					found = true;
				}
			}
		}
		
		return re;
	}

}
