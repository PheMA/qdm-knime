//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.12.15 at 03:09:44 PM CST 
//


package edu.phema.jaxb.ihe.svs;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for CodingRationale.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="CodingRationale">
 *   &lt;restriction base="{urn:ihe:iti:svs:2008}cs">
 *     &lt;enumeration value="SH"/>
 *     &lt;enumeration value="HL7"/>
 *     &lt;enumeration value="SRC"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum CodingRationale {

    SH("SH"),
    @XmlEnumValue("HL7")
    HL_7("HL7"),
    SRC("SRC");
    private final String value;

    CodingRationale(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CodingRationale fromValue(String v) {
        for (CodingRationale c: CodingRationale.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
