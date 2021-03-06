//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.12.15 at 03:09:44 PM CST 
//


package edu.phema.jaxb.ihe.svs;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ValueSetExtendedType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ValueSetExtendedType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:ihe:iti:svs:2008}ValueSetType">
 *       &lt;sequence>
 *         &lt;element name="ConceptList" type="{urn:ihe:iti:svs:2008}ConceptListType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="displayName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ValueSetExtendedType", propOrder = {
    "conceptList"
})
public abstract class ValueSetExtendedType
    extends ValueSetType
{

    @XmlElement(name = "ConceptList", required = true)
    protected List<ConceptListType> conceptList;
    @XmlAttribute
    protected String displayName;

    /**
     * Gets the value of the conceptList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the conceptList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConceptList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ConceptListType }
     * 
     * 
     */
    public List<ConceptListType> getConceptList() {
        if (conceptList == null) {
            conceptList = new ArrayList<ConceptListType>();
        }
        return this.conceptList;
    }

    /**
     * Gets the value of the displayName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the value of the displayName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplayName(String value) {
        this.displayName = value;
    }

}
