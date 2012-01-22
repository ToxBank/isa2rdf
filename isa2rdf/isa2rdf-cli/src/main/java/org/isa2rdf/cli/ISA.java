package org.isa2rdf.cli;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Java API to the ToxBank API OWL.
 */
public class ISA {

	public static final String URI ="http://onto.toxbank.net/isa/";

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
     * ProtocolApplication HASPROTOCOL Protocol.
     */
    public static final Property HASPROTOCOL = property("hasProtocol");
    /**
     * PROCESSINGNODE APPLIESPROTOCOLS ProtocolApplication
     */
    public static final Property APPLIESPROTOCOLS = property("hasProtocolApplication");
    
    /**
     * MaterialNode HASMATERIAL Material
     */
    public static final Property HASMATERIAL = property("hasMaterial");
    /**
     * DataNode HASDATA Data
     */
    public static final Property HASDATA = property("hasData");    
    
    public static void init(Model model) {
    	 resourceWithParent(model,ANNOTATABLE,IDENTIFIABLE);
    	 resourceWithParent(model,ACCESSIBLE,ANNOTATABLE);
    	 resourceWithParent(model,PROCESSINGNODE,ACCESSIBLE);
    	 resourceWithParent(model,MaterialProcessing,PROCESSINGNODE);
    	 resourceWithParent(model,DataProcessing,PROCESSINGNODE);
    	 resourceWithParent(model,DataAcquisition,PROCESSINGNODE);
    	 resourceWithParent(model,NODE,ACCESSIBLE);
    	 resourceWithParent(model,MaterialNode,NODE);
    	 resourceWithParent(model,Material,ACCESSIBLE);
    	 resourceWithParent(model,DataNode,NODE);
    	 resourceWithParent(model,Data,ACCESSIBLE);
    	 resourceWithParent(model,Protocol,ACCESSIBLE);
    	 resourceWithParent(model,ProtocolApplication,ACCESSIBLE);
    	 
    	 //properties
    	 propertyWithDomainRange(model,hasAccessionID,ACCESSIBLE,null,OWL.DatatypeProperty,true);
    	  /**
    	     * PROCESSINGNODE HASINPUT NODE.
    	     * PROCESSINGNODE HASOUTPUT NODE.
    	     */
    	 propertyWithDomainRange(model,HASINPUT,PROCESSINGNODE,NODE,OWL.ObjectProperty);
    	 propertyWithDomainRange(model,HASOUTPUT,PROCESSINGNODE,NODE,OWL.ObjectProperty);
    	    /**
    	     * ProtocolApplication HASPROTOCOL Protocol.
    	     */
    	    propertyWithDomainRange(model,HASPROTOCOL,ProtocolApplication,Protocol,OWL.ObjectProperty,true);
    	    /**
    	     * PROCESSINGNODE APPLIESPROTOCOLS ProtocolApplication
    	     */
    	    propertyWithDomainRange(model,APPLIESPROTOCOLS,PROCESSINGNODE,ProtocolApplication,OWL.ObjectProperty);
    	    
    	    /**
    	     * MaterialNode HASMATERIAL Material
    	     */
    	    propertyWithDomainRange(model,HASMATERIAL,MaterialNode,Material,OWL.ObjectProperty,true);
    	    /**
    	     * DataNode HASDATA Data
    	     */
    	    propertyWithDomainRange(model,HASDATA,DataNode,Data,OWL.ObjectProperty,true);        	 

    }
}
