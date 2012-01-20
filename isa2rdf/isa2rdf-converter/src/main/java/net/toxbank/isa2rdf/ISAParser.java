package net.toxbank.isa2rdf;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;

import net.toxbank.isa.ISA;
import net.toxbank.isa.RowStudy;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;

public class ISAParser {
	//File dir;
	
	protected static String prefix = "http://toxbank.net/isa/";
	
	public ISAParser() throws Exception {
		
	}
	/*
	public ISAParser() throws Exception {
		this(new ISAObject(ISA.createModel(), prefix, prefixURI));
	}
	*/
	public Model parseDir(File dir) throws Exception {
	
		if (!dir.isDirectory()) throw new Exception(String.format("%s is not a directory",dir));
		OntModel model = ISA.createModel(true);
		
		String[] files;
		final String prefixDir = String.format("%s%s/",prefix,dir.getName());
		model.setNsPrefix(dir.getName(), prefixDir);
		//investigation
		files = dir.list(new InvestigationFileNameFilter());
		for (String file:files) System.out.println(file);
		//Studies
		files = dir.list(new StudyFileNameFilter());
		for (String file:files) {
			String filename = file; //file.replace("-", "_");
			String studyPrefix = String.format("%s%s",prefixDir,filename.replace(".txt", ""));
			model.setNsPrefix(filename.replace(".txt", "F"), studyPrefix+"/Factor/");
			model.setNsPrefix(filename.replace(".txt", "C"), studyPrefix+"/Char/");
			model.setNsPrefix(filename.replace(".txt", "S"), studyPrefix+"/Study/");
			model.setNsPrefix(filename.replace(".txt", "E"), studyPrefix+"/entry/");
			model.setNsPrefix(filename.replace(".txt", "n"), studyPrefix+"/node/");
			model.setNsPrefix(filename.replace(".txt", "P"), studyPrefix+"/Protocol/");
			model.setNsPrefix(filename.replace(".txt", ""), studyPrefix+"/");
			parseStudyFile(studyPrefix,new File(dir,file), model);
		}
		//Assays
		files = dir.list(new AssayFileNameFilter());
		//for (String file:files) System.out.println(file);
		return model;
	}
	
	public void parseStudyFile(String prefixDir,File file,OntModel model) throws Exception {
		FileReader reader = new FileReader(file);
		StudyParser sparser = new StudyParser(prefixDir,file.getName(),reader, model);
		while (sparser.hasNext()) {
			RowStudy row = sparser.next();
		}
		reader.close();
	}
	/*
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
	*/
}

class ISAFileNameFilter implements FilenameFilter {
	String prefix = "i_";
	public ISAFileNameFilter(String prefix) {
		this.prefix = prefix;
	}
	@Override
	public boolean accept(File dir, String name) {
		return name.startsWith(prefix) && name.endsWith(".txt");
	}
}

class AssayFileNameFilter extends ISAFileNameFilter {
	
	public AssayFileNameFilter() {
		super("a_");
	}
}

class StudyFileNameFilter extends ISAFileNameFilter {
	
	public StudyFileNameFilter() {
		super("s_");
	}
}

class InvestigationFileNameFilter extends ISAFileNameFilter {
	
	public InvestigationFileNameFilter() {
		super("i_");
	}
}

