//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.12.15 at 03:09:44 PM CST 
//


package edu.phema.jaxb.ihe.svs;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for RetrieveValueSetResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RetrieveValueSetResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ValueSet" type="{urn:ihe:iti:svs:2008}ValueSetResponseType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="cacheExpirationHint" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RetrieveValueSetResponseType", propOrder = {
    "valueSet"
})
public class RetrieveValueSetResponseType {

    @XmlElement(name = "ValueSet", required = true)
    protected ValueSetResponseType valueSet;
    @XmlAttribute
    protected XMLGregorianCalendar cacheExpirationHint;

    /**
     * Gets the value of the valueSet property.
     * 
     * @return
     *     possible object is
     *     {@link ValueSetResponseType }
     *     
     */
    public ValueSetResponseType getValueSet() {
        return valueSet;
    }

    /**
     * Sets the value of the valueSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link ValueSetResponseType }
     *     
     */
    public void setValueSet(ValueSetResponseType value) {
        this.valueSet = value;
    }

    /**
     * Gets the value of the cacheExpirationHint property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCacheExpirationHint() {
        return cacheExpirationHint;
    }

    /**
     * Sets the value of the cacheExpirationHint property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCacheExpirationHint(XMLGregorianCalendar value) {
        this.cacheExpirationHint = value;
    }

}
