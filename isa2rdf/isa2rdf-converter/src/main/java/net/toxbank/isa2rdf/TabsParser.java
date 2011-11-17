package net.toxbank.isa2rdf;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Resource;

public abstract class TabsParser<E> implements Iterator<E>, Closeable {
	protected BufferedReader reader;
	protected int count = 0;
	protected ColumnHeader[] header;
	protected String[] tabs;

	
	public TabsParser(Reader in) {
		reader = new BufferedReader(in);
		
	}
	
	@Override
	public void close() throws IOException {
		if (reader!=null) reader.close();
		
	}

	@Override
	public boolean hasNext() {
		try {
			readHeader();
			count++;
			String line = reader.readLine();
			tabs = line==null?null:line.split("\t");
			if (tabs!=null)
			for (int i=0;i< tabs.length;i++)
				if (tabs[i]!=null)
					tabs[i] = tabs[i].replace("\"","").trim();
			
			return line != null;
		} catch (Exception x) {
			x.printStackTrace();
			return false;
		}
	}

	@Override
	public E next() {
		try {
			return transform(tabs);
		} catch (Exception x) {
			x.printStackTrace(); //???
			return null;
		}
	}

	@Override
	public void remove() {
	}
	protected abstract E transform(String[] tabs) throws Exception;
	
	protected void readHeader() throws Exception {
		if (count>0) return; 
		String line = reader.readLine();
		String[] h = line.split("\t");
		header = new ColumnHeader[h.length];
		for (int i=0;i< header.length;i++)
			header[i] = new ColumnHeader(h[i],i);
	}
	
}

class ColumnHeader {
	String title;
	String label;
	int index = -1;
	public int getIndex() {
		return index;
	}

	protected Resource resource;
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

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}
	protected boolean isProtocol() {
		return title.contains("Protocol");
	}
	protected boolean isNamedNode() {
		return title.contains("Name");
	}
	protected boolean isFile() {
		return title.contains("File");
	}	
	protected boolean isFactor() {
		return title.contains("Factor");
	}	
	protected boolean isParameter() {
		return title.contains("Parameter");
	}		
	protected boolean isPerformer() {
		return title.startsWith("Performer");
	}		
	protected boolean isDate() {
		return title.equals("Date");
	}	
	
	protected boolean isCharacteristic() {
		return title.contains("Characteristics");
	}
	protected boolean isComment() {
		return title.contains("Comment");
	}
	protected boolean isUnit() {
		return title.equals("Unit");
	}
	protected boolean isTermREF() {
		return title.equals("TermSourceREF");
	}
	protected boolean isTermNo() {
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
