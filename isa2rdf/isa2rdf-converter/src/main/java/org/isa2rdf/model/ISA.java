package org.isa2rdf.model;

import org.isa2rdf.data.OT;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Java API to the ToxBank API OWL.
 */
public class ISA {
	final public static String TBKeywordsNS = "http://www.owl-ontologies.com/toxbank.owl/";
	public static final String URI ="http://onto.toxbank.net/isa/";
	public static final String URI_ENTREZ = "http://onto.toxbank.net/isa/Entrez/";
	public static final String URI_GENESYMBOL = "http://onto.toxbank.net/isa/Symbol/";
	public static final String URI_REFSEQ = "http://onto.toxbank.net/isa/RefSeq/";
	public static final String URI_UniGene = "http://onto.toxbank.net/isa/Unigene/";
	public static final String URI_Uniprot = "http://purl.uniprot.org/uniprot/";
	
	public static String FOAF = "http://xmlns.com/foaf/0.1/";

	private static final Resource otresource(String local) {
	        return ResourceFactory.createResource(String.format("%s%s",OT.getURI(),local));
	}
    private static final Property otproperty(String local) {
        return ResourceFactory.createProperty(String.format("%s%s",OT.getURI(), local));
    }
	
    private static final Resource resource(String local) {
        return ResourceFactory.createResource(String.format("%s%s",URI,local));
    }
    private static final Resource resourceWithParent(Model model, Resource resource,Resource parent) {
    	if (parent!=null) {
    		model.add(resource,RDFS.subClassOf, parent);
    	}
    	return resource;
    	
    }
    private static final Property property(String local) {
        return ResourceFactory.createProperty(URI, local);
    }
    private static final Property propertyWithDomainRange(Model model,Property property,Resource domain,Resource range,Resource parent) {
    	return propertyWithDomainRange(model,property, domain, range,parent,false);
    }
    private static final Property propertyWithDomainRange(Model model,Property property,Resource domain,Resource range, Resource parent, boolean functional) {
    	if (domain != null) {
    		model.add(property, RDFS.domain, domain);
    	}
        if (range!=null) {
        	model.add(property, RDFS.range, range);
        }
        if (parent!=null) {
        	model.add(property,RDF.type,parent);
        }
        if (functional)
        	model.add(property,RDF.type,OWL.FunctionalProperty);
        return property;
    }

    public static final Resource IDENTIFIABLE = resource("Identifiable");
    public static final Resource ANNOTATABLE = resource("Annotatable");
    public static final Resource ACCESSIBLE = resource("Accessible");
    
    public static final Resource GraphElement = resource("GraphElement"); 
    public static final Resource PROCESSINGNODE = resource("Processing");
    public static final Resource MaterialProcessing = resource("MaterialProcessing");
    public static final Resource DataProcessing = resource("DataProcessing");
    public static final Resource DataAcquisition = resource("DataAcquisition");
    public static final Resource NODE = resource("Node"); 
    public static final Resource MaterialNode = resource("MaterialNode"); 
    public static final Resource DataNode = resource("DataNode");
    public static final Resource Protocol = resource("Protocol");
    public static final Resource ProtocolApplication = resource("ProtocolApplication");
    public static final Resource Material = resource("Material"); 
    public static final Resource Data = resource("Data");
    public static final Resource MetabolightsData = resource("MetabolightsData");
    public static final Resource AssayResult = resource("AssayResult");
    /**
     * TODO : replace with ToxBank User object or FOAF: Person
     */
    public static final Resource Contact = resource("Contact");
    
    public static final Resource HASREFERENCES = resource("HASREFERENCES");
    public static final Resource Investigation = resource("Investigation");
    public static final Resource Study = resource("Study");
    public static final Resource Assay = resource("Assay");
    public static final Resource ReferenceSources = resource("ReferenceSources");
    
