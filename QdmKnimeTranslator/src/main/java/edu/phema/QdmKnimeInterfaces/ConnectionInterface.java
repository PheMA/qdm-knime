package edu.phema.QdmKnimeInterfaces;

import java.awt.Point;

import edu.phema.jaxb.knime.Config;
import edu.phema.jaxb.knime.ObjectFactory;

public interface ConnectionInterface {
	void setId (int id);
	void setSource (NodeInterface source, int port);
	void setDest (NodeInterface dest, int port);
	NodeInterface getSource();
	NodeInterface getDest();

	void addBendpoint (int x, int y);   // from source to destiny 
	Point[] getBendpoins();
	
	Config getKnimeWorkflowConfig(ObjectFactory elementFactory);
}
