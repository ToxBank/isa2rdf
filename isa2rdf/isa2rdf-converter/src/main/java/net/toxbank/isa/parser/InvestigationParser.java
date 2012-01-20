package net.toxbank.isa.parser;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import net.toxbank.isa.AStudy;
import net.toxbank.isa.AnAssay;
import net.toxbank.isa.ColumnHeader;
import net.toxbank.isa.RowAssay;
import net.toxbank.isa.RowStudy;
import net.toxbank.isa.TemplateAssay;
import net.toxbank.isa.TemplateStudy;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;

public class InvestigationParser<E> extends TabsParser<E> {
	protected String prefixDir;
	protected String name;
	protected OntModel model;
	protected TemplateStudy tstudy;
	protected TemplateAssay tassay;
	protected AStudy study;
	protected AnAssay assay;
	protected File dir;
	protected static String prefix = "http://toxbank.net/isa/";
	
	public InvestigationParser(File dir,String prefixDir,String name, Reader in, OntModel model) {
		super(in);
		this.model = model;
		this.prefixDir = prefixDir;
		this.name = name;
		this.dir = dir;
	}

	@Override
	protected ColumnHeader<Resource>[] readHeader() throws Exception {
		return super.readHeader();
	}
	@Override
	protected E transform(String[] tabs) throws Exception {
		//for (String tab:tabs) {
			//System.out.print(tab);
			//System.out.print("\t");
			
		//}
		//System.out.println("");
		if (tabs.length==0) return null;
		if ("STUDY".equals(tabs[0])) {
			tstudy = null; tassay = null; study = null; assay = null;
		} else if ("Study File Name".equals(tabs[0])) {
			if (tabs.length>1) {
				String filename = tabs[1];
				String studyPrefix = String.format("%s%s",prefixDir,filename.replace(".txt", ""));
				TemplateStudy tstudy = new TemplateStudy(studyPrefix,filename,model);
				study = parseStudyFile(tstudy,studyPrefix, new File(dir,filename));
			}
		} else if ("Study Assay File Name".equals(tabs[0])) {
			for (int i=1; i < tabs.length; i++) {
				String filename = tabs[i];
				String assayPrefix = String.format("%s%s",prefixDir,filename.replace(".txt", ""));
				tassay = new TemplateAssay(assayPrefix,filename,model);
				assay = parseAssayFile(tassay, assayPrefix, new File(dir,filename));
				study.addAssay(assay);
			}
		}
			
		return null;
	}
	
	public AStudy parseStudyFile(TemplateStudy tstudy, String studyPrefix,File file) throws Exception {
		
		String filename = file.getName();
		FileReader reader = new FileReader(file);
		model.setNsPrefix(filename.replace(".txt", "_F"), studyPrefix+"/Factor/");
		model.setNsPrefix(filename.replace(".txt", "_C"), studyPrefix+"/Char/");
		model.setNsPrefix(filename.replace(".txt", "_S"), studyPrefix+"/Study/");
		model.setNsPrefix(filename.replace(".txt", "_E"), studyPrefix+"/entry/");
		model.setNsPrefix(filename.replace(".txt", "_n"), studyPrefix+"/node/");
		model.setNsPrefix(filename.replace(".txt", "_P"), studyPrefix+"/Protocol/");
		model.setNsPrefix(filename.replace(".txt", ""), studyPrefix+"/");
		StudyParser sparser = new StudyParser(tstudy,reader);
		while (sparser.hasNext()) {
			RowStudy row = sparser.next();
		}
		reader.close();
		return sparser.getStudy();
	}
	
	public AStudy  parseStudyFile(String studyPrefix,File file,OntModel model) throws Exception {
		return parseStudyFile(new TemplateStudy(studyPrefix,name,model),studyPrefix,file);
	}
	
	public AnAssay parseAssayFile(TemplateAssay tassay, String assayPrefix,File file) throws Exception {
		String filename = file.getName();
		model.setNsPrefix(filename.replace(".txt", "_F"), assayPrefix+"/Factor/");
		model.setNsPrefix(filename.replace(".txt", "_C"), assayPrefix+"/Char/");
		model.setNsPrefix(filename.replace(".txt", "_A"), assayPrefix+"/Assay/");
		model.setNsPrefix(filename.replace(".txt", "_E"), assayPrefix+"/entry/");
		model.setNsPrefix(filename.replace(".txt", "_n"), assayPrefix+"/node/");
		model.setNsPrefix(filename.replace(".txt", "_P"), assayPrefix+"/Protocol/");
		model.setNsPrefix(filename.replace(".txt", "_L"), assayPrefix+"/File/");
		model.setNsPrefix(filename.replace(".txt", "_M"), assayPrefix+"/Comment/");
		model.setNsPrefix(filename.replace(".txt", ""), assayPrefix+"/");
		FileReader reader = new FileReader(file);
		AssayParser aparser = new AssayParser(tassay,reader);
		while (aparser.hasNext()) {
			RowAssay row = aparser.next();
		}
		reader.close();
		return aparser.getAssay();
	}

	public AnAssay parseAssayFile(String assayPrefix,File file,OntModel model) throws Exception {
		return parseAssayFile(new TemplateAssay(assayPrefix,name,model),assayPrefix,file);
	}
	

	
	/*
			//Studies
			files = dir.list(new StudyFileNameFilter());
			for (String file:files) {
				String filename = file; //file.replace("-", "_");
				String studyPrefix = String.format("%s%s",prefixDir,filename.replace(".txt", ""));
				parseStudyFile(studyPrefix,new File(dir,file), model);
			}
			//Assays
			files = dir.list(new AssayFileNameFilter());
			for (String file:files) {
				String filename = file; //file.replace("-", "_");
				String assayPrefix = String.format("%s%s",prefixDir,filename.replace(".txt", ""));

				parseAssayFile(assayPrefix,new File(dir,file), model);
			}
		*/
		
}
