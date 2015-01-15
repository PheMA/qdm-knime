/**
 * 
 */
package edu.phema.knime.nodeSettings;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

/**
 * @author moh
 *
 */
public class RowFilter {
	
	private final String templateDir = "src/main/resources/nodeXml/rowFilterTemplate.xml";
	
	private final JAXBElement<Config> xmlRootNode;
	
	private final Config model;
	
	private final Config rowFilterNode;
	
	private final Config nodeAnnotationConfig;
	
	private final ObjectFactory objectFactory = new ObjectFactory();



	/**
	 * @throws JAXBException 
	 * @throws IOException 
	 * 
	 */
	public RowFilter() throws JAXBException, IOException {
		// TODO Auto-generated constructor stub
		JAXBContext jaxbContext = JAXBContext.newInstance(
				Config.class);
		
		xmlRootNode = jaxbContext.createUnmarshaller()
				.unmarshal(
				new StreamSource(new StringReader(Toolkit.readFile(templateDir, Charset.defaultCharset()))), 
				Config.class);
		
		HashMap <String, Config> configMap = Toolkit
				.indexConfigsInConfig(xmlRootNode.getValue());
		
		model = configMap.get("model");
		
		rowFilterNode = objectFactory.createConfig();
		
		rowFilterNode.setKey("rowFilter");
		
		model.getEntryOrConfig().add(rowFilterNode);
		rowFilterNode.getEntryOrConfig().add(
				Toolkit.makeEntry("include", EntryType.XBOOLEAN, 
				"true", objectFactory));
		rowFilterNode.getEntryOrConfig().add(
				Toolkit.makeEntry("deepFiltering", EntryType.XBOOLEAN, 
						"false", objectFactory));  // no idea what it is for
		
		int modelPos = xmlRootNode.getValue().getEntryOrConfig().indexOf(model);
		
		nodeAnnotationConfig = objectFactory.createConfig();
		nodeAnnotationConfig.setKey("nodeAnnotation");
		
		xmlRootNode.getValue().getEntryOrConfig().add(modelPos + 1, nodeAnnotationConfig);
		
		/*
		 * <config key="nodeAnnotation">
		 * <entry key="text" type="xstring" value="Test Node 5"/>
		 * <entry key="bgcolor" type="xint" value="16777215"/>
		 * <entry key="x-coordinate" type="xint" value="531"/>
		 * <entry key="y-coordinate" type="xint" value="430"/>
		 * <entry key="width" type="xint" value="144"/>
		 * <entry key="height" type="xint" value="17"/>
		 * <entry key="alignment" type="xstring" value="CENTER"/>
		 * <config key="styles"/>
		 * </config>
		 * */
		
		Entry nodeAnnotationText = Toolkit.makeEntry("text", 
				EntryType.XSTRING, "Row Filter Node", objectFactory);
		
		nodeAnnotationConfig.getEntryOrConfig().add(nodeAnnotationText);
		
		nodeAnnotationConfig.getEntryOrConfig().add(
				Toolkit.makeEntry("bgcolor", 
						EntryType.XINT, "16777215", objectFactory));
		
		nodeAnnotationConfig.getEntryOrConfig().add(
				Toolkit.makeEntry("x-coordinate", EntryType.XINT, 
						"531", objectFactory));
		
		nodeAnnotationConfig.getEntryOrConfig().add(
				Toolkit.makeEntry("y-coordinate", EntryType.XINT, 
						"430", objectFactory));
		
		nodeAnnotationConfig.getEntryOrConfig().add(
				Toolkit.makeEntry("width", EntryType.XINT, 
						"140", objectFactory));
		
		nodeAnnotationConfig.getEntryOrConfig().add(
				Toolkit.makeEntry("height", EntryType.XINT, 
						"17", objectFactory));
		
		nodeAnnotationConfig.getEntryOrConfig().add(
				Toolkit.makeEntry("alignment", EntryType.XSTRING, 
						"CENTER", objectFactory));
		
		Config styleNode = objectFactory.createConfig();
		styleNode.setKey("styles");
		nodeAnnotationConfig.getEntryOrConfig().add(styleNode);

	
	}
	
	
	public void setMissingValuesMatch(){
		HashMap <String, Entry> entriesMap = Toolkit.indexEntriesInConfig(rowFilterNode);
		HashMap <String, Config> configsMap = Toolkit.indexConfigsInConfig(rowFilterNode);
		
		/*
		 * First, set up row filter method
		 * */
		if (entriesMap.containsKey("RowFilter_TypeID")){
			entriesMap.get("RowFilter_TypeID").setValue("MissingVal_RowFilter");
		} else {
			rowFilterNode.getEntryOrConfig().add(
					Toolkit.makeEntry("RowFilter_TypeID", EntryType.XSTRING, 
							"MissingVal_RowFilter", objectFactory));
		}
		
		/*
		 * Second, remove all attributes that belong to text matching
		 * and also remove all old settings of upper and lower bounds
		 * */
		if (entriesMap.containsKey("CaseSensitive")){
			rowFilterNode.getEntryOrConfig().remove(entriesMap.get("CaseSensitive"));
		}
		if (entriesMap.containsKey("Pattern")){
			rowFilterNode.getEntryOrConfig().remove(entriesMap.get("Pattern"));
		}
		if (entriesMap.containsKey("hasWildCards")){
			rowFilterNode.getEntryOrConfig().remove(entriesMap.get("hasWildCards"));
		}
		if (entriesMap.containsKey("isRegExpr")){
			rowFilterNode.getEntryOrConfig().remove(entriesMap.get("isRegExpr"));
		}
		if (configsMap.containsKey("lowerBound")){
			rowFilterNode.getEntryOrConfig().remove(configsMap.get("lowerBound"));
		}
		if (configsMap.containsKey("upperBound")){
			rowFilterNode.getEntryOrConfig().remove(configsMap.get("upperBound"));
		}

	}
	