    public static final Resource OntologyTerm = resource("OntologyTerm");
    public static final Resource FreeTextTerm = resource("FreeTextTerm");
    public static final Resource PropertyValue = resource("PropertyValue");
    public static final Resource CharacteristicValue = resource("CharacteristicValue");
    public static final Resource FactorValue = resource("FactorValue");
    public static final Resource ParameterValue = resource("ParameterValue");
    public static final Resource UnitValue = resource("UnitValue");
    public static final Resource Unit = resource("Unit");
    public static final Resource Design = resource("Design");
    
    public static final Resource Property = resource("Property");
    public static final Resource Characteristic = resource("Characteristic");
    public static final Resource Factor = resource("Factor");
    public static final Resource Parameter = resource("Parameter");
    /**
     * Properties
     */
    /**
     * All ACCESSIBLE classes have accessionID
     * TODO : set classrestriction
     */
    public static final Property hasAccessionID = property("hasAccessionID");
    
    /**
     * PROCESSINGNODE HASINPUT NODE.
     * PROCESSINGNODE HASOUTPUT NODE.
     */
    public static final Property HASINPUT = property("hasInputNode");
    public static final Property HASOUTPUT = property("hasOutputNode");
    /**
     * ProtocolApplication APPLIESPROTOCOL Protocol.
     */
    public static final Property APPLIESPROTOCOL = property("appliesProtocol");
    /**
     * PROCESSINGNODE APPLIESPROTOCOLS ProtocolApplication
     */
    public static final Property hasProtocolApplication = property("hasProtocolApplication");
    
    /**
     * MaterialNode HASMATERIAL Material
     */
    public static final Property HASMATERIAL = property("hasMaterial");
    /**
     * DataNode HASDATA Data
     */
    public static final Property HASDATA = property("hasData");    
    /**
     * GraphElement (node,processingnode) HASSTUDY Study
     */
    public static final Property HASSTUDY = property("hasStudy");
    public static final Property HASASSAY = property("hasAssay");
    public static final Property USES = property("uses");
    
    /**
     * STUDY HASPROTOCOL Protocol.
     */
    public static final Property HASPROTOCOL = property("hasProtocol");
    
    public static final Property HASXREF = property("hasXref");
    
    public static final Property HASSAMPLEFIELD = property("hasSampleField");
    public static final Property HASASSAYFIELD = property("hasAssayFields");
    //FreeTextTerms
    public static final Property HASONTOLOGYTERM = property("hasOntologyTerm");
    
    //public static final Property HASPROPERTYVALUE = property("hasPropertyValue"); //x
    public static final Property HASFACTORVALUE = property("hasFactorValue");
    public static final Property HASCHARACTERISTICVALUE = property("hasCharacteristicValue");
    public static final Property HASPARAMVALUE = property("hasParameterValue");
    public static final Property HASVALUE = property("hasValue");
    public static final Property HASPROPERTY = property("hasProperty");
    public static final Property HASFACTOR = property("hasFactor");
    public static final Property HASPARAMETER = property("hasParameter");
    //Contacts can be Investigation or Study owners
    public static final Property HASOWNER = property("hasOwner");
    public static final Property HASORDERDAUTHOR = property("hasOrderedAuthor");
    public static final Property HASORDER = property("hasOrder");
    public static final Property HASUNITVALUE = property("hasUnitValue");
    public static final Property HASUNIT = property("hasUnit");

    public static final Property HASENDPOINT = property("hasEndpoint");
    public static final Property USESTECHNOLOGY = property("usesTechnology");
    public static final Property USESPLATFORM= property("usesPlatform");
    
    public static final Property HASPROBE= property("hasProbe");
    
