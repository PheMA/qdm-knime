/**
 * 
 */
package edu.phema.QdmKnime;

import java.awt.Point;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import javax.xml.bind.JAXBException;

import net.lingala.zip4j.exception.ZipException;
import edu.phema.Enum.QdmKnime.CreateTableColumnClassEnum;
import edu.phema.QdmKnimeInterfaces.NodeInterface;
import edu.phema.jaxb.knime.Config;
import edu.phema.jaxb.knime.EntryType;
import edu.phema.jaxb.knime.ObjectFactory;
import edu.phema.knime.exceptions.SetUpIncompleteException;
import edu.phema.knime.exceptions.WrittenAlreadyException;
import edu.phema.knime.nodeSettings.RowFilter;

/**
 * @author moh
 *
 */
public class Attribute implements NodeInterface {

	int id = Integer.MIN_VALUE;
	
	private Path workflowRoot = Paths.get("");
	
	private final Point nodeLocation = new Point(150, new Random().nextInt(500));
	
	private int resourceElementId = Integer.MIN_VALUE; 
	
	private int nodeWidth = 130;
	
	private int nodeHeight = 67;

	private final RowFilter rowFilter;
	
	
	/**
	 * @throws IOException 
	 * @throws JAXBException 
	 * 
	 */
	public Attribute() {
		// TODO Auto-generated constructor stub
		try {
			rowFilter = new RowFilter();
			rowFilter.setAnnotationText("Attribute");
		} catch (JAXBException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("Cannot read template for row filter node. ");
		}
	}

	public Attribute(int id) {
		// TODO Auto-generated constructor stub
		this.id = id;
		try {
			rowFilter = new RowFilter();
			rowFilter.setAnnotationText("Attribute");
		} catch (JAXBException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("Cannot read template for row filter node. ");
		}
	}
	
	public void setMode_isPresent(){
		rowFilter.setMissingValuesMatch();
		rowFilter.setIncludeOrExclude(false);
	}
	
	public void setMode_Comparison(Number upper, Number lower){
		rowFilter.setRangeValues(CreateTableColumnClassEnum.Double, upper, lower);
		rowFilter.setIncludeOrExclude(true);
	}
	
	public void setMode_textTool(String pattern, boolean caseSensitive, 
			boolean hasWildCards, boolean isRegExpr){
		rowFilter.setStringMatching(pattern, caseSensitive, hasWildCards, isRegExpr);
		rowFilter.setIncludeOrExclude(true);
	}
	
	public void setAttributeName (String attribute){
		//this.attributeName = attribute;
		rowFilter.setColumnName(attribute);
		rowFilter.setAnnotationText("Attribute: " + attribute);
	}
	
