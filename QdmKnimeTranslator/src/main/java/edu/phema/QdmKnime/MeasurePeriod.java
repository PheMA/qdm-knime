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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import edu.phema.QdmKnimeInterfaces.MeasurePeriodInterface;
import edu.phema.knime.exceptions.SetUpIncompleteException;
import edu.phema.knime.exceptions.WrittenAlreadyException;

/**
 * @author admin
 *
 */
public class MeasurePeriod extends MetaNode implements MeasurePeriodInterface {

	/**
	 * 
	 */
	
	Date measureStart = null;
	Date measureEnd = null;
	String dateFormat = "yyyyMMddHHmm";
	
	public MeasurePeriod() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param id
	 */
	public MeasurePeriod(int id) {
		super(id);
		// TODO Auto-generated constructor stub
	}
	
	public MeasurePeriod(int id, String start_yyyyMMddHHmm, String end_yyyyMMddHHmm) throws ParseException{
		super(id);
		this.setMeasureStart(start_yyyyMMddHHmm);
		this.setMeasureEnd(end_yyyyMMddHHmm);
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.NodeInterface#setInputElementId(int, int)
	 */
	@Override
	public void setInputElementId(int port, int elementId)
			throws IndexOutOfBoundsException {
		// TODO Auto-generated method stub
		// Do nothing
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.NodeInterface#getOutputElementId(int)
	 */
	@Override
	public int getOutputElementId(int port) throws IndexOutOfBoundsException {
		// TODO Auto-generated method stub
		return Integer.MIN_VALUE;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.MeasurePeriodInterface#setMeasureStart(java.lang.String)
	 */
	@Override
	public void setMeasureStart(String yyyyMMddHHmm) throws ParseException {
		// TODO Auto-generated method stub
		SimpleDateFormat ft = new SimpleDateFormat (dateFormat);
		measureStart = ft.parse(yyyyMMddHHmm);
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.MeasurePeriodInterface#setMeasureEnd(java.lang.String)
	 */
	@Override
	public void setMeasureEnd(String yyyyMMddHHmm) throws ParseException {
		// TODO Auto-generated method stub
		SimpleDateFormat ft = new SimpleDateFormat (dateFormat);
		measureEnd = ft.parse(yyyyMMddHHmm);
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.MeasurePeriodInterface#getMeasureStart()
	 */
	@Override
	public Date getMeasureStart() {
		// TODO Auto-generated method stub
		return this.measureStart;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.MeasurePeriodInterface#getMeasureEnd()
	 */
	@Override
	public Date getMeasureEnd() {
		// TODO Auto-generated method stub
		return this.measureEnd;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnime.MetaNode#write()
	 */
	@Override
	public void write() throws WrittenAlreadyException,
			SetUpIncompleteException, IOException, ZipException {
		// TODO Auto-generated method stub

		SimpleDateFormat ft = new SimpleDateFormat (dateFormat);
		
		Path tempFolder = getWorkflowRoot().resolve("temp");
		Path sourceFolder = Paths.get("src/main/resources/metaNodeRepos/dataElements");
		String zipFileName = "MeasurePeriod.zip";
		String inZipFolderName = "MeasurePeriod";
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
		
		if (measureStart != null && measureEnd != null){
			Path settingsTemplateInTemp = tempFolderForUnzip
					.resolve(inZipFolderName)
					.resolve("Java Snippet (#1)/settings.xml.template");
			Path settingsInTemp = tempFolderForUnzip
					.resolve(inZipFolderName)
					.resolve("Java Snippet (#1)/settings.xml");
			settingsInTemp.toFile().delete();
			String settingsOutContent = Toolkit.readFile(settingsTemplateInTemp.toString())
					.replace("$#{startyyyyMMddHHmm}#$", ft.format(measureStart))
					.replace("$#{endyyyyMMddHHmm}#$", ft.format(measureEnd));
			PrintWriter outStream = new PrintWriter(settingsInTemp.toFile());
			outStream.print(settingsOutContent);
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

	@Override
	public void setDateFormat(String dateFormat) {
		// TODO Auto-generated method stub
		this.dateFormat = dateFormat;
	}

}
