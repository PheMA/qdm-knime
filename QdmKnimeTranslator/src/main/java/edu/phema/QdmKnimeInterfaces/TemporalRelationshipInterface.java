package edu.phema.QdmKnimeInterfaces;

public interface TemporalRelationshipInterface extends NodeInterface {
	public static enum Unit {
		seconds, minutes, hours, days, weeks, months, years
	};
	public static enum Operator {
		equalTo, greaterThan, greaterThanOrEqualTo, lessThan, lessThanOrEqualTo, none
	};
	public static enum TemporalTypeCode {
		CONCURRENT(true, true, true, true), DURING(true, true, true, true), 
		EAE(false, true, false, true), EAS(false, true, true, false), 
		EBE(false, true, false, true), EBS(false, true, true, false), 
		ECW(false, true, false, true), ECWS(false, true, true, false), 
		EDU(false, true, true, true), OVERLAP(true, true, true, true), 
		SAE(true, false, false, true), SAS(true, false, true, false), 
		SBE(true, false, false, true), SBS(true, false, true, false), 
		SCW(true, false, true, false), SCWE(true, false, false, true), 
		SDU(true, false, true, true);
		
		public final boolean REQUIRE_LEFT_START;
		public final boolean REQUIRE_LEFT_END;
		public final boolean REQUIRE_RIGHT_START;
		public final boolean REQUIRE_RIGHT_END;
		TemporalTypeCode(boolean leftStart, boolean leftEnd, boolean rightStart, boolean rightEnd){
			this.REQUIRE_LEFT_START = leftStart;
			this.REQUIRE_LEFT_END = leftEnd;
			this.REQUIRE_RIGHT_START = rightStart;
			this.REQUIRE_RIGHT_END = rightEnd;
		}
		
	};
	void setLeftElement (NodeInterface node);
	void setRightElement (NodeInterface node);
	//void setTemporalTypeCode (TemporalTypeCode typeCode);
	NodeInterface getLeftElement ();  // QDM data element node ID
	NodeInterface getRightElement();
	TemporalTypeCode getTemporalType();
	Operator getOperator();
	double getQuantity();
	Unit getUnit();
	void setOperator(Operator operator);  // <= 120 days: <=
	void setQuantity(double quantity);    // <= 120 days: 120
	void setUnit(Unit unit);            // <= 120 days: days
}

