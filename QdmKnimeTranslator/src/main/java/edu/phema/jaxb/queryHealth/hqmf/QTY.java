//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.12.23 at 03:59:37 PM EST 
//


package edu.phema.jaxb.queryHealth.hqmf;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 *  is an abstract generalization for all data types (1) whose value set has an
 *         order relation (less-or-equal) and (2) where difference is defined in all of the data type's
 *         totally ordered value subsets. The quantity type abstraction is needed in defining certain
 *         other types, such as the interval and the probability distribution. 
 * 
 * <p>Java class for QTY complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QTY">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:hl7-org:v3}ANY">
 *       &lt;sequence>
 *         &lt;element name="expression" type="{urn:hl7-org:v3}ED" minOccurs="0"/>
 *         &lt;element name="originalText" type="{urn:hl7-org:v3}ED" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QTY", propOrder = {
    "expression",
    "originalText"
})
public abstract class QTY
    extends ANY
{

    protected ED expression;
    protected ED originalText;

    /**
     * Gets the value of the expression property.
     * 
     * @return
     *     possible object is
     *     {@link ED }
     *     
     */
    public ED getExpression() {
        return expression;
    }

    /**
     * Sets the value of the expression property.
     * 
     * @param value
     *     allowed object is
     *     {@link ED }
     *     
     */
    public void setExpression(ED value) {
        this.expression = value;
    }

    /**
     * Gets the value of the originalText property.
     * 
     * @return
     *     possible object is
     *     {@link ED }
     *     
     */
    public ED getOriginalText() {
        return originalText;
    }

    /**
     * Sets the value of the originalText property.
     * 
     * @param value
     *     allowed object is
     *     {@link ED }
     *     
     */
    public void setOriginalText(ED value) {
        this.originalText = value;
    }

}
