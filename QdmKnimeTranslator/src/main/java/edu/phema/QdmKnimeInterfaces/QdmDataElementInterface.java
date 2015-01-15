/**
 * 
 */
package edu.phema.QdmKnimeInterfaces;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import edu.phema.Enum.QdmKnime.CodeSystemEnum;
import edu.phema.jaxb.ihe.svs.CD;
import edu.phema.jaxb.ihe.svs.ConceptListType;
import edu.phema.jaxb.ihe.svs.RetrieveValueSetResponseType;


/**
 * @author Huan Mo (huan.mo@vanderbilt.edu)
 *
 */
public interface QdmDataElementInterface extends NodeInterface {
	
	void setQdmDataElementText(String text); // a descriptive text from QDM
	void setQdmDataType(String qdmDataType);
	String getQdmDataType();
	
	
	void setValueSet(String vsacXml) throws JAXBException;
	void setValueSet(JAXBElement<RetrieveValueSetResponseType> vsacXmlJaxb);
	void addValues(ConceptListType conceptList);
	
	void addValue(CD code);
	
	void setValueSetOid(String oid);
	void setValueSetDisplayName(String displayName);
	void setValueSetVersion(String version);  // yyyymmdd
	void setValueSetVersion();  // Set it to today yyyymmdd
	
	String getValueSetXML();
	String getValueSetOid();
	String getValueSetDisplayName();
	
	int getNumberOfCodes();
	String[] getCodes();
	String[] getCodes(CodeSystemEnum codeSystem);
	String[] getCodeDisplayNames();
	String[] getCodeDisplayNames(CodeSystemEnum codeSystem);
	
	void setVariableForSQL(String name, String Variable);
	String[] getVariableNamesForSQL();
	void addQdmAttributes(
			String requiredColumn, String dataType, String explanation);
	
}
