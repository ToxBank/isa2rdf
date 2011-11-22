package net.toxbank.isa2rdf;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;

import com.hp.hpl.jena.rdf.model.Resource;

public class ISAParser {
	//File dir;
	
	
	public ISAParser() throws Exception {
		
	}
	/*
	public ISAParser() throws Exception {
		this(new ISAObject(ISA.createModel(), prefix, prefixURI));
	}
	*/
	public ISAObject parse(File dir,String name) throws Exception {
	

		final String prefixURI = String.format("http://toxbank.net/isa/%s",name);
		
		ISAObject model = new ISAObject("isa", prefixURI) ;
			
		String[] files = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("a_") && name.endsWith(".txt");
			}
		});
         
		parse(dir,files,false,model,prefixURI);
		
		files = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("s_") && name.endsWith(".txt");
			}
		});
		parse(dir,files,true,model,prefixURI);		
		
		return model;
	}
	
	public void parse(File dir, String[] files,boolean isStudy, final ISAObject model,String prefixURI) throws Exception {
		String tag = isStudy?"study":"assay";
		

		for (int i=0; i < files.length; i++) {
			String file = files[i];
			final String prefix = String.format("%s/%s/%s",prefixURI,tag,file.replace(".txt", ""));
			
			EntryParser parser = null;
			if (isStudy) parser = new ParserStudy(new FileReader(new File(dir,file)),
										file.replace(".txt", "").replace("-","_").trim(),
										//String.format("%s%d",tag,i+1),
										prefix,
										model);
			else parser = new ParserAssay(new FileReader(new File(dir,file)),
										//String.format("%s%d",tag,i+1),
										file.replace(".txt", "").replace("-","_").trim(),
										prefix,
										model);
			
			while (parser.hasNext()) {
				Resource resource = parser.next();
				//System.out.println(tabs==null?"Nothing!":tabs.length==0?"0":tabs[0]);
			}
			parser.close();
			
		}	
		
	}

}
