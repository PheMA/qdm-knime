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
public interface MeasurePeriodInterface_v2a extends NodeInterface {
	void setMeasureStart(String yyyyMMddHHmm) throws ParseException;
	void setMeasureEnd(String yyyyMMddHHmm) throws ParseException;
	Date getMeasureStart();
	Date getMeasureEnd();
	// void setDateFormat(String dateFormat); // default "yyyyMMddHHmm"
}
