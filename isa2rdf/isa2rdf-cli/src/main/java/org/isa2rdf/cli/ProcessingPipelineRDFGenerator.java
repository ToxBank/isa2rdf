package org.isa2rdf.cli;

import java.util.Collection;

import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.model.processing.DataAcquisition;
import uk.ac.ebi.bioinvindex.model.processing.DataNode;
import uk.ac.ebi.bioinvindex.model.processing.DataProcessing;
import uk.ac.ebi.bioinvindex.model.processing.MaterialNode;
import uk.ac.ebi.bioinvindex.model.processing.MaterialProcessing;
import uk.ac.ebi.bioinvindex.model.processing.Node;
import uk.ac.ebi.bioinvindex.model.processing.Processing;
import uk.ac.ebi.bioinvindex.model.processing.ProtocolApplication;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

public class ProcessingPipelineRDFGenerator<NODE extends Identifiable>  extends RDFGenerator<NODE,Model>{

	/**
	 * I will graph all the instances of {@link #objects} which are {@link Processing} (and nodes, materials, etc.).
	 * WARNING: I need to assign a temporary IDs to the objecs, via {@link Identifiable#setId(Long)}, BE AWARE that
	 * the objects are changed if their ID is null. Otherwise ENSURE the IDs returned by {@link Identifiable#getId()}
	 * are distinct.
	 *
	 */
	public ProcessingPipelineRDFGenerator ( String prefix, Collection<NODE> objects, Model model) {
		super(prefix,objects,model);
	}
	
	public ProcessingPipelineRDFGenerator (  String prefix,  Collection<NODE> objects ) {
		this(prefix, objects,ModelFactory.createDefaultModel());
		model.setNsPrefix( "", prefix+"/" );
		model.setNsPrefix( "isa", ISA.URI );
		model.setNsPrefix( "owl", OWL.NS );
		model.setNsPrefix( "dc", DC.NS );
		model.setNsPrefix( "dcterms", DCTerms.NS );
		model.setNsPrefix("xsd", XSDDatatype.XSD+"#");
		ISA.init(model);
	}

	/**
	 * Creates the DOT string corresponding to the graph of {@link Processing} objects in the collection I manage
	 *
	 */
	public Model createGraph () throws Exception
	{
	
		for ( NODE object: objects )
		{
			if ( object == null ) continue;
			if ( object instanceof MaterialProcessing )
				graphProcessing ( (MaterialProcessing) object );
			else if ( object instanceof DataAcquisition )
				graphProcessing ( (DataAcquisition) object );
			else if ( object instanceof DataProcessing )
				graphProcessing ( (DataProcessing) object );
		}

		return model;
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
			Resource protoAppNode = getResource(protoApp, resource);
			processingNode.addProperty(ISA.APPLIESPROTOCOLS, protoAppNode);
			
			Resource protocolNode = getResource(protoApp.getProtocol(), ISA.Protocol);
			protocolNode.addLiteral(RDFS.label, protoApp.getProtocol ().getName ());
			
			protoAppNode.addProperty(ISA.HASPROTOCOL, protocolNode);
			//for (ParameterValue paramValue: protoApp.getParameterValues()) {
			//	paramValue.get
			//}
			//TODO
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