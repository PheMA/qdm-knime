//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.12.15 at 03:09:44 PM CST 
//


package edu.phema.jaxb.ihe.svs;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ValueSetResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ValueSetResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{urn:ihe:iti:svs:2008}ValueSetExtendedType">
 *       &lt;sequence>
 *         &lt;element name="ConceptList" type="{urn:ihe:iti:svs:2008}ConceptListType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="displayName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="id" use="required" type="{urn:ihe:iti:svs:2008}valueSetIdType" />
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ValueSetResponseType")
public class ValueSetResponseType
    extends ValueSetExtendedType
{


}