    public static void init(Model model) {
    	 resourceWithParent(model,ANNOTATABLE,IDENTIFIABLE);
    	 resourceWithParent(model,ACCESSIBLE,ANNOTATABLE);
    	 resourceWithParent(model,OntologyTerm,ANNOTATABLE);
    	 resourceWithParent(model,GraphElement,ACCESSIBLE);
    	 resourceWithParent(model,ReferenceSources,ACCESSIBLE);
    	 resourceWithParent(model,PROCESSINGNODE,GraphElement);
    	 resourceWithParent(model,MaterialProcessing,PROCESSINGNODE);
    	 resourceWithParent(model,DataProcessing,PROCESSINGNODE);
    	 resourceWithParent(model,DataAcquisition,PROCESSINGNODE);
    	 resourceWithParent(model,NODE,GraphElement);
    	 resourceWithParent(model,MaterialNode,NODE);
    	 resourceWithParent(model,Material,ACCESSIBLE);
    	 resourceWithParent(model,DataNode,NODE);
    	 resourceWithParent(model,Data,ACCESSIBLE);
    	 resourceWithParent(model,Protocol,ACCESSIBLE);
    	 resourceWithParent(model,ProtocolApplication,ACCESSIBLE);
    	 
    	 resourceWithParent(model,HASREFERENCES,ACCESSIBLE);
    	 resourceWithParent(model,Investigation,HASREFERENCES);
    	 resourceWithParent(model,Study,HASREFERENCES);
    	 resourceWithParent(model,Assay,HASREFERENCES);
    	 
    	 resourceWithParent(model,FreeTextTerm,ANNOTATABLE);
    	 resourceWithParent(model,PropertyValue,FreeTextTerm);
    	 resourceWithParent(model,UnitValue,FreeTextTerm);
    	 resourceWithParent(model,Unit,FreeTextTerm);
    	 resourceWithParent(model,Design,FreeTextTerm);
    	 
    	 resourceWithParent(model,CharacteristicValue,PropertyValue);
    	 resourceWithParent(model,FactorValue,PropertyValue);
    	 resourceWithParent(model,ParameterValue,PropertyValue);
    	 
    	 resourceWithParent(model,Property,FreeTextTerm);
    	 resourceWithParent(model,Characteristic,Property);
    	 resourceWithParent(model,Factor,Property);
    	 resourceWithParent(model,Parameter,Property);
    	 
    	 resourceWithParent(model,Contact,ANNOTATABLE);
    	 
    	 //properties
    	 propertyWithDomainRange(model,hasAccessionID,ACCESSIBLE,null,OWL.DatatypeProperty,true);
    	  /**
    	     * PROCESSINGNODE HASINPUT NODE.
    	     * PROCESSINGNODE HASOUTPUT NODE.
    	     */
    	 propertyWithDomainRange(model,HASINPUT,PROCESSINGNODE,NODE,OWL.ObjectProperty);
    	 propertyWithDomainRange(model,HASOUTPUT,PROCESSINGNODE,NODE,OWL.ObjectProperty);
    	    /**
    	     * ProtocolApplication APPLIESPROTOCOL Protocol.
    	     */
    	propertyWithDomainRange(model,APPLIESPROTOCOL,ProtocolApplication,Protocol,OWL.ObjectProperty,true);
    	    /**
    	     * PROCESSINGNODE APPLIESPROTOCOLS ProtocolApplication
    	     */
    	propertyWithDomainRange(model,hasProtocolApplication,PROCESSINGNODE,ProtocolApplication,OWL.ObjectProperty);
    	    
    	    /**
    	     * MaterialNode HASMATERIAL Material
    	     */
    	propertyWithDomainRange(model,HASMATERIAL,MaterialNode,Material,OWL.ObjectProperty,true);
    	    /**
    	     * DataNode HASDATA Data
    	     */
    	propertyWithDomainRange(model,HASDATA,DataNode,Data,OWL.ObjectProperty,true);     
    	    
    	propertyWithDomainRange(model,HASSTUDY,Investigation,Study,OWL.ObjectProperty,false);
    	propertyWithDomainRange(model,HASASSAY,Study,Assay,OWL.ObjectProperty,false);
    	
    	propertyWithDomainRange(model,HASPROTOCOL,Study,Protocol,OWL.ObjectProperty,false);
    	
    	//propertyWithDomainRange(model,HASXREF,HASREFERENCES,null,OWL.DatatypeProperty,true);
    	
    	propertyWithDomainRange(model,HASSAMPLEFIELD,NODE,null,OWL.DatatypeProperty,true);
    	
    	propertyWithDomainRange(model,HASASSAYFIELD,NODE,null,OWL.DatatypeProperty,false);
    	
    	propertyWithDomainRange(model,HASONTOLOGYTERM,FreeTextTerm,OntologyTerm,OWL.ObjectProperty,false);
    	
    	propertyWithDomainRange(model,HASFACTORVALUE,Data,FactorValue,OWL.ObjectProperty,false);
    	propertyWithDomainRange(model,HASCHARACTERISTICVALUE,Material,CharacteristicValue,OWL.ObjectProperty,false);
    	propertyWithDomainRange(model,HASPARAMVALUE,ProtocolApplication,ParameterValue,OWL.ObjectProperty,false);
    	propertyWithDomainRange(model,HASVALUE,PropertyValue,null,OWL.DatatypeProperty,false);
    	propertyWithDomainRange(model,HASPROPERTY,PropertyValue,Property,OWL.ObjectProperty,false);
    	propertyWithDomainRange(model,HASFACTOR,FactorValue,Factor,OWL.ObjectProperty,true);
    	propertyWithDomainRange(model,HASPARAMETER,ParameterValue,Parameter,OWL.ObjectProperty,false);
    	
    	
    	//
    	propertyWithDomainRange(model,DCTerms.title,null,null,OWL.AnnotationProperty,false);
    	propertyWithDomainRange(model,DCTerms.hasVersion,null,null,OWL.AnnotationProperty,false);
    	propertyWithDomainRange(model,DCTerms.description,null,null,OWL.AnnotationProperty,false);
    	propertyWithDomainRange(model,DCTerms.abstract_,null,null,OWL.AnnotationProperty,false);
    	propertyWithDomainRange(model,DCTerms.created,null,null,OWL.AnnotationProperty,false);
    	
    	propertyWithDomainRange(model,RDFS.isDefinedBy,null,null,OWL.AnnotationProperty,false);
    	
    	propertyWithDomainRange(model,HASOWNER,null,Contact,OWL.AnnotationProperty,false);
    	
    	propertyWithDomainRange(model,HASUNITVALUE,PropertyValue,null,OWL.DatatypeProperty,false);
    	propertyWithDomainRange(model,HASUNIT,PropertyValue,OntologyTerm,OWL.ObjectProperty,false);
    	
    	propertyWithDomainRange(model,HASENDPOINT,Assay,OntologyTerm,OWL.ObjectProperty,false);
    	propertyWithDomainRange(model,USESTECHNOLOGY,Assay,OntologyTerm,OWL.ObjectProperty,false);
    	propertyWithDomainRange(model,USESPLATFORM,Assay,OntologyTerm,OWL.ObjectProperty,false);
    	

    	propertyWithDomainRange(model,HASPROBE,otresource(OT.OTClass.DataEntry.name()),null,OWL.ObjectProperty,false);
    	//OpenTox dataset
    	
    }
    public static void main(String[] args) {
    	Model model = ModelFactory.createDefaultModel();
    	init(model);
		model.setNsPrefix("isa", ISA.URI);
		model.setNsPrefix("dcterms", DCTerms.NS);
		model.setNsPrefix("foaf", ISA.FOAF);
		model.setNsPrefix("rdfs", RDFS.getURI());
		model.setNsPrefix("rdf", RDF.getURI());
		model.setNsPrefix("owl", OWL.getURI());
		//model.setNsPrefix("tb", TOXBANK.URI);
    	model.write(System.out,"N3");
    }
}
