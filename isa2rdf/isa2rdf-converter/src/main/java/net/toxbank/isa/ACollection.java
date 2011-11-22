package net.toxbank.isa;


import com.hp.hpl.jena.ontology.Individual;

public abstract class ACollection<T extends TemplateCollection> extends AnyISAObject<Individual> {
	//this is perhaps redundant, as the superclass is known - but what if > 1 ?
	T template;
	public ACollection(String uri,T template) throws Exception {
		super(template.createInstance(uri));
		this.template = template;
	}

	public abstract ARow addRow(String uri) throws Exception ;
	

}
