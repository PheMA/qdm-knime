package edu.phema.knime.nodeSettings;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
//import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;

import edu.phema.Enum.QdmKnime.CreateTableColumnClassEnum;
import edu.phema.QdmKnime.Toolkit;
import edu.phema.jaxb.knime.Config;
import edu.phema.jaxb.knime.Entry;
import edu.phema.jaxb.knime.EntryType;
import edu.phema.jaxb.knime.ObjectFactory;

public class TableCreator {

	private final String templateDir = "src/main/resources/nodeXml/tableCreatorTemplate.xml";
	
	private final JAXBElement<Config> xmlRootNode;
	
	private final Config rowIndices;
	
	private final Config columnIndices;
	
	private final Config values;
	
	private final Config model;
	
	private final Entry nodeAnnotationText;
	
	private final ObjectFactory objectFactory = new ObjectFactory();

	private final Config columnProperties;
	
	private final HashMap<String, Integer> map = new HashMap<String, Integer>();
	
	private int maxColumn = -1;
	
	private final HashMap<Config, Entry> findColumnName = new HashMap<Config, Entry>();
	
	private final HashMap<Config, Entry> findCellClass = new HashMap<Config, Entry>();
	
	public TableCreator() throws JAXBException, IOException {
		// TODO Auto-generated constructor stub
		
		JAXBContext jaxbContext = JAXBContext.newInstance(
				Config.class);
		
		xmlRootNode = jaxbContext.createUnmarshaller()
				.unmarshal(
				new StreamSource(new StringReader(Toolkit.readFile(templateDir, Charset.defaultCharset()))), 
				Config.class);
		
		Config model_ = null;
		
		Config rowIndices_ = null;
		
		Config columnIndices_ = null;
		
		Config values_ = null;

		Config columnProperties_ = null;
		
		Config nodeAnnotation_ = null;
		
		Entry nodeAnnotationText_ = null;
		
		for (Object xnode : xmlRootNode.getValue().getEntryOrConfig()){
			if (xnode instanceof Config){
				Config configNode = (Config) xnode;
				if (configNode.getKey().equals("model")){
					model_ = configNode;
				} else if (configNode.getKey().equals("nodeAnnotation")){
					nodeAnnotation_ = configNode;
				}
			} 
		}
		if (model_ == null){
			throw new IOException("Cannot find model node!");
		} else {
			for (Object xnode : model_.getEntryOrConfig()){
				if (xnode instanceof Config){
					Config configNode = (Config) xnode;
					if (configNode.getKey().equals("rowIndices")){
						rowIndices_ = configNode;
					} else if (configNode.getKey().equals("columnIndices")){
						columnIndices_ = configNode;
					} else if (configNode.getKey().equals("values")){
						values_ = configNode;
					} else if (configNode.getKey().equals("columnProperties")){
						columnProperties_ = configNode;
					}
				} 
			}
		}
		if (nodeAnnotation_ != null){
			for (Object xnode : nodeAnnotation_.getEntryOrConfig()){
				if (xnode instanceof Entry){
					Entry entryNode = (Entry) xnode;
					if (entryNode.getKey().equals("text")){
						nodeAnnotationText_ = entryNode;
					} 
				} 
			}
		}
		
		nodeAnnotationText = nodeAnnotationText_;
		
		if (rowIndices_ == null){
			throw new IOException("Cannot find rowIndices node!");
		} else if (columnIndices_ == null){
			throw new IOException("Cannot find columnIndices node!");
		} else if (values_ == null){
			throw new IOException("Cannot find values node!");
		} else if (columnProperties_ == null){
			throw new IOException("Cannot find columnProperties node!");
		}
		
		model = model_;
		columnIndices = columnIndices_;
		rowIndices = rowIndices_;
		values = values_;
		columnProperties = columnProperties_;
		
	}

