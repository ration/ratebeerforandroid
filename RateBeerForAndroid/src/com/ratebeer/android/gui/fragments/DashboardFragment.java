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
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ItemClick;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
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
import com.ratebeer.android.gui.*;
import com.ratebeer.android.gui.components.PosterService;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.components.helpers.ErrorLogSender;
import com.ratebeer.android.gui.components.helpers.SearchUiHelper;
import com.ratebeer.android.gui.fragments.SetDrinkingStatusDialogFragment.OnDialogResult;

@EFragment(R.layout.fragment_dashboard)
@OptionsMenu(R.menu.dashboard)
public class DashboardFragment extends RateBeerFragment {

	@ViewById
	protected Button drinkingStatus;
	@ViewById
	protected Button myprofile, offlineratings, beerstyles, top50, places, events, beermail, bycountry;
	@ViewById
	protected ListView styles;
	private LayoutInflater inflater;
	private Float density = null;

	@Bean
	protected ErrorLogSender errorLogSender;
	
	public DashboardFragment() {
	}

	@AfterViews
	public void init() {
				
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
		}
		
		// Update drinking status
		showDrinkingStatus();
		refreshDrinkingStatus();
		
		// Show legal stuff on first app start
		if (getSettings().isFirstStart()) {
			getSettings().recordFirstStart();
			load(TextInfoFragment_.builder().title(getString(R.string.app_legal_title))
					.info(getString(R.string.app_legal)).build());
		}
		
	}
	
	@Click
	protected void drinkingStatusClicked() {
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

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		new SearchUiHelper(getActivity()).enhanceSearchInMenu(menu);
	}

	@OptionsItem(R.id.menu_search)
	protected void onStartSearch() {
		// Open standard search interface
		// Note that this method is only called on API < 8 as SearchView is used for API level >= 8 (via ActionBarSherlock)
		getActivity().onSearchRequested();		
	}
	
	@OptionsItem(R.id.menu_scanbarcode)
	protected void onStartBarcodeScanner() {
    	// Start the search activity (without specific search string), which offers the actual scanning feature
		load(SearchFragment_.builder().startBarcodeScanner(true).build());
	}
	
	@OptionsItem(R.id.menu_calculator)
	protected void onStartCalculator() {
		// Start calculator screen
		load(CalculatorFragment_.builder().build());
	}

	@OptionsItem(R.id.menu_preferences)
	protected void onOpenPreferences() {
		PreferencesInterface_.intent(getActivity()).start();
	}

	@OptionsItem(R.id.menu_sendreport)
	protected void onSendErrorReport() {
		errorLogSender.collectAndSendLog(getUser() == null? "<none>": getUser().getUsername());
	}

	@OptionsItem(R.id.menu_about)
	protected void onOpenAbout() {
		load(AboutFragment_.builder().build());
	}

	private void updateProfileImage() {
		if (getUser() != null) {
			RateBeerForAndroid.getImageCache(getActivity()).loadImage(
					ImageUrls.getUserPhotoUrl(getUser().getUsername()),
					new ImageLoadingListener() {
						@Override
						public void onLoadingStarted(String url, View view) {
						}

						@Override
						public void onLoadingFailed(String url, View view, FailReason reason) {
						}

						@Override
						public void onLoadingComplete(String url, View view, Bitmap bitmap) {
							if (getActivity() == null) {
								return;
							}
							if (density == null) {
								density = getResources().getDisplayMetrics().density;
							}
							Drawable d = new BitmapDrawable(getResources(), bitmap);
							d.setBounds(0, 0, (int) (48 * density), (int) (48 * density));
							myprofile.setCompoundDrawables(null, d, null, null);
						}

						@Override
						public void onLoadingCancelled(String url, View view) {
						}
					});
		}
	}

	@Click
	protected void myprofileClicked() {
		if (getUser() == null) {
			// No user yet, but this is required so start the login screen
			SignIn_.intent(getActivity()).extraIsRedirect(true).start();
		} else {
			load(UserViewFragment_.builder().userName(getUser().getUsername())
					.userId(getUser().getUserID()).build());
		}
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

	@ItemClick(R.id.styles)
	protected void onStyleClicked(Style item) {
		load(StyleViewFragment_.builder().style(item).build());
	}
	
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
