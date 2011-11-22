package net.toxbank.isa2rdf;

import java.io.Reader;

import net.toxbank.isa.ISAClass;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Resource;

public class EntryParser extends TabsParser<Resource> {
	protected ISAObject model;

    protected Individual collection;
	
	public ISAObject getModel() {
		return model;
	}

	public EntryParser(Reader in, String prefix, String prefixURI, ISAObject model) throws Exception {
		super(in);
		
		this.model = model;
		model.setPrefixURI(prefixURI);
		model.model.setNsPrefix(prefix, prefixURI);
	//	model.initModel(prefix);
		collection = model.addCollection(getEntryClass(),prefix);
	}
	
	protected ISAClass getEntryClass() {
		return ISAClass.ISACollection;
	}
	protected ISAClass getNodeClass() {
		return ISAClass.NamedNode;
	}	
	

	


	@Override
	protected void readHeader() throws Exception {
		super.readHeader();

		int lastIndex = -1;
		OntClass node = null;
		
	
		for (int i=0; i < header.length; i++) {
			if (header[i].isNamedNode()) {
				node = model.addNamedNodeClass(getNodeClass(), node, header[i], i);
				header[i].setResource(node);
				lastIndex = i;
			} else if (header[i].isProtocol()) {
				OntClass protocolClass = model.addProtocolNodeClass(node, header[i], i);
				node = protocolClass;
				header[i].setResource(protocolClass);

				lastIndex = i;
			} else if (header[i].isFactor()) {
				header[i].setResource(model.addFactor(node, header[i], i+1));
				lastIndex = i;
			} else if (header[i].isCharacteristic()) {
				header[i].setResource(model.addCharacteristic(node, header[i], i+1));
				lastIndex = i;
			} else if (header[i].isComment()) {

				header[i].setResource(model.addComment(node, header[i], i+1));
				lastIndex = i;
			} else if (header[i].isPerformer()) {

				header[i].setResource(model.addPerformer(node, header[i], i+1));
			} else if (header[i].isParameter()) {

				header[i].setResource(model.addParameter(node, header[i], i+1));
				lastIndex = i;
			} else if (header[i].isDate()) {

				header[i].setResource(model.addDate(node, header[i], i+1));
			} else if (header[i].isFile()) {

				header[i].setResource(model.addFile(node, header[i], i+1));
			} else if (header[i].isUnit()) {
				if (lastIndex>=0) header[lastIndex].setUnit(header[i]);
				lastIndex = i; //units can have terms!
			} else if (header[i].isTermREF()) {
				if (lastIndex>=0) header[lastIndex].setTermREF(header[i]);			
			} else if (header[i].isTermNo()) {
				if (lastIndex>=0) header[lastIndex].setTermNo(header[i]);						
			} else {
				System.err.println("What we don't support yet???\t"+header[i]+"\t"+header[lastIndex]);
			}
			
			
		}
/*
		for (ColumnHeader column:header) { 
			System.out.println(
					String.format("%s\t%s\t%s\t%s",
					column.index,column.title,column.termREF==null?"-":column.termREF.index, 
							column.termNo==null?"-":column.termNo.index )
							);
		}
*/		
	}
	
	@Override
	protected Resource transform(String[] tabs) throws Exception {
		
		Individual row = model.addRow(count,collection);
		Individual node = null;

		for (int i=0; i < tabs.length; i++) tabs[i] = tabs[i].replace("\"","");
		for (int i=0; i < tabs.length; i++) {
			
			String unit = null;
			String term = null;
			try {
			 unit = header[i].getUnitValue(tabs); 
			 term = header[i].getTerm(tabs); 
			} catch (Exception x) {
				System.out.println(header[i]);
				x.printStackTrace();
			}
	
			if (header[i].getNamedNodeClass()!=null) { //named node
				node = model.addNamedNode(header[i], tabs[i], row);
			} else if (header[i].getProtocolClass() != null) {
				node = model.addProtocolNode(header[i], tabs[i], row);
			} else if (header[i].getFileProperty() != null) {
				if (node!=null) 
					node = model.addProtocolNode(header[i], tabs[i], node);
			} else if (header[i].getObjectProperty() != null) {
				if (node!=null) 
					model.addNodeAttribute( header[i], tabs[i], unit, term, node);

			} else if (header[i].getDataProperty() != null) {
				if (node!=null) 
					model.addNodeDataAttribute(unit, header[i], tabs[i], node);
			}
		}
	
		return row;
	}
	
	
	

}



/**
SELECT ?study ?nl ?fl ?l
WHERE { 
?node <http://www.owl-ontologies.com/toxbank.owl#isPartOfEntry>  ?study.
?node rdfs:label ?nl.
?node  ?f ?z.
{
{?f rdfs:subPropertyOf <http://www.owl-ontologies.com/toxbank.owl#hasCharacteristic>.
?f rdfs:label ?fl.
?z rdfs:label ?l.}
union
{?f rdfs:subPropertyOf <http://www.owl-ontologies.com/toxbank.owl#hasFactor>.
?f rdfs:label ?fl.
?z rdfs:label ?l.}
}
}
order by ?study ?node
 */

