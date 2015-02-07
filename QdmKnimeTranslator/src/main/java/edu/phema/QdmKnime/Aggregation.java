/**
 * 
 */
package edu.phema.QdmKnime;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FileUtils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import edu.phema.QdmKnimeInterfaces.AggregationInterface;
import edu.phema.QdmKnimeInterfaces.NodeInterface;
import edu.phema.knime.exceptions.SetUpIncompleteException;
import edu.phema.knime.exceptions.WrittenAlreadyException;

/**
 * @author Huan Mo
 *
 */
public class Aggregation extends MetaNode implements AggregationInterface {

	/**
	 * 
	 */
	
	String nodeText = "";
	String groupByNodeText = "";
	String filterNodeText = "";
//	String folderName = "Aggregative";
	final int numberOfInPorts = 1;
	final int numberOfOutPorts = 1;
	
	NodeInterface inputElement = this; 
	
	public Aggregation() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param id
	 */
//	public Aggregation(int id) {
//		super(id);
//		folderName = this.m_makeFolderName();
		// TODO Auto-generated constructor stub
//	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.NodeInterface#setInputElementId(int, int)
	 */
	@Override
	public void setInputElement(int port, NodeInterface node)
			throws IndexOutOfBoundsException {
		// TODO Auto-generated method stub
		if (port == 0)
			inputElement = node;
		else
			throw new IndexOutOfBoundsException("Aggregation node " + this.getId() + " only have one input port. ");
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.NodeInterface#getOutputElementId(int)
	 */
	@Override
	public NodeInterface getOutputElement(int port) throws IndexOutOfBoundsException {
		// TODO Auto-generated method stub
		return inputElement;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.AggregationInterface#setInputElementId(int)
	 */
	@Override
	public void setInputElement(NodeInterface node) {
		// TODO Auto-generated method stub
		setInputElement(0, node);
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.AggregationInterface#getOutputElement()
	 */
	@Override
	public NodeInterface getOutputElement() {
		// TODO Auto-generated method stub
		return getOutputElement(0);
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.AggregationInterface#setNodeText(java.lang.String)
	 */
	@Override
	public void setNodeText(String text) {
		// TODO Auto-generated method stub
		nodeText = text;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.AggregationInterface#setGroupByNodeText(java.lang.String)
	 */
	@Override
	public void setGroupByNodeText(String text) {
		// TODO Auto-generated method stub
		groupByNodeText = text;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.AggregationInterface#setFilterNodeText(java.lang.String)
	 */
	@Override
	public void setFilterNodeText(String text) {
		// TODO Auto-generated method stub
		filterNodeText = text;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnime.MetaNode#write()
	 */
	@Override
	public void write() throws WrittenAlreadyException,
			SetUpIncompleteException, IOException, ZipException {
		// TODO Auto-generated method stub
		
		Path tempFolder = getWorkflowRoot().resolve("temp");
		Path sourceFolder = Paths.get("src/main/resources/metaNodeRepos/aggrFunction");
		String zipFileName = "AGGREGATION.zip";
		String inZipFolderName = "AGGREGATION";
		Path workflowRoot = super.getWorkflowRoot();
		//folderName = m_makeFolderName();
		Path nodeFolderPath = workflowRoot.resolve(this.getFolderName());
		if (nodeFolderPath.toFile().exists()) {
			throw new WrittenAlreadyException(nodeFolderPath.toString() + " exists already! ");
		}
		if (workflowRoot.getNameCount() == 0){
			throw new SetUpIncompleteException("Workflow root is not set up for Node" + super.getId());
		}
		
		Path tempZipPath = tempFolder.resolve(zipFileName);
		tempZipPath.toFile().mkdirs();  // To make sure the "temp" folder is there. Do I need to check success? 
		
		Path sourceZipPath = sourceFolder.resolve(zipFileName);
		
		Files.copy(sourceZipPath, tempZipPath, StandardCopyOption.REPLACE_EXISTING); // throws IOException
		
		Path tempFolderForUnzip = tempFolder.resolve("unzip");

		if (Files.exists(tempFolderForUnzip)){
			FileUtils.deleteDirectory(tempFolderForUnzip.toFile());		
		}

		tempFolderForUnzip.toFile().mkdir();
		
		ZipFile zipFile = new ZipFile(tempZipPath.toString());
		zipFile.extractAll(tempFolderForUnzip.toString());
		
		Path workflowTemplate = tempFolderForUnzip.resolve(inZipFolderName).resolve("workflow.knime.template");
		Path workflowInTemp = tempFolderForUnzip.resolve(inZipFolderName).resolve("workflow.knime");
		
		Path groupByNodeFolderInTemp = tempFolderForUnzip.resolve(inZipFolderName).resolve("GroupBy (#1)");
		Path filterNodeFolderInTemp = tempFolderForUnzip.resolve(inZipFolderName).resolve("Row Filter (#2)");
		
		
		
		if (nodeText.length() > 0){
			Files.deleteIfExists(workflowInTemp);
			String workflowTemplateContent = Toolkit.readFile(
					workflowTemplate.toString());
			
			String workflowOutContent = workflowTemplateContent.replace(
					"#${annotationText}$#", 
					nodeText);
			
			PrintWriter outStream = new PrintWriter(workflowInTemp.toFile());
			
			outStream.print(workflowOutContent);
			
			outStream.close();
		}
		
		Path groupBySettingsTemplate = groupByNodeFolderInTemp.resolve("settings.xml.template");
		Path groupBySettingsInTemp = groupByNodeFolderInTemp.resolve("settings.xml");
		
		if (groupByNodeText.length() > 0){
			Files.deleteIfExists(groupBySettingsInTemp);
			String groupBySettingsTemplateContent = Toolkit.readFile(
					groupBySettingsTemplate.toString());
			
			String groupBySettingsOutContent = groupBySettingsTemplateContent.replace(
					"$#{functionName}$#", 
					groupByNodeText);
			
			PrintWriter outStream = new PrintWriter(groupBySettingsInTemp.toFile());
			
			outStream.print(groupBySettingsOutContent);
			
			outStream.close();
		}
		
		
		Path filterSettingsTemplate = filterNodeFolderInTemp.resolve("settings.xml.template");
		Path filterSettingsInTemp = filterNodeFolderInTemp.resolve("settings.xml");
		
		if (filterNodeText.length() > 0){
			Files.deleteIfExists(filterSettingsInTemp);
			String filterSettingsTemplateContent = Toolkit.readFile(
					filterSettingsTemplate.toString());
			
			String filterSettingsOutContent = filterSettingsTemplateContent.replace(
					"#${selectionText}$#", 
					filterNodeText);
			
			PrintWriter outStream = new PrintWriter(filterSettingsInTemp.toFile());
			
			outStream.print(filterSettingsOutContent);
			
			outStream.close();
		}

		
		Files.move(tempFolderForUnzip.resolve(inZipFolderName), 
				nodeFolderPath, StandardCopyOption.REPLACE_EXISTING);


	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnime.MetaNode#getNumberOfInPorts()
	 */
	@Override
	public int getNumberOfInPorts() {
		// TODO Auto-generated method stub
		return 1;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnime.MetaNode#getNumberOfOutPorts()
	 */
	@Override
	public int getNumberOfOutPorts() throws SetUpIncompleteException {
		// TODO Auto-generated method stub
		return 1;
	}

//	private synchronized String m_makeFolderName(){
//		String nameString = "Aggregative";
//		String sn = super.getId() == super.UNKNOWN_ID ? "Unknown" : String.valueOf(super.getId());
//		String fn = nameString.substring(0, Math.min(nameString.length(), 12))
//				+ " (#" + sn + ")"; 
//		return fn;
//	}

	
	/* (non-Javadoc)
	 * @see edu.phema.QdmKnime.MetaNode#getNodeName()
	 */
	@Override
	public String getNodeName() {
		// TODO Auto-generated method stub
		return "Aggregative Function (manual setup required)";
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnime.MetaNode#getFolderName()
	 */
	@Override
	public String getFolderName() {
		// TODO Auto-generated method stub
		String nameString = "Aggregative";
		String sn = super.getId() == super.UNKNOWN_ID ? "Unknown" : String.valueOf(super.getId());
		String fn = nameString.substring(0, Math.min(nameString.length(), 12))
				+ " (#" + sn + ")"; 
		return fn;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnime.MetaNode#getGoodOutPorts()
	 */
	@Override
	public int[] getGoodOutPorts() {
		// TODO Auto-generated method stub
		return new int[] {0};
	}
	

}
