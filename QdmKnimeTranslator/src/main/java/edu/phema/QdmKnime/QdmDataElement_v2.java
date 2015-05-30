/**
 * 
 */
package edu.phema.QdmKnime;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
// import java.util.Set;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

//import org.eclipse.persistence.jaxb.JAXBContext;



import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
// import javax.xml.rpc.ServiceException;
// import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import edu.phema.Enum.QdmKnime.CodeSystemEnum;
import edu.phema.Enum.QdmKnime.CreateTableColumnClassEnum;
import edu.phema.QdmKnimeInterfaces.NodeInterface;
import edu.phema.QdmKnimeInterfaces.QdmDataElementInterface;
import edu.phema.jaxb.ihe.svs.CD;
import edu.phema.jaxb.ihe.svs.ConceptListType;
import edu.phema.jaxb.ihe.svs.ObjectFactory;
import edu.phema.jaxb.ihe.svs.RetrieveValueSetResponseType;
import edu.phema.jaxb.ihe.svs.ValueSetResponseType;
import edu.phema.knime.exceptions.SetUpIncompleteException;
import edu.phema.knime.exceptions.WrittenAlreadyException;
import edu.phema.knime.nodeSettings.TableCreator;
// import edu.phema.rxnorm.BeanService.RxConcept;
// import edu.phema.rxnorm.BeanService.RxConceptGroup;
// import gov.nih.nlm.mor.axis.services.RxNormDBService.DBManager;
import edu.phema.rxnorm.BeanService.RxConcept;
import edu.phema.rxnorm.BeanService.RxConceptGroup;

/**
 * @author moh
 *
 */
public class QdmDataElement_v2 extends MetaNode implements QdmDataElementInterface {

	/**
	 * TODO Copy the v2 QDM Data Element to the package
	 * TODO Make workflow.knime to workflow.knime.template
	 * TODO On the template, update the two "Text Description from QDM" to #${qdmDescText}$#
	 */
	
	private final ArrayList<CD> valueSet = new ArrayList<CD>();
	private String qdmText = "";
	private String qdmDataType = "";
	private String valueSetOid = "";
	private String valueSetDisplayName = "";
	private String valueSetVersion = "";
	private final HashMap<String, String> variablesForSQL = new HashMap<String, String>();  //TODO remove this table
	
