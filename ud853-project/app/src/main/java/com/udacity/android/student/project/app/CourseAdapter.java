package com.udacity.android.student.project.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static com.udacity.android.student.project.app.data.CourseTable.CatalogCursor.CODE;
import static com.udacity.android.student.project.app.data.CourseTable.CatalogCursor.SUBTITLE;
import static com.udacity.android.student.project.app.data.CourseTable.CatalogCursor.TITLE;

/**
 *
 * course catalog view
 */

/**
 * {@link com.udacity.android.student.project.app.CourseAdapter} exposes a list of courses
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class CourseAdapter extends CursorAdapter {

	/**
	 * Cache of the children views for a catalog list item.
	 */
	public static class ViewHolder {

		public final TextView codeAndTitleView;
		public final TextView subTitleView;


		public ViewHolder(View view) {
			codeAndTitleView = (TextView) view.findViewById(R.id.item_code_and_title);
			subTitleView = (TextView) view.findViewById(R.id.item_subtitle);
		}
	}
	public CourseAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// Choose the layout type
		//int viewType = getItemViewType(cursor.getPosition());
		int layoutId =R.layout.list_catalog_item;

		View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

		ViewHolder viewHolder = new ViewHolder(view);
		view.setTag(viewHolder);

		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		ViewHolder viewHolder = (ViewHolder) view.getTag();

		viewHolder.codeAndTitleView.setText(CODE.getString(cursor)+ " "+TITLE.getString(cursor));
		viewHolder.subTitleView.setText(SUBTITLE.getString(cursor));
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

}