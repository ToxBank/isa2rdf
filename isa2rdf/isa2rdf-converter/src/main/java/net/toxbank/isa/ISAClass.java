package net.toxbank.isa;

import java.net.URLEncoder;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.vocabulary.RDFS;

public enum ISAClass {

	/**
     * parent of Study and Assay
	 */
	ISACollection {
		@Override
		public String getComment() {
			return "A Study or an Assay";
		}			
	},
	Study {
		@Override
		public String getComment() {
			return "ISA-TAB Study";
		}
	},
	Assay {
		public String getComment() {
			return "ISA-TAB Assay";
		}
	},
	/**
	 * parent of StudyEntry and AssayEntry  (a row)
	 */
	ISAEntry {
		public String getComment() {
			return "A row (treatment) in a Study or Assay";
		}
	},
	StudyEntry {
		@Override
		public String getComment() {
			return "A row (treatment) in a Study";
		}
	},
	AssayEntry {
		@Override
		public String getComment() {
			return "A row in an ISA-TAB Assay";
		}			
	},
	/**
	 * parent of NamedNode and Protocol
	 */
	ISANode {
		@Override
		public String getComment() {
			return "Node or edge in the experimental graph (namednode or a protocol).";
		}
	},
	/**
	     * parent of AssayNode and StudyNode
	 */
	NamedNode {
		@Override
		public String getComment() {
			return "The nodes in the experimental graph in a Study or Assay (either [biological] material, such as a sample or an RNA extract, or a data object)";
		}			
	},
	AssayNode {
		@Override
		public String getComment() {
			return "The nodes in the experimental graph in an Assay";
		}				
	},
	StudyNode {
		@Override
		public String getComment() {
			return "The nodes in the experimental graph in a Study";
		}				
	},
	Value {
		@Override
		public String getComment() {
			return "A value, with units and assigned term from an ontology";
		}			
	},
	Dataset {
		@Override
		public String getComment() {
			return "A pointer to the data - file name or URI";
		}				
	},
	Protocol {
		@Override
		public String getComment() {
			return "A protocol takes one or more inputs (biological material or data) and generates one or more "+ 
				   "outputs (biological material or data). The protocols correspond to edges in the experimental "+
				   "graph, while materials and data correspond to the nodes. One or more Protocol REF columns should be used to specify the method used to transform a material or a data node";
		}				
	},
	Parameter {
		@Override
		public String getComment() {
			return "Protocol parameter";
		}				
	};
	
	public String getPrefix() {
		return String.format("isa%s",name().toLowerCase().charAt(0));
	}
	public String getURI() {
		return String.format("%s",name().toLowerCase());
	}	
	public String getURI(String value) {
		return String.format("%s/%s",getURI(),URLEncoder.encode(value));
	}
	public String getURI(int value) {
		return String.format("%s/%s%s",getURI(),name().charAt(0), value);
	}		
	public String getNS() {
		return String.format(ISA._NS, toString());
	}
	public OntClass getOntClass(OntModel model) {
		OntClass c = model.getOntClass(getNS());
		if (c==null) {
			c = model.createClass(getNS());
			String label = getLabel();
			if (label != null) model.add(c,RDFS.label,getLabel());
			if (getComment() != null) model.add(c,RDFS.comment,getComment());
		}
		return c;
	}
	public OntClass createOntClass(OntModel model) {
		return getOntClass(model);
	}		
	public void assignType(OntModel model,Individual individual) {
		individual.addOntClass(getOntClass(model));
	}	
	public ISAObjectProperty getProperty() {return null;}
	public String getLabel() { return null;}
	public String getComment() { return null;}

};