	private final TableCreator requiredAttributes = initializeAttributesTable();
	private int attributeTableRowCount = 0;
	// private final DBManager rxnormManager; 
	private final ArrayList<String> seenAttributes = new ArrayList<String>();
	
	
	public QdmDataElement_v2() {
		
//		requiredAttributes = initializeAttributesTable();
		attributeTableRowCount = 2;
		// rxnormManager = Toolkit.getRxnormManager();
	}

	
	private static TableCreator initializeAttributesTable(){
		TableCreator attributeTable = null;
		try {
			attributeTable = new TableCreator();
			attributeTable.setNodeAnnotationText(
					"Instruction: %%00010required output columns");
			attributeTable.setColumnProperties(0, 
					"requiredColumn", CreateTableColumnClassEnum.String);
			attributeTable.setColumnProperties(1, 
					"dataType", CreateTableColumnClassEnum.String);
			attributeTable.setColumnProperties(2, 
					"explanation", CreateTableColumnClassEnum.String);
			attributeTable.setCell("eventId", 0, 0);
			attributeTable.setCell("StringCell", 0, 1);
			attributeTable.setCell("Event Id", 0, 2);
			attributeTable.setCell("pid", 1, 0);
			attributeTable.setCell("StringCell", 1, 1);
			attributeTable.setCell("Patient Id", 1, 2);

		} catch (JAXBException | IOException e) {
			e.printStackTrace();
		}
		return attributeTable;
	}
	
	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.QdmDataElementInterface#setQdmDataElementText(java.lang.String)
	 * Descriptive text from HQMF
	 */
	@Override
	public void setQdmDataElementText(String text) {
		qdmText = text;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.QdmDataElementInterface#setQdmDataType(java.lang.String)
	 */
	@Override
	public void setQdmDataType(String qdmDataType) {
		this.qdmDataType = qdmDataType;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.QdmDataElementInterface#getQdmDataType()
	 */
	@Override
	public String getQdmDataType() {
		return qdmDataType;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.QdmDataElementInterface#addValueSet(java.lang.String)
	 */
	@Override
	public void setValueSet(String vsacXml) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(
				RetrieveValueSetResponseType.class);
		
		JAXBElement<RetrieveValueSetResponseType> node = jaxbContext.createUnmarshaller()
				.unmarshal(
				new StreamSource(new StringReader(vsacXml)), 
				RetrieveValueSetResponseType.class);
		setValueSet(node);
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.QdmDataElementInterface#addValueSet(javax.xml.bind.JAXBElement)
	 */
	@Override
	public void setValueSet(
			JAXBElement<RetrieveValueSetResponseType> vsacXmlJaxb) {
		for(int i = 0; 
				i < vsacXmlJaxb.getValue().getValueSet().getConceptList().size(); 
				i++){
			this.addValues(
					vsacXmlJaxb.getValue().getValueSet()
					.getConceptList().get(i));
		}
		setValueSetOid(vsacXmlJaxb.getValue().getValueSet().getId());
		setValueSetVersion(vsacXmlJaxb.getValue().getValueSet().getVersion());
		setValueSetDisplayName(vsacXmlJaxb.getValue()
				.getValueSet().getDisplayName());
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.QdmDataElementInterface#addValueSet(edu.phema.jaxb.ihe.svs.ConceptListType)
	 */
	@Override
	public void addValues(ConceptListType conceptList) {
		for (int i = 0; i < conceptList.getConcept().size(); i ++){
			this.addValue(conceptList.getConcept().get(i));
		}
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.QdmDataElementInterface#addValue(edu.phema.jaxb.ihe.svs.CD)
	 */
	@Override
	public void addValue(CD code) {
		valueSet.add(code);
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.QdmDataElementInterface#setValueSetOid(java.lang.String)
	 * 
	 * The oid here doesn't connect to vsac.  It is to construct a value set xml
	 */
	@Override
	public void setValueSetOid(String oid) {
		valueSetOid = oid;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.QdmDataElementInterface#setValueSetDisplayName(java.lang.String)
	 */
	@Override
	public void setValueSetDisplayName(String displayName) {
		valueSetDisplayName = displayName;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.QdmDataElementInterface#setValueSetVersion(java.lang.String)
	 */
	@Override
	public void setValueSetVersion(String version) {
		valueSetVersion = version;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.QdmDataElementInterface#setValueSetVersion()
	 * Set it to today yyyymmdd
	 */
	@Override
	public void setValueSetVersion() {
		setValueSetVersion(new SimpleDateFormat("yyyyMMdd").format(new Date()));
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.QdmDataElementInterface#getValueSetXML()
	 */
	@Override
	public String getValueSetXML() {
		String re;
		
		StringWriter stringWriter = new StringWriter();
		
		ObjectFactory objFactory = new ObjectFactory();
		
		RetrieveValueSetResponseType rootNode = objFactory
				.createRetrieveValueSetResponseType();
		
		ValueSetResponseType valSetNode = objFactory.createValueSetResponseType();
		rootNode.setValueSet(valSetNode);
		valSetNode.setDisplayName(valueSetDisplayName);
		valSetNode.setId(valueSetOid);
		valSetNode.setVersion(valueSetVersion);
		
		ConceptListType conceptListType = objFactory.createConceptListType();
		conceptListType.getConcept().addAll(valueSet);
		valSetNode.getConceptList().add(conceptListType);
		
		try {
			Marshaller mars = JAXBContext.newInstance(RetrieveValueSetResponseType.class)
					.createMarshaller();
			mars.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			mars.marshal(objFactory.createRetrieveValueSetResponse(rootNode), 
					stringWriter);
		} catch (PropertyException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		re = stringWriter.toString();
		return re;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.QdmDataElementInterface#getValueSetOid()
	 */
	@Override
	public String getValueSetOid() {
		return valueSetOid;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.QdmDataElementInterface#getValueSetDisplayName()
	 */
	@Override
	public String getValueSetDisplayName() {
		return valueSetDisplayName;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.QdmDataElementInterface#getNumberOfCodes()
	 */
	@Override
	public int getNumberOfCodes() {
		return valueSet.size();
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.QdmDataElementInterface#getCodes()
	 */
	@Override
	public String[] getCodes() {
		ArrayList<String> codes = new ArrayList<String>();
		for (CD cd : valueSet){
			codes.add(cd.getCode());
		}
		return codes.toArray(new String[codes.size()]);
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.QdmDataElementInterface#getCodes(edu.phema.Enum.QdmKnime.CodeSystemEnum)
	 */
	@Override
	public String[] getCodes(CodeSystemEnum codeSystem) {
		ArrayList<String> codes = new ArrayList<String>();
		for (CD cd : valueSet){
			if(cd.getCodeSystemName().equals(codeSystem.name())){
				codes.add(cd.getCode());
			}
		}
		return codes.toArray(new String[codes.size()]);
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.QdmDataElementInterface#getCodeDisplayNames()
	 */
	@Override
	public String[] getCodeDisplayNames() {
		ArrayList<String> names = new ArrayList<String>();
		for (CD cd : valueSet){
			names.add(cd.getDisplayName());
		}
		return names.toArray(new String[names.size()]);
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.QdmDataElementInterface#getCodeDisplayNames(edu.phema.Enum.QdmKnime.CodeSystemEnum)
	 */
	@Override
	public String[] getCodeDisplayNames(CodeSystemEnum codeSystem) {
		ArrayList<String> names = new ArrayList<String>();
		for (CD cd : valueSet){
			if(cd.getCodeSystemName().equals(codeSystem.name())){
				names.add(cd.getDisplayName());
			}
		}
		return names.toArray(new String[names.size()]);
	}

	
	public CodeSystemEnum[] getAllCodeSystems(){
		HashMap<CodeSystemEnum, Boolean> seen_systems = 
				new HashMap<CodeSystemEnum, Boolean>();
		String unknownSystem = "";
		for (CD cd : valueSet){
			try{
				CodeSystemEnum syst = CodeSystemEnum.valueOf(cd.getCodeSystemName());
				if (syst != null){
					seen_systems.put(CodeSystemEnum.valueOf(cd.getCodeSystemName()), new Boolean(true));
				}
			} catch (IllegalArgumentException e){
				if (! unknownSystem.equals(cd.getCodeSystemName())){
					System.err.println("Unknown codeSystem " + cd.getCodeSystem());
				}
				unknownSystem = cd.getCodeSystem();
			}
		}
		return seen_systems.keySet().toArray(new CodeSystemEnum[seen_systems.size()]);
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.QdmDataElementInterface#addQdmAttributes(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void addQdmAttributes(String requiredColumn, String dataType,
			String explanation) {
		if (! seenAttributes.contains(requiredColumn)){
			requiredAttributes.setCell(requiredColumn, 
					attributeTableRowCount, 0);
			requiredAttributes.setCell(dataType, 
					attributeTableRowCount, 1);
			requiredAttributes.setCell(explanation, 
					attributeTableRowCount, 2);
			attributeTableRowCount ++;
			seenAttributes.add(requiredColumn);
		}
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnime.MetaNode#write()
	 */
	@Override
	public void write() throws WrittenAlreadyException,
			SetUpIncompleteException, IOException, ZipException {
		
		
		/*
		 * Move archived node to new workflow
		 * */
		Path workflowRoot = super.getWorkflowRoot();
		Path sourceFolder = Paths.get("src/main/resources/metaNodeRepos/dataElements");
		Path tempFolder = getWorkflowRoot().resolve("temp");
		//folderName = m_makeFolderName();
		Path nodeFolderPath = workflowRoot.resolve(this.getFolderName());
		if (nodeFolderPath.toFile().exists()) {
			throw new WrittenAlreadyException(nodeFolderPath.toString() + " exists already! ");
		}
		if (workflowRoot.getNameCount() == 0){   // probably I need to find a better default directory
			throw new SetUpIncompleteException("Workflow root is not set up for Node" + super.getId());
		}
		
		/*
		 * TODO Update the new zip file
		 * */
		String zipFileName = "DATA_ELEMENT_V2.zip";
		
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
		
		/*
		 * Need to implement settings
		 * 1. put value sets (Done)
		 * 2. put variables for SQL (Done)
		 * 3. put required attributes
		 * 4. put texts
		 * 
		 * */
		
		/*
		 * Removed rxnorm requirements
		 * */
		
		if (valueSet.size() > 0){
			try {
				TableCreator tb = new TableCreator();
				tb.setColumnProperties(0, "code", CreateTableColumnClassEnum.String);
				tb.setColumnProperties(1, "codeSystem", CreateTableColumnClassEnum.String);
				tb.setColumnProperties(2, "codeSystemName", CreateTableColumnClassEnum.String);
				tb.setColumnProperties(3, "codeSystemVersion", CreateTableColumnClassEnum.String);
				tb.setColumnProperties(4, "displayName", CreateTableColumnClassEnum.String);
				for (int i = 0; i < valueSet.size(); i++){
					CD cd = valueSet.get(i);
					tb.setCell(cd.getCode(), i, 0);
					tb.setCell(cd.getCodeSystem(), i, 1);
					tb.setCell(cd.getCodeSystemName(), i, 2);
					tb.setCell(cd.getCodeSystemVersion(), i, 3);
					tb.setCell(cd.getDisplayName(), i, 4);
				}
				tb.setNodeAnnotationText(valueSetDisplayName);
				String newSettings = tb.getSettings();
				Path settingsXml = tempFolderForUnzip
						.resolve("DATA_ELEMENT_V2/Table Creator (#7)/settings.xml");
				Files.move(settingsXml, 
						tempFolderForUnzip.resolve("DATA_ELEMENT_V2/Table Creator (#7)/settings.old.xml"), 
						StandardCopyOption.REPLACE_EXISTING);
				PrintWriter outStream = new PrintWriter(settingsXml.toFile());
				outStream.print(newSettings);
				outStream.close();

			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		if (requiredAttributes != null) {
			Path settingsXml = tempFolderForUnzip
					.resolve("DATA_ELEMENT_V2/Table Creator (#1)/settings.xml");
			Files.move(settingsXml, 
					tempFolderForUnzip.resolve("DATA_ELEMENT_V2/Table Creator (#1)/settings.old.xml"), 
					StandardCopyOption.REPLACE_EXISTING);
			PrintWriter outStream = new PrintWriter(settingsXml.toFile());
			outStream.print(requiredAttributes.getSettings());
			outStream.close();
		}
		
		if (! qdmText.equals("")){
			Path workflowDir = tempFolderForUnzip
					.resolve("DATA_ELEMENT_V2");
			Files.move(workflowDir.resolve("workflow.knime"), workflowDir.resolve("workflow.knime.old"), 
					StandardCopyOption.REPLACE_EXISTING);
			String templateString = Toolkit.readFile(
					workflowDir.resolve("workflow.knime.template").toString(), 
					Charset.defaultCharset());
			String newWorkflowString = templateString.replace("#${qdmDescText}$#", 
					qdmText);
			
			PrintWriter outStream = new PrintWriter(workflowDir
					.resolve("workflow.knime").toFile());
			
			outStream.print(newWorkflowString);
			outStream.close();
		}
		
		
		
		Files.move(tempFolderForUnzip.resolve("DATA_ELEMENT_V2"), 
				nodeFolderPath, StandardCopyOption.REPLACE_EXISTING);

	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnime.MetaNode#getNumberOfInPorts()
	 */
	@Override
	public int getNumberOfInPorts() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnime.MetaNode#getNumberOfOutPorts()
	 */
	@Override
	public int getNumberOfOutPorts() throws SetUpIncompleteException {
		return 1;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnime.MetaNode#getNodeName()
	 */
	@Override
	public String getNodeName() {
		return qdmDataType;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnime.MetaNode#getFolderName()
	 */
	@Override
	public String getFolderName() {
		String sn = super.getId() == super.UNKNOWN_ID ? "Unknown" : String.valueOf(super.getId());
		String fn = qdmDataType.replaceAll("[\\/:\"*?<>|]+", "_").substring(0, Math.min(qdmDataType.length(), 12))
				+ " (#" + sn + ")"; 
		return fn;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnime.MetaNode#getGoodOutPorts()
	 */
	@Override
	public int[] getGoodOutPorts() {
		int[] goodPorts = {0};
		return goodPorts;
	}


	@Override
	public NodeInterface getOutputElement(int port) {
		NodeInterface re = null;
		if (port == 0){
			re = this;
		}
		return re;
	}

	@Override
	public void setInputElement(int port, NodeInterface node) {
		//throw new IndexOutOfBoundsException("QDM Data Element has no input elements! ");
		// Do nothing
	}

}
