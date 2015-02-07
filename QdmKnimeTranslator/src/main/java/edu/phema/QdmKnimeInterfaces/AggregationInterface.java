/**
 * 
 */
package edu.phema.QdmKnimeInterfaces;

/**
 * @author Huan Mo
 *
 */
public interface AggregationInterface extends NodeInterface {
	public void setInputElement(NodeInterface node);  // super.setInputElementId(0, int elementId)
	public NodeInterface getOutputElement(); // super.getOutputElementId(0);
	public void setNodeText(String text);
	public void setGroupByNodeText(String text);
	public void setFilterNodeText(String text);
}
