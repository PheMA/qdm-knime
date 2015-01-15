/**
 * 
 */
package edu.phema.QdmKnime;

import java.awt.Point;
import java.util.ArrayList;

import edu.phema.QdmKnimeInterfaces.ConnectionInterface;
import edu.phema.jaxb.knime.Config;
import edu.phema.jaxb.knime.Entry;
import edu.phema.jaxb.knime.EntryType;
import edu.phema.jaxb.knime.ObjectFactory;

/**
 * @author Huan
 *
 */
public class Connection implements ConnectionInterface {

	/**
	 * 
	 */
	
	private ArrayList <Point> bendpoints = new ArrayList<Point>();
	private int sourceID;
	private int sourcePort;
	private int destID;
	private int destPort;
	private int id;
	
	public Connection() {
		// TODO Auto-generated constructor stub
	}

	public Connection(int id){
		this.id = id;
	}
		
	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.ConnectionInterface#setId(int)
	 */
	@Override
	public void setId(int id) {
		// TODO Auto-generated method stub
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.ConnectionInterface#setSource(int, int)
	 */
	@Override
	public void setSource(int sourceID, int port) {
		// TODO Auto-generated method stub
		this.sourceID = sourceID;
		this.sourcePort = port;
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.ConnectionInterface#setDest(int, int)
	 */
	@Override
	public void setDest(int destID, int port) {
		// TODO Auto-generated method stub
		this.destID = destID;
		this.destPort = port;
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.ConnectionInterface#addBendpoint(int, int)
	 */
	@Override
	public void addBendpoint(int x, int y) {
		// TODO Auto-generated method stub
		Point pt = new Point(x, y);
		bendpoints.add(pt);

	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.ConnectionInterface#getBendpoins()
	 */
	@Override
	public Point[] getBendpoins() {
		// TODO Auto-generated method stub
		return (Point[]) bendpoints.toArray();
	}

	/* (non-Javadoc)
	 * @see edu.vanderbilt.mc.phema.QdmKnimeInterfaces.ConnectionInterface#getKnimeWorkflowConfig()
	 */
	@Override
	public Config getKnimeWorkflowConfig(ObjectFactory elementFactory) {
		// TODO Auto-generated method stub
		Config connectionXml = elementFactory.createConfig();
		connectionXml.setKey("connnection_" + id);
		
		Entry entrySourceID = elementFactory.createEntry();
		entrySourceID.setKey("sourceID");
		entrySourceID.setType(EntryType.XINT);
		entrySourceID.setValue(String.valueOf(sourceID));		
		connectionXml.getEntryOrConfig().add(entrySourceID);
		
		Entry entryDestID = elementFactory.createEntry();
		entryDestID.setKey("destID");
		entryDestID.setType(EntryType.XINT);
		entryDestID.setValue(String.valueOf(destID));		
		connectionXml.getEntryOrConfig().add(entryDestID);

		Entry entrySourcePort = elementFactory.createEntry();
		entrySourcePort.setKey("sourcePort");
		entrySourcePort.setType(EntryType.XINT);
		entrySourcePort.setValue(String.valueOf(sourcePort));		
		connectionXml.getEntryOrConfig().add(entrySourcePort);
		
		Entry entryDestPort = elementFactory.createEntry();
		entryDestPort.setKey("destPort");
		entryDestPort.setType(EntryType.XINT);
		entryDestPort.setValue(String.valueOf(destPort));		
		connectionXml.getEntryOrConfig().add(entryDestPort);
		
		/* work on bendpoints */
		if (! bendpoints.isEmpty())	{
			Entry entryConnUIClass = elementFactory.createEntry();
			entryConnUIClass.setKey("ui_classname");
			entryConnUIClass.setType(EntryType.XSTRING);
			entryConnUIClass.setValue("org.knime.core.node.workflow.ConnectionUIInformation");
			connectionXml.getEntryOrConfig().add(entryConnUIClass);
		
			Config bendpointsXml = elementFactory.createConfig();
			bendpointsXml.setKey("ui_settings");
			Entry entryBendpointsSize = elementFactory.createEntry();
			entryBendpointsSize.setKey("extrainfo.conn.bendpoints_size");
			entryBendpointsSize.setType(EntryType.XINT);
			entryBendpointsSize.setValue(String.valueOf(bendpoints.size()));
			bendpointsXml.getEntryOrConfig().add(entryBendpointsSize);
		
			for (int i = 0; i < bendpoints.size(); i++){
				Point pt = bendpoints.get(i);

				Config onePoint = elementFactory.createConfig();
				onePoint.setKey("extrainfo.conn.bendpoints_" + i);

				Entry entryDim = elementFactory.createEntry();
				entryDim.setKey("array-size");
				entryDim.setType(EntryType.XINT);
				entryDim.setValue("2");
				onePoint.getEntryOrConfig().add(entryDim);

				Entry entryX = elementFactory.createEntry();
				entryX.setKey("0");
				entryX.setType(EntryType.XINT);
				entryX.setValue(String.valueOf(pt.x));
				onePoint.getEntryOrConfig().add(entryX);
				
				Entry entryY = elementFactory.createEntry();
				entryY.setKey("1");
				entryY.setType(EntryType.XINT);
				entryY.setValue(String.valueOf(pt.y));
				onePoint.getEntryOrConfig().add(entryY);
				
				bendpointsXml.getEntryOrConfig().add(onePoint);
			}
			
			connectionXml.getEntryOrConfig().add(bendpointsXml);
		
		}
		
		return connectionXml;
	}

}
