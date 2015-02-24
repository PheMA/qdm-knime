/**
 * 
 */
package edu.phema.QdmKnime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
//import java.util.Random;



import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import edu.phema.Enum.QdmKnime.CreateTableColumnClassEnum;
import edu.phema.QdmKnimeInterfaces.NodeInterface;
import edu.phema.QdmKnimeInterfaces.TemporalRelationshipInterface;
import edu.phema.knime.exceptions.SetUpIncompleteException;
import edu.phema.knime.exceptions.WrittenAlreadyException;
import edu.phema.knime.nodeSettings.RowFilter;

import javax.xml.bind.JAXBException;
//import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
	
	private NodeInterface leftElementNode = this;
	private NodeInterface rightElementNode = this;

	private Operator operator = Operator.greaterThan;

	private double quantity = 0.0;

	private Unit unit = Unit.days;
	
//	String folderName;
	
	private Path sourceFolder = Paths.get("src/main/resources/metaNodeRepos/temporalRelationships");
	
	private Path tempFolder = Paths.get("");

//	private Random randMachine = new Random();

	private final HashMap <String, String> toModifyAnnotation = new HashMap<String, String>();
	
	public TemporalRelationship(TemporalTypeCode temporalType) {
		// TODO Auto-generated constructor stub
		this.temporalType = temporalType;
//		folderName = m_makeFolderName();
//		super.setX(300 + randMachine.nextInt(5) * 50);
//		super.setY(100 + randMachine.nextInt(5) * 25);
	}

	/**
	 * @param id
	 */
//	public TemporalRelationship(int id, TemporalTypeCode temporalType) {
//		super(id);
		// TODO Auto-generated constructor stub
//		this.temporalType = temporalType;
//		folderName = m_makeFolderName();
//		super.setX(300 + randMachine.nextInt(5) * 50);
//		super.setY(100 + randMachine.nextInt(5) * 25);
		
		/*
		 * The following is useless mostly
		 * */
