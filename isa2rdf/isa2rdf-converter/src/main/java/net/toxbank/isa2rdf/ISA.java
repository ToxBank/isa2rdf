package net.toxbank.isa2rdf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URLEncoder;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

public class ISA {
	/** <p>The RDF model that holds the vocabulary terms</p> */
	private static Model m_model = ModelFactory.createDefaultModel();
	/** <p>The namespace of the vocabalary as a string ({@value})</p> */
	protected static final String _NS = "http://www.toxbank.net/isa.owl#%s";

	public static final String NS = String.format(_NS,"");
	
	public static String getURI() {return NS;}
	/** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );

    
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
			return String.format(_NS, toString());
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
	
    /**
     * Object properties
     */
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
		   		Property p = model.getObjectProperty(String.format(_NS, toString()));
		   		if (p==null)
		   			p=	model.createObjectProperty(String.format(_NS, toString()),isFunctional());
		   		if (getComment()!=null) {
		   			model.add(p,RDFS.comment,getComment());
		   			
		   		}

		   		return p;
		   	}
		   	public String getURI() {
		   		return String.format(_NS, toString());
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
    /**
     * Data properties
     */
    public enum ISADataProperty {
    	isFirstNode,
    	hasPerformer,
    	hasDate,
    	hasUnit;
	   	public Property createProperty(OntModel jenaModel) {
	   		Property p = jenaModel.getDatatypeProperty(String.format(_NS, toString()));
	   		return p!= null?p:
	   				jenaModel.createDatatypeProperty(String.format(_NS, toString()));
	   	}
	   	public String getURI() {
	   		return String.format(_NS, toString());
	   	}
    };

    public static OntModel createModel() throws Exception {
    	return createModel(OntModelSpec.OWL_DL_MEM);
    }
	public static OntModel createModel(OntModelSpec spec) throws Exception {
		OntModel jenaModel = ModelFactory.createOntologyModel( spec,null);

		jenaModel.setNsPrefix( "isa", ISA.NS );
		jenaModel.setNsPrefix( "owl", OWL.NS );
		jenaModel.setNsPrefix( "dc", DC.NS );
		jenaModel.setNsPrefix( "dcterms", DCTerms.NS );
		jenaModel.setNsPrefix("xsd", XSDDatatype.XSD+"#");
		return jenaModel;
	}
	public static void writeStream(Model jenaModel, OutputStream output, String mediaType, boolean isXml_abbreviation) throws IOException {
		write(jenaModel,new OutputStreamWriter(output),mediaType,isXml_abbreviation);
	}
    public static void write(Model jenaModel, Writer output, String mediaType, boolean isXml_abbreviation) throws IOException {
    	try {
    		RDFWriter fasterWriter = null;
			if ("application/rdf+xml".equals(mediaType)) {
				if (isXml_abbreviation)
					fasterWriter = jenaModel.getWriter("RDF/XML-ABBREV");//lot smaller ... but could be slower
				else
					fasterWriter = jenaModel.getWriter("RDF/XML");
				fasterWriter.setProperty("xmlbase",jenaModel.getNsPrefixURI(""));
				fasterWriter.setProperty("showXmlDeclaration", Boolean.TRUE);
				fasterWriter.setProperty("showDoctypeDeclaration", Boolean.TRUE);
			}
			else if (mediaType.equals("application/x-turtle"))
				fasterWriter = jenaModel.getWriter("TURTLE");
			else if (mediaType.equals("text/n3"))
				fasterWriter = jenaModel.getWriter("N3");
			else if (mediaType.equals("text/n-triples"))
				fasterWriter = jenaModel.getWriter("N-TRIPLE");	
			else {
				fasterWriter = jenaModel.getWriter("RDF/XML-ABBREV");
				fasterWriter.setProperty("showXmlDeclaration", Boolean.TRUE);
				fasterWriter.setProperty("showDoctypeDeclaration", Boolean.TRUE);	//essential to get XSD prefixed
				fasterWriter.setProperty("xmlbase",jenaModel.getNsPrefixURI(""));
			}
			
			fasterWriter.write(jenaModel,output,"http://www.owl-ontologies.com/toxbank.owl");

    	} catch (Exception x) {
    		Throwable ex = x;
    		while (ex!=null) {
    			if (ex instanceof IOException) 
    				throw (IOException)ex;
    			ex = ex.getCause();
    		}
    	} finally {

    		try {if (output !=null) output.flush(); } catch (Exception x) { x.printStackTrace();}
    	}
    }
}