	public void setStringMatching(String pattern, boolean caseSensitive, 
			boolean hasWildCards, boolean isRegExpr){
		HashMap <String, Entry> entriesMap = Toolkit.indexEntriesInConfig(rowFilterNode);
		HashMap <String, Config> configsMap = Toolkit.indexConfigsInConfig(rowFilterNode);
		if (entriesMap.containsKey("RowFilter_TypeID")){
			entriesMap.get("RowFilter_TypeID").setValue("StringComp_RowFilter");
		} else {
			rowFilterNode.getEntryOrConfig().add(
					Toolkit.makeEntry("RowFilter_TypeID", EntryType.XSTRING, 
							"StringComp_RowFilter", objectFactory));
		}
		if (configsMap.containsKey("lowerBound")){
			rowFilterNode.getEntryOrConfig().remove(configsMap.get("lowerBound"));
		}
		if (configsMap.containsKey("upperBound")){
			rowFilterNode.getEntryOrConfig().remove(configsMap.get("upperBound"));
		}
		
		
		if (entriesMap.containsKey("Pattern")){
			entriesMap.get("Pattern").setValue(pattern);
		} else {
			rowFilterNode.getEntryOrConfig().add(
					Toolkit.makeEntry("Pattern", EntryType.XSTRING, 
							pattern, objectFactory));
		}		
		if (entriesMap.containsKey("CaseSensitive")){
			entriesMap.get("CaseSensitive").setValue(String.valueOf(caseSensitive));
		} else {
			rowFilterNode.getEntryOrConfig().add(
					Toolkit.makeEntry("CaseSensitive", EntryType.XBOOLEAN, 
							String.valueOf(caseSensitive), objectFactory));
		}
		if (entriesMap.containsKey("hasWildCards")){
			entriesMap.get("hasWildCards").setValue(String.valueOf(
					isRegExpr ? false : hasWildCards));
		} else {
			rowFilterNode.getEntryOrConfig().add(
					Toolkit.makeEntry("hasWildCards", EntryType.XBOOLEAN, 
							String.valueOf(isRegExpr ? 
									false : hasWildCards), objectFactory));
		}
		if (entriesMap.containsKey("isRegExpr")){
			entriesMap.get("isRegExpr").setValue(String.valueOf(isRegExpr));
		} else {
			rowFilterNode.getEntryOrConfig().add(
					Toolkit.makeEntry("isRegExpr", EntryType.XBOOLEAN, 
							String.valueOf(isRegExpr), objectFactory));
		}



	}
	