	public String getAttributeName (){
		return rowFilter.getColumnName();
	}
	
	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.NodeInterface#setWorkflowRoot(java.lang.String)
	 */
	@Override
	public void setWorkflowRoot(String dir) {
		// TODO Auto-generated method stub
		workflowRoot = Paths.get(dir);
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.NodeInterface#setId(int)
	 */
	@Override
	public void setId(int id) {
		// TODO Auto-generated method stub
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.NodeInterface#setX(int)
	 */
	@Override
	public void setX(int x) {
		// TODO Auto-generated method stub
		this.nodeLocation.x = x;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.NodeInterface#setY(int)
	 */
	@Override
	public void setY(int y) {
		// TODO Auto-generated method stub
		this.nodeLocation.y = y;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.NodeInterface#write()
	 */
	@Override
	public void write() throws WrittenAlreadyException,
			SetUpIncompleteException, IOException, ZipException {
		// TODO Auto-generated method stub
		Path nodeFolderPath = workflowRoot.resolve(this.getFolderName());
		if (nodeFolderPath.toFile().exists()) {
			throw new WrittenAlreadyException(nodeFolderPath.toString() + " exists already! ");
		}
		if (workflowRoot.getNameCount() == 0){
			throw new SetUpIncompleteException("Workflow root is not set up for Node" + this.getId());
		}
		nodeFolderPath.toFile().mkdirs();
		Path settingsXml = nodeFolderPath.resolve("settings.xml");
		PrintWriter outStream = new PrintWriter(settingsXml.toFile());
		outStream.print(rowFilter.getSettings());
		outStream.close();
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.NodeInterface#getKnimeWorkflowConfig(edu.phema.jaxb.knime.ObjectFactory)
	 */
	@Override
	public Config getKnimeWorkflowConfig(ObjectFactory elementFactory) {
		// TODO Auto-generated method stub
		Config nodeRootConfig = elementFactory.createConfig();
		nodeRootConfig.setKey(this.getNodeKey());
		nodeRootConfig.getEntryOrConfig().add(
				Toolkit.makeEntry("id", EntryType.XINT, String.valueOf(id), elementFactory));
		nodeRootConfig.getEntryOrConfig().add(
				Toolkit.makeEntry("node_settings_file", EntryType.XSTRING, 
				getFolderName() + "/settings.xml", elementFactory));
		nodeRootConfig.getEntryOrConfig().add(
				Toolkit.makeEntry("node_is_meta", EntryType.XBOOLEAN, "false", elementFactory));
		nodeRootConfig.getEntryOrConfig().add(
				Toolkit.makeEntry("node_type", EntryType.XSTRING, "NativeNode", elementFactory));
		nodeRootConfig.getEntryOrConfig().add(
				Toolkit.makeEntry("ui_classname", 
						EntryType.XSTRING, "org.knime.core.node.workflow.NodeUIInformation", elementFactory));
		
		nodeRootConfig.getEntryOrConfig().add(
				Toolkit.nodeUIsettings(getX(), getY(), getWidth(), getHeight(), elementFactory));
		
		
		return nodeRootConfig;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.NodeInterface#getId()
	 */
	@Override
	public int getId() {
		// TODO Auto-generated method stub
		return id;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.NodeInterface#getNodeKey()
	 * I don't understand why I need this...
	 */
	@Override
	public String getNodeKey() {
		// TODO Auto-generated method stub
		return "node_" + id;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.NodeInterface#getNumberOfInPorts()
	 */
	@Override
	public int getNumberOfInPorts() {
		// TODO Auto-generated method stub
		return 1;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.NodeInterface#getNumberOfOutPorts()
	 */
	@Override
	public int getNumberOfOutPorts() throws SetUpIncompleteException {
		// TODO Auto-generated method stub
		return 1;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.NodeInterface#getGoodOutPorts()
	 */
	@Override
	public int[] getGoodOutPorts() {
		// TODO Auto-generated method stub
		return new int[] {0};
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.NodeInterface#getOutputElementId(int)
	 */
	@Override
	public int getOutputElementId(int port) throws IndexOutOfBoundsException {
		// TODO Auto-generated method stub
		int ret = Integer.MIN_VALUE;
		if (port == 0)
			ret = resourceElementId;
		else throw new IndexOutOfBoundsException();
		return ret;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.NodeInterface#getX()
	 */
	@Override
	public int getX() {
		// TODO Auto-generated method stub
		return nodeLocation.x;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.NodeInterface#getY()
	 */
	@Override
	public int getY() {
		// TODO Auto-generated method stub
		return nodeLocation.y;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.NodeInterface#setWidth(int)
	 */
	@Override
	public void setWidth(int w) {
		// TODO Auto-generated method stub
		nodeWidth = w;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.NodeInterface#setHeight(int)
	 */
	@Override
	public void setHeight(int h) {
		// TODO Auto-generated method stub
		nodeHeight = h;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.NodeInterface#getWidth()
	 */
	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return nodeWidth;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.NodeInterface#getHeight()
	 */
	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return nodeHeight;
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.NodeInterface#getNodeName()
	 */
	@Override
	public String getNodeName() {
		// TODO Auto-generated method stub
		return "Row Filter";
	}

	/* (non-Javadoc)
	 * @see edu.phema.QdmKnimeInterfaces.NodeInterface#getFolderName()
	 */
	@Override
	public String getFolderName() {
		// TODO Auto-generated method stub
		String sn = this.getId() >= 0 ? String.valueOf(this.getId()) : "unknown";
		return this.getNodeName() + " (#" + sn + ")";
	}

	@Override
	public void setInputElementId(int port, int elementId)
			throws IndexOutOfBoundsException {
		// TODO Auto-generated method stub
		if (port == 0)
			resourceElementId = elementId;
		else
			throw new IndexOutOfBoundsException();
			
	}

}