//		leftElementNodeId = id;
//		rightElementNodeId = id;
//	}
	

	/* (non-Javadoc)
	 * @see edu.vanderbilt.phema.QdmKnimeInterfaces.TemporalRelationshipInterface#setLeftElement(NodeInterface)
	 */
	@Override
	public void setLeftElement(NodeInterface node) {
		// TODO Auto-generated method stub
		leftElementNode = node;
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.phema.QdmKnimeInterfaces.TemporalRelationshipInterface#setRightElement(NodeInterface)
	 */
	@Override
	public void setRightElement(NodeInterface node) {
		// TODO Auto-generated method stub
		rightElementNode = node;
	}


	/* (non-Javadoc)
	 * @see edu.vanderbilt.phema.QdmKnimeInterfaces.TemporalRelationshipInterface#getLeftElement()
	 */
	@Override
	public synchronized NodeInterface getLeftElement() {
		// TODO Auto-generated method stub
		return leftElementNode;
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.phema.QdmKnimeInterfaces.TemporalRelationshipInterface#getRightElement()
	 */
	@Override
	public synchronized NodeInterface getRightElement() {
		// TODO Auto-generated method stub
		return rightElementNode;
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
	public synchronized void setQuantity(double quantity) {
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
	@SuppressWarnings("resource")
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
		
		if (toModifyAnnotation.size() > 0){
			Path workflowKnime = tempFolderForUnzip.resolve( /* Node Folder resource name */ temporalType.name())
					.resolve("workflow.knime");
			Path workflowKnime_temp = tempFolderForUnzip.resolve( /* Node Folder resource name */ temporalType.name())
					.resolve("workflow.knime.temp");
			Path workflowKnime_old = tempFolderForUnzip.resolve( /* Node Folder resource name */ temporalType.name())
					.resolve("workflow.knime.old");
			
			/*
			 * in CONCURRENT node, the key for annotation of left/right input is somehow different,
			 * which suchs. 
			 * */
			String annotationKey = "annotation_2";
			if (temporalType.equals(TemporalTypeCode.CONCURRENT)){
				annotationKey = "annotation_0";
			}
			
			try {
				Document workflowKnimeDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
						.parse(workflowKnime.toFile());
				workflowKnimeDoc.getDocumentElement().normalize();
				XPathExpression annotXpath = XPathFactory.newInstance()
						.newXPath().compile("/config[@key=\"workflow.knime\"]/config[@key=\"annotations\"]/config[@key=\"" 
								+ annotationKey + "\"]/entry[@key=\"text\"]");
				
				Node annotEntry = (Node) annotXpath.evaluate(workflowKnimeDoc, XPathConstants.NODE);
				String annotText = annotEntry.getAttributes().getNamedItem("value").getTextContent();
				for (String from : toModifyAnnotation.keySet()){
					String to = toModifyAnnotation.get(from);
					annotText = annotText.replace(from, to);
				}
				annotEntry.getAttributes().getNamedItem("value").setNodeValue(annotText);
				
				
				
				FileOutputStream workflowFOS = new FileOutputStream(workflowKnime_temp.toFile());
				TransformerFactory.newInstance().newTransformer().transform(new DOMSource(workflowKnimeDoc), 
						new StreamResult(workflowFOS));
				workflowFOS.close();
				
				Files.move(workflowKnime, workflowKnime_old, StandardCopyOption.REPLACE_EXISTING);
				Files.move(workflowKnime_temp, workflowKnime, StandardCopyOption.REPLACE_EXISTING);

				
				
			} catch (SAXException | ParserConfigurationException | XPathExpressionException | 
					TransformerException | TransformerFactoryConfigurationError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		if (haveGoodOperations()){
			
	
			/*
			 * Set up Unit for Time Difference
			 * */
			
			Path timeDiffSettings = tempFolderForUnzip.resolve( /* Node Folder resource name */ temporalType.name()
					).resolve(getTimeDifferenceSettingNodeFolder()).resolve("settings.xml");
			Path timeDiffSettings_temp = tempFolderForUnzip.resolve( /* Node Folder resource name */ temporalType.name()
					).resolve(getTimeDifferenceSettingNodeFolder()).resolve("settings.xml.temp");
			Path timeDiffSettings_old = tempFolderForUnzip.resolve( /* Node Folder resource name */ temporalType.name()
					).resolve(getTimeDifferenceSettingNodeFolder()).resolve("settings.xml.old");
			
			//Files.move(timeDiffSettings, timeDiffSettings_old, StandardCopyOption.REPLACE_EXISTING);
			
			
			try {
				File timeDiffTemplateFile = timeDiffSettings.toFile();
				Document timeDiffXmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(timeDiffTemplateFile);
				timeDiffXmlDoc.getDocumentElement().normalize();
				XPathExpression granXpath = XPathFactory.newInstance()
						.newXPath().compile("/config/config[@key=\"model\"]/entry[@key=\"granularity\"]");
				Node granEntry = (Node) granXpath.evaluate(timeDiffXmlDoc, XPathConstants.NODE);
				/* update unit to getTimeUnitKnime() */
				granEntry.getAttributes().getNamedItem("value").setNodeValue(getTimeUnitKnime()); 
				
				
				FileOutputStream fosTimeDiff = new FileOutputStream(timeDiffSettings_temp.toFile());
				TransformerFactory.newInstance().newTransformer().transform(new DOMSource(timeDiffXmlDoc), 
						new StreamResult(fosTimeDiff));
				fosTimeDiff.close();
				
				Files.move(timeDiffSettings, timeDiffSettings_old, StandardCopyOption.REPLACE_EXISTING);
				Files.move(timeDiffSettings_temp, timeDiffSettings, StandardCopyOption.REPLACE_EXISTING);
				
			} catch (SAXException | ParserConfigurationException | XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerFactoryConfigurationError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			/*
			 * Set up quantity and operator on Row Filter
			 * */
			Path rowFilterSettings = tempFolderForUnzip.resolve( /* Node Folder resource name */ temporalType.name()
					).resolve(getRowFilterSettingNodeFolder()).resolve("settings.xml");
			Path rowFilterSettings_temp = tempFolderForUnzip.resolve( /* Node Folder resource name */ temporalType.name()
					).resolve(getRowFilterSettingNodeFolder()).resolve("settings.xml.temp");
			Path rowFilterSettings_old = tempFolderForUnzip.resolve( /* Node Folder resource name */ temporalType.name()
					).resolve(getRowFilterSettingNodeFolder()).resolve("settings.xml.old");
			
			try {
				RowFilter rowFilter = new RowFilter();
				rowFilter.setIncludeOrExclude(true);
				rowFilter.setColumnName("time_diff");
				if (this.operator == Operator.lessThan || this.operator == Operator.lessThanOrEqualTo) {
					rowFilter.setRangeValues(CreateTableColumnClassEnum.Double, Double.valueOf(quantity), Double.valueOf(0.0));
				} else if (this.operator == Operator.equalTo) {
					rowFilter.setRangeValues(CreateTableColumnClassEnum.Double, 
							Double.valueOf(quantity + 0.5), Double.valueOf(quantity - 0.5));
				} else /* Default greaterThan 0 */ {
					rowFilter.setRangeValues(CreateTableColumnClassEnum.Double, null, Double.valueOf(quantity));
				}
				
				PrintWriter pwRowFilter = new PrintWriter(rowFilterSettings_temp.toFile());
				pwRowFilter.print(rowFilter.getSettings());
				pwRowFilter.close();
				
				Files.move(rowFilterSettings, rowFilterSettings_old, StandardCopyOption.REPLACE_EXISTING);
				Files.move(rowFilterSettings_temp, rowFilterSettings, StandardCopyOption.REPLACE_EXISTING);
				
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//			String operatorTemplateContent = Toolkit.readFile(
//					operatorTemplate.toString(), 
//					Charset.defaultCharset());
//			String operatorSettingsContent = operatorTemplateContent.replace(
//					"#${setOperator}$#", getOperatorString());
			
//			PrintWriter outStream = new PrintWriter(operatorSettings.toFile());
			
//			outStream.print(operatorSettingsContent);
//			outStream.close();
			
			/*
			 * Need to implement quantity and unit: 120 days
			 * 
			 * #${quantity}$#
			 * 
			 * #${unit}$#
			 * 
			 * */
			
//			Path timeShiftingTemplate = tempFolderForUnzip.resolve( /* Node Folder resource name */ temporalType.name()
//					).resolve(getTimeShiftingSettingNodeFolder()).resolve("settings.xml.template");
//			Path timeShiftingSettings = tempFolderForUnzip.resolve( /* Node Folder resource name */ temporalType.name()
//					).resolve(getTimeShiftingSettingNodeFolder()).resolve("settings.xml");
//			timeShiftingSettings.toFile().delete();
			
//			String timeShiftingTemplateContent = Toolkit.readFile(
//					timeShiftingTemplate.toString(), 
//					Charset.defaultCharset());
//			String timeShiftingSettingsContent = timeShiftingTemplateContent
//					.replace("#${quantity}$#", String.valueOf(getQuantity()))
//					.replace("#${unit}$#", getShiftingUnitKnime());
			
//			PrintWriter outStream2 = new PrintWriter(timeShiftingSettings.toFile());
//			outStream2.print(timeShiftingSettingsContent);
//			outStream2.close();
			
			
		}
		
		Files.move(tempFolderForUnzip.resolve(temporalType.name()), 
				nodeFolderPath, StandardCopyOption.REPLACE_EXISTING);
	}
	
	private String getTimeUnitKnime(){
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
				temporalType.equals(TemporalTypeCode.EAE) ||
				temporalType.equals(TemporalTypeCode.EAS) ||
				temporalType.equals(TemporalTypeCode.EBE) ||
				temporalType.equals(TemporalTypeCode.EBS) ||
				temporalType.equals(TemporalTypeCode.SAE) ||
				temporalType.equals(TemporalTypeCode.SAS) ||
				temporalType.equals(TemporalTypeCode.SBE) ||
				temporalType.equals(TemporalTypeCode.SBS));	
	}
	
	private String getRowFilterSettingNodeFolder(){
		// The folder names happen to be all the same for all temporal types
		//String re = "Rule_based Row Filter (#55)";
		String re = "Row Filter (#67)";
		return re;
	}
	
	private String getTimeDifferenceSettingNodeFolder(){
		// The folder names happen to be all the same for all temporal types
		String re = "Time Difference (#68)";
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
		String temperalName = temporalType.name();
		String sn = super.getId() == super.UNKNOWN_ID ? "Unknown" : String.valueOf(super.getId());
		String fn = temperalName.substring(0, Math.min(temperalName.length(), 12))
				+ " (#" + sn + ")"; 
		return fn;
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.phema.QdmKnime.MetaNode#getGoodOutPorts()
	 */
	@Override
	public synchronized int[] getGoodOutPorts() {
		// TODO Auto-generated method stub
		return new int[] {0, 1};
	}
	
//	private synchronized String m_makeFolderName(){
//		String temperalName = temporalType.name();
//		String sn = super.getId() == super.UNKNOWN_ID ? "Unknown" : String.valueOf(super.getId());
//		String fn = temperalName.substring(0, Math.min(temperalName.length(), 12))
//				+ " (#" + sn + ")"; 
//		return fn;
//	}

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
	public double getQuantity() {
		// TODO Auto-generated method stub
		return quantity;
	}

	@Override
	public Unit getUnit() {
		// TODO Auto-generated method stub
		return unit;
	}

	@Override
	public NodeInterface getOutputElement(int port) {
		// TODO Auto-generated method stub
		NodeInterface ret = null;
		if (port == 0){
			ret = this.getLeftElement();
		} else if (port == 1) {
			ret = this.getRightElement();
		} 
		return ret;
	}

	@Override
	public void setInputElement(int port, NodeInterface node) {
		// TODO Auto-generated method stub
		if (port == 0)
			this.setLeftElement(node);
		else if (port == 1)
			this.setRightElement(node);
		else throw new IndexOutOfBoundsException();
	}

	@Override
	public void modifyAnnotateTexts(String old, String newText) {
		// TODO Auto-generated method stub
		toModifyAnnotation.put(old, newText);
	}


}
