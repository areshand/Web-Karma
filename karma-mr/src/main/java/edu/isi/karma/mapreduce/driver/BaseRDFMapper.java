package edu.isi.karma.mapreduce.driver;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.planning.UserSpecifiedRootStrategy;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;
import edu.isi.karma.rdf.BaseKarma;
import edu.isi.karma.rdf.RDFGeneratorRequest;

public abstract class BaseRDFMapper extends Mapper<Writable, Text, Text, Text> {

	private static Logger LOG = LoggerFactory.getLogger(BaseRDFMapper.class);

	protected BaseKarma karma;
	protected String header = null;
	protected String delimiter = null;
	protected boolean hasHeader = false;
	@Override
	public void setup(Context context) {

		karma = new BaseKarma();
		String inputTypeString = context.getConfiguration().get(
				"karma.input.type");
		String modelUri = context.getConfiguration().get("model.uri");
		String modelFile = context.getConfiguration().get("model.file");
		String baseURI = context.getConfiguration().get("base.uri");
		String contextURI = context.getConfiguration().get("context.uri");
		String rdfGenerationRoot = context.getConfiguration().get("rdf.generation.root");
		String rdfSelection = context.getConfiguration().get("rdf.generation.selection");
		delimiter = context.getConfiguration().get("karma.input.delimiter");
		hasHeader = context.getConfiguration().getBoolean("karma.input.header", false);
		karma.setup("./karma.zip/karma", inputTypeString, modelUri, modelFile, 
				baseURI, contextURI, rdfGenerationRoot, rdfSelection);
	
	}

	@Override
	public void map(Writable key, Text value, Context context) throws IOException,
			InterruptedException {

		String filename = key.toString();
		String contents = value.toString();
		LOG.debug(key.toString() + " started");
		if(hasHeader && header ==null)
		{
			header=contents;
			LOG.debug("found header: " + header);
			return;
		}
		else if(hasHeader && header != null)
		{
			contents = header+"\n" + contents;
		}
		StringWriter sw = new StringWriter();
		KR2RMLRDFWriter outWriter = configureRDFWriter(sw);
		try {
			RDFGeneratorRequest request = new RDFGeneratorRequest("model", filename);
			request.setDataType(karma.getInputType());
			request.setInputData(contents);
			request.setAddProvenance(false);
			request.addWriter(outWriter);
			request.setMaxNumLines(0);
			if(delimiter != null)
			{
				request.setDelimiter(delimiter);
			}
			if(karma.getContextId() != null)
			{
				request.setContextName(karma.getContextId().getName());
			}
			if(karma.getRdfGenerationRoot() != null)
			{
				request.setStrategy(new UserSpecifiedRootStrategy(karma.getRdfGenerationRoot()));
			}
			if (karma.getContextId() != null) {
				request.setContextName(karma.getContextId().getName());
			}
			karma.getGenerator().generateRDF(request);

			String results = sw.toString();
			if (!results.equals("[\n\n]\n")) {
				writeRDFToContext(context, results);
				
			}
			else
			{
				LOG.info("RDF is empty! ");
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Unable to generate RDF: " + e.getMessage());
			throw new IOException();
		}
		LOG.debug(key.toString() + " finished");
	}

	protected abstract KR2RMLRDFWriter configureRDFWriter(StringWriter sw);

	protected abstract void writeRDFToContext(Context context, String results)
			throws IOException, InterruptedException;

}