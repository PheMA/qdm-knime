/**
 * 
 */
package edu.phema.QdmKnimeInterfaces;

import java.text.ParseException;
import java.util.Date;

/**
 * @author admin
 *
 */
public interface MeasurePeriodInterface_v2b extends NodeInterface {
	
	/*
	 * No functions more than a metanode
	 * 
	 * Input ports:
	 * 
	 * 0: Patient data with pid
	 * 1: flow variable of startMeasurePeriod and endMeasurePeriod; both are strings with string of "yyyyMMddHHmm"
	 * 
	 * */
	
	// void setMeasureStart(String yyyyMMddHHmm) throws ParseException;
	// void setMeasureEnd(String yyyyMMddHHmm) throws ParseException;
	// Date getMeasureStart();
	// Date getMeasureEnd();
	// void setDateFormat(String dateFormat); // default "yyyyMMddHHmm"
}
