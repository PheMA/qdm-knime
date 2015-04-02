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
import java.text.SimpleDateFormat;

import org.apache.commons.io.FileUtils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import edu.phema.QdmKnimeInterfaces.NodeInterface;
import edu.phema.QdmKnimeInterfaces.RelayNodeInterface;
import edu.phema.knime.exceptions.SetUpIncompleteException;
import edu.phema.knime.exceptions.WrittenAlreadyException;

/**
 * @author admin
 *
 */
public class RelayNode extends MetaNode implements RelayNodeInterface {

	/**
	 * 
	 */
	
	DataType knimeDataType;
	
	NodeInterface inputElement;
	
	String commentText = null;
	
	NodeInterface from;
	NodeInterface to;
	
	public RelayNode(DataType type) {
		// TODO Auto-generated constructor stub
		this.knimeDataType = type;
	}

	/**
	 * @param id
	 */
//	public RelayNode(int id, DataType type) {
//		super(id);
		// TODO Auto-generated constructor stub
//		this.knimeDataType = type;
//	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.NodeInterface#setInputElement(int, NodeInterface)
	 */
	@Override
	public void setInputElement(int port, NodeInterface node) {
		// TODO Auto-generated method stub
		inputElement = node;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.NodeInterface#getOutputElementId(int)
	 */
	@Override
	public NodeInterface getOutputElement(int port) {
		// TODO Auto-generated method stub
		NodeInterface re = null;
		if (port == 0) {
			re = this.inputElement;
		}
		return re;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.RelateNodeInterface#setComment(java.lang.String)
	 */
	@Override
	public void setComment(String text) {
		// TODO Auto-generated method stub
		this.commentText = text;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.RelateNodeInterface#setDataType(edu.phema.QdmKnimeInterfaces.RelateNodeInterface.DataType)
	 */
	@Override
	public void setDataType(DataType type) {
		// TODO Auto-generated method stub
		this.knimeDataType = type;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.RelateNodeInterface#getDataType()
	 */
	@Override
	public DataType getDataType() {
		// TODO Auto-generated method stub
		return this.knimeDataType;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnime.MetaNode#write()
	 */
	@Override
	public void write() throws WrittenAlreadyException,
			SetUpIncompleteException, IOException, ZipException {
		// TODO Auto-generated method stub
		
		Path tempFolder = getWorkflowRoot().resolve("temp");
		Path sourceFolder = Paths.get("src/main/resources/metaNodeRepos/relays");
		String zipFileName = this.knimeDataType.ZIP_FILE_NAME;
		String inZipFolderName = this.knimeDataType.FOLDER_NAME;
		Path workflowRoot = super.getWorkflowRoot();
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
		
		if (this.commentText != null) {
			Path workflowTemplateInTemp = tempFolderForUnzip
					.resolve(inZipFolderName)
					.resolve("workflow.knime.template");
			Path workflowInTemp = tempFolderForUnzip
					.resolve(inZipFolderName)
					.resolve("workflow.knime");
			Files.move(workflowInTemp, 
					tempFolderForUnzip
					.resolve(inZipFolderName)
					.resolve("workflow.knime.old"), StandardCopyOption.REPLACE_EXISTING);
			String newWorkflow = Toolkit.readFile(workflowTemplateInTemp.toString()).
					replace("$#{comments}#$", commentText);
			PrintWriter outStream = new PrintWriter(workflowInTemp.toFile());
			outStream.print(newWorkflow);
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

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnime.MetaNode#getNodeName()
	 */
	@Override
	public String getNodeName() {
		// TODO Auto-generated method stub
		return this.knimeDataType.toString();
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnime.MetaNode#getFolderName()
	 */
	@Override
	public String getFolderName() {
		// TODO Auto-generated method stub
		String sn = super.getId() == super.UNKNOWN_ID ? "Unknown" : String.valueOf(super.getId());
		String fn = this.getNodeName().substring(0, Math.min(this.getNodeName().length(), 12))
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

	@Override
	public NodeInterface getFrom() {
		// TODO Auto-generated method stub
		return from;
	}

	@Override
	public NodeInterface getTo() {
		// TODO Auto-generated method stub
		return to;
	}

	@Override
	public void setFrom(NodeInterface from) {
		// TODO Auto-generated method stub
		this.from = from;
	}

	@Override
	public void setTo(NodeInterface to) {
		// TODO Auto-generated method stub
		this.to = to;
	}

}
