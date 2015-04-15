package com.udacity.android.student.project.app.data;

/**
 * Created by fsilvesteris
 */
public class StatementBuilder {
	private final StringBuilder sb = new StringBuilder();

	public StatementBuilder() {

	}



	public StringBuilder appendFieldNames(DatabaseField... fields) {

		String delim="";
		for (DatabaseField field : fields) {
			sb.append(delim).append(field.getFieldName());
			delim=", ";
		}
		return sb;
	}


	public StatementBuilder append(String... values) {
		for (String value : values) {
			sb.append(value);
		}
		return this;
	}

	@Override
	public String toString() {
		return sb.toString();
	}
}
