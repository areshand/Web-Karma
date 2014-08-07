package edu.isi.karma.controller.command.selection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.python.core.PyCode;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.er.helper.CloneTableUtils;
import edu.isi.karma.er.helper.PythonRepository;
import edu.isi.karma.er.helper.PythonTransformationHelper;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;

public class MiniSelection extends Selection {

	private String pythonCode;
	
	private static Logger logger = LoggerFactory
			.getLogger(MiniSelection.class);

	MiniSelection(Workspace workspace, String worksheetId, String hTableId,
			String name, String pythonCode) throws IOException {
		super(workspace, worksheetId, hTableId, name);
		this.pythonCode = pythonCode;
		populateSelection();
	}
	
	public void updateSelection() throws IOException {
		if (this.status == SelectionStatus.UP_TO_DATE)
			return;
		evalColumns.clear();
		for (Entry<Row, Boolean> entry : this.selectedRowsCache.entrySet()) {
			Row key = entry.getKey();
			PythonInterpreter interpreter = new PythonInterpreter();
			entry.setValue(evaluatePythonExpression(key, getCompiledCode(pythonCode, interpreter), interpreter));
		}
		this.status = SelectionStatus.UP_TO_DATE;
	}
	
	public void addInputColumns(String hNodeId) {
		evalColumns.add(hNodeId);
	}
	
	private void populateSelection() throws IOException {
		List<Table> tables = new ArrayList<Table>();
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		CloneTableUtils.getDatatable(worksheet.getDataTable(), workspace.getFactory().getHTable(hTableId), tables, SuperSelectionManager.DEFAULT_SELECTION);
		PythonInterpreter interpreter = new PythonInterpreter();
		PyCode code = getCompiledCode(pythonCode, interpreter);
		for (Table t : tables) {
			for (Row r : t.getRows(0, t.getNumRows(), SuperSelectionManager.DEFAULT_SELECTION)) {
				selectedRowsCache.put(r, evaluatePythonExpression(r, code, interpreter));
			}
		}
	}
	
	private boolean evaluatePythonExpression(Row r, PyCode code, PythonInterpreter interpreter) {
		evalColumns.clear();
		ArrayList<Node> nodes = new ArrayList<Node>(r.getNodes());
		Node node = nodes.get(0);
		interpreter.set("nodeid", node.getId());
		PyObject output = interpreter.eval(code);
		return PythonTransformationHelper.getPyObjectValueAsBoolean(output);
	}
	
	private PyCode getCompiledCode(String pythonCode, PythonInterpreter interpreter) throws IOException {
		
		String trimmedTransformationCode = pythonCode.trim();
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		if (trimmedTransformationCode.isEmpty()) {
			trimmedTransformationCode = "return False";
		}
		String transformMethodStmt = PythonTransformationHelper
				.getPythonTransformMethodDefinitionState(worksheet,
						trimmedTransformationCode);


		logger.debug("Executing PySelection\n" + transformMethodStmt);

		// Prepare the Python interpreter
		PythonRepository repo = PythonRepository.getInstance();
		repo.initializeInterperter(interpreter);
		repo.importUserScripts(interpreter);
		
		repo.compileAndAddToRepositoryAndExec(interpreter, transformMethodStmt);
		
		interpreter.set("workspaceid", workspace.getId());
		interpreter.set("command", this);
		return repo.getTransformCode();
	}	

}
