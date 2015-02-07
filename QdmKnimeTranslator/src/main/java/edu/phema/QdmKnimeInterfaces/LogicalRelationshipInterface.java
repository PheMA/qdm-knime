package edu.phema.QdmKnimeInterfaces;

import edu.phema.knime.exceptions.SetUpIncompleteException;

public interface LogicalRelationshipInterface extends NodeInterface {
	public static enum EntityLevel {patient, event};
	public static enum LogicalTypeCode {AND, OR, AND_NOT};
	void setLeftElement (NodeInterface node);
	void setRightElement (NodeInterface node);
	// void setLogicalTypeCode(LogicalTypeCode typeCode);  // Should be initialized in constructors
	LogicalTypeCode getLogicalTypeCode();
	// int getOutputElementId(int port) throws IndexOutOfBoundsException;  // null if it is patient level data?
	EntityLevel getOutputEntityLevel(int port) throws IndexOutOfBoundsException;
}
