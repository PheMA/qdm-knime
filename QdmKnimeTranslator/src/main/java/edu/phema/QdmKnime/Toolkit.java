/**
 * 
 */
package edu.phema.QdmKnime;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.xml.rpc.ServiceException;

import edu.phema.jaxb.knime.Config;
import edu.phema.jaxb.knime.Entry;
import edu.phema.jaxb.knime.EntryType;
import edu.phema.jaxb.knime.ObjectFactory;
import gov.nih.nlm.mor.axis.services.RxNormDBService.DBManager;
import gov.nih.nlm.mor.axis.services.RxNormDBService.DBManagerService;
import gov.nih.nlm.mor.axis.services.RxNormDBService.DBManagerServiceLocator;

/**
 * @author Huan
 *
 */
public class Toolkit {

	public static Entry makeEntry(String key, EntryType entryType, String value, ObjectFactory elementFactory){
		Entry anEntry = elementFactory.createEntry();
		anEntry.setKey(key);
		anEntry.setType(entryType);
		anEntry.setValue(value);
		return anEntry;
	}
	
	public static Config nodeUIsettings(int x, int y, int width, int height, ObjectFactory elementFactory){
		/*
		 * Example:
		 * <config key="ui_settings">
		 * <config key="extrainfo.node.bounds">
		 * <entry key="array-size" type="xint" value="4"/>
		 * <entry key="0" type="xint" value="391"/>
		 * <entry key="1" type="xint" value="324"/>
		 * <entry key="2" type="xint" value="114"/>
		 * <entry key="3" type="xint" value="66"/>
		 * </config>
		 * </config>
		 */
		Config ui_settings = elementFactory.createConfig();
		ui_settings.setKey("ui_settings");

		Config extrainfo = elementFactory.createConfig();
		extrainfo.setKey("extrainfo.node.bounds");
		extrainfo.getEntryOrConfig().add(makeEntry("array-size", EntryType.XINT, "4", elementFactory));
		extrainfo.getEntryOrConfig().add(makeEntry("0", EntryType.XINT, String.valueOf(x), elementFactory));
		extrainfo.getEntryOrConfig().add(makeEntry("1", EntryType.XINT, String.valueOf(y), elementFactory));
		extrainfo.getEntryOrConfig().add(makeEntry("2", EntryType.XINT, String.valueOf(width), elementFactory));
		extrainfo.getEntryOrConfig().add(makeEntry("3", EntryType.XINT, String.valueOf(height), elementFactory));
		
		ui_settings.getEntryOrConfig().add(extrainfo);
		
		return ui_settings;
	}
	
	public static HashMap <String, Config> indexConfigsInConfig (Config inConfig){
		HashMap <String, Config> ret = new HashMap <String, Config>();
		Object[] subNodes = inConfig.getEntryOrConfig().toArray();
		for (Object node : subNodes){
			if (node.getClass() == Config.class){
				Config configNode = (Config) node;
				ret.put(configNode.getKey(), configNode);
			}
		}
		return ret;
	}

	public static HashMap <String, Entry> indexEntriesInConfig (Config inConfig){
		HashMap <String, Entry> ret = new HashMap <String, Entry>();
		Object[] subNodes = inConfig.getEntryOrConfig().toArray();
		for (Object node : subNodes){
			if (node.getClass() == Entry.class){
				Entry entryNode = (Entry) node;
				ret.put(entryNode.getKey(), entryNode);
			}
		}
		return ret;
	}

	
	/*
	 * Copy from http://stackoverflow.com/questions/326390/how-to-create-a-java-string-from-the-contents-of-a-file
	 * */
	public static String readFile(String path, Charset encoding) throws IOException {
			  byte[] encoded = Files.readAllBytes(Paths.get(path));
			  return new String(encoded, encoding);
	}
	
	public static String readFile(String path) throws IOException {
		return readFile(path, Charset.defaultCharset());
	}
	
	public static DBManager getRxnormManager() {
		String rxhost = "http://mor.nlm.nih.gov";
		String rxURI = rxhost + "/axis/services/RxNormDBService";
		DBManager dbmanager = null;
		// Locate the RxNorm API web service
		try {
			URL rxURL = new URL(rxURI);
			DBManagerService rxnormService = new DBManagerServiceLocator();
			dbmanager = rxnormService.getRxNormDBService(rxURL);
		} catch (MalformedURLException | ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dbmanager;
	}
	
	public static void addWorkflowOverHeads(Config inConfig, ObjectFactory factory){
		/*
		 * <entry key="created_by" type="xstring" value="2.10.2.0044326"/>
		 * <entry key="version" type="xstring" value="2.10.0"/>
		 * <entry key="name" type="xstring" isnull="true" value=""/>
		 * <config key="authorInformation">
		 * <entry key="authored-by" type="xstring" value="Huan"/>
		 * <entry key="authored-when" type="xstring" value="2014-12-02 19:52:20 -0600"/>
		 * <entry key="lastEdited-by" type="xstring" value="Huan"/>
		 * <entry key="lastEdited-when" type="xstring" value="2014-12-04 17:38:49 -0600"/>
		 * </config>
		 * <entry key="customDescription" type="xstring" isnull="true" value=""/>
		 * <entry key="state" type="xstring" value="IDLE"/>
		 * <config key="workflow_credentials"/>
		 * */
		
		SimpleDateFormat formattor = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
		
		inConfig.getEntryOrConfig().add(makeEntry("created_by", EntryType.XSTRING, "2.10.2.0044326", factory));
		inConfig.getEntryOrConfig().add(makeEntry("version", EntryType.XSTRING, "2.10.0", factory));
		Entry nameEntry =  makeEntry("name", EntryType.XSTRING, "", factory);
		nameEntry.setIsnull(true);
		inConfig.getEntryOrConfig().add(nameEntry);
		Config authorInfoConfig = factory.createConfig();

		authorInfoConfig.setKey("authorInformation");
		inConfig.getEntryOrConfig().add(authorInfoConfig);
		authorInfoConfig.getEntryOrConfig().add(
				makeEntry("authored-by", EntryType.XSTRING, System.getProperty("user.name"), factory));
		authorInfoConfig.getEntryOrConfig().add(
				makeEntry("authored-when", EntryType.XSTRING, 
				formattor.format(new Date()), factory));
		authorInfoConfig.getEntryOrConfig().add(
				makeEntry("lastEdited-by", EntryType.XSTRING, System.getProperty("user.name"), factory));
		authorInfoConfig.getEntryOrConfig().add(
				makeEntry("lastEdited-when", EntryType.XSTRING, 
				formattor.format(new Date()), factory));

		Entry custDescEntry =  makeEntry("customDescription", EntryType.XSTRING, "", factory);
		custDescEntry.setIsnull(true);
		inConfig.getEntryOrConfig().add(custDescEntry);
		inConfig.getEntryOrConfig().add(makeEntry("state", EntryType.XSTRING, "IDLE", factory));
		
		Config cred = factory.createConfig();
		cred.setKey("workflow_credentials");
		inConfig.getEntryOrConfig().add(cred);
		
	}
	
}
