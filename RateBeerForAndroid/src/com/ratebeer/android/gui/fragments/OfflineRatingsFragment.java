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

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.List;

import org.json.JSONException;

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
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.OrmLiteDao;
import com.googlecode.androidannotations.annotations.ViewById;
import com.j256.ormlite.dao.Dao;
import com.ratebeer.android.R;
import com.ratebeer.android.api.command.PostRatingCommand;
import com.ratebeer.android.app.persistance.DatabaseHelper;
import com.ratebeer.android.app.persistance.OfflineRating;
import com.ratebeer.android.gui.components.ArrayAdapter;
import com.ratebeer.android.gui.components.ImportExport;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.fragments.ConfirmDialogFragment.OnDialogResult;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

@EFragment(R.layout.fragment_offlineratings)
public class OfflineRatingsFragment extends RateBeerFragment {

	private static final int MENU_ADD = 0;
	private static final int MENU_EXPORT = 1;
	private static final int MENU_IMPORT = 2;
	private static final int MENU_DELETE = 10;
	
	@ViewById(R.id.empty)
	protected TextView emptyText;
	@ViewById(R.id.offlineratings)
	protected ListView ratingsView;
	
	@OrmLiteDao(helper = DatabaseHelper.class, model = OfflineRating.class)
	Dao<OfflineRating, Long> offlineRatingDao;
	
	public OfflineRatingsFragment() {
	}

