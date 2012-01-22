package org.isa2rdf.cli;

import java.util.Collection;

import uk.ac.ebi.bioinvindex.model.Accessible;
import uk.ac.ebi.bioinvindex.model.Data;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.model.Material;
import uk.ac.ebi.bioinvindex.model.Protocol;
import uk.ac.ebi.bioinvindex.model.processing.DataAcquisition;
import uk.ac.ebi.bioinvindex.model.processing.DataNode;
import uk.ac.ebi.bioinvindex.model.processing.DataProcessing;
import uk.ac.ebi.bioinvindex.model.processing.MaterialNode;
import uk.ac.ebi.bioinvindex.model.processing.MaterialProcessing;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;


public abstract class RDFGenerator<NODE extends Identifiable,MODEL extends Model> {
	
	protected Collection<NODE> objects;
	private MODEL model;
	protected String prefix;
	protected long tempIdCounter=1;
	
	public long getTempIdCounter() {
		return tempIdCounter;
	}

	public void setTempIdCounter(long tempIdCounter) {
		this.tempIdCounter = tempIdCounter;
	}

	public MODEL getModel() {
		return model;
	}

	public void setModel(MODEL model) {
		this.model = model;
	}

	public RDFGenerator (  String prefix, Collection<NODE> objects, MODEL model) {	
		this.objects = objects;
		this.prefix = prefix;
		setModel(model);
	}

	public abstract MODEL createGraph () throws Exception;
	public MODEL createGraph ( String fileName ) throws Exception {
		return createGraph();
	}
	
	
	protected String getURI(Identifiable node) throws Exception {
		String p = "ISA_";
		if ( node instanceof MaterialNode ) p = "MN_";
		else if ( node instanceof DataNode) p = "DN_";		
		else if ( node instanceof MaterialProcessing ) p = "MN_"; 
		else if ( node instanceof DataAcquisition ) p = "DAN_"; 
		else if ( node instanceof DataProcessing ) p = "DPN_";
		else if ( node instanceof Material ) p = "M_";
		else if ( node instanceof Data ) p = "D_";
		else if ( node instanceof Protocol ) p = "P_";
		if (node.getId()==null) node.setId(tempIdCounter++);
		return String.format("%s/%s%d",prefix,p,node.getId());
	}

	protected Resource getResource(Identifiable node,Resource clazz)  throws Exception {
		if (node==null) return null;
		Resource resource = model.createResource(getURI(node), clazz);
		if (resource instanceof Accessible) {
			resource.addProperty(ISA.hasAccessionID, ((Accessible) node).getAcc());
		}
		return resource;
	}
}
