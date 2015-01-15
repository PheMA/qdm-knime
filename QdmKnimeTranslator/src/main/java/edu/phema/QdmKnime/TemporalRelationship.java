/**
 * 
 */
package edu.phema.QdmKnime;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Random;

import org.apache.commons.io.FileUtils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import edu.phema.QdmKnimeInterfaces.TemporalRelationshipInterface;
import edu.phema.knime.exceptions.SetUpIncompleteException;
import edu.phema.knime.exceptions.WrittenAlreadyException;

/**
 * @author Huan
 *
 */
public class TemporalRelationship extends MetaNode implements
		TemporalRelationshipInterface {

	/**
	 * 
	 */
	private final TemporalTypeCode temporalType;
	
	private int leftElementNodeId = Integer.MIN_VALUE;
	private int rightElementNodeId = Integer.MIN_VALUE;

	@SuppressWarnings("unused")
	private Operator operator = Operator.none;

	@SuppressWarnings("unused")
	private int quantity = 0;

	@SuppressWarnings("unused")
	private Unit unit = Unit.days;
	
	String folderName;
	
	private Path sourceFolder = Paths.get("src/main/resources/metaNodeRepos/temporalRelationships");
	
	private Path tempFolder = Paths.get("");

	private Random randMachine = new Random();

	
	public TemporalRelationship(TemporalTypeCode temporalType) {
		// TODO Auto-generated constructor stub
		this.temporalType = temporalType;
		folderName = m_makeFolderName();
		super.setX(300 + randMachine.nextInt(5) * 50);
		super.setY(100 + randMachine.nextInt(5) * 25);
	}

	/**
	 * @param id
	 */
	public TemporalRelationship(int id, TemporalTypeCode temporalType) {
		super(id);
		// TODO Auto-generated constructor stub
		this.temporalType = temporalType;
		folderName = m_makeFolderName();
		super.setX(300 + randMachine.nextInt(5) * 50);
		super.setY(100 + randMachine.nextInt(5) * 25);
		
		/*
		 * The following is useless mostly
		 * */
		leftElementNodeId = id;
		rightElementNodeId = id;
	}
	
	@Override
	public void setId(int id){
		super.setId(id);
		folderName = m_makeFolderName();
		/*
		 * The following is useless mostly
		 * */
		if (leftElementNodeId == Integer.MIN_VALUE) {
			leftElementNodeId = id;
		}
		if (leftElementNodeId == Integer.MIN_VALUE) {
			leftElementNodeId = id;
		}
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.phema.QdmKnimeInterfaces.TemporalRelationshipInterface#setLeftId(int)
	 */
	@Override
	public void setLeftId(int element_node_id) {
		// TODO Auto-generated method stub
		leftElementNodeId = element_node_id;
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.phema.QdmKnimeInterfaces.TemporalRelationshipInterface#setRightId(int)
	 */
	@Override
	public void setRightId(int element_node_id) {
		// TODO Auto-generated method stub
		rightElementNodeId = element_node_id;
	}


	/* (non-Javadoc)
	 * @see edu.vanderbilt.phema.QdmKnimeInterfaces.TemporalRelationshipInterface#getLeftId()
	 */
	@Override
	public synchronized int getLeftId() {
		// TODO Auto-generated method stub
		return leftElementNodeId;
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.phema.QdmKnimeInterfaces.TemporalRelationshipInterface#getRightId()
	 */
	@Override
	public synchronized int getRightId() {
		// TODO Auto-generated method stub
		return rightElementNodeId;
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.phema.QdmKnimeInterfaces.TemporalRelationshipInterface#setOperator(edu.vanderbilt.phema.QdmKnimeInterfaces.TemporalRelationshipInterface.Operator)
	 * <= 120 days: <=
	 */
	@Override
	public synchronized void setOperator(Operator operator) {
		// TODO Auto-generated method stub
		// #${setOperator}$#
		this.operator = operator;
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.phema.QdmKnimeInterfaces.TemporalRelationshipInterface#setQuantity(float)
	 * <= 120 days: 120
	 */
	@Override
	public synchronized void setQuantity(int quantity) {
		// TODO Auto-generated method stub
		this.quantity = quantity;

	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.phema.QdmKnimeInterfaces.TemporalRelationshipInterface#setUnit(edu.vanderbilt.phema.QdmKnimeInterfaces.TemporalRelationshipInterface.Unit)
	 * <= 120 days: days
	 */
	@Override
	public synchronized void setUnit(Unit unit) {
		// TODO Auto-generated method stub
		this.unit = unit;
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.phema.QdmKnime.MetaNode#write()
	 */
	@Override
	public synchronized void write() throws WrittenAlreadyException,
			SetUpIncompleteException, IOException, ZipException {
		// TODO Auto-generated method stub
		
		Path workflowRoot = super.getWorkflowRoot();
		//folderName = m_makeFolderName();
		Path nodeFolderPath = workflowRoot.resolve(folderName);
		if (nodeFolderPath.toFile().exists()) {
			throw new WrittenAlreadyException(nodeFolderPath.toString() + " exists already! ");
		}
		if (workflowRoot.getNameCount() == 0){
			throw new SetUpIncompleteException("Workflow root is not set up for Node" + super.getId());
		}
		
		String zipFileName = temporalType.name() + ".zip";
		
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
		
				 
		if (haveGoodOperations()){
			
			/*
			 * Set up Operator (<= 120 days: <=)
			 * */
			
			Path operatorTemplate = tempFolderForUnzip.resolve( /* Node Folder resource name */ temporalType.name()
					).resolve(getOperatorSettingNodeFolder()).resolve("settings.xml.template");
			Path operatorSettings = tempFolderForUnzip.resolve( /* Node Folder resource name */ temporalType.name()
					).resolve(getOperatorSettingNodeFolder()).resolve("settings.xml");
			operatorSettings.toFile().delete();
			
			String operatorTemplateContent = Toolkit.readFile(
					operatorTemplate.toString(), 
					Charset.defaultCharset());
			String operatorSettingsContent = operatorTemplateContent.replace(
					"#${setOperator}$#", getOperatorString());
			
			PrintWriter outStream = new PrintWriter(operatorSettings.toFile());
			
			outStream.print(operatorSettingsContent);
			outStream.close();
			
			/*
			 * Need to implement quantity and unit: 120 days
			 * 
			 * #${quantity}$#
			 * 
			 * #${unit}$#
			 * 
			 * */
			
			Path timeShiftingTemplate = tempFolderForUnzip.resolve( /* Node Folder resource name */ temporalType.name()
					).resolve(getTimeShiftingSettingNodeFolder()).resolve("settings.xml.template");
			Path timeShiftingSettings = tempFolderForUnzip.resolve( /* Node Folder resource name */ temporalType.name()
					).resolve(getTimeShiftingSettingNodeFolder()).resolve("settings.xml");
			timeShiftingSettings.toFile().delete();
			
			String timeShiftingTemplateContent = Toolkit.readFile(
					timeShiftingTemplate.toString(), 
					Charset.defaultCharset());
			String timeShiftingSettingsContent = timeShiftingTemplateContent
					.replace("#${quantity}$#", String.valueOf(getQuantity()))
					.replace("#${unit}$#", getShiftingUnitKnime());
			
			PrintWriter outStream2 = new PrintWriter(timeShiftingSettings.toFile());
			outStream2.print(timeShiftingSettingsContent);
			outStream2.close();
			
			
		}
		
		Files.move(tempFolderForUnzip.resolve(temporalType.name()), 
				nodeFolderPath, StandardCopyOption.REPLACE_EXISTING);
	}
	
	private String getShiftingUnitKnime(){
		String re = "Day";
		switch (unit){
		case days: re = "Day"; break;
		case hours: re = "Hour"; break;
		case minutes: re = "Minute"; break;
		case months: re = "Month"; break;
		case seconds: re = "Second"; break;
		case weeks: re = "Week"; break;
		case years: re = "Year"; break;
		}
		return re;
	}
	
	private String getOperatorString() {
		String re = " Wrong ";
		switch(operator){
		case equalTo: re = " = "; break;
		case greaterThan: re = " &gt; "; break;
		case greaterThanOrEqualTo: re = " &gt;= "; break;
		case lessThan: re = " &lt; "; break;
		case lessThanOrEqualTo: re = " &lt;= "; break;
		case none: break;
		}
		return re; 
	}
	
	private boolean haveGoodOperations(){
		return 	operator != Operator.none && 
				quantity != 0 && ( 
				temporalType == TemporalTypeCode.EAE ||
				temporalType == TemporalTypeCode.EAS ||
				temporalType == TemporalTypeCode.EBE ||
				temporalType == TemporalTypeCode.EBS ||
				temporalType == TemporalTypeCode.SAE ||
				temporalType == TemporalTypeCode.SAS ||
				temporalType == TemporalTypeCode.SBE ||
				temporalType == TemporalTypeCode.SBS);	
	}
	
	private String getOperatorSettingNodeFolder(){
		// The folder names happen to be all the same for all temporal types
		String re = "Rule_based Row Filter (#55)";
		return re;
	}
	
	private String getTimeShiftingSettingNodeFolder(){
		// The folder names happen to be all the same for all temporal types
		String re = "Date_Time Shift  (#60)";
		return re;
	}
	
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
	 * @see edu.vanderbilt.phema.QdmKnime.MetaNode#getNumberOfInPorts()
	 */
	@Override
	public synchronized int getNumberOfInPorts() {
		// TODO Auto-generated method stub
		return 2;
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.phema.QdmKnime.MetaNode#getNumberOfOutPorts()
	 */
	@Override
	public synchronized int getNumberOfOutPorts() throws SetUpIncompleteException {
		// TODO Auto-generated method stub
		return 2;
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.phema.QdmKnime.MetaNode#getNodeName()
	 */
	@Override
	public synchronized String getNodeName() {
		// TODO Auto-generated method stub
		String nodeName = "";
		switch (temporalType) {
			case CONCURRENT: nodeName = "Concurrent With"; break;
			case DURING: nodeName = "Occurs During"; break;
			case EAE: nodeName = "Ends After End Of"; break;
			case EBE: nodeName = "Ends Before End Of"; break;
			case EBS: nodeName = "Ends Before Start Of"; break;
			case ECW: nodeName = "Ends Concurrrent With"; break;
			case ECWS: nodeName = "Ends Concurrent With Start Of"; break;
			case EDU: nodeName = "Ends During"; break;
			case OVERLAP: nodeName = "Overlaps With"; break;
			case SAE: nodeName = "Starts After Start Of"; break;
			case SBE: nodeName = "Start Before End Of"; break;
			case SBS: nodeName = "Starts Before Start Of"; break;
			case SCW: nodeName = "Starts Concurrent With"; break;
			case SCWE: nodeName = "Start Concurrent With End Of"; break;
			case SDU: nodeName = "Start During"; break;
		}
		
		return nodeName;
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.phema.QdmKnime.MetaNode#getFolderName()
	 */
	@Override
	public synchronized String getFolderName() {
		// TODO Auto-generated method stub
		return folderName;
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.phema.QdmKnime.MetaNode#getGoodOutPorts()
	 */
	@Override
	public synchronized int[] getGoodOutPorts() {
		// TODO Auto-generated method stub
		return new int[] {0, 1};
	}
	
	private synchronized String m_makeFolderName(){
		String temperalName = temporalType.name();
		String sn = super.getId() == super.UNKNOWN_ID ? "Unknown" : String.valueOf(super.getId());
		String fn = temperalName.substring(0, Math.min(temperalName.length(), 12))
				+ " (#" + sn + ")"; 
		return fn;
	}

	@Override
	public TemporalTypeCode getTemporalType() {
		// TODO Auto-generated method stub
		return temporalType;
	}

	@Override
	public Operator getOperator() {
		// TODO Auto-generated method stub
		return operator;
	}

	@Override
	public int getQuantity() {
		// TODO Auto-generated method stub
		return quantity;
	}

	@Override
	public Unit getUnit() {
		// TODO Auto-generated method stub
		return unit;
	}

	@Override
	public int getOutputElementId(int port) throws IndexOutOfBoundsException {
		// TODO Auto-generated method stub
		int ret = Integer.MIN_VALUE;
		if (port == 0){
			ret = this.getLeftId();
		} else if (port == 1) {
			ret = this.getRightId();
		} else {
			throw new IndexOutOfBoundsException();
		}
		return ret;
	}

	@Override
	public void setInputElementId(int port, int elementId)
			throws IndexOutOfBoundsException {
		// TODO Auto-generated method stub
		if (port == 0)
			this.setLeftId(elementId);
		else if (port == 1)
			this.setRightId(elementId);
		else throw new IndexOutOfBoundsException();
	}


}
