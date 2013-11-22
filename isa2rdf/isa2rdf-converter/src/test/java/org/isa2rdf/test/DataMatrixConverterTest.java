package org.isa2rdf.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Hashtable;

import org.isa2rdf.datamatrix.DataMatrixConverter;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class DataMatrixConverterTest {
	@Test
	public void test() throws Exception {
		Hashtable<String, String> lookup = new Hashtable<String, String>();
		lookup.put("20-5 vs 5-5", "http://example.org/X1");
		lookup.put("5-15 vs 5-5", "http://example.org/X2");
		lookup.put("20-0 vs 0-0", "http://example.org/X3");
		DataMatrixConverter conv = new DataMatrixConverter("http://onto.toxbank.net/isa/bii/data_types/microarray_derived_data",
									lookup,
									"http://example.org/investigation/123");
		//String file = "D:/src-toxbank/isa-tab-files/NOTOX-APAP-Tx/NOTOX_PILOT_APAP_DerivedTxDataMatrix.txt";
		String file = "D:/src-toxbank/isa-tab-files/NOTOX-APAP-Tx 12-nov-2013/NOTOX-APAP-Tx/NOTOX_PILOT_APAP_DerivedTxDataMatrix.txt";
		File rdf = File.createTempFile("isa",".rdf");
		rdf.deleteOnExit();
		System.out.println(rdf.getAbsolutePath());
		FileOutputStream out = null;
		FileReader fr = null;
		Model model = null;
		try {
			fr = new FileReader(file);
			out = new FileOutputStream(rdf);
			conv.writeRDF(fr,"test", 5, out);


		} catch (Exception x) {
			throw x;
		} finally {
			try {fr.close();} catch (Exception x) {}
			try {out.close();} catch (Exception x) {}
		}
		//now check if rdf is ok
		FileInputStream in = null;
		try {
			in = new FileInputStream(rdf);
			model = ModelFactory.createDefaultModel();
  			model.read(in, "http://example.org", "RDF/XML");
  			model.write(System.out,"N3");
  			DataMatrixConverter.getDataMatrix(model);
		} catch (Exception x) {
			throw x;
		} finally {
			in.close();
		}
		
	}
}
