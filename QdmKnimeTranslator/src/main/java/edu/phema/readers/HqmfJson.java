/**
 * 
 */
package edu.phema.readers;

//import java.io.FileReader;
import java.io.IOException;
//import java.nio.file.Paths;



import java.util.ArrayList;
import java.util.HashMap;

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
	
	final private JSONObject rootJsonObject;
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
	
	public String getMeasureStartDatetime(){
		String re = "";
		try {
			JSONObject measurePeriod = rootJsonObject.getJSONObject("measure_period");
			JSONObject measureStart = measurePeriod.getJSONObject("low");
			re = measureStart.getString("value");
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return re;
	}
	
	public String getMeasureEndDatetime(){
		String re = "";
		try {
			JSONObject measurePeriod = rootJsonObject.getJSONObject("measure_period");
			JSONObject measureStart = measurePeriod.getJSONObject("high");
			re = measureStart.getString("value");
			
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
	
	public String[] getDataCrtieriaList(){
		String [] re = new String[] {};
		JSONObject dataCriteria;
		try {
			dataCriteria = rootJsonObject.getJSONObject("data_criteria");
			re = JSONObject.getNames(dataCriteria);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return re;
	}

	public String getDataCriteriaInfo(String criteriaKey, String infoKey){
		String re = null;
		try{
			JSONObject dataCriteria = rootJsonObject.getJSONObject("data_criteria");
			JSONObject criterion = dataCriteria.getJSONObject(criteriaKey);
			re = criterion.getString(infoKey);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return re;
	}

	public String[] getDataCriteriaFieldsList(String criteriaKey) {
		String[] re = new String[]{};
		try{
			JSONObject dataCriteria = rootJsonObject.getJSONObject("data_criteria");
			JSONObject criterion = dataCriteria.getJSONObject(criteriaKey);
			if (criterion.has("field_values")){
				JSONObject field_values = criterion.getJSONObject("field_values");
				re = JSONObject.getNames(field_values);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return re;
	}
	
	/*
	 * That is lab result
	 * re = "" means there no "value" object
	 * */
	public String typeOfValueInDataCriteria(String criteriaKey){
		String re = "";
		try{
			JSONObject dataCriteria = rootJsonObject.getJSONObject("data_criteria");
			JSONObject criterion = dataCriteria.getJSONObject(criteriaKey);
			if (criterion.has("value")) {
				JSONObject value_obj = criterion.getJSONObject("value");
				re = value_obj.getString("type");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return re;
	}
	
	public String typeOfFieldValueInDataCriteria(String criteriaKey, String fieldKey){
		String re = "";
		try{
			JSONObject dataCriteria = rootJsonObject.getJSONObject("data_criteria");
			JSONObject criterion = dataCriteria.getJSONObject(criteriaKey);
			JSONObject field_values_obj = criterion.getJSONObject("field_values");
			JSONObject field_obj = field_values_obj.getJSONObject(fieldKey);
			re = field_obj.getString("type");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return re;
	}
	
	/*
	 * return: {Double high, Double low}
	 * */
	public Double[] getValueIVL_PQInDataCriteriaHL (String criteriaKey) {
		Double[] re = new Double[2];
		try{
			JSONObject dataCriteria = rootJsonObject.getJSONObject("data_criteria");
			JSONObject criterion = dataCriteria.getJSONObject(criteriaKey);
			JSONObject value_obj = criterion.getJSONObject("value");
			re = getIVL_PQValues(value_obj);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return re;
	}
	
	public String getTextOfValueInDataCriteria (String criteriaKey) {
		String re = "value:%%00010";
		try{
			JSONObject dataCriteria = rootJsonObject.getJSONObject("data_criteria");
			JSONObject criterion = dataCriteria.getJSONObject(criteriaKey);
			JSONObject value_obj = criterion.getJSONObject("value");
			if (value_obj == null) {
				throw new JSONException(
						"Cannot find value at " + criteriaKey);
			}
			if (value_obj.getString("type").equals("IVL_PQ")){
				re = re + getIVL_PQDescription(value_obj);
			} else {
				re = re + value_obj.toString(); 
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return re;
	}

	/*
	 * return: {Double high, Double low}
	 * */
	public Double[] getFieldValues_IVL_PQInDataCriteriaHL (String criteriaKey, String fieldKey) {
		Double[] re = new Double[2];
		try{
			JSONObject dataCriteria = rootJsonObject.getJSONObject("data_criteria");
			JSONObject criterion = dataCriteria.getJSONObject(criteriaKey);
			JSONObject field_values_obj = criterion.getJSONObject("field_values");
			JSONObject field_obj = field_values_obj.getJSONObject(fieldKey);
			re = getIVL_PQValues(field_obj);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return re;
	}
	
	public HashMap<String, String> getFieldValuesCDInDataCriteria (String criteriaKey, String fieldKey) {
		HashMap<String, String> re = new HashMap<String, String>();
		try{
			JSONObject dataCriteria = rootJsonObject.getJSONObject("data_criteria");
			JSONObject criterion = dataCriteria.getJSONObject(criteriaKey);
			JSONObject field_values_obj = criterion.getJSONObject("field_values");
			JSONObject field_obj = field_values_obj.getJSONObject(fieldKey);
			for(String key : JSONObject.getNames(field_obj)){
				re.put(key, field_obj.getString(key));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return re;
	}
	
	public HashMap<String, String> getValueCDInDataCriteria (String criteriaKey) {
		HashMap<String, String> re = new HashMap<String, String>();
		try{
			JSONObject dataCriteria = rootJsonObject.getJSONObject("data_criteria");
			JSONObject criterion = dataCriteria.getJSONObject(criteriaKey);
			JSONObject value_obj = criterion.getJSONObject("value");
			for(String key : JSONObject.getNames(value_obj)){
				re.put(key, value_obj.getString(key));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return re;
	}

	
	public String getTextOfFieldValuesInDataCriteria (String criteriaKey, String fieldKey) {
		String re = fieldKey + ":%%00010";
		try{
			JSONObject dataCriteria = rootJsonObject.getJSONObject("data_criteria");
			JSONObject criterion = dataCriteria.getJSONObject(criteriaKey);
			JSONObject field_values_obj = criterion.getJSONObject("field_values");
			JSONObject field_obj = field_values_obj.getJSONObject(fieldKey);
			if (field_obj == null) {
				throw new JSONException(
						"Cannot find field value " + fieldKey + " at " + criteriaKey);
			}
			if (field_obj.getString("type").equals("IVL_PQ")){
				re = re + getIVL_PQDescription(field_obj);
			} else {
				re = re + field_obj.toString(); 
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return re;
	}

	
	private static Double[] getIVL_PQValues (JSONObject ivl_pq) throws NumberFormatException, JSONException{
		Double[] re = new Double[2];
		re[0] = null;
		re[1] = null;
		if (ivl_pq.has("high")){
			re[0] = new Double(Double.valueOf(ivl_pq.getJSONObject("high").getString("value")));
		}
		if (ivl_pq.has("low")){
			re[1] = new Double(Double.valueOf(ivl_pq.getJSONObject("low").getString("value")));
		}
		return re;
	}
	
	private static String getIVL_PQDescription (JSONObject ivl_pq) {
		String re = "";
		if (ivl_pq.has("high")){
			JSONObject high_obj;
			String unit = "";
			try {
				high_obj = ivl_pq.getJSONObject("high");
				re = re + " less than: " + high_obj.getString("value");
				if (high_obj.has("unit")){
					re = re + " " + high_obj.getString("unit");
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (ivl_pq.has("low")){
			JSONObject low_obj;
			String unit = "";
			try {
				low_obj = ivl_pq.getJSONObject("low");
				re = re + " greater than: " + low_obj.getString("value");
				if (low_obj.has("unit")){
					re = re + " " + low_obj.getString("unit");
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		System.out.println(hqmf.getMeasureStartDatetime());
		System.out.println(hqmf.getSourceDataCriteriaInfo("LaboratoryTestResultLdlCTest", "negation"));
		System.out.println(hqmf.getTextOfValueInDataCriteria("LaboratoryTestResultLdlCTest_precondition_64"));
		System.out.println(hqmf.getDataCriteriaInfo("LaboratoryTestResultLdlCTest_precondition_64", "value"));
	}

}
