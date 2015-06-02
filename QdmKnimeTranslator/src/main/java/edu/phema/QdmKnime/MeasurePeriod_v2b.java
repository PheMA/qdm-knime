/**
 * 
 */
package edu.phema.QdmKnime;

import java.io.IOException;
// import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;

import org.apache.commons.io.FileUtils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
// import edu.phema.QdmKnimeInterfaces.MeasurePeriodInterface;
import edu.phema.QdmKnimeInterfaces.MeasurePeriodInterface_v2b;
import edu.phema.QdmKnimeInterfaces.NodeInterface;
import edu.phema.knime.exceptions.SetUpIncompleteException;
import edu.phema.knime.exceptions.WrittenAlreadyException;

/**
 * @author admin
 *
 */
public class MeasurePeriod_v2b extends MetaNode implements MeasurePeriodInterface_v2b {

	/**
	 * 
	 */
	
	/*
	 * No functions more than a metanode
	 * 
	 * Input ports:
	 * 
	 * 0: Patient data with pid
	 * 1: flow variable of startMeasurePeriod and endMeasurePeriod; both are strings with string of "yyyyMMddHHmm"
	 * 
	 * */

	
	// Date measureStart = null;
	// Date measureEnd = null;
	// String dateFormat = "yyyyMMddHHmm";
	NodeInterface inputElement = this;
	
	
	public MeasurePeriod_v2b() {
		// TODO Auto-generated constructor stub
		super();
	}



	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.NodeInterface#setInputElement(int, NodeInterface)
	 */
	@Override
	public void setInputElement(int port, NodeInterface node)
			throws IndexOutOfBoundsException {
		// TODO Auto-generated method stub
		inputElement = node;
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
	 * @see edu.phema.QdmKnime.MetaNode#write()
	 */
	@Override
	public void write() throws WrittenAlreadyException,
			SetUpIncompleteException, IOException, ZipException {
		// TODO Auto-generated method stub

		//SimpleDateFormat ft = new SimpleDateFormat (dateFormat);
		
		Path tempFolder = getWorkflowRoot().resolve("temp");
		Path sourceFolder = Paths.get("src/main/resources/metaNodeRepos/dataElements");
		String zipFileName = "MeasurePeriod_v2b.zip";
		String inZipFolderName = "MeasurePeriod_v2b";
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
		return "Measure Period";
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


}
