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

import java.util.ArrayList;
import java.util.Date;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.UserSettings;
import com.ratebeer.android.api.command.GetDrinkingStatusCommand;
import com.ratebeer.android.api.command.GetTopBeersCommand.TopListType;
import com.ratebeer.android.api.command.ImageUrls;
import com.ratebeer.android.api.command.Style;
import com.ratebeer.android.app.RateBeerForAndroid;
import com.ratebeer.android.gui.SignIn_;
import com.ratebeer.android.gui.components.PosterService;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.fragments.SetDrinkingStatusDialogFragment.OnDialogResult;
import com.ratebeer.android.gui.fragments.StylesFragment.StyleAdapter;

@EFragment(R.layout.fragment_dashboard)
public class DashboardFragment extends RateBeerFragment {

	private static final int MENU_SCANBARCODE = 1;
	private static final int MENU_SEARCH = 2;
	private static final int MENU_CALCULATOR = 3;

	@ViewById
	protected Button drinkingStatus;
	@ViewById
	protected Button myprofile, offlineratings, beerstyles, top50, places, events, beermail, bycountry;
	@ViewById
	protected ListView styles;
	private LayoutInflater inflater;
	private Float density = null;

	public DashboardFragment() {
	}

	@AfterViews
	public void init() {
				
		myprofile.setOnClickListener(onProfileButtonClick());
		offlineratings.setOnClickListener(onButtonClick(OfflineRatingsFragment_.builder().build(), false));
		beerstyles.setOnClickListener(onButtonClick(StylesFragment_.builder().build(), false));
		top50.setOnClickListener(onButtonClick(TopBeersFragment_.builder().topList(TopListType.Top50).build(), false));
		bycountry.setOnClickListener(onButtonClick(TopBeersFragment_.builder().topList(TopListType.TopByCountry)
				.build(), false));
		places.setOnClickListener(onButtonClick(PlacesFragment_.builder().build(), false));
		events.setOnClickListener(onButtonClick(EventsFragment_.builder().build(), true));
		beermail.setOnClickListener(onButtonClick(MailsFragment_.builder().build(), true));
		
		updateProfileImage();
		
		// For tablets, also load the beer styles list
		if (styles != null) {
			styles.setAdapter(new StylesFragment.StyleAdapter(getActivity(), 
					new ArrayList<Style>(Style.ALL_STYLES.values()), inflater));
			styles.setOnItemClickListener(onItemSelected);
		}
		
		// Update drinking status
		drinkingStatus.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new SetDrinkingStatusDialogFragment(new OnDialogResult() {
					@Override
					public void onSetNewStatus(String newStatus) {
						// Update now drinking status
						Intent i = new Intent(PosterService.ACTION_SETDRINKINGSTATUS);
						i.putExtra(PosterService.EXTRA_NEWSTATUS, newStatus);
						i.putExtra(PosterService.EXTRA_BEERID, PosterService.NO_BEER_EXTRA);
						i.putExtra(PosterService.EXTRA_MESSENGER, new Messenger(new Handler() {
							@Override
							public void handleMessage(Message msg) {
								// Callback from the poster service; just refresh the drinking status
								// if (msg.arg1 == PosterService.RESULT_SUCCESS)
								execute(new GetDrinkingStatusCommand(getUser()));
							}
						}));
						getActivity().startService(i);
					}
				}).show(getFragmentManager(), "dialog");
			}
		});
		showDrinkingStatus();
		refreshDrinkingStatus();
		
		// Show legal stuff on first app start
		if (getSettings().isFirstStart()) {
			getSettings().recordFirstStart();
			load(TextInfoFragment_.builder().title(getString(R.string.app_legal_title))
					.info(getString(R.string.app_legal)).build());
		}
		
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		boolean showSearch = true;
		if (android.os.Build.VERSION.SDK_INT >= 16) { 
			if (getResources().getConfiguration().screenWidthDp >= 800) {
				showSearch = false; // Already shown as SearchView
			}
		}
		if (showSearch) {
			// For phones, the dashboard & search fragments show a search icon in the action bar
			// Note that tablets always show an search input in the action bar through the HomeTablet activity directly
			MenuItem item = menu.add(Menu.NONE, MENU_SEARCH, Menu.NONE, R.string.home_search);
			item.setIcon(R.drawable.ic_action_search);
			item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}
		
		MenuItem item2 = menu.add(Menu.NONE, MENU_SCANBARCODE, Menu.NONE, R.string.search_barcodescanner);
		item2.setIcon(R.drawable.ic_action_barcode);
		item2.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		MenuItem item3 = menu.add(Menu.NONE, MENU_CALCULATOR, Menu.NONE, R.string.home_calculator);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			item3.setIcon(R.drawable.ic_menu_calculator);
		} else {
			item3.setIcon(R.drawable.ic_action_calculator);
		}
		item3.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SEARCH:
			// Open standard search interface
			getActivity().onSearchRequested();
			break;
		case MENU_SCANBARCODE:
	    	// Start the search activity (without specific search string), which offers the actual scanning feature
			load(SearchFragment_.builder().startBarcodeScanner(true).build());
			break;
		case MENU_CALCULATOR:
			// Start calculator screen
			load(CalculatorFragment_.builder().build());
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateProfileImage() {
		if (getUser() != null) {
			RateBeerForAndroid.getImageCache(getActivity()).loadImage(getActivity(),
					ImageUrls.getUserPhotoUrl(getUser().getUsername()),
					new ImageLoadingListener() {
						@Override
						public void onLoadingStarted() {
						}

						@Override
						public void onLoadingFailed(FailReason arg0) {
						}

						@Override
						public void onLoadingComplete(Bitmap arg0) {
							if (getActivity() == null) {
								return;
							}
							if (density == null) {
								density = getResources().getDisplayMetrics().density;
							}
							Drawable d = new BitmapDrawable(getResources(), arg0);
							d.setBounds(0, 0, (int) (48 * density), (int) (48 * density));
							myprofile.setCompoundDrawables(null, d, null, null);
						}

						@Override
						public void onLoadingCancelled() {
						}
					});
		}
	}

	private OnClickListener onProfileButtonClick() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getUser() == null) {
					// No user yet, but this is required so start the login screen
					SignIn_.intent(getActivity()).extraIsRedirect(true).start();
				} else {
					load(UserViewFragment_.builder().userName(getUser().getUsername()).userId(getUser().getUserID())
							.build());
				}
			}
		};
	}

	private OnClickListener onButtonClick(final RateBeerFragment fragment, final boolean requiresUser) {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (requiresUser && getUser() == null) {
					// No user yet, but this is required so start the login screen
					SignIn_.intent(getActivity()).extraIsRedirect(true).start();
				} else {
					load(fragment);
				}
			}
		};
	}

	private OnItemClickListener onItemSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Style item = ((StyleAdapter)styles.getAdapter()).getItem(position);
			load(StyleViewFragment_.builder().style(item).build());
		}
	};
	
	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (getActivity() == null) {
			return;
		}
		if (result.getCommand().getMethod() == ApiMethod.GetDrinkingStatus) {
			GetDrinkingStatusCommand getCommand = (GetDrinkingStatusCommand) result.getCommand();
			// Override the user settings, in which the drinking status is contained
			getSettings().saveUserSettings(new UserSettings(getUser().getUserID(), getUser().getUsername(), 
					getUser().getPassword(), getCommand.getDrinkingStatus(), getUser().isPremium(), new Date()));
			showDrinkingStatus();
		}
	}
	
	private void refreshDrinkingStatus() {
		// At max refresh every 5 minutes
		Date d = new Date((new Date()).getTime() - (5 * 60 * 1000)); // = 5 minutes ago
		if (getUser() != null && getUser().getLastDrinkingStatusUpdate().before(d)) {
			execute(new GetDrinkingStatusCommand(getUser()));
		}	
	}

	public void showDrinkingStatus() {
		if (getUser() == null || getUser().getDrinkingStatus() == null || getUser().getDrinkingStatus().equals("")) {
			drinkingStatus.setVisibility(View.GONE);
		} else {
			drinkingStatus.setVisibility(View.VISIBLE);
			drinkingStatus.setText(getString(R.string.home_nowdrinking, getUser().getDrinkingStatus()));
		}
	}

}
