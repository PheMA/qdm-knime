/**
 * 
 */
package edu.phema.hl7v3;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;

import edu.phema.QdmKnime.Toolkit;
//import edu.phema.jaxb.uvqm.hl7v3.POQMMT000001UVQualityMeasureDocument;
import edu.phema.jaxb.queryHealth.hqmf.POQMMT000001UVQualityMeasureDocument;

/**
 * @author moh
 *
 */
public class EMeasureNotFinished {

	/**
	 * 
	 */
	
	private final JAXBElement<POQMMT000001UVQualityMeasureDocument> emeasureRoot;
	
	public EMeasureNotFinished(String emeasureXml) throws JAXBException {
		// TODO Auto-generated constructor stub
		emeasureRoot = JAXBContext.newInstance(
				POQMMT000001UVQualityMeasureDocument.class)
				.createUnmarshaller()
				.unmarshal(new StreamSource(new StringReader(emeasureXml)), 
						POQMMT000001UVQualityMeasureDocument.class);
		
		/*
		 * Run xpath on jaxb: 
		 * http://www.eclipse.org/eclipselink/documentation/2.4/moxy/runtime008.htm
		 * */
		
		
		
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws JAXBException 
	 */
	public static void main(String[] args) throws JAXBException, IOException {
		// TODO Auto-generated method stub
		String fileDir = 
				"src/main/resources/hqmf_queryHealth/samples/HQMF_Measure60.xml";
		String fileDir2 = "src/main/resources/eMeasures/EH_CMS30v2_NQF0639_AMI10_StatinMed/CMS30v2.xml";
		EMeasureNotFinished em = new EMeasureNotFinished(Toolkit.readFile(fileDir));
		System.out.println("done");
	}

}