	/*
	 * upper/lower bounds can be null
	 * */
	public void setRangeValues(CreateTableColumnClassEnum type, Number upperBound, Number lowerBound){
		HashMap <String, Entry> entriesMap = Toolkit.indexEntriesInConfig(rowFilterNode);
		HashMap <String, Config> configsMap = Toolkit.indexConfigsInConfig(rowFilterNode);
		
		/*
		 * First, set up row filter method
		 * */
		if (entriesMap.containsKey("RowFilter_TypeID")){
			entriesMap.get("RowFilter_TypeID").setValue("RangeVal_RowFilter");
		} else {
			rowFilterNode.getEntryOrConfig().add(
					Toolkit.makeEntry("RowFilter_TypeID", EntryType.XSTRING, 
							"RangeVal_RowFilter", objectFactory));
		}
		
		/*
		 * Second, remove all attributes that belong to text matching
		 * and also remove all old settings of upper and lower bounds
		 * */
		if (entriesMap.containsKey("CaseSensitive")){
			rowFilterNode.getEntryOrConfig().remove(entriesMap.get("CaseSensitive"));
		}
		if (entriesMap.containsKey("Pattern")){
			rowFilterNode.getEntryOrConfig().remove(entriesMap.get("Pattern"));
		}
		if (entriesMap.containsKey("hasWildCards")){
			rowFilterNode.getEntryOrConfig().remove(entriesMap.get("hasWildCards"));
		}
		if (entriesMap.containsKey("isRegExpr")){
			rowFilterNode.getEntryOrConfig().remove(entriesMap.get("isRegExpr"));
		}
		if (configsMap.containsKey("lowerBound")){
			rowFilterNode.getEntryOrConfig().remove(configsMap.get("lowerBound"));
		}
		if (configsMap.containsKey("upperBound")){
			rowFilterNode.getEntryOrConfig().remove(configsMap.get("upperBound"));
		}
		
		/*
		 * Third, add lower and(or) upper bound(s)
		 * */
		Config upperBoundConfig = objectFactory.createConfig();
		Config lowerBoundConfig = objectFactory.createConfig();
		upperBoundConfig.setKey("upperBound");
		lowerBoundConfig.setKey("lowerBound");
		rowFilterNode.getEntryOrConfig().add(upperBoundConfig);
		rowFilterNode.getEntryOrConfig().add(lowerBoundConfig);
		
		Entry upperBoundDatacell = Toolkit.makeEntry("datacell", EntryType.XSTRING, 
				"", objectFactory);
		Entry lowerBoundDatacell = Toolkit.makeEntry("datacell", EntryType.XSTRING, 
				"", objectFactory);
		upperBoundConfig.getEntryOrConfig().add(upperBoundDatacell);
		lowerBoundConfig.getEntryOrConfig().add(lowerBoundDatacell);
		if (upperBound == null | type == null 
				| type == CreateTableColumnClassEnum.String){
			upperBoundDatacell.setIsnull(true);
		} else {
			upperBoundDatacell.setValue(type.CELL_CLASS);
			Config dataCellConfig = objectFactory.createConfig();
			dataCellConfig.setKey(type.CELL_CLASS);
			upperBoundConfig.getEntryOrConfig().add(dataCellConfig);
			if (type == CreateTableColumnClassEnum.Double){
				dataCellConfig.getEntryOrConfig().add(
						Toolkit.makeEntry("DoubleCell", EntryType.XDOUBLE, 
								String.valueOf(upperBound), objectFactory));
			} else if (type == CreateTableColumnClassEnum.Int){
				dataCellConfig.getEntryOrConfig().add(
						Toolkit.makeEntry("IntCell", EntryType.XINT, 
								String.valueOf(upperBound), objectFactory));
			}
		}
		if (lowerBound == null | type == null 
				| type == CreateTableColumnClassEnum.String){
			lowerBoundDatacell.setIsnull(true);
		} else {
			lowerBoundDatacell.setValue(type.CELL_CLASS);
			Config dataCellConfig = objectFactory.createConfig();
			dataCellConfig.setKey(type.CELL_CLASS);
			lowerBoundConfig.getEntryOrConfig().add(dataCellConfig);
			if (type == CreateTableColumnClassEnum.Double){
				dataCellConfig.getEntryOrConfig().add(
						Toolkit.makeEntry("DoubleCell", EntryType.XDOUBLE, 
								String.valueOf(lowerBound), objectFactory));
			} else if (type == CreateTableColumnClassEnum.Int){
				dataCellConfig.getEntryOrConfig().add(
						Toolkit.makeEntry("IntCell", EntryType.XINT, 
								String.valueOf(lowerBound), objectFactory));
			}
		}
		
		
	}
	
