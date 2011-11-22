package net.toxbank.isa2rdf;

import java.io.Reader;

import com.hp.hpl.jena.rdf.model.Resource;

public class ParserInvestigation extends TabsParser<Resource> {

	public ParserInvestigation(Reader in) {
		super(in);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Resource transform(String[] tabs) throws Exception {
		for (String tab : tabs) {
			System.out.println(String.format("%s\t",tab));
			
		}
		return null;
	}

}
