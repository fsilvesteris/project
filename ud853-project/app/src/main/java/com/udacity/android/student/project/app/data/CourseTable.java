package com.udacity.android.student.project.app.data;

import android.database.Cursor;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.System.*;

/**
 * Maintaining field indices spread about in source code is a somewhat error prone task.
 * Whilst the following is not by any means a definitive solution, it does hide and keep all the indices in
 * one place where it is easier to maintain.
 * Instead of exposing the indices, only the enumerated types are exposed.  This does introduce a flaw in that due
 * to the common pool of member names across enumerations, any code that references the names should ensure that
 * the member from the correct enumeration is used for a given cursor, as the underlying index that is referenced via
 * the member is different.
 * <p/>
 * So, whilst this approach may be somewhat unorthodox, and there is a small upfront coding effort, by removing the
 * need to expose field indices, it is at least a partial solution.
 * KY Apr-2015.
 * <p/>
 * Created by fsilvesteris
 */
public class CourseTable implements BaseColumns {

	private static final String TABLE_NAME = "course";

	public enum CourseTableColumns implements DatabaseField {

		PROVIDER(1), TYPE(2), CODE(3), TITLE(4), SUBTITLE(5), LEVEL(6), HOMEPAGE(7), SHORT_SUMMARY(8),SYLLABUS(9);

		private final String fieldName;

		private final int fieldIndex;

		private final String definition;

		private static final String[] FIELD_NAMES = new String[values().length];

		private CourseTableColumns(int fieldIndex) {
			this(fieldIndex, null, null);
		}

		private CourseTableColumns(int fieldIndex, String fieldName, String definition) {
			this.fieldIndex = fieldIndex;
			this.fieldName = fieldName == null ? name().toLowerCase() : fieldName;
			this.definition = definition == null ? "TEXT NOT NULL" : definition;
		}

		public String getFieldName() {
			return fieldName;
		}

		public int getFieldIndex() {
			return fieldIndex;
		}

		public String getFieldDefinition() {
			return definition;
		}

		public String getString(Cursor cursor)
		{
			return cursor.getString(fieldIndex);
		}

		static {
			String[] fields = getFieldNameFrom(values(), false);
			arraycopy(fields, 0, FIELD_NAMES, 0, fields.length);
		}


	}


	public enum CatalogCursor implements CursorField {
		CODE(1), TITLE(2), SUBTITLE(3), SHORT_SUMMARY(4);

		private final int fieldIndex;

		private static final String[] CURSOR_FIELDS = new String[values().length + 1];

		private CatalogCursor(int fieldIndex) {
			this.fieldIndex = fieldIndex;
		}

		public int getFieldIndex() {
			return fieldIndex;
		}

		public String getString(Cursor cursor)
		{
			return cursor.getString(fieldIndex);
		}

		public String getFieldName() {
			return CourseTableColumns.valueOf(name()).getFieldName();
		}

		static {
			String[] fields = getFieldNameFrom(values(), true);
			arraycopy(fields, 0, CURSOR_FIELDS, 0, fields.length);
		}

		public static String[] getFieldNames() {
			return CURSOR_FIELDS;
		}

	}

	public enum CourseTableCursor implements CursorField {
		PROVIDER(1), TYPE(2), CODE(3), TITLE(4), SUBTITLE(5), LEVEL(6), HOMEPAGE(7),SHORT_SUMMARY(8);

		private final int fieldIndex;

		private static final String[] CURSOR_FIELDS = new String[values().length + 1];

		private CourseTableCursor(int fieldIndex) {
			this.fieldIndex = fieldIndex;
		}

		public int getFieldIndex() {
			return fieldIndex;
		}

		public String getString(Cursor cursor)
		{
			return cursor.getString(fieldIndex);
		}


		public static String[] getFieldNames() {
			return CURSOR_FIELDS;
		}

		static {
			String[] fields = getFieldNameFrom(values(), true);
			arraycopy(fields, 0, CURSOR_FIELDS, 0, fields.length);
		}

	}


	static {
		Map<Integer, CourseTableColumns> map = new HashMap<>();

		for (CourseTableColumns column : CourseTableColumns.values()) {
			int index = column.getFieldIndex();

			//the following is in-lieu of a test case right now - ensures that the code will fall over at startup if it encounters a duplicate index
			if (map.containsKey(index)) {
				throw new RuntimeException("Duplicate fieldIndex found. Check " + column.getClass().getName() + ":" + column.name() + " and " + map.get(index).name());
			}
			map.put(index, column);
		}

	}


	private static String[] getFieldNameFrom(CursorField[] cursorFields, boolean isCatalog) {
		List<String> list = new ArrayList<>();

		Map<Integer, String> map = new TreeMap<>();

		for (CursorField field : cursorFields) {
			CourseTableColumns column = CourseTableColumns.valueOf(field.name());
			map.put(field.getFieldIndex(), column.getFieldName());
		}


		if (isCatalog) {
			list.add(TABLE_NAME + "." + _ID);
		}
		list.addAll(map.values());


		return list.toArray(new String[list.size()]);
	}


	private CourseTable() {

	}

	public static String name() {
		return TABLE_NAME;
	}

	public static String getTableCreationSQL() {
		StatementBuilder sb = new StatementBuilder();

		sb.append("CREATE TABLE ").append(TABLE_NAME).append(" (").append(_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");

		//note - using FIELD_NAMES, not values directly in case the natural order of the enumeration does not correspond with the assigned index order

		Map<String, String> map = new HashMap<>();
		for (CourseTableColumns column : CourseTableColumns.values()) {
			String definition = column.getFieldDefinition();
			map.put(column.getFieldName(), definition != null ? definition : "");
		}

		for (String fieldName : CourseTableColumns.FIELD_NAMES) {

			sb.append(fieldName).append(" ").append(map.get(fieldName)).append(", ");
		}

		sb.append(" UNIQUE (");
		sb.appendFieldNames(CourseTableColumns.PROVIDER, CourseTableColumns.TYPE, CourseTableColumns.CODE);
		sb.append(") ON CONFLICT REPLACE ");

		sb.append(")");

		return sb.toString();
	}


}