	public void setIncludeOrExclude (boolean isInclude){
		HashMap <String, Entry> entriesMap = Toolkit.indexEntriesInConfig(rowFilterNode);
		if (entriesMap.containsKey("include")){  
			entriesMap.get("include").setValue(isInclude ? "true" : "false");
		} else { // useless (initialized in constructor)
			rowFilterNode.getEntryOrConfig().add(
					Toolkit.makeEntry("include", EntryType.XBOOLEAN, 
					isInclude ? "true" : "false", objectFactory));
		}
	}

	public void setColumnName (String columnName){
		HashMap <String, Entry> entriesMap = Toolkit.indexEntriesInConfig(rowFilterNode);
		if (entriesMap.containsKey("columnName")){
			entriesMap.get("columnName").setValue(columnName);
		} else {
			rowFilterNode.getEntryOrConfig().add(
					Toolkit.makeEntry("columnName", EntryType.XSTRING, 
					columnName, objectFactory));
		}
	}

	public String getColumnName (){
		String columnName = "";
		HashMap <String, Entry> entriesMap = Toolkit.indexEntriesInConfig(rowFilterNode);
		if (entriesMap.containsKey("columnName")){
			columnName = entriesMap.get("columnName").getValue();
		} 
		
		return columnName;
	}

	
	public void setAnnotationText (String text){
		HashMap <String, Entry> entriesMap = 
				Toolkit.indexEntriesInConfig(nodeAnnotationConfig);
		String formattedText = text.replaceAll("\\n", "%%00010");
		entriesMap.get("text").setValue(formattedText);
		Pattern lineFeed = Pattern.compile("%%00010");
		Matcher matcher = lineFeed.matcher(formattedText);
		
		int count = 1;
		while (matcher.find())
			count ++;
		
		entriesMap.get("height").setValue(String.valueOf(17 * count));
		
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

	public static void main (String[] args) throws JAXBException, IOException{
		RowFilter obj = new RowFilter();
		obj.setAnnotationText("Hello\nWorld");
		obj.setColumnName("result");
		obj.setIncludeOrExclude(true);
		obj.setRangeValues(CreateTableColumnClassEnum.Double, new Double (12.0), new Double (7.0));
		// obj.setRangeValues(CreateTableColumnClassEnum.Int, new Integer (12), null);
		// obj.setMissingValuesMatch();
		obj.setStringMatching("\\d+", false, true, true);
		System.out.println(obj.getSettings());
	}

}
