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
	
	final private ArrayList<JSONObject> jsonObjRegistry = new ArrayList<JSONObject>();
	
	final public int ROOT_ACCESS;
	
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
		ROOT_ACCESS = this.registerJsonObject(rootJsonObject);
	}
	
	private int registerJsonObject(JSONObject obj) {
		int re = jsonObjRegistry.size();
		jsonObjRegistry.add(obj);
		return re;
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

	public String getStringValue(int jsonReg, String key){
		String re = null;
		JSONObject obj = jsonObjRegistry.get(jsonReg);
		if (obj.has(key)){
			try {
				re = obj.getString(key);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return re;
	}
	
	public String[] getStringArray(int jsonReg, String key){
		String[] re = null;
		JSONObject obj = jsonObjRegistry.get(jsonReg);
		if(obj.has(key)){
			try {
				JSONArray jArray = obj.getJSONArray(key);
				int size = jArray.length();
				re = new String[size];
				for (int i = 0; i < size; i ++){
					re[i] = jArray.getString(i);
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return re;
	}
	
	public Boolean getBooleanValue(int jsonReg, String key){
		Boolean re = null;
		JSONObject obj = jsonObjRegistry.get(jsonReg);
		if (obj.has(key)){
			try {
				re = Boolean.valueOf(obj.getBoolean(key));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return re;
	}
	
	public Boolean getBooleanValue(Integer jsonReg, String key){
		return this.getBooleanValue(jsonReg.intValue(), key);
	}
	
	public String getStringValue(Integer jsonReg, String key){
		return getStringValue(jsonReg.intValue(), key);
	}

	public Integer getJsonObjectRegistry(int parentJsonReg, String key){
		Integer re = null;
		JSONObject parent = jsonObjRegistry.get(parentJsonReg);
		try {
			re = new Integer(this.registerJsonObject(parent.getJSONObject(key)));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return re;
	}
	
	public Integer getJsonObjectRegistry(Integer parentJsonReg, String key){
		return getJsonObjectRegistry(parentJsonReg.intValue(), key);
	}
	
	public int[] getJsonArrayRegistries (int parentJson, String key) {
		JSONObject parent = jsonObjRegistry.get(parentJson);
		int[] re = null;
		if (parent.has(key)){
			try {
				JSONArray children = parent.getJSONArray(key);
				int size = children.length();
				re = new int[size];
				for (int i = 0; i < size; i ++){
					JSONObject child = children.getJSONObject(i);
					re[i] = this.registerJsonObject(child);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return re;
	}
	
	public int[] getJsonArrayRegestries(Integer parentJsonReg, String key){
		return this.getJsonArrayRegistries(parentJsonReg.intValue(), key);
	}
	
	/*
	 * Order is not preserved
	 * */
	public HashMap<String, String> getPopulationsList() {
		HashMap <String, String> re = new HashMap <String, String>();
		try{
			JSONArray populationsArray = rootJsonObject.getJSONArray("population_criteria");
			JSONObject populationsMap = populationsArray.getJSONObject(0);
			String[] keys = JSONObject.getNames(populationsMap);
			for (String key : keys){
				re.put(key, populationsMap.getString(key));
			}
		} catch(JSONException e){
			e.printStackTrace();
		}
		
		return re;		
	}
	
	public HashMap <String, Integer> getPopulationCriteriaAccesses (){
		HashMap<String, Integer> re = new HashMap<String, Integer> ();
		try {
			JSONObject populationCriteria = rootJsonObject.getJSONObject("population_criteria");
			String[] pops = JSONObject.getNames(populationCriteria);
			for (String pop : pops) {
				re.put(pop, this.registerJsonObject(populationCriteria.getJSONObject(pop)));
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return re;
	}
	
	public HashMap <String, Integer> getSourceDataCrtieriaAccesses(){
		HashMap <String, Integer> re = new HashMap <String, Integer>();
		JSONObject sourceDataCriteria = null;
		String[] names;
		try {
			sourceDataCriteria = rootJsonObject.getJSONObject("source_data_criteria");
			names = JSONObject.getNames(sourceDataCriteria);
			for (String name : names){
				re.put(name, 
						new Integer(this.registerJsonObject(
								sourceDataCriteria.getJSONObject(name))));
				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
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

	
	public HashMap<String, Integer> getDataCrtieriaAccesses(){
		String [] names = new String[] {};
		HashMap<String, Integer> re = new HashMap<String, Integer>();
		JSONObject dataCriteria;
		try {
			dataCriteria = rootJsonObject.getJSONObject("data_criteria");
			names = JSONObject.getNames(dataCriteria);
			for (String name : names){
				re.put(name, this.registerJsonObject(dataCriteria.getJSONObject(name)));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return re;
	}

//	public String getDataCriteriaInfo(String criteriaKey, String infoKey){
//		String re = null;
//		try{
//			JSONObject dataCriteria = rootJsonObject.getJSONObject("data_criteria");
//			JSONObject criterion = dataCriteria.getJSONObject(criteriaKey);
//			re = criterion.getString(infoKey);
//		} catch (JSONException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return re;
//	}

//	public String[] getDataCriteriaFieldsList(String criteriaKey) {
//		String[] re = new String[]{};
//		try{
//			JSONObject dataCriteria = rootJsonObject.getJSONObject("data_criteria");
//			JSONObject criterion = dataCriteria.getJSONObject(criteriaKey);
//			if (criterion.has("field_values")){
//				JSONObject field_values = criterion.getJSONObject("field_values");
//				re = JSONObject.getNames(field_values);
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		return re;
//	}
	
	/*
	 * That is lab result
	 * re = "" means there no "value" object
	 * */
//	public String typeOfValueInDataCriteria(String criteriaKey){
//		String re = "";
//		try{
//			JSONObject dataCriteria = rootJsonObject.getJSONObject("data_criteria");
//			JSONObject criterion = dataCriteria.getJSONObject(criteriaKey);
//			if (criterion.has("value")) {
//				JSONObject value_obj = criterion.getJSONObject("value");
//				re = value_obj.getString("type");
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		return re;
//	}
	
//	public String typeOfFieldValueInDataCriteria(String criteriaKey, String fieldKey){
//		String re = "";
//		try{
//			JSONObject dataCriteria = rootJsonObject.getJSONObject("data_criteria");
//			JSONObject criterion = dataCriteria.getJSONObject(criteriaKey);
//			JSONObject field_values_obj = criterion.getJSONObject("field_values");
//			JSONObject field_obj = field_values_obj.getJSONObject(fieldKey);
//			re = field_obj.getString("type");
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		return re;
//	}
	
	/*
	 * return: {Double high, Double low}
	 * */
//	public Double[] getValueIVL_PQInDataCriteriaHL (String criteriaKey) {
//		Double[] re = new Double[2];
//		try{
//			JSONObject dataCriteria = rootJsonObject.getJSONObject("data_criteria");
//			JSONObject criterion = dataCriteria.getJSONObject(criteriaKey);
//			JSONObject value_obj = criterion.getJSONObject("value");
//			re = getIVL_PQValues(value_obj);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		return re;
//	}
	
//	public String getTextOfValueInDataCriteria (String criteriaKey) {
//		String re = "value:%%00010";
//		try{
//			JSONObject dataCriteria = rootJsonObject.getJSONObject("data_criteria");
//			JSONObject criterion = dataCriteria.getJSONObject(criteriaKey);
//			JSONObject value_obj = criterion.getJSONObject("value");
//			if (value_obj == null) {
//				throw new JSONException(
//						"Cannot find value at " + criteriaKey);
//			}
//			if (value_obj.getString("type").equals("IVL_PQ")){
//				re = re + getIVL_PQDescription(value_obj);
//			} else {
//				re = re + value_obj.toString(); 
//			}
//
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		return re;
//	}

	/*
	 * return: {Double high, Double low}
	 * */
	public Double[] getHighLowOfIVL_PQ (Integer access) {
		Double[] re = new Double[2];
		JSONObject ivl_pq = jsonObjRegistry.get(access.intValue());
		try{
			re = getIVL_PQValues(ivl_pq);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return re;
	}
	
//	public HashMap<String, String> getFieldValuesCDInDataCriteria (String criteriaKey, String fieldKey) {
//		HashMap<String, String> re = new HashMap<String, String>();
//		try{
//			JSONObject dataCriteria = rootJsonObject.getJSONObject("data_criteria");
//			JSONObject criterion = dataCriteria.getJSONObject(criteriaKey);
//			JSONObject field_values_obj = criterion.getJSONObject("field_values");
//			JSONObject field_obj = field_values_obj.getJSONObject(fieldKey);
//			for(String key : JSONObject.getNames(field_obj)){
//				re.put(key, field_obj.getString(key));
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		return re;
//	}
	
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

	public HashMap <String, Integer> getChildrenJSONObjectAccesses (Integer access){
		HashMap <String, Integer> re = new HashMap <String, Integer>();
		JSONObject current = jsonObjRegistry.get(access.intValue());
		String[] names = JSONObject.getNames(current);
		for (String name : names){
			try {
				re.put(name, new Integer(this.registerJsonObject(current.getJSONObject(name))));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return re;
	}
	
//	public String getTextOfFieldValuesInDataCriteria (String criterionKey, String fieldKey) {
//		String re = fieldKey + ":%%00010";
//		try{
//			JSONObject dataCriteria = rootJsonObject.getJSONObject("data_criteria");
//			JSONObject criterion = dataCriteria.getJSONObject(criterionKey);
//			JSONObject field_values_obj = criterion.getJSONObject("field_values");
//			JSONObject field_obj = field_values_obj.getJSONObject(fieldKey);
//			if (field_obj == null) {
//				throw new JSONException(
//						"Cannot find field value " + fieldKey + " at " + criterionKey);
//			}
//			if (field_obj.getString("type").equals("IVL_PQ")){
//				re = re + getIVL_PQDescription(field_obj);
//			} else {
//				re = re + field_obj.toString(); 
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		return re;
//	}

//	public int getNumberOfTemporalRefsInDataCriteria (String criterionKey) {
//		int re = 0;
//		try {
//			JSONObject dataCriteria = rootJsonObject.getJSONObject("data_criteria");
//			JSONObject criterion = dataCriteria.getJSONObject(criterionKey);
//			if (criterion.has("temporal_references")){
//				JSONArray temporalReferences = criterion.getJSONArray("temporal_references");
//				re = temporalReferences.length();
//			}
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}		
//		return re;
//	}
	
//	public HashMap<String, String> getTemporalRefInDataCritieria (String criterionKey, int index){
//		HashMap<String, String> re = new HashMap<String, String>();
//		try {
//			JSONObject dataCriteria = rootJsonObject.getJSONObject("data_criteria");
//			JSONObject criterion = dataCriteria.getJSONObject(criterionKey);
//			JSONObject temporalRef;
//			if (criterion.has("temporal_references")){
//				JSONArray temporalReferences = criterion.getJSONArray("temporal_references");
//				if (index >= 0 && index < temporalReferences.length()){
//					temporalRef = temporalReferences.getJSONObject(index);
//					String[] keys = JSONObject.getNames(temporalRef);
//					for (String key : keys) {
//						re.put(key, temporalRef.getString(key));
//					}
//				}
//			}
//		} catch (JSONException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}		

//		return re;
//	}
	
	public Integer getTemporalRange_IVL_PQInDataCritieria(Integer access){
		JSONObject ivlPq = null;
		JSONObject temporalRef = jsonObjRegistry.get(access.intValue());
		try {
			if (temporalRef.has("range") && 
					temporalRef.getJSONObject("range").getString("type").equals("IVL_PQ")){
				ivlPq = temporalRef.getJSONObject("range");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Integer re = null;
		if (ivlPq != null){
			re = new Integer(this.registerJsonObject(ivlPq));
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
	
	public String getIVL_PQDescription (Integer access) {
		String re = "";
		JSONObject ivl_pq = jsonObjRegistry.get(access.intValue());
		if (ivl_pq.has("high")){
			JSONObject high_obj;
			String unit = "";
			try {
				high_obj = ivl_pq.getJSONObject("high");
				re = re + 
						(high_obj.has("inclusive?") && high_obj.getBoolean("inclusive?") ? 
								" less than or equal to: " : 
									" less than: ") + 
						high_obj.getString("value");
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
				re = re + (
						low_obj.has("inclusive?") && low_obj.getBoolean("inclusive?") ?
							" greater than or eaqual to: "	: 
								" greater than: ") + low_obj.getString("value");
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
		System.out.println(hqmf.getPopulationsList().toString());
		System.out.println(hqmf.getMeasureStartDatetime());
//		System.out.println(hqmf.getSourceDataCriteriaInfo("LaboratoryTestResultLdlCTest", "negation"));
//		System.out.println(hqmf.getTextOfValueInDataCriteria("LaboratoryTestResultLdlCTest_precondition_64"));
//		System.out.println(hqmf.getDataCriteriaInfo("LaboratoryTestResultLdlCTest_precondition_64", "value"));
//		System.out.println(hqmf.getTemporalRefInDataCritieria("DiagnosisActiveHospitalMeasuresAmi_precondition_7", 0).toString());
//		System.out.println(hqmf.getTemporalRange_IVL_PQInDataCritieria(
//				"OccurrenceAEmergencyDepartmentVisit2_precondition_46", 0));
	}

}
