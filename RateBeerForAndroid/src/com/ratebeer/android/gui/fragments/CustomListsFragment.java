/*
    This file is part of RateBeer For Android.
    
    RateBeer for Android is free software: you can redistribute it 
    and/or modify it under the terms of the GNU General Public 
    License as published by the Free Software Foundation, either 
    version 3 of the License, or (at your option) any later version.

    RateBeer for Android is distributed in the hope that it will be 
    useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
    of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with RateBeer for Android.  If not, see 
    <http://www.gnu.org/licenses/>.
 */
package com.ratebeer.android.gui.fragments;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.OrmLiteDao;
import com.googlecode.androidannotations.annotations.ViewById;
import com.j256.ormlite.dao.Dao;
import com.ratebeer.android.R;
import com.ratebeer.android.app.persistance.CustomList;
import com.ratebeer.android.app.persistance.DatabaseHelper;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.components.helpers.ArrayAdapter;
import com.ratebeer.android.gui.fragments.ConfirmDialogFragment.OnDialogResult;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

@EFragment(R.layout.fragment_customlists)
@OptionsMenu(R.menu.customlists)
public class CustomListsFragment extends RateBeerFragment {

	private static final int MENU_DELETE = 10;
	
	@ViewById(R.id.empty)
	protected TextView emptyText;
	@ViewById(R.id.lists)
	protected ListView listsView;
	
	@OrmLiteDao(helper = DatabaseHelper.class, model = CustomList.class)
	Dao<CustomList, Long> customListDao;
	
	public CustomListsFragment() {
	}

	@AfterViews
	public void init() {

		listsView.setOnItemClickListener(onItemSelected);
		registerForContextMenu(listsView);
		loadCustomLists();
		
	}
	
	@OptionsItem(R.id.menu_newlist)
	protected void onNewList() {
		new ListNameDialog(this).show(getFragmentManager(), "listname");
	}
	
	public void createList(String listName) {
		try {
			CustomList customList = new CustomList(listName);
			customListDao.create(customList);
			load(CustomListFragment_.builder().customListId(customList.getCustomListId()).build());
		} catch (SQLException e) {
			// Not available!
			publishException(emptyText, getString(R.string.custom_notavailable));
			return;
		}
	}
	
	private void loadCustomLists() {
		
		// Get custom lists from database
		List<CustomList> result;
		try {
			result = customListDao.queryForAll();
		} catch (SQLException e) {
			// Not available!
			publishException(emptyText, getString(R.string.custom_notavailable));
			return;
		}
		
		// Show in list view
		if (listsView.getAdapter() == null) {
			listsView.setAdapter(new CustomListsAdapter(getActivity(), result));
		} else {
			((CustomListsAdapter) listsView.getAdapter()).replace(result);
		}
		listsView.setVisibility(result.size() == 0 ? View.GONE : View.VISIBLE);
		emptyText.setVisibility(result.size() == 0 ? View.VISIBLE : View.GONE);
		
	}

	private OnItemClickListener onItemSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			CustomList item = ((CustomListsAdapter)listsView.getAdapter()).getItem(position);
			load(CustomListFragment_.builder().customListId(item.getCustomListId()).build());
		}
	};

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.add(Menu.NONE, MENU_DELETE, MENU_DELETE, R.string.custom_deletelist);
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();
		final CustomList customList = (CustomList) listsView.getItemAtPosition(acmi.position);
		new ConfirmDialogFragment(new OnDialogResult() {
			@Override
			public void onConfirmed() {
				deleteCustomList(customList);
				loadCustomLists();
			}
		}, R.string.custom_confirmdeletelist, customList.getName()).show(getFragmentManager(), "dialog");
		return super.onContextItemSelected(item);
	}

	protected void deleteCustomList(CustomList customList) {
		try {
			customListDao.delete(customList);
		} catch (SQLException e) {
			Crouton.makeText(getActivity(), R.string.custom_notavailable, Style.ALERT).show();
		}
	}

	private class CustomListsAdapter extends ArrayAdapter<CustomList> {

		public CustomListsAdapter(Context context, List<CustomList> result) {
			super(context, result);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Get the right view, using a ViewHolder
			ViewHolder holder;
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_customlist, null);
				holder = new ViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.name);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data
			CustomList item = getItem(position);
			holder.name.setText(item.getName());

			return convertView;
		}

	}

	protected static class ViewHolder {
		TextView name;
	}

}
