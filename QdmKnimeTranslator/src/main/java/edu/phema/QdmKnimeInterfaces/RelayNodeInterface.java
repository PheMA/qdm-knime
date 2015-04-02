/**
 * 
 */
package edu.phema.QdmKnimeInterfaces;

/**
 * @author admin
 *
 */
public interface RelayNodeInterface extends NodeInterface {
	public static enum DataType {
		Data("DataRelay", "DataRelay.zip"), 
		PMML("PMMLRelay", "PMMLRelay.zip"), 
		Database("DatabaseRelay", "DatabaseRelay.zip"),
		FlowVariable("FlowVariableRelay", "FlowVariableRelay.zip");
		
		public final String FOLDER_NAME;
		public final String ZIP_FILE_NAME;
		DataType(String folderName, String zipName){
			this.FOLDER_NAME = folderName;
			this.ZIP_FILE_NAME = zipName;
		}		
	}
	
	void setComment(String text);
	void setDataType(DataType type);
	NodeInterface getFrom();
	NodeInterface getTo();
	void setFrom(NodeInterface from);
	void setTo(NodeInterface to);
	DataType getDataType();
}
