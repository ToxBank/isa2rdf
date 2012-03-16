package org.isa2rdf.cli;

import java.util.ArrayList;
import java.util.Collection;

import org.isatools.tablib.utils.BIIObjectStore;

import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.model.Investigation;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.model.processing.Assay;
import uk.ac.ebi.bioinvindex.model.processing.DataAcquisition;
import uk.ac.ebi.bioinvindex.model.processing.DataNode;
import uk.ac.ebi.bioinvindex.model.processing.DataProcessing;
import uk.ac.ebi.bioinvindex.model.processing.MaterialNode;
import uk.ac.ebi.bioinvindex.model.processing.MaterialProcessing;
import uk.ac.ebi.bioinvindex.model.processing.Node;
import uk.ac.ebi.bioinvindex.model.processing.Processing;
import uk.ac.ebi.bioinvindex.model.processing.ProtocolApplication;
import uk.ac.ebi.bioinvindex.model.term.ParameterValue;
import uk.ac.ebi.bioinvindex.model.xref.ReferenceSource;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class ProcessingPipelineRDFGenerator<NODE extends Identifiable>  extends RDFGenerator<NODE,Model>{
	
	
	/**
	 * I will graph all the instances of {@link #objects} which are {@link Processing} (and nodes, materials, etc.).
	 * WARNING: I need to assign a temporary IDs to the objecs, via {@link Identifiable#setId(Long)}, BE AWARE that
	 * the objects are changed if their ID is null. Otherwise ENSURE the IDs returned by {@link Identifiable#getId()}
	 * are distinct.
	 *
	 */
	public ProcessingPipelineRDFGenerator ( String prefix,  BIIObjectStore store , Model model) {
		super(prefix,store,model);
		getModel().setNsPrefix( "", prefix+"/" );
		getModel().setNsPrefix( "isa", ISA.URI );
		getModel().setNsPrefix( "owl", OWL.NS );
		getModel().setNsPrefix( "dc", DC.NS );
		getModel().setNsPrefix( "dcterms", DCTerms.NS );
		getModel().setNsPrefix( "rdfs", RDFS.getURI() );
		getModel().setNsPrefix( "rdf", RDF.getURI() );
		getModel().setNsPrefix("xsd", XSDDatatype.XSD+"#");
		ISA.init(getModel());
	}
	
	public ProcessingPipelineRDFGenerator (  String prefix,  BIIObjectStore store ) {
		this(prefix, store,ModelFactory.createDefaultModel());

	}

	/**
	 * Creates the DOT string corresponding to the graph of {@link Processing} objects in the collection I manage
	 *
	 */
	public Model createGraph () throws Exception
	{
		Collection<Identifiable> objects = new ArrayList<Identifiable>();
        objects.addAll(store.values(Processing.class));
		for ( Identifiable object: objects )
		{
			if ( object == null ) continue;
			if ( object instanceof MaterialProcessing )
				graphProcessing ( (MaterialProcessing) object );
			else if ( object instanceof DataAcquisition )
				graphProcessing ( (DataAcquisition) object );
			else if ( object instanceof DataProcessing )
				graphProcessing ( (DataProcessing) object );
		}
		objects.clear();
		//why there are no URL for the ontologies???
		//FIXME - use owl:ontology and ontology uri
		
        objects.addAll(store.values(ReferenceSource.class));
	        for ( Identifiable object: objects )  {
	        	ReferenceSource xs = ((ReferenceSource)object);
	        	Resource xref = getResource(object, ISA.ReferenceSources);
	        	getModel().add(xref,DCTerms.title,xs.getName());
	        	if (xs.getDescription()!=null)
	        		getModel().add(xref,DCTerms.description,xs.getDescription());
	        	if (xs.getVersion() != null)
	        		getModel().add(xref,DCTerms.hasVersion,xs.getVersion());
	        	//getModel().add(xref,DCTerms.hasVersion,((ReferenceSource)object).getUrl());
	      }		
	    objects.clear();
	    objects.addAll(store.values(Study.class));   
	    for ( Identifiable object: objects )  {
	    	Study xs = ((Study)object);
	    	Resource xref = getResource(object, ISA.Study);
	    	for (Assay assay :xs.getAssays()) {
	    		Resource xassay = getResourceID(assay, ISA.Assay);	
	    		getModel().add(xref,ISA.HASASSAY,xassay);
	    	}
	    	/*
	    	for (Investigation inv :xs.getInvestigations()) {
	    		Resource xinv = getResource(inv, ISA.Investigation);	
	    		getModel().add(xref,ISA.HASASSAY,xassay);
	    	}
	    	*/
	    }

	    objects.clear();
	    objects.addAll(store.values(Investigation.class));   
	    for ( Identifiable object: objects )  {
	    	Investigation xs = ((Investigation)object);
	    	Resource xref = getResource(object, ISA.Investigation);
	    	for (Study study :xs.getStudies()) {
	    		Resource xstudy = getResourceID(study, ISA.Study);	
	    		getModel().add(xref,ISA.HASSTUDY,xstudy);
	    	}
	    }	   

		return getModel();
	}
	
	

	/** IN/OUT Node prefix to be used with IDs and labels
	private String getNodePrefx ( Node<?, ?> node ) {
		if ( node instanceof MaterialNode ) return "MN_";
		if ( node instanceof DataNode) return "DN_";
		return "";
	}
	 */



	/** Works on a single processing step */
	private Resource graphProcessing ( Processing<?, ?> processing ) throws Exception
	{

		Resource resource = ISA.PROCESSINGNODE;
		if ( processing instanceof MaterialProcessing ) {
			resource = ISA.MaterialProcessing;
		}
		else if ( processing instanceof DataAcquisition ) {
			resource = ISA.DataAcquisition;
		}
		else if ( processing instanceof DataProcessing ) {
			resource = ISA.DataProcessing;
		}
		
		Resource processingNode = getResource(processing, resource);
		
		for ( ProtocolApplication protoApp: processing.getProtocolApplications () ) {
			Resource protoAppNode = getResource(protoApp, ISA.ProtocolApplication);
			processingNode.addProperty(ISA.hasProtocolApplication, protoAppNode);
			
			Resource protocolNode = getResource(protoApp.getProtocol(), ISA.Protocol);
			protocolNode.addLiteral(RDFS.label, protoApp.getProtocol ().getName ());
			
			protoAppNode.addProperty(ISA.APPLIESPROTOCOL, protocolNode);
			for (ParameterValue paramValue: protoApp.getParameterValues()) {
				Resource xpv = getResource(paramValue, ISA.ParameterValue);
				protoAppNode.addProperty(ISA.HASPARAMVALUE, xpv);
			}
			

		}

		// for each input: input -> processing
		for ( Node<?, ?> node: processing.getInputNodes () ) {
			Resource inputNode = graphNode(node,false);
			processingNode.addProperty(ISA.HASINPUT, inputNode);
		}
		
		// for each out: processing -> out
		for (  Node<?, ?> node: processing.getOutputNodes () ) {
			Resource outputNode = graphNode(node,false);
			processingNode.addProperty(ISA.HASOUTPUT, outputNode);
		}		
		return processingNode;
		

	}


	/** Forwards to the specific node */
	private Resource graphNode ( Node<?, ?> node , boolean output) throws Exception 
	{
		if ( node instanceof MaterialNode ) return graphMaterialNode ( (MaterialNode) node , output);
		if ( node instanceof DataNode ) return graphDataNode ( (DataNode) node ,output);
		throw new RuntimeException ( "Don't know what to do with node of type " + node.getClass ().getName () );
	}
	

	/** Works on a single IN/OUT node */
	private Resource graphMaterialNode ( MaterialNode node , boolean output ) throws Exception
	{
		Resource materialNode = getResource(node, ISA.MaterialNode);
		Resource material = getResource(node.getMaterial(), ISA.Material);
		if (material != null) materialNode.addProperty(ISA.HASMATERIAL, material);
		return materialNode;
		/*
		dotCode.append ( "    subgraph cluster_" + nodeId + " { rank = same; color = white; { " + materialId + "; " + nodeId + " }" );
		dotCode.append ( "      " + materialId + "[label = \"" + materialLabel + "\", shape=box, style = filled, color = gold];\n" );
		dotCode.append ( "      " + nodeId + " -> " + materialId + ";\n" );
		dotCode.append ( "      " + nodeId + "[label = \""+ nodeLabel + "\", shape = ellipse, style = filled, color = ivory ];\n" );
		dotCode.append ( "    }\n" );
		*/
	}
	
	/** Works on a single IN/OUT node */

	/** Works on a single node */
	private Resource graphDataNode ( DataNode node , boolean output) throws Exception 
	{
		Resource dataNode = getResource(node, ISA.DataNode);
		Resource data = getResource(node.getData(), ISA.Data);
		if (data != null) dataNode.addProperty(ISA.HASDATA, data);
		return dataNode;
			/*
			dotCode.append ( "    subgraph cluster_" + nodeId + " { rank = same; color = white; { " + dataId + "; " + nodeId + " }" );
			dotCode.append ( "      " + dataId + "[label = \"" + dataLabel + "\", shape=box, style = filled, color = yellow];\n" );
			dotCode.append ( "      " + nodeId + " -> " + dataId + ";\n" );
			dotCode.append ( "      " + nodeId + "[label = \"" + nodeLabel + "\", shape = ellipse, style = filled, color = ivory ];\n" );
			dotCode.append ( "    }\n" );
			*/
	}


}