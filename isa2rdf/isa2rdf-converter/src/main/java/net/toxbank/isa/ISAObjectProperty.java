package net.toxbank.isa;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

public enum ISAObjectProperty {
	hasAssay {
		
	},
	   	hasCharacteristic,
   	hasFactor {
		@Override
		public String getComment() {
			return 
		   	"A factor corresponds to an independent "+
		   	"variable manipulated by the experimentalist with the intention to affect biological systems in a way"+ 
		   	"that can be measured by an assay. The value of a factor is given in the Study or Assay file,"+ 
		   	"accordingly. If both Study and Assay have a  Factor Value, these must be different.";
		}
	},
   	hasComment,
	hasParameter,
	hasPart,
	isPartOf {
		@Override
		public Property createInverse(OntModel model) {
			return hasPart.createProperty(model);
		}
	},
	hasEntry,
	isPartOfCollection {
		@Override
		public boolean isFunctional() {
			return true;
		}    
		@Override
		public ISAObjectProperty getInverse() {
			return hasEntry;
		}
		
	},
	hasStudyEntry,
	isPartOfStudy {
		@Override
		public boolean isFunctional() {
			return true;
		}
		@Override
		public ISAObjectProperty getInverse() {
			return hasStudyEntry;
		}
	},
	hasAssayEntry,
	isPartOfAssay {
		@Override
		public boolean isFunctional() {
			return true;
		}
		@Override
		public ISAObjectProperty getInverse() {
			return hasAssayEntry;
		}		
	},
	hasNode,
	isPartOfEntry {
		@Override
		public boolean isFunctional() {
			return true;
		}
   		@Override
		public ISAObjectProperty getInverse() {
			return hasNode;
		}
	},
	hasStudyNode,
	isPartOfStudyEntry {
		@Override
		public boolean isFunctional() {
			return true;
		}
		@Override
		public ISAObjectProperty getInverse() {
			return hasStudyNode;
		}
		
	},    
	hasAssayNode,
	isPartOfAssayEntry {
		@Override
		public boolean isFunctional() {
			return true;
		}
		@Override
		public ISAObjectProperty getInverse() {
			return hasAssayNode;
		}
	
	},    	
	hasNext {
		@Override
		public boolean isFunctional() {
			return true;
		}    		
	},
	//hasNextEntry,
	//hasNextProtocol,
	hasMember,
	hasDataset {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return super.getComment();
		}
	};
	   	public Property createProperty(OntModel model) {
	   		Property p = model.getObjectProperty(String.format(ISA._NS, toString()));
	   		if (p==null)
	   			p=	model.createObjectProperty(String.format(ISA._NS, toString()),isFunctional());
	   		if (getComment()!=null) {
	   			model.add(p,RDFS.comment,getComment());
	   			
	   		}

	   		return p;
	   	}
	   	public String getURI() {
	   		return String.format(ISA._NS, toString());
	   	}
	   	public boolean isFunctional() {return false;}
	   	public String getComment() { return null;}
	   	public ISAObjectProperty getInverse() {return null;}
	   	public Property createInverse(OntModel model) {
	   		ISAObjectProperty p = getInverse();
			if (p != null) {
				Property pi = p.createProperty(model);
				model.add(createProperty(model),OWL.inverseOf,pi);
				return pi;
			}
			return null;		   		
	   	}
}