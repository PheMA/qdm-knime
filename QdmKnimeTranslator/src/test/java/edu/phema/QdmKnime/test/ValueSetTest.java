/**
 * 
 */
package edu.phema.QdmKnime.test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.bind.JAXBException;

import edu.phema.Enum.QdmKnime.CodeSystemEnum;
import edu.phema.QdmKnime.QdmDataElement;
import edu.phema.QdmKnime.Toolkit;

/**
 * @author moh
 *
 */
public class ValueSetTest {

	/**
	 * 
	 */
	public ValueSetTest() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws JAXBException 
	 */
	public static void main(String[] args) throws IOException, JAXBException {
		// TODO Auto-generated method stub
		String testVSACXml = "src/test/resources/valueSet_infection.xml";
		String in = Toolkit.readFile(testVSACXml, Charset.defaultCharset());
		//System.out.println(in);
		QdmDataElement dataElement = new QdmDataElement(1);
		dataElement.setValueSet(in);
		System.out.println(dataElement.getValueSetXML());
		
		System.out.println(CodeSystemEnum.valueOf("abc"));
		
	}

}
