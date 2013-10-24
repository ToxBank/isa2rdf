package org.isa2rdf.datamatrix;

public interface IRowProcessor<ROW> {
	ROW process(ROW row) throws Exception;
	void footer(ROW row) throws Exception;
	void header(ROW row) throws Exception;
}
