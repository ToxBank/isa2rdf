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

    public static OntModel createModel(boolean initOntology) throws Exception {
    	OntModel model = createModel(OntModelSpec.OWL_DL_MEM);
    	initModel(model);
    	return model;
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
	public static void initModel(OntModel model) throws Exception {
		
		OntClass node = ISA.ISAClass.ISANode.createOntClass(model);
		OntClass namedNode = ISA.ISAClass.NamedNode.createOntClass(model);
		model.add(namedNode,RDFS.subClassOf,node);
		
		OntClass study = ISA.ISAClass.Study.createOntClass(model);
		OntClass assay = ISA.ISAClass.Assay.createOntClass(model);
		
		OntClass collection = ISA.ISAClass.ISACollection.createOntClass(model);
		model.add(study,RDFS.subClassOf,collection);
		Property hasAssay = ISA.ISAObjectProperty.hasAssay.createProperty(model);
		model.add(hasAssay,RDFS.domain,study);
		model.add(hasAssay,RDFS.range,assay);
		
		OntClass studyEntry = ISA.ISAClass.StudyEntry.createOntClass(model);
		Property isPartOf = ISA.ISAObjectProperty.isPartOfStudy.createProperty(model);
		model.add(isPartOf,RDFS.domain,studyEntry);
		model.add(isPartOf,RDFS.range,study);
		Property pi = ISA.ISAObjectProperty.isPartOfStudy.createInverse(model);
		model.add(pi,RDFS.range,studyEntry);
		model.add(pi,RDFS.domain,study);
		
		Property isPartOfCollection = ISA.ISAObjectProperty.isPartOfCollection.createProperty(model);
		model.add(isPartOf,RDFS.subPropertyOf,isPartOfCollection);

		
		OntClass studyNode = ISA.ISAClass.StudyNode.createOntClass(model);
		model.add(studyNode,RDFS.subClassOf,namedNode);
		Property isPartOfStudyEntry = ISA.ISAObjectProperty.isPartOfStudyEntry.createProperty(model);
		model.add(isPartOfStudyEntry,RDFS.domain,studyNode);
		model.add(isPartOfStudyEntry,RDFS.range,studyEntry);
		pi = ISA.ISAObjectProperty.isPartOfStudyEntry.createInverse(model);
		model.add(pi,RDFS.range,studyNode);
		model.add(pi,RDFS.domain,studyEntry);

		Property partOfEntry = ISA.ISAObjectProperty.isPartOfEntry.createProperty(model);
		model.add(isPartOfStudyEntry,RDFS.subPropertyOf,partOfEntry);
		ISA.ISAObjectProperty.isPartOfEntry.createInverse(model);

		
		OntClass entry = ISA.ISAClass.ISAEntry.createOntClass(model);
		model.add(studyEntry,RDFS.subClassOf,entry);
		model.add(isPartOfCollection,RDFS.domain, entry);
		model.add(isPartOfCollection,RDFS.range,collection);

		OntClass protocol = ISA.ISAClass.Protocol.createOntClass(model);
		model.add(protocol,RDFS.subClassOf,node);
		
		Property param = ISA.ISAObjectProperty.hasParameter.createProperty(model);
		model.add(param,RDFS.domain,protocol);
		model.add(param,RDFS.range,ISA.ISAClass.Parameter.createOntClass(model));			

		
		Property factors = ISA.ISAObjectProperty.hasFactor.createProperty(model);
		model.add(factors,RDFS.domain,namedNode);
		model.add(factors,RDFS.range,ISA.ISAClass.Value.createOntClass(model));		
		
		Property chars = ISA.ISAObjectProperty.hasCharacteristic.createProperty(model);
		model.add(chars,RDFS.domain,namedNode);
		model.add(chars,RDFS.range,ISA.ISAClass.Value.createOntClass(model));	
		
		Property comm = ISA.ISAObjectProperty.hasCharacteristic.createProperty(model);
		model.add(comm,RDFS.domain,namedNode);
		model.add(comm,RDFS.range,ISA.ISAClass.Value.createOntClass(model));		
		
		Property next = ISA.ISAObjectProperty.hasNext.createProperty(model);
		model.add(next,RDFS.domain,node);
		model.add(next,RDFS.range,node);		
		
		///

		model.add(assay,RDFS.subClassOf,ISA.ISAClass.ISACollection.createOntClass(model));
		
		OntClass assayEntry = ISA.ISAClass.AssayEntry.createOntClass(model);
		Property isPartOfAssay = ISA.ISAObjectProperty.isPartOfAssay.createProperty(model);
		model.add(isPartOfAssay,RDFS.domain,assayEntry);
		model.add(isPartOfAssay,RDFS.range,assay);
		model.add(isPartOfAssay,RDFS.subPropertyOf,isPartOfCollection);
		Property pia = ISA.ISAObjectProperty.isPartOfAssay.createInverse(model);
		model.add(pia,RDFS.range,assayEntry);
		model.add(pia,RDFS.domain,assay);
		
		OntClass assayNode = ISA.ISAClass.AssayNode.createOntClass(model);
		model.add(assayNode,RDFS.subClassOf,ISA.ISAClass.NamedNode.createOntClass(model));		
		Property isPartAssayOfEntry = ISA.ISAObjectProperty.isPartOfAssayEntry.createProperty(model);
		model.add(isPartAssayOfEntry,RDFS.domain,assayNode);
		model.add(isPartAssayOfEntry,RDFS.range,assayEntry);
		pi = ISA.ISAObjectProperty.isPartOfAssayEntry.createInverse(model);
		model.add(pi,RDFS.range,assayNode);
		model.add(pi,RDFS.domain,assayEntry);	
		
		model.add(isPartAssayOfEntry,RDFS.subPropertyOf,partOfEntry);
		
		model.add(assayEntry,RDFS.subClassOf,entry);
		
		Property files = ISA.ISAObjectProperty.hasDataset.createProperty(model);
		model.add(files,RDFS.domain,assayNode);
		model.add(files,RDFS.range,ISA.ISAClass.Dataset.createOntClass(model));		

	}
}
