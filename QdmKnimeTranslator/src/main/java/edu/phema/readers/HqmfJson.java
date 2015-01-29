/**
 * 
 */
package edu.phema.readers;

//import java.io.FileReader;
import java.io.IOException;
//import java.nio.file.Paths;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.phema.QdmKnime.Toolkit;

/**
 * @author Huan Mo
 *
 */
public class HqmfJson {

	/**
	 * @throws ParseException 
	 * 
	 */
	
	final JSONObject rootJsonObject;
//	final String id;
//	final String hqmf_id;
//	final String hqmf_set_id;
//	final String hqmf_version_number;
//	final String title;
//	final String description;
//	final String cms_id;
	
	public HqmfJson(String hqmfJsonString) throws JSONException {
		// TODO Auto-generated constructor stub
		rootJsonObject = new JSONObject(hqmfJsonString);
	}
	
	
	/*
	 * id, hqmf_id, hqmf_set_id, hqmf_version_number
	 * title, description, cms_id
	 * 
	 * */
	public String getInfo(String key){
		String re = null;
		try {
			re = rootJsonObject.getString(key);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return re;
	}

	
	/*
	 * Order is not preserved
	 * */
	public String[] getPopulationList() {
		String [] re = new String[] {};
		try{
			JSONArray populationsArray = rootJsonObject.getJSONArray("populations");
			JSONObject populationsMap = populationsArray.getJSONObject(0);
			re = JSONObject.getNames(populationsMap);
		} catch(JSONException e){
			e.printStackTrace();
		}
		
		return re;		
	}
	
	public String[] getSourceDataCrtieriaList(){
		String [] re = new String[] {};
		JSONObject sourceDataCriteria;
		try {
			sourceDataCriteria = rootJsonObject.getJSONObject("source_data_criteria");
			re = JSONObject.getNames(sourceDataCriteria);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return re;
	}
	
	public String getSourceDataCriteriaInfo(String criteriaKey, String infoKey){
		String re = null;
		try{
			JSONObject sourceDataCriteria = rootJsonObject.getJSONObject("source_data_criteria");
			JSONObject criterion = sourceDataCriteria.getJSONObject(criteriaKey);
			re = criterion.getString(infoKey);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return re;
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws JSONException 
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws IOException, JSONException {
		// TODO Auto-generated method stub
		String testFile = "src/test/resources/cypress-bundle-latest/sources/eh/CMS30v4/hqmf_model.json";
		String jsonString = Toolkit.readFile(testFile);
		HqmfJson hqmf = new HqmfJson(jsonString);
		System.out.println(hqmf.getInfo("id"));
		System.out.println(hqmf.getInfo("cms_id"));
		System.out.println(hqmf.getPopulationList()[0]);
		System.out.println(hqmf.getSourceDataCriteriaInfo("LaboratoryTestResultLdlCTest", "negation"));
	}

}
