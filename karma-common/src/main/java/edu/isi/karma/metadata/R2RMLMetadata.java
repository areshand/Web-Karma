package edu.isi.karma.metadata;

import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class R2RMLMetadata extends KarmaPublishedMetadata {

	public R2RMLMetadata(Workspace workspace) throws KarmaException {
		super(workspace);
	}
	
	
	@Override
	public void setup() {
	
	}

	@Override
	protected ContextParameter getDirectoryContextParameter() {
		return ContextParameter.R2RML_PUBLISH_DIR;
	}

	@Override
	protected ContextParameter getRelativeDirectoryContextParameter() {
		return ContextParameter.R2RML_PUBLISH_RELATIVE_DIR;
	}
	
	@Override
	protected String getDirectoryPath() {
		return "R2RML/";
	}

	@Override
	public KarmaMetadataType getType() {
		return StandardPublishMetadataTypes.R2RML_MODEL;
	}


	

}
