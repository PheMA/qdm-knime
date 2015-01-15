/**
 * 
 */
package edu.phema.QdmKnime;

import java.awt.Point;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.lingala.zip4j.exception.ZipException;
import edu.phema.QdmKnimeInterfaces.NodeInterface;
import edu.phema.jaxb.knime.Config;
import edu.phema.jaxb.knime.EntryType;
import edu.phema.jaxb.knime.ObjectFactory;
import edu.phema.knime.exceptions.SetUpIncompleteException;
import edu.phema.knime.exceptions.WrittenAlreadyException;

/**
 * @author Huan
 *
 */
public abstract class MetaNode implements NodeInterface {

	/**
	 * 
	 */
	
	public final int UNKNOWN_ID = Integer.MIN_VALUE;
	
	private int id = UNKNOWN_ID;  // serial number of nodes
		
	private Path workflowRoot = Paths.get("");
	
	private final Point nodeLocation = new Point(10, 10);
	
	private int nodeWidth = 130;
	
	private int nodeHeight = 67;
	
	public MetaNode() {
		// TODO Auto-generated constructor stub
	}
	
	public MetaNode(int id){
		this.id = id;		
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.NodeInterface#setRoot(java.lang.String)
	 */
	@Override
	public synchronized void setWorkflowRoot(String dir) {
		// TODO Auto-generated method stub
		workflowRoot = Paths.get(dir);
	}
	
	public synchronized Path getWorkflowRoot() {
		return workflowRoot;
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.NodeInterface#setId(int)
	 */
	@Override
	public synchronized void setId(int id) {
		// TODO Auto-generated method stub
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.NodeInterface#setX(int)
	 */
	@Override
	public synchronized void setX(int x) {
		// TODO Auto-generated method stub
		nodeLocation.x = x;
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.NodeInterface#setY(int)
	 */
	@Override
	public synchronized void setY(int y) {
		// TODO Auto-generated method stub
		nodeLocation.y = y;
	}


	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.NodeInterface#write()
	 */
	@Override
	abstract public void write() throws WrittenAlreadyException,
			SetUpIncompleteException, IOException, ZipException;

	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.NodeInterface#getKnimeWorkflowConfig()
	 */
	@Override
	public synchronized Config getKnimeWorkflowConfig(ObjectFactory elementFactory) {
		// TODO Auto-generated method stub
		/*
		 * Example:
		 * <config key="node_1">
		 * <entry key="id" type="xint" value="1"/>
		 * <entry key="node_settings_file" type="xstring" value="StartBeforeS (#1)/workflow.knime"/>
		 * <entry key="node_is_meta" type="xboolean" value="true"/>
		 * <entry key="node_type" type="xstring" value="MetaNode"/>
		 * <entry key="ui_classname" type="xstring" value="org.knime.core.node.workflow.NodeUIInformation"/>
		 * <config key="ui_settings">
		 * <config key="extrainfo.node.bounds">
		 * <entry key="array-size" type="xint" value="4"/>
		 * <entry key="0" type="xint" value="391"/>
		 * <entry key="1" type="xint" value="324"/>
		 * <entry key="2" type="xint" value="114"/>
		 * <entry key="3" type="xint" value="66"/>
		 * </config>
		 * </config>
		 * </config>
		 */
								
		Config nodeRootConfig = elementFactory.createConfig();
		nodeRootConfig.setKey("node_" + id);
		nodeRootConfig.getEntryOrConfig().add(
				Toolkit.makeEntry("id", EntryType.XINT, String.valueOf(id), elementFactory));
		nodeRootConfig.getEntryOrConfig().add(
				Toolkit.makeEntry("node_settings_file", EntryType.XSTRING, 
				getFolderName() + "/workflow.knime", elementFactory));
		nodeRootConfig.getEntryOrConfig().add(
				Toolkit.makeEntry("node_is_meta", EntryType.XBOOLEAN, "true", elementFactory));
		nodeRootConfig.getEntryOrConfig().add(
				Toolkit.makeEntry("node_type", EntryType.XSTRING, "MetaNode", elementFactory));
		nodeRootConfig.getEntryOrConfig().add(
				Toolkit.makeEntry("ui_classname", 
						EntryType.XSTRING, "org.knime.core.node.workflow.NodeUIInformation", elementFactory));
		
		nodeRootConfig.getEntryOrConfig().add(
				Toolkit.nodeUIsettings(getX(), getY(), getWidth(), getHeight(), elementFactory));
		
		
		return nodeRootConfig;
	}


	
	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.NodeInterface#getId()
	 */
	@Override
	public synchronized int getId() {
		// TODO Auto-generated method stub
		return id;
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.NodeInterface#getNodeKey()
	 */
	@Override
	public synchronized String getNodeKey() {
		// TODO Auto-generated method stub
		return "node_" + id;
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.NodeInterface#getNumberOfInPorts()
	 */
	@Override
	abstract public int getNumberOfInPorts();

	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.NodeInterface#getNumberOfOutPorts()
	 */
	@Override
	abstract public int getNumberOfOutPorts() throws SetUpIncompleteException;

	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.NodeInterface#getX()
	 */
	@Override
	public synchronized int getX() {
		// TODO Auto-generated method stub
		return nodeLocation.x;
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.NodeInterface#getY()
	 */
	@Override
	public synchronized int getY() {
		// TODO Auto-generated method stub
		return nodeLocation.y;
	}
	

	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.NodeInterface#setWidth(int)
	 */
	public synchronized void setWidth(int width) {
		// TODO Auto-generated method stub
		nodeWidth = width;
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.NodeInterface#setHeight(int)
	 */
	public synchronized void setHeight(int height) {
		// TODO Auto-generated method stub
		nodeHeight = height;
	}
	
	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.NodeInterface#getWidth()
	 */
	@Override
	public synchronized int getWidth() {
		// TODO Auto-generated method stub
		return nodeWidth;
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.NodeInterface#getHeight()
	 */
	@Override
	public synchronized int getHeight() {
		// TODO Auto-generated method stub
		return nodeHeight;
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.NodeInterface#getNodeName()
	 */
	@Override
	abstract public String getNodeName(); 
	
	@Override
	abstract public String getFolderName();
	
	@Override
	abstract public int[] getGoodOutPorts();

}
