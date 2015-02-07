package edu.phema.QdmKnimeInterfaces;

public interface TemporalRelationshipInterface extends NodeInterface {
	public static enum Unit {
		seconds, minutes, hours, days, weeks, months, years
	};
	public static enum Operator {
		equalTo, greaterThan, greaterThanOrEqualTo, lessThan, lessThanOrEqualTo, none
	};
	public static enum TemporalTypeCode {
		CONCURRENT, DURING, EAE, EAS, EBE, EBS, ECW, ECWS, EDU, OVERLAP, SAE, SAS, SBE, SBS, SCW, SCWE, SDU		
	};
	void setLeftElement (NodeInterface node);
	void setRightElement (NodeInterface node);
	//void setTemporalTypeCode (TemporalTypeCode typeCode);
	NodeInterface getLeftElement ();  // QDM data element node ID
	NodeInterface getRightElement();
	TemporalTypeCode getTemporalType();
	Operator getOperator();
	int getQuantity();
	Unit getUnit();
	void setOperator(Operator operator);  // <= 120 days: <=
	void setQuantity(int quantity);    // <= 120 days: 120
	void setUnit(Unit unit);            // <= 120 days: days
}

