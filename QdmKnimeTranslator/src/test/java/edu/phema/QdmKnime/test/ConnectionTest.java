/**
 * 
 */
package edu.phema.QdmKnime.test;


import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
//import javax.xml.bind.util.JAXBSource;
//import javax.xml.namespace.QName;


import org.junit.Test;

import edu.phema.QdmKnime.Connection;
import edu.phema.jaxb.knime.ObjectFactory;

/**
 * @author Huan
 *
 */
public class ConnectionTest {

	@Test
	public void testConnection() throws JAXBException {
		// TODO Auto-generated method stub
		ObjectFactory elementFactory = new ObjectFactory();
		Connection conn = new Connection();
		conn.setId(2);
		conn.setSource(-1, 0);
		conn.setDest(61, 1);
		conn.addBendpoint(178, 95);
		conn.addBendpoint(578, 95);
		
		JAXBElement<?> jaxbConn = elementFactory.createConfig(conn.getKnimeWorkflowConfig(elementFactory));
			JAXBContext contextA = JAXBContext.newInstance(ObjectFactory.class);
			Marshaller m = contextA.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			//m.setProperty(Marshaller.JAXB_FRAGMENT, true);
			
			StringWriter writer = new StringWriter();
			m.marshal(jaxbConn, writer);
			String result = writer.toString();
			assert result != null;
			System.out.println(result);
			/*			
			 * JAXBElement<Config> jaxbElementA = new JAXBElement<Config>(
					new QName(""), 
					Config.class, jaxbConn);
			*/
			// JAXBSource sourceA = new JAXBSource(contextA, jaxbElementA);
					
	}

}
