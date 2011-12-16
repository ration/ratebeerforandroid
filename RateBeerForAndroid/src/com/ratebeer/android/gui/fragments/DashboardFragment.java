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

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.UserSettings;
import com.ratebeer.android.api.command.GetTopBeersCommand.TopListType;
import com.ratebeer.android.api.command.GetUserIdCommand;
import com.ratebeer.android.api.command.GetUserImageCommand;
import com.ratebeer.android.api.command.Style;
import com.ratebeer.android.app.RateBeerForAndroid;
import com.ratebeer.android.gui.SignIn;
import com.ratebeer.android.gui.components.PosterService;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.fragments.SetDrinkingStatusDialogFragment.OnDialogResult;
import com.ratebeer.android.gui.fragments.StylesFragment.StyleAdapter;

public class DashboardFragment extends RateBeerFragment {

	private static final int MENU_SCANBARCODE = 1;
	private static final int MENU_SEARCH = 2;

	private Button drinkingStatus, myProfileButton;
	private ListView stylesView;
	private LayoutInflater inflater;
	private Float density = null;

	public DashboardFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		this.inflater = inflater;
		return inflater.inflate(R.layout.fragment_dashboard, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
				
		// Bind a click listener to the big dashboard buttons
		/*((Button) getView().findViewById(R.id.search)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().onSearchRequested();
			}
		});*/
		myProfileButton = (Button) getView().findViewById(R.id.myprofile);
		myProfileButton.setOnClickListener(onProfileButtonClick());
		((Button) getView().findViewById(R.id.offlineratings)).
			setOnClickListener(onButtonClick(new OfflineRatingsFragment(), false));
		((Button) getView().findViewById(R.id.beerstyles)).
			setOnClickListener(onButtonClick(new StylesFragment(), false));
		((Button) getView().findViewById(R.id.top50)).
			setOnClickListener(onButtonClick(new TopBeersFragment(TopListType.Top50), false));
		((Button) getView().findViewById(R.id.bycountry)).
			setOnClickListener(onButtonClick(new TopBeersFragment(TopListType.TopByCountry), false));
		((Button) getView().findViewById(R.id.places)).
			setOnClickListener(onButtonClick(new PlacesFragment(), false));
		((Button) getView().findViewById(R.id.events)).
			setOnClickListener(onButtonClick(new EventsFragment(), true));
		((Button) getView().findViewById(R.id.beermail)).
			setOnClickListener(onButtonClick(new MailsFragment(), true));
		
		updateProfileImage();
		
		// For tablets, also load the beer styles list
		if (RateBeerForAndroid.isTablet(getResources())) {
			stylesView = (ListView) getView().findViewById(R.id.styles);
			stylesView.setAdapter(new StylesFragment.StyleAdapter(getActivity(), 
					new ArrayList<Style>(Style.ALL_STYLES.values()), inflater));
			stylesView.setOnItemClickListener(onItemSelected);
		}
		
		// Update drinking status
		drinkingStatus = (Button) getView().findViewById(R.id.drinking_status);
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
								refreshDrinkingStatus();
							}
						}));
						getActivity().startService(i);
					}
				}).show(getSupportFragmentManager(), "dialog");
			}
		});
		showDrinkingStatus();
		refreshDrinkingStatus();
		
		// Show legal stuff on first app start
		if (getRateBeerApplication().getSettings().isFirstStart()) {
			getRateBeerApplication().getSettings().recordFirstStart();
			getRateBeerActivity().load(new TextInfoFragment(getString(R.string.app_legal_title), getString(R.string.app_legal)));
		}
		
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem item2 = menu.add(Menu.NONE, MENU_SCANBARCODE, MENU_SCANBARCODE, R.string.search_barcodescanner);
		item2.setIcon(R.drawable.ic_action_barcode);
		item2.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		if (getActivity() != null && !RateBeerForAndroid.isTablet(getResources())) {
			// For phones, the dashboard (and the dashboard only) shows a search icon in the action bar
			// Not that tablets always show an search input in the action bar through the HomeTablet activity directly
			MenuItem item = menu.add(Menu.NONE, MENU_SEARCH, MENU_SEARCH, R.string.home_search);
			item.setIcon(R.drawable.ic_action_search);
			item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}
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
			getRateBeerActivity().load(new SearchFragment(true));
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateProfileImage() {
		if (getRateBeerActivity().getUser() != null) {
			execute(new GetUserImageCommand(getRateBeerActivity().getApi(), getRateBeerActivity().getUser().getUsername()));
		}
	}

	private OnClickListener onProfileButtonClick() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				UserSettings usr = getRateBeerActivity().getUser();
				if (usr == null) {
					// No user yet, but this is required so start the login screen
					Intent i = new Intent(getActivity(), SignIn.class);
					i.putExtra(SignIn.EXTRA_REDIRECT, true);
					startActivity(i);
				} else {
					getRateBeerActivity().load(new UserViewFragment(usr.getUsername(), usr.getUserID()));
				}
			}
		};
	}

	private OnClickListener onButtonClick(final RateBeerFragment fragment, final boolean requiresUser) {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (requiresUser && getRateBeerActivity().getUser() == null) {
					// No user yet, but this is required so start the login screen
					Intent i = new Intent(getActivity(), SignIn.class);
					i.putExtra(SignIn.EXTRA_REDIRECT, true);
					startActivity(i);
				} else {
					getRateBeerActivity().load(fragment);
				}
			}
		};
	}

	private OnItemClickListener onItemSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Style item = ((StyleAdapter)stylesView.getAdapter()).getItem(position);
			getRateBeerActivity().load(new StyleViewFragment(item));
		}
	};
	
	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (result.getCommand().getMethod() == ApiMethod.GetUserId) {
			GetUserIdCommand getCommand = (GetUserIdCommand) result.getCommand();
			// Override the user settings, in which the drinking status is contained
			getRateBeerActivity().getSettings().saveUserSettings(new UserSettings(getCommand.getUserId(), getRateBeerActivity().getUser().getUsername(), 
					getRateBeerActivity().getUser().getPassword(), getCommand.getDrinkingStatus(), getCommand.isPremium()));
			showDrinkingStatus();
		} else if (result.getCommand().getMethod() == ApiMethod.GetUserImage) {
			GetUserImageCommand userImageCommand = (GetUserImageCommand) result.getCommand();
			if (userImageCommand.getImage() != null) {
				if (density == null) {
					density = getResources().getDisplayMetrics().density;
				}
				Drawable d = userImageCommand.getImage();
				d.setBounds(0, 0, (int)(48 * density), (int)(48 * density));
				myProfileButton.setCompoundDrawables(null, d, null, null);
			}
		}
	}
	
	private void refreshDrinkingStatus() {
		if (getRateBeerActivity() != null && getRateBeerActivity().getUser() != null) {
			execute(new GetUserIdCommand(getRateBeerActivity().getApi()));
		}	
	}

	public void showDrinkingStatus() {
		if (getRateBeerActivity().getUser() == null || getRateBeerActivity().getUser().getDrinkingStatus() == null || 
				getRateBeerActivity().getUser().getDrinkingStatus().equals("")) {
			drinkingStatus.setVisibility(View.GONE);
		} else {
			drinkingStatus.setVisibility(View.VISIBLE);
			drinkingStatus.setText(getString(R.string.home_nowdrinking, getRateBeerActivity().getUser().getDrinkingStatus()));
		}
	}

}
