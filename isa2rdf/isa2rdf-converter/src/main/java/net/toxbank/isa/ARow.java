package net.toxbank.isa;


import java.net.URLEncoder;

import net.toxbank.isa2rdf.ColumnHeader;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

public class ARow<CT extends TemplateCollection,T extends TemplateRow<CT>> extends AnyISAObject<Individual> {
	protected T template;
	public ARow(String uri,T template) throws Exception {
		super(template.createInstance(uri));
		this.template = template;
	}
	protected Property getPartOfEntryProperty() {
		return  ISAObjectProperty.isPartOfEntry.createProperty(getModel());
	}

	public void parse(ColumnHeader[] header,String[] tabs) throws Exception {
		
		for (int i=0; i < tabs.length; i++) tabs[i] = tabs[i].replace("\"","");
		
		Individual node = null;
		for (int i=0; i < tabs.length; i++) {
			if (header[i].getResource()==null) continue;

			Individual vi = null;
			String unit = null;
			String term = null;
			try {
				unit = header[i].getUnitValue(tabs); 
				term = header[i].getTerm(tabs); 
			} catch (Exception x) {}
	
			if (header[i].getResource() instanceof TemplateNode) { //named node
				String uri = String.format("%s/Column/C%d",getResource().getURI(),i);
				node = ((TemplateNode)header[i].getResource()).createInstance(uri);
				Property partOf = getPartOfEntryProperty();
				getModel().add(node,partOf,resource);
			} else if (header[i].getProtocolClass() != null) {
				
				String uri = String.format("http://protocol.example.com/PR/%s",URLEncoder.encode(tabs[i]));
				Individual pi = getModel().getIndividual(uri);
				if (pi ==null) {
					pi = getModel().createIndividual(uri,header[i].getProtocolClass());
				}
				getModel().add(pi,RDFS.label,tabs[i]);
				getModel().add(pi,getPartOfEntryProperty(),resource);
				node = pi;
				
			} else if (header[i].getFileProperty() != null) {
				if (node!=null)  {
					
					String uri = String.format("%s/V/%s",header[i].getFileProperty().getURI(),URLEncoder.encode(tabs[i]));
					vi = getModel().getIndividual(uri);
					if (vi == null) 
						vi = getModel().createIndividual(uri,ISAClass.Dataset.createOntClass(getModel()));
					getModel().add(node, header[i].getFileProperty(), vi);

				}
			} else if (header[i].getObjectProperty() != null) {
				if (node!=null) {
					String uri = String.format("%s/V/%s",header[i].getObjectProperty().getURI(),URLEncoder.encode(tabs[i]));
					vi = getModel().getIndividual(uri);
					if (vi == null) 
						vi = getModel().createIndividual(uri,ISAClass.Value.createOntClass(getModel()));
					getModel().add(node, header[i].getObjectProperty(), vi);
					if (unit!=null)
						getModel().add(vi,ISADataProperty.hasUnit.createProperty(getModel()),getModel().createTypedLiteral(unit));
					if (term!=null) {
						//if (term.startsWith("CHEBI:")) {
						//	String chebi = getModel().getNsPrefixURI("CHEBI");
						//	getModel().add(vi,OWL.sameAs,getModel().createResource(term.replace("CHEBI:", chebi)));
						//} else
							getModel().add(vi,RDFS.seeAlso,getModel().createTypedLiteral(term));
					}
					
				}
			} else if (header[i].getDataProperty() != null) {
				if (node!=null) {
					Literal l =  getModel().createTypedLiteral(tabs[i]);
					getModel().add(node, header[i].getDataProperty(),l);
				}
				 
			}
			
			if (vi!=null) {
				Property partOf = ISAObjectProperty.isPartOfEntry.createProperty(getModel());
				getModel().add(node,partOf,resource);
				getModel().add(vi,RDFS.label,tabs[i]);
			}
		}
		
	
	}		
	
	protected Resource findInstanceWithValue(ObjectProperty p, Literal value) {
		
		
		
		Resource res = null;
		ResIterator it = getModel().listResourcesWithProperty(RDFS.label, value);
		while (it.hasNext()) {
			res = it.next();
			System.out.println(res);
		}
		it.close();
		return res;
	}
	/*
	public Individual addNodeAttribute(ColumnHeader header,String value,
			String unit, String term,
			Individual node ) throws Exception {
		if (header.getTermREF()!=null) {
			
		}
		String uri = generateURI(model, prefixURI, ISAClass.Value, null, value);
		Individual vi = model.getIndividual(uri);
		model.add(node, header.getObjectProperty(), vi);
		
		if (unit!=null) {
			model.add(vi,ISA.ISADataProperty.hasUnit.createProperty(model),model.createTypedLiteral(unit));
		}
		if (term!=null) {
			model.add(vi,RDFS.comment,model.createTypedLiteral(term));
		}
		return vi;
	}	
	*/
	
	/*
	public Individual addNamedNode(ColumnHeader header, String name, Individual row) throws Exception {
		//String uri = String.format("%s/%s",prefix,tabs[i]);
		String uri = generateURI(model, prefixURI, ISAClass.NamedNode, header, name);
		Individual node = model.createIndividual(uri,header.getNamedNodeClass());
		Property partOf = ISA.ISAObjectProperty.isPartOfEntry.createProperty(model);
		model.add(node,partOf,row);
		return node;
	}	
	
	public Individual addProtocolNode(ColumnHeader header,String value, Individual row ) throws Exception {
		String uri = generateURI(model, prefixURI, ISAClass.Protocol, header,value);
		Individual node = model.createIndividual(uri,header.getProtocolClass());
		model.add(node,RDFS.label,value);
		Property partOf = ISA.ISAObjectProperty.isPartOfEntry.createProperty(model);
		model.add(node,partOf,row);
		return node;
	}			
	public Individual addFileNode(String prefixURI,ColumnHeader header,String value, Individual node ) throws Exception {
		String uri = generateURI(model, prefixURI, ISAClass.ValueFile, null,value);
		Individual vi = model.getIndividual(uri);
		model.add(node, header.getFileProperty(), vi);
		return vi;
	}


	public Literal addNodeDataAttribute(String prefixURI,ColumnHeader header,String value,Individual node ) throws Exception {
		Literal l =  model.createTypedLiteral(value);
		model.add(node, header.getDataProperty(),l);
		return l;
	}	
	*/
}