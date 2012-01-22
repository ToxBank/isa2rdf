package org.isa2rdf.cli;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.OWL;
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
    		model.createStatement(resource,RDFS.subClassOf, parent);
    	}
    	return resource;
    	
    }
    private static final Property property(String local) {
        return ResourceFactory.createProperty(URI, local);
    }
    private static final Property propertyWithDomainRange(String local,Resource domain,Resource range) {
    	Property property =   ResourceFactory.createProperty(URI, local);
    	/*
    	if (domain != null) ResourceFactory.createStatement(property, RDFS.domain, domain);
        if (range!=null) 	ResourceFactory.createStatement(property, RDFS.range, range);
        */
        return property;
    }

    public static final Resource IDENTIFIABLE = resource("Identifiable");
    public static final Resource ANNOTATABLE = resource("Annotatable");
    //resourceWithParent("Annotatable",IDENTIFIABLE);
    public static final Resource ACCESSIBLE = resource("Accessible"); 
    //resourceWithParent("Accessible",ANNOTATABLE);
    
    public static final Resource PROCESSINGNODE = resource("Processing");
    //resourceWithParent("Processing",ACCESSIBLE);
    public static final Resource MaterialProcessing = resource("MaterialProcessing");
    	//resourceWithParent("MaterialProcessing",PROCESSINGNODE);
    public static final Resource DataProcessing = resource("DataProcessing");
    //resourceWithParent("DataProcessing",PROCESSINGNODE);
    public static final Resource DataAcquisition = resource("DataAcquisition");
    //resourceWithParent("DataAcquisition",PROCESSINGNODE);
    
    public static final Resource NODE = resource("Node"); 
    	//resourceWithParent("Node",ACCESSIBLE);
    public static final Resource MaterialNode = resource("MaterialNode"); 
    	//resourceWithParent("MaterialNode",NODE);
    public static final Resource DataNode = resource("DataNode");
    	//resourceWithParent("DataNode",NODE);
    
    public static final Resource Protocol = resource("Protocol");
    public static final Resource ProtocolApplication = resource("ProtocolApplication");

    public static final Resource Material = resource("Material"); 
    	//resourceWithParent("Material",NODE);
    public static final Resource Data = resource("Data"); 
    	//resourceWithParent("Data",NODE);
    /**
     * Properties
     */
    /**
     * All ACCESSIBLE classes have accessionID
     * TODO : set classrestriction
     */
    public static final Property hasAccessionID = propertyWithDomainRange("hasAccessionID",ACCESSIBLE,null);
    
    /**
     * PROCESSINGNODE HASINPUT NODE.
     * PROCESSINGNODE HASOUTPUT NODE.
     */
    public static final Property HASINPUT = propertyWithDomainRange("hasInputNode",PROCESSINGNODE,NODE);
    public static final Property HASOUTPUT = propertyWithDomainRange("hasOutputNode",PROCESSINGNODE,NODE);
    /**
     * ProtocolApplication HASPROTOCOL Protocol.
     */
    public static final Property HASPROTOCOL = propertyWithDomainRange("hasProtocol",ProtocolApplication,Protocol);
    /**
     * PROCESSINGNODE APPLIESPROTOCOLS ProtocolApplication
     */
    public static final Property APPLIESPROTOCOLS = propertyWithDomainRange("hasProtocolApplication",PROCESSINGNODE,ProtocolApplication);
    
    /**
     * MaterialNode HASMATERIAL Material
     */
    public static final Property HASMATERIAL = propertyWithDomainRange("hasMaterial",MaterialNode,Material);
    /**
     * DataNode HASDATA Data
     */
    public static final Property HASDATA = propertyWithDomainRange("hasData",DataNode,Data);    
    
    public static void init(Model model) {
    	 resourceWithParent(model,ANNOTATABLE,IDENTIFIABLE);
    	 resourceWithParent(model,ACCESSIBLE,ANNOTATABLE);
    	 resourceWithParent(model,PROCESSINGNODE,ACCESSIBLE);
    	 resourceWithParent(model,MaterialProcessing,PROCESSINGNODE);
    	 resourceWithParent(model,DataProcessing,PROCESSINGNODE);
    	 resourceWithParent(model,DataAcquisition,PROCESSINGNODE);
    	 resourceWithParent(model,NODE,ACCESSIBLE);
    	 resourceWithParent(model,MaterialNode,NODE);
    	 resourceWithParent(model,Material,NODE);
    	 resourceWithParent(model,Data,NODE);
    	 resourceWithParent(model,Protocol,ACCESSIBLE);
    	 resourceWithParent(model,ProtocolApplication,ACCESSIBLE);

    }
}
