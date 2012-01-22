package org.isa2rdf.cli;


import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Unit test for simple App.
 */
public class AppTest  {

	@org.junit.Test

	public void testRDF() throws Exception {

		String filesPath = "D:\\src-pol\\isa2rdf\\isa2rdf\\isa2rdf-converter\\src\\test\\resources\\BII-S-3";
		IsaClient cli = new IsaClient();
		Model model = cli.process(filesPath);

		File out = new File(new File(filesPath),"isatab.owl");
		FileOutputStream output = new FileOutputStream(out);
		IsaClient.writeStream(model, output, "application/rdf+xml", true);
		output.close();

		Model test = ModelFactory.createDefaultModel();
		FileReader reader = new FileReader(out);
		test.read(reader,null);
		reader.close();
		
		out = new File(new File(filesPath),"isatab.n3");
		output = new FileOutputStream(out);
		IsaClient.writeStream(model, output, "text/n3", true);
		output.close();


		
        /*
        
        Set<Class<? extends Identifiable>> types = store.types();
       
        for (Class<? extends Identifiable> t : types) {
        	htmlwriter.write("<table style='border-style:dotted'");
        	htmlwriter.write(String.format("<thead><caption>%s</caption><thead><tbody>",t));
            for (Identifiable c : store.values(t)) try {
            	Object id = c.getId();
            	if (c instanceof DataNode) id = "DN_"+id;
            	if (c instanceof MaterialNode) id = "MN_"+id;
            	htmlwriter.write(String.format("<tr><th>%s</th><td>%s</td></tr>",id,c));
            } catch (Exception x) {
            	x.printStackTrace();
            }
            htmlwriter.write("</tbody></table>");
        }
        /*
        objects.addAll(store.types());
        htmlwriter.write("<table>");
        for (Identifiable i : objects) {
        	htmlwriter.write(String.format("<tr><th>%s</th><td>%s</td></tr>",i.getId(),i.getClass().getName()));
        }
        */
      
		/*
		FormatInstance investigationFormatInstance = isatabInstance.getFormatInstance("investigation");
		assertNotNull("Sigh! No investigation format loaded", investigationFormatInstance);
		List<SectionInstance> investigationInstances = investigationFormatInstance.getSectionInstances("investigation");
		assertNotNull("Ouch! No investigation instances in investigation format", investigationInstances);
		assertEquals("Ouch! Bad number of investigations in investigation format ", 1, investigationInstances.size());
		SectionInstance investigationInstance = investigationInstances.get(0);
		assertEquals("Bad value for Investigation Title", "A Test Investigation, made for testing purposes",
				investigationInstance.getString(0, "Investigation Title")
		);

		List<SectionInstance> studyInstances = investigationFormatInstance.getSectionInstances("study");
		assertNotNull("Ouch! No study instances in investigation format", studyInstances);
		assertEquals("Ouch! Bad number of study in investigation format ", 1, studyInstances.size());

		List<SectionInstance> designInstances = investigationFormatInstance.getSectionInstances("studyDesigns");
		assertNotNull("Ouch! No study designs instances in investigation format", designInstances);
		assertEquals("Ouch! Bad number of study designs in investigation format ", 1, designInstances.size());


		List<FormatInstance> study1FormatInstances = isatabInstance.getFormatInstances("study_samples");
		assertNotNull("Sigh! No study format loaded", study1FormatInstances);
		assertEquals("Ops! Bad # of study formats loaded", 1, study1FormatInstances.size());
		SectionInstance studySampleInstance = study1FormatInstances.get(0).getSectionInstance("study_samples");
		assertNotNull("Oh no! No samples in the study instance", studySampleInstance);
		List<Record> sampleRecords = studySampleInstance.getRecords();
		assertNotNull("Arg! No records in study sample", sampleRecords);
		assertEquals("Oh no! Bad # of samples in the study instance", 12, sampleRecords.size());
		assertEquals("Oh no! Sample value retrieval failed", "Study1.animal4.liver",
				studySampleInstance.getString(3, "Sample Name")
		);


		List<FormatInstance> txInstances = isatabInstance.getFormatInstances("transcriptomics_assay");
		assertNotNull("Urp! No transcriptomics file loadd", txInstances);
		assertEquals("Ops! Bad # of TX formats loaded", 1, txInstances.size());
		SectionInstance txPipelineInstance = txInstances.get(0).getSectionInstance("transcriptomics_pipeline");
		assertNotNull("Oh no! No transcriptomics pipeline in the TX file", txPipelineInstance);
		List<Record> txRecords = txPipelineInstance.getRecords();
		assertNotNull("Arg! No records in TX file", txRecords);
		assertEquals("Oh no! Bad # of pipeline records in the TX file", 12, txRecords.size());
		assertEquals("Oh no! Pipeline value retrieval failed", "scan3.normalized",
				txPipelineInstance.getString(2, "Normalization Name")
		);


		List<FormatInstance> msInstances = isatabInstance.getFormatInstances("ms_spec_assay");
		assertNotNull("Urp! No MS/SPEC file loadd", msInstances);
		assertEquals("Ops! Bad # of MS/SPEC formats loaded", 1, msInstances.size());
		SectionInstance msPipelineInstance = msInstances.get(0).getSectionInstance("ms_spec_pipeline");
		assertNotNull("Oh no! No MS/SPEC pipeline in the TX file", msPipelineInstance);
		List<Record> msRecords = msPipelineInstance.getRecords();
		assertNotNull("Arg! No records in MAS/SPEC file", msRecords);
		assertEquals("Oh no! Bad # of pipeline records in the MS/SPEC file", 12, msRecords.size());
		assertEquals("Oh no! Pipeline value retrieval failed", "20 ppm", msPipelineInstance.getString(3, 6));


		out.println("\n\n_________ /end: ISATAB Loading Test __________\n\n\n");
		*/
	}
	
	  
}
