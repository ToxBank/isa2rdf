package net.toxbank.isa;

import net.toxbank.isa2rdf.ColumnHeader;
import net.toxbank.isa2rdf.ISA;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TemplateAssay extends TemplateCollection {
	public TemplateAssay(OntModel model) {
		this(null,null,model);
	}
	public TemplateAssay(String uri,String name,OntModel model) {
		super(model.createClass(uri));
		OntClass assay = ISA.ISAClass.Assay.createOntClass(model);
		getModel().add(resource,RDFS.subClassOf,assay);
		
		if (name != null) model.add(resource,RDFS.label,name);
	}
	protected TemplateAssay(String uri,OntClass clazz) {
		super(uri,clazz);
	}
	public TemplateAssay createSubclass(String uri) throws Exception {
		OntModel model = ((OntModel)resource.getModel());
		OntClass subclass = uri==null?model.createClass():model.createClass(uri);
		model.add(subclass,RDFS.subClassOf,resource);
		return new TemplateAssay(uri,subclass);
	}
	public TemplateRowAssay createRowTemplate() throws Exception {
		return new TemplateRowAssay(this);
	}

	@Override
	public TemplateRow createRowTemplate(ColumnHeader[] headers, String uri)
			throws Exception {
		return new TemplateRowAssay(uri,this);
	}
	
	
	@Override
	protected TemplateNode createNode(ColumnHeader header,String uri) throws Exception {
		return new TemplateNodeAssay(header, uri, (TemplateRowAssay)rowTemplate);
	}
	
	
	public AnAssay parse(ColumnHeader[] headers,String[][] tabs) throws Exception {
		AnAssay assay = new AnAssay(String.format("%s/Assay/A1",getResource().getURI()),this);
		for (int i=0; i < tabs.length;i++) {
			RowAssay row = assay.addRow(String.format("%s/Row/R%d", assay.getResource().getURI(),i+1));
			row.parse(headers,tabs[i]);
			
		}
		return assay;
	}	
	/*
	@Override
	public OntClass addProtocolNodeClass(ColumnHeader header, String uri) throws Exception {
		OntClass p = super.addProtocolNodeClass(header, uri);
		getModel().add(p,RDFS.subClassOf,ISA.ISAClass.AssayNode.createOntClass(getModel()));
		return p;
	}
	*/
}