	public void setCell(String value, int rowIndex, int columnIndex){
		String map_key = String.valueOf(rowIndex) + "+" + String.valueOf(columnIndex);
		
		if (columnIndex > maxColumn){
			this.initializeColumnTo(columnIndex);
			//maxColumn = columnIndex;
		}
		
		Entry valueEntry = objectFactory.createEntry();
		valueEntry.setType(EntryType.XSTRING);
		valueEntry.setValue(value);
		int key = 1;
		
		if (map.containsKey(map_key)){
			key = map.get(map_key).intValue();  
			valueEntry.setKey(String.valueOf(key - 1)); // - 1 is for the array_size
			values.getEntryOrConfig().set(key, valueEntry); 
		} else {
			
			values.getEntryOrConfig().add(valueEntry);
			key = values.getEntryOrConfig().indexOf(valueEntry);
			valueEntry.setKey(String.valueOf(key - 1));
			Entry valuesArraySize = (Entry) values.getEntryOrConfig().get(0);
			valuesArraySize.setValue(String.valueOf(
					Integer.valueOf(valuesArraySize.getValue()) + 1
					));
			
			map.put(map_key, new Integer(key));
			
			Entry rowEntry = objectFactory.createEntry();
			rowEntry.setType(EntryType.XINT);
			rowEntry.setKey(String.valueOf(key - 1));
			rowEntry.setValue(String.valueOf(rowIndex));
			rowIndices.getEntryOrConfig().add(rowEntry);
			Entry rowsArraySize = (Entry) rowIndices.getEntryOrConfig().get(0);
			rowsArraySize.setValue(String.valueOf(
					Integer.valueOf(rowsArraySize.getValue()) + 1
					));

			
			Entry colEntry = objectFactory.createEntry();
			colEntry.setType(EntryType.XINT);
			colEntry.setKey(String.valueOf(key - 1));
			colEntry.setValue(String.valueOf(columnIndex));
			columnIndices.getEntryOrConfig().add(colEntry);
			Entry columnsArraySize = (Entry) columnIndices.getEntryOrConfig().get(0);
			columnsArraySize.setValue(String.valueOf(
					Integer.valueOf(columnsArraySize.getValue()) + 1
					));

		}
	}
	
	public void setNodeAnnotationText(String text){
		nodeAnnotationText.setValue(text);
	}
	
	public void setColumnProperties(int key, 
			String columnName, 
			CreateTableColumnClassEnum columnClass){
		if (key > maxColumn){
			initializeColumnTo(key);
		}
		Config columnConfig = (Config) columnProperties.getEntryOrConfig().get(key);
		findColumnName.get(columnConfig).setValue(columnName);
		findCellClass.get(columnConfig).setValue(columnClass.CELL_CLASS);
	}
	
	private void initializeColumnTo(int max){
		for (int i = maxColumn + 1; i <= max; i++){
			Config columnConfig = buildColumn(i, "column" + String.valueOf(i + 1), 
					CreateTableColumnClassEnum.String);
			columnProperties.getEntryOrConfig().add(columnConfig);
		}
		maxColumn = max;
	}
	
	private Config buildColumn(int key, String columnName, CreateTableColumnClassEnum columnClass){
		Config columnConfig = objectFactory.createConfig();
		columnConfig.setKey(String.valueOf(key));
		columnConfig.getEntryOrConfig().add(Toolkit.makeEntry("UserSetValues", 
				EntryType.XBOOLEAN, "true", objectFactory));
		columnConfig.getEntryOrConfig().add(Toolkit.makeEntry("MissValuePattern", 
				EntryType.XSTRING, "", objectFactory));
		columnConfig.getEntryOrConfig().add(Toolkit.makeEntry("ReadPossValsFromFile", 
				EntryType.XBOOLEAN, "false", objectFactory));
		columnConfig.getEntryOrConfig().add(Toolkit.makeEntry("SkipThisColumn", 
				EntryType.XBOOLEAN, "false", objectFactory));
		
		Entry columnNameEntry = Toolkit.makeEntry("ColumnName", 
				EntryType.XSTRING, columnName, objectFactory);
		columnConfig.getEntryOrConfig().add(columnNameEntry);
		findColumnName.put(columnConfig, columnNameEntry);
		
		Config columnClassConfig = objectFactory.createConfig();
		columnClassConfig.setKey("ColumnClass");
		columnConfig.getEntryOrConfig().add(columnClassConfig);
		Entry cell_classEntry = Toolkit.makeEntry("cell_class", EntryType.XSTRING, 
				columnClass.CELL_CLASS, objectFactory);
		findCellClass.put(columnConfig, cell_classEntry);
		columnClassConfig.getEntryOrConfig().add(cell_classEntry);
		columnClassConfig.getEntryOrConfig().add(Toolkit.makeEntry("is_null", 
				EntryType.XBOOLEAN, "false", objectFactory));
		
		return columnConfig;
	}

	public String getSettings(){
		StringWriter writer = new StringWriter();
		try {
			JAXBContext contextA = JAXBContext.newInstance(ObjectFactory.class);
			Marshaller m = contextA.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(this.xmlRootNode, writer);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		String result = writer.toString();
		assert result != null;
		return result;
	}
}
