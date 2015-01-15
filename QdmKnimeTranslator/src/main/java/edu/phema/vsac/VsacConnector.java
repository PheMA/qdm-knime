/**
 * 
 */
package edu.phema.vsac;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;

import edu.phema.jaxb.ihe.svs.ObjectFactory;
import edu.phema.jaxb.ihe.svs.RetrieveValueSetResponseType;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.ConnectException;

/**
 * @author moh
 *
 */
public class VsacConnector {
	private String tgt;
	private final String account;
	private final String passcode;
	private final String restServer = "https://vsac.nlm.nih.gov/vsac/ws";
	Client client = ClientBuilder.newClient();
	
	
	/*
	 * main is for test purposes. Please fill in the UMLS account and password
	 * */
	public static void main(String[] args) throws ConnectException, JAXBException{
		VsacConnector cnt = new VsacConnector("account", "password"); // account, password
		System.out.println(cnt.getTgt());
		System.out.println(cnt.getTicket());
		//System.out.println(cnt.getValueSetXml("2.16.840.1.113883.3.666.5.1738", "21021231"));

		StringWriter stringWriter = new StringWriter();
		//JAXBElement<RetrieveValueSetResponseType> xmlRoot = cnt.getValueSetJaxb("2.16.840.1.113883.3.666.5.1738", "21021231");
		JAXBElement<RetrieveValueSetResponseType> xmlRoot = cnt.getValueSetJaxb("2.16.840.1.113883.3.666.5.1738");
		
		System.out.println(xmlRoot.toString());

		Marshaller mars = JAXBContext.newInstance(
				RetrieveValueSetResponseType.class).createMarshaller();
		mars.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		mars.marshal(xmlRoot, stringWriter);
		
		System.out.println(stringWriter.toString());
	
	}
	
	public VsacConnector (String account, String passcode) throws ConnectException{
		this.account = account;
		this.passcode = passcode;
		renewTgt();
	}
	
	public String getValueSetXml(String oid, String effectiveDate /*yyyyMMdd*/) throws ConnectException{
		String retrievedXml = "";
		
		
		//System.out.println(target.getUri().toString());
		
		WebTarget target = this.prepareTargetToGetValueSet(oid, effectiveDate);
		
		Response response = target.request().get();
		
		if (response.getStatus() == 200) {
			retrievedXml = response.readEntity(String.class);
		} else {
			throw new ConnectException(response.readEntity(String.class));
		}
		return retrievedXml;
	}
	
	public String getValueSetXml(String oid) throws ConnectException{
		return getValueSetXml(oid, "");
	}

	public JAXBElement<RetrieveValueSetResponseType> getValueSetJaxb(
			String oid, String effectiveDate /*yyyyMMdd*/) throws ConnectException {
		
		WebTarget target = this.prepareTargetToGetValueSet(oid, effectiveDate);
		
		Response response = target.request().get();
		
		if (response.getStatus() != 200) {
			throw new ConnectException(response.readEntity(String.class));
		}
		
		RetrieveValueSetResponseType xmlRoot = response.readEntity(RetrieveValueSetResponseType.class);
		
		return new ObjectFactory().createRetrieveValueSetResponse(xmlRoot);
	}
	
	public JAXBElement<RetrieveValueSetResponseType> getValueSetJaxb(
			String oid) throws ConnectException {
		return getValueSetJaxb(oid, "");
	}
	
	private WebTarget prepareTargetToGetValueSet (
			String oid, String effectiveDate /*yyyyMMdd*/) throws ConnectException{
		
		WebTarget target = client.target(restServer)
				.path("RetrieveValueSet");
		target = target.queryParam("id", oid);
		if (effectiveDate.matches("\\d{8}")){
			target = target.queryParam("effectiveDate", effectiveDate);
		}
		String ticket = "";
		try {
			ticket = getTicket();
		} catch (ConnectException e) {
			// TODO Auto-generated catch block
			renewTgt();
			ticket = getTicket();
		}
		target = target.queryParam("ticket", ticket);
		return target;
	}
	
	private void renewTgt() throws ConnectException{
		WebTarget target = client.target(restServer)
				.path("Ticket");
		Form form = new Form();
		form.param("username", account);
		form.param("password", passcode);
		Response response = target.request(MediaType.TEXT_PLAIN)
				.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));
		if (response.getStatus() == 200) {
			tgt = response.readEntity(String.class);
		} else {
			throw new ConnectException(response.readEntity(String.class));
		}
	}
	
	private String getTicket() throws ConnectException{
		String ticket = "";
		WebTarget target = client.target(restServer)
				.path("Ticket/" + tgt);
		Form form = new Form();
		form.param("service", "http://umlsks.nlm.nih.gov");
		Response response = target.request(MediaType.TEXT_PLAIN)
				.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));
		if (response.getStatus() == 200) {
			ticket = response.readEntity(String.class);
		} else {
			throw new ConnectException(response.readEntity(String.class));
		}
		return ticket;
	}
	
	public String getTgt(){
		return tgt;
	}
}
