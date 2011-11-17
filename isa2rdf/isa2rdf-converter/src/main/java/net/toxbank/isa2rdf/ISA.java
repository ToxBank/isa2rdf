package net.toxbank.isa2rdf;

import java.io.IOException;
import java.io.OutputStream;
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
	protected static final String _NS = "http://www.owl-ontologies.com/toxbank.owl#%s";
	public static final String NS = String.format(_NS,"");
	
	public static String getURI() {return NS;}
	/** <p>The namespace of the vocabalary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );

    
	public enum ISAClass {
		ISACollection, //parent of Study and Assay
		Study,
		Assay,
		ISAEntry, //parent of StudyEntrt and AssayEntry  (a row)
		StudyEntry,
		AssayEntry,
		ISANode, //parent of NamedNode and Protocol
		NamedNode, //parent of AssayNode and StudyNode
		AssayNode,
		StudyNode,
		Value,
		ValueFile,
		Protocol;
		
		public String getPrefix() {
			return String.format("tb%s",name().toLowerCase().charAt(0));
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
				c = createOntClass(model);
				String label = getLabel();
				if (label != null) model.add(c,RDFS.comment,getLabel());
			}
			return c;
		}
		public OntClass createOntClass(OntModel model) {
			return model.createClass(getNS());
		}		
		public void assignType(OntModel model,Individual individual) {
			individual.addOntClass(getOntClass(model));
		}	
		public ISAObjectProperty getProperty() {return null;}
		public String getLabel() { return null;}

	};
	
    /**
     * Object properties
     */
    public enum ISAObjectProperty {
 	   	hasCharacteristic,
	   	hasFactor,
	   	hasComment,
		hasParameter,
    	isPartOf, //parent of isPartOf*
    	isPartOfCollection,
    	isPartOfEntry,
    	hasNext,
    	//hasNextEntry,
    	//hasNextProtocol,
    	hasMember,
    	hasFile;
		   	public Property createProperty(OntModel model) {
		   		Property p = model.getObjectProperty(String.format(_NS, toString()));
		   		return p!= null?p:
		   				model.createObjectProperty(String.format(_NS, toString()));
		   	}
		   	public String getURI() {
		   		return String.format(_NS, toString());
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

		jenaModel.setNsPrefix( "tb", ISA.NS );
		jenaModel.setNsPrefix( "owl", OWL.NS );
		jenaModel.setNsPrefix( "dc", DC.NS );
		jenaModel.setNsPrefix( "dcterms", DCTerms.NS );
		jenaModel.setNsPrefix("xsd", XSDDatatype.XSD+"#");
		return jenaModel;
	}
	
    public static void write(OntModel jenaModel, OutputStream output, String mediaType, boolean isXml_abbreviation) throws IOException {
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
