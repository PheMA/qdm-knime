/**
 * 
 */
package edu.phema.QdmKnimeInterfaces;

/**
 * @author Huan Mo
 *
 */
public interface AggregationInterface extends NodeInterface {
	public void setInputElementId(int elementId);  // super.setInputElementId(0, int elementId)
	public int getOutputElementId(); // super.getOutputElementId(0);
	public void setNodeText(String text);
	public void setGroupByNodeText(String text);
	public void setFilterNodeText(String text);
}
