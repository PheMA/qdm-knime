/**
 * 
 */
package edu.phema.Enum.QdmKnime;

/**
 * @author moh
 *
 */
public enum CodeSystemEnum {
	ICD9CM ("2.16.840.1.113883.6.103", "ICD9:", "http://hl7.org/fhir/sid/icd-9"),
	ICD10CM ("2.16.840.1.113883.6.90", "ICD10:", "http://hl7.org/fhir/sid/icd-10"),
	LOINC ("2.16.840.1.113883.6.1", "LOINC:", "http://loinc.org"),
	SNOMEDCT ("2.16.840.1.113883.6.96", "", "http://snomed.info/sct"),
	RXNORM ("2.16.840.1.113883.6.88", "", ""),
	CPT ("2.16.840.1.113883.6.12", "", ""),
	NDC ("2.16.840.1.113883.6.69", "NDC:", "");
	
	public final String OID;
	public final String I2B2_PREFIX;
	public final String FHIR_URI;
	CodeSystemEnum (String oid, String i2b2, String fhir){
		OID = oid;
		I2B2_PREFIX = i2b2;
		FHIR_URI = fhir;
	}
}
