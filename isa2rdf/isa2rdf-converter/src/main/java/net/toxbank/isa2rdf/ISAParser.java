package net.toxbank.isa2rdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.OutputStream;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;

public class ISAParser {
	File dir;
	final OntModel model;
	
	public ISAParser(OntModel model) throws Exception {
		this.model = model;
	}
	
	public ISAParser() throws Exception {
		this(ISA.createModel());
	}
	
	public void parse(File dir) throws Exception {
		this.dir = dir;
		
		String[] files = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("a_") && name.endsWith(".txt");
			}
		});
         
		parse(dir,files,false,model);
		
		files = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("s_") && name.endsWith(".txt");
			}
		});
		parse(dir,files,true,model);		
		
	}
	
	public void parse(File dir, String[] files,boolean isStudy, final OntModel model) throws Exception {
		String tag = isStudy?"study":"assay";
		
		model.setNsPrefix("",ISA.NS);
		for (int i=0; i < files.length; i++) {
			String file = files[i];
			final String prefix = String.format("http://toxbank.net/%s/%s",tag,file.replace(".txt", ""));
			
			EntryParser parser = null;
			if (isStudy) parser = new ParserStudy(new FileReader(new File(dir,file)),
										String.format("%s%d",tag,i+1),prefix,model);
			else parser = new ParserAssay(new FileReader(new File(dir,file)),
										String.format("%s%d",tag,i+1),prefix,model);
			
			while (parser.hasNext()) {
				Resource resource = parser.next();
				//System.out.println(tabs==null?"Nothing!":tabs.length==0?"0":tabs[0]);
			}
			parser.close();
			
		}	
		
	}

}
