//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.12.23 at 03:59:37 PM EST 
//


package edu.phema.jaxb.queryHealth.hqmf;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for ActStatus.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ActStatus">
 *   &lt;restriction base="{urn:hl7-org:v3}cs">
 *     &lt;enumeration value="cancelled"/>
 *     &lt;enumeration value="nullified"/>
 *     &lt;enumeration value="obsolete"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum ActStatus {

    @XmlEnumValue("cancelled")
    CANCELLED("cancelled"),
    @XmlEnumValue("nullified")
    NULLIFIED("nullified"),
    @XmlEnumValue("obsolete")
    OBSOLETE("obsolete");
    private final String value;

    ActStatus(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ActStatus fromValue(String v) {
        for (ActStatus c: ActStatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
