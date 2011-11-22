package net.toxbank.isa2rdf;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;

public class ColumnHeader<R> {
	String title;
	String label;
	int index = -1;
	public int getIndex() {
		return index;
	}

	protected R resource;
	protected ColumnHeader unit=null;
	public ColumnHeader getUnit() {
		return unit;
	}

	public void setUnit(ColumnHeader unit) {
		this.unit = unit;
	}

	public ColumnHeader getTermREF() {
		return termREF;
	}

	public void setTermREF(ColumnHeader termREF) {
		this.termREF = termREF;
	}

	public ColumnHeader getTermNo() {
		return termNo;
	}

	public void setTermNo(ColumnHeader termNo) {
		this.termNo = termNo;
	}

	protected ColumnHeader termREF=null;
	protected ColumnHeader termNo=null;
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getTitle() {
		return title;
	}
	
	public ColumnHeader(String title,int index) {
		this.title = title.replace("\"","").trim().replace(" ", "");
		this.label = title;
		this.index = index;
	}

	public R getResource() {
		return resource;
	}

	public void setResource(R resource) {
		this.resource = resource;
	}
	public boolean isProtocol() {
		return title.contains("Protocol");
	}
	public boolean isNamedNode() {
		return title.contains("Name");
	}
	public boolean isFile() {
		return title.contains("File");
	}	
	public boolean isFactor() {
		return title.contains("Factor");
	}	
	public boolean isParameter() {
		return title.contains("Parameter");
	}		
	public boolean isPerformer() {
		return title.startsWith("Performer");
	}		
	public boolean isDate() {
		return title.equals("Date");
	}	
	
	public boolean isCharacteristic() {
		return title.contains("Characteristics");
	}
	public boolean isComment() {
		return title.contains("Comment");
	}
	public boolean isUnit() {
		return title.equals("Unit");
	}
	public boolean isTermREF() {
		return title.equals("TermSourceREF");
	}
	public boolean isTermNo() {
		return title.equals("TermAccessionNumber");
	}	
		

	@Override
	public String toString() {
		return title;
		/*
		if (isTermREF() || isTermNo()) return title;
		else return String.format("%s\t{%s%s%s}\t%s",title,termREF==null?"":termREF,termREF==null?"":":",termNo==null?"":termNo,unit==null?"":unit);
		*/
	}
	
	public OntClass getNamedNodeClass() {
		if (isNamedNode() && (resource instanceof OntClass)) return (OntClass)resource; 
		else return null;
	}
	public OntClass getProtocolClass() {
		if (isProtocol() && (resource instanceof OntClass)) return (OntClass)resource; 
		else return null;
	}	
	public ObjectProperty getObjectProperty() {
		if (resource instanceof ObjectProperty) return (ObjectProperty)resource; 
		else return null;
	}		
	public DatatypeProperty getDataProperty() {
		if (resource instanceof DatatypeProperty) return (DatatypeProperty)resource; 
		else return null;
	}	
	public ObjectProperty getFileProperty() {
		if (isFile() && (resource instanceof ObjectProperty)) return (ObjectProperty)resource; 
		else return null;
	}		
	public String getUnitValue(String[] values) {
		return (unit==null)?null:values[unit.index];
	}
	public String getTerm(String[] values) {
		if ((termREF == null) && (termNo==null)) return null;
		String t1 = (termREF==null)?null:"".equals(values[termREF.index])?null:values[termREF.index];
		String t2 = (termNo==null)?null:"".equals(values[termNo.index])?null:values[termNo.index];
		if (t1==null && t2==null) return null;
		return String.format("%s:%s",t1,t2);
	}	
}