/**
 * 
 */
package edu.phema.Enum.QdmKnime;

/**
 * @author moh
 *
 */
public enum CreateTableColumnClassEnum {
	Int("org.knime.core.data.def.IntCell"),
	Double("org.knime.core.data.def.DoubleCell"),
	String("org.knime.core.data.def.StringCell");
	
	public final String CELL_CLASS;
	CreateTableColumnClassEnum(String cellClass){
		CELL_CLASS = cellClass;
	}
	
}
