/**
 * 
 */
package edu.phema.QdmKnime.test;

import gov.nih.nlm.mor.axis.services.RxNormDBService.DBManager;
import gov.nih.nlm.mor.axis.services.RxNormDBService.DBManagerService;
import gov.nih.nlm.mor.axis.services.RxNormDBService.DBManagerServiceLocator;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import BeanService.RxConcept;
import BeanService.RxConceptGroup;

/**
 * @author moh
 *
 */
public class RxNormTest {

	/**
	 * 
	 */
	public RxNormTest() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 * @throws ServiceException 
	 * @throws MalformedURLException 
	 * @throws RemoteException 
	 */
	public static void main(String[] args) throws ServiceException, MalformedURLException, RemoteException {
		// TODO Auto-generated method stub
		String rxhost = "http://mor.nlm.nih.gov";
		String rxURI = rxhost + "/axis/services/RxNormDBService";

		// Locate the RxNorm API web service
		URL rxURL = new URL(rxURI);
		DBManagerService rxnormService = new DBManagerServiceLocator();
		DBManager dbmanager = rxnormService.getRxNormDBService(rxURL);
		
		// Get the concept unique identifier for a string
		String [] rxcuis = dbmanager.findRxcuiByString("aspirin");
		// print results
		for (int j = 0; j < rxcuis.length; j++)
		    System.out.println("RXCUI = " + rxcuis[j]);
		if (rxcuis.length == 0) 
		    System.out.println("No concept found");
		
		String[] types = {"IN", "BN"};
		
		
		RxConceptGroup[] ret = dbmanager.getRelatedByType("597974",  types);
		for (int i = 0; i < ret.length; i++){
			System.out.println(ret[i].getType());
			RxConcept[] cps = ret[i].getRxConcept();
			for(RxConcept cp : cps){
				System.out.println(cp.getSTR());
			}
		}

	}

}
