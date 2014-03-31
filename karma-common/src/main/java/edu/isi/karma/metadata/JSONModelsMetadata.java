package edu.isi.karma.metadata;

import edu.isi.karma.modeling.ModelingConfiguration;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class JSONModelsMetadata extends KarmaUserMetadata {

	
	public JSONModelsMetadata(Workspace workspace) throws KarmaException
	{
		super(workspace);
	}
	
	@Override
	public void setup() {
	
	}

	@Override
	protected ContextParameter getDirectoryContextParameter() {
		return ContextParameter.JSON_MODELS_DIR;
	}

	@Override
	protected String getDirectoryPath() {
		return ModelingConfiguration.getModelsJsonDir();
	}
	@Override
	public KarmaMetadataType getType() {
		return StandardUserMetadataTypes.JSON_MODELS;
	}
}
