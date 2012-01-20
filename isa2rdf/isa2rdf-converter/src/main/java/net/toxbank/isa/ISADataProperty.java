package net.toxbank.isa;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;

public enum ISADataProperty {
	isFirstNode,
	hasPerformer,
	hasDate,
	hasUnit;
   	public Property createProperty(OntModel jenaModel) {
   		Property p = jenaModel.getDatatypeProperty(String.format(ISA._NS, toString()));
   		return p!= null?p:
   				jenaModel.createDatatypeProperty(String.format(ISA._NS, toString()));
   	}
   	public String getURI() {
   		return String.format(ISA._NS, toString());
   	}
};