	@AfterViews
	public void init() {

		ratingsView.setOnItemClickListener(onItemSelected);
		registerForContextMenu(ratingsView);
		loadRatings();
		
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem item = menu.add(Menu.NONE, MENU_ADD, MENU_ADD, R.string.rate_offline_addrating);
		item.setIcon(R.drawable.ic_action_addrating);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		MenuItem item2 = menu.add(Menu.NONE, MENU_EXPORT, MENU_EXPORT, R.string.rate_offline_export);
		item2.setIcon(R.drawable.ic_action_export);
		item2.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		MenuItem item3 = menu.add(Menu.NONE, MENU_IMPORT, MENU_IMPORT, R.string.rate_offline_import);
		item3.setIcon(R.drawable.ic_menu_import);
		item3.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ADD:
			load(RateFragment_.builder().build());
			break;
		case MENU_EXPORT:
			new ConfirmDialogFragment(new OnDialogResult() {
				@Override
				public void onConfirmed() {
					try {
						ImportExport.exportRatings(offlineRatingDao.queryForAll(), ImportExport.DEFAULT_RATINGS_FILE);
					} catch (JSONException e) {
						Crouton.makeText(getActivity(), R.string.error_cannotread, Style.ALERT).show();
					} catch (IOException e) {
						Crouton.makeText(getActivity(), R.string.error_cannotread, Style.ALERT).show();
					} catch (SQLException e) {
						Crouton.makeText(getActivity(), R.string.rate_offline_notavailable, Style.ALERT).show();
					}
				}
			}, R.string.rate_offline_export_info, ImportExport.DEFAULT_RATINGS_FILE).show(getFragmentManager(), "exportdialog");
			break;
		case MENU_IMPORT:
			new ConfirmDialogFragment(new OnDialogResult() {
				@Override
				public void onConfirmed() {
					try {
						ImportExport.importRatings(offlineRatingDao, ImportExport.DEFAULT_RATINGS_FILE);
					} catch (JSONException e) {
						Crouton.makeText(getActivity(), R.string.error_filenotvalid, Style.ALERT).show();
					} catch (IOException e) {
						Crouton.makeText(getActivity(), R.string.error_cannotwrite, Style.ALERT).show();
					} catch (SQLException e) {
						Crouton.makeText(getActivity(), R.string.rate_offline_notavailable, Style.ALERT).show();
					}
					loadRatings();
				}
			}, R.string.rate_offline_import_info, ImportExport.DEFAULT_RATINGS_FILE).show(getFragmentManager(), "importdialog");
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void loadRatings() {
		
		// Get ratings from database
		List<OfflineRating> result;
		try {
			result = offlineRatingDao.queryForAll();
		} catch (SQLException e) {
			// Not available!
			publishException(emptyText, getString(R.string.rate_offline_notavailable));
			return;
		}
		
		// Show in list view
		if (ratingsView.getAdapter() == null) {
			ratingsView.setAdapter(new OfflineRatingsAdapter(getActivity(), result));
		} else {
			((OfflineRatingsAdapter) ratingsView.getAdapter()).replace(result);
		}
		ratingsView.setVisibility(result.size() == 0 ? View.GONE : View.VISIBLE);
		emptyText.setVisibility(result.size() == 0 ? View.VISIBLE : View.GONE);
		
	}

	private OnItemClickListener onItemSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			OfflineRating item = ((OfflineRatingsAdapter)ratingsView.getAdapter()).getItem(position);
			load(RateFragment_.buildFromOfflineID(item.getOfflineId()));
		}
	};

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.add(Menu.NONE, MENU_DELETE, MENU_DELETE, R.string.rate_offline_delete);
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();
		final OfflineRating rating = (OfflineRating) ratingsView.getItemAtPosition(acmi.position);
		new ConfirmDialogFragment(new OnDialogResult() {
			@Override
			public void onConfirmed() {
				deleteRating(rating);
				loadRatings();
			}
		}, R.string.rate_offline_confirmdelete, rating.getBeerName()).show(getFragmentManager(), "dialog");
		return super.onContextItemSelected(item);
	}

	protected void deleteRating(OfflineRating rating) {
		try {
			offlineRatingDao.delete(rating);
		} catch (SQLException e) {
			Crouton.makeText(getActivity(), R.string.rate_offline_notavailable, Style.ALERT).show();
		}
	}

	private class OfflineRatingsAdapter extends ArrayAdapter<OfflineRating> {

		private final DateFormat dateFormat;
		
		public OfflineRatingsAdapter(Context context, List<OfflineRating> objects) {
			super(context, objects);
			this.dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (getActivity() == null) {
				return convertView;
			}
			
			// Get the right view, using a ViewHolder
			ViewHolder holder;
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_offlinerating, null);
				holder = new ViewHolder();
				holder.beerName = (TextView) convertView.findViewById(R.id.beerName);
				holder.total = (TextView) convertView.findViewById(R.id.total);
				holder.aroma = (TextView) convertView.findViewById(R.id.aroma);
				holder.appearance = (TextView) convertView.findViewById(R.id.appearance);
				holder.taste = (TextView) convertView.findViewById(R.id.taste);
				holder.palate = (TextView) convertView.findViewById(R.id.palate);
				holder.overall = (TextView) convertView.findViewById(R.id.overall);
				holder.status = (TextView) convertView.findViewById(R.id.status);
				holder.comments = (TextView) convertView.findViewById(R.id.comments);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data
			OfflineRating item = getItem(position);
			holder.beerName.setText(item.getBeerName());
			holder.total.setTag(item);
			if (item.getAroma() == null || item.getAppearance() == null && item.getTaste() == null || item.getPalate() == null || item.getOverall() == null) {
				holder.total.setText("");
			} else {
				holder.total.setText(Float.toString(PostRatingCommand.calculateTotal(
					item.getAroma(), item.getAppearance(), item.getTaste(), 
					item.getPalate(), item.getOverall())));
			}
			holder.aroma.setText(item.getAroma() == null? "": Integer.toString(item.getAroma()));
			holder.appearance.setText(item.getAppearance() == null? "": Integer.toString(item.getAppearance()));
			holder.taste.setText(item.getTaste() == null? "": Integer.toString(item.getTaste()));
			holder.palate.setText(item.getPalate() == null? "": Integer.toString(item.getPalate()));
			holder.overall.setText(item.getOverall() == null? "": Integer.toString(item.getOverall()));
			holder.status.setText(getString(R.string.rate_offline_savedat, dateFormat.format(item.getTimeSaved())));
			holder.comments.setText(item.getComments());

			return convertView;
		}

	}

	protected static class ViewHolder {
		TextView beerName, total, aroma, appearance, taste, palate, overall, status, comments;
	}

}
