package edu.phema.QdmKnime.test;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import edu.phema.Enum.QdmKnime.CreateTableColumnClassEnum;
import edu.phema.jaxb.knime.ObjectFactory;
import edu.phema.knime.nodeSettings.TableCreator;

public class TableCreatorTest {

	public static void main(String[] args) throws JAXBException, IOException {
		// TODO Auto-generated method stub
		TableCreator tb = new TableCreator();
		tb.setCell("Hello", 3, 5);
		tb.setColumnProperties(5, "name", CreateTableColumnClassEnum.String);
		tb.setColumnProperties(6, "someInt", CreateTableColumnClassEnum.Int);
		tb.setCell("world", 3, 5);
		tb.setNodeAnnotationText("Wonderful People");
		System.out.println(tb.getSettings());
		
	}

}
