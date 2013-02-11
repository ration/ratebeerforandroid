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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.InstanceState;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;
import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.command.Country;
import com.ratebeer.android.api.command.GetBrewerBeersCommand;
import com.ratebeer.android.api.command.GetBrewerDetailsCommand;
import com.ratebeer.android.api.command.GetBrewerDetailsCommand.BrewerDetails;
import com.ratebeer.android.api.command.SearchBeersCommand;
import com.ratebeer.android.api.command.SearchBeersCommand.BeerSearchResult;
import com.ratebeer.android.api.command.State;
import com.ratebeer.android.app.location.LocationUtils;
import com.ratebeer.android.app.location.SimpleItemizedOverlay;
import com.ratebeer.android.app.location.SimpleItemizedOverlay.OnBalloonClickListener;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.components.helpers.ActivityUtil;
import com.ratebeer.android.gui.components.helpers.ArrayAdapter;
import com.viewpagerindicator.TabPageIndicator;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

@EFragment(R.layout.fragment_brewerview)
@OptionsMenu({R.menu.refresh, R.menu.share})
public class BrewerViewFragment extends RateBeerFragment implements OnBalloonClickListener {

	protected static final String BASE_URI_FACEBOOK = "https://www.facebook.com/%1$s";
	protected static final String BASE_URI_TWITTER = "https://twitter.com/%1$s";

	@FragmentArg
	@InstanceState
	protected int brewerId;
	@InstanceState
	protected BrewerDetails brewer;
	@InstanceState
	protected ArrayList<BeerSearchResult> beers = null;

	@ViewById
	protected ViewPager pager;
	@ViewById
	protected TabPageIndicator titles;
	protected ListView beersView;
	protected TextView nameText, descriptionText;
	protected Button locationText, websiteButton, facebookButton, twitterButton;
	protected FrameLayout mapFrame;

	public BrewerViewFragment() {
	}

	@AfterViews
	public void init() {

		pager.setAdapter(new BrewerPagerAdapter());
		titles.setViewPager(pager);
		beersView.setOnItemClickListener(onBeerSelected);

		if (brewer != null) {
			publishDetails(brewer);
			publishBeers(beers);
		} else {
			refreshDetails();
			refreshBeers();
		}
	}

	@OptionsItem(R.id.menu_refresh)
	protected void onRefresh() {
		refreshDetails();
		refreshBeers();
	}
	
	@OptionsItem(R.id.menu_share)
	protected void onShare() {
		if (brewer != null) {
			// Start a share intent for this event
			Intent s = new Intent(Intent.ACTION_SEND);
			s.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			s.setType("text/plain");
			s.putExtra(Intent.EXTRA_TEXT,
					getString(R.string.brewers_share, brewer.brewerName, Integer.toString(brewer.brewerId)));
			startActivity(Intent.createChooser(s, getString(R.string.brewers_sharebrewer)));
		}
	}

	private void refreshDetails() {
		execute(new GetBrewerDetailsCommand(getUser(), brewerId));
	}

	private void refreshBeers() {
		execute(new GetBrewerBeersCommand(getUser(), brewerId, getUser() != null ? getUser().getUserID()
				: SearchBeersCommand.NO_USER));
	}

	private OnClickListener onLocationClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Try to start Google Maps at the location
			Intent intent = new Intent(Intent.ACTION_VIEW);
			try {
				intent.setData(Uri.parse("geo:0,0?q="
						+ URLEncoder.encode((brewer.address != null ? brewer.address : "")
								+ (brewer.city != null ? " " + brewer.city : ""), HttpHelper.UTF8)));
			} catch (UnsupportedEncodingException e) {
			}
			startActivity(intent);
		}
	};

	private OnClickListener onWebsiteClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String web = brewer.website;
			if (!web.startsWith("http://")) {
				// http:// should be explicit in the web address
				web = "http://" + web;
			}
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(web));
		    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			if (ActivityUtil.isIntentAvailable(getActivity(), i)) {
				startActivity(i);
			} else {
				publishException(null, getString(R.string.error_invalidurl));
			}
		}
	};

	private OnClickListener onFacebookClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(BASE_URI_FACEBOOK, brewer.facebook))));
		}
	};

	private OnClickListener onTwitterClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(BASE_URI_TWITTER, brewer.twitter))));
		}
	};

	private OnItemClickListener onBeerSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			BeerSearchResult item = ((BrewerBeersAdapter) beersView.getAdapter()).getItem(position);
			if (item.isAlias) {
				// Unfortunately this is the only possible workaround for now to prohibit viewing an aliased beer as
				// if it were a normal one (see issue 8)
				// TODO: Actually we might want to link to the full website or even parse that HTML page
				Crouton.makeText(getActivity(), R.string.search_aliasedbeer, Style.INFO).show();
				return;
			}
			load(BeerViewFragment_.builder().beerId(item.beerId).beerName(item.beerName).ratingsCount(item.rateCount)
					.build());
		}
	};

	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (result.getCommand().getMethod() == ApiMethod.GetBrewerDetails) {
			publishDetails(((GetBrewerDetailsCommand) result.getCommand()).getDetails());
		} else if (result.getCommand().getMethod() == ApiMethod.GetBrewerBeers) {
			publishBeers(((GetBrewerBeersCommand) result.getCommand()).getBeers());
		}
	}

	private void publishDetails(BrewerDetails details) {
		this.brewer = details;
		if (details == null) {
			return;
		}
		// Show details
		setDetails(details);
	}

	private void publishBeers(ArrayList<BeerSearchResult> beers) {
		this.beers = beers;
		if (beersView.getAdapter() == null) {
			beersView.setAdapter(new BrewerBeersAdapter(getActivity(), beers));
		} else {
			((BrewerBeersAdapter) beersView.getAdapter()).replace(beers);
		}
	}

	private void setDetails(BrewerDetails details) {
		nameText.setText(brewer.brewerName);
		descriptionText.setText(brewer.description == null? "": brewer.description);
		String state = "";
		String country = "";
		if (brewer.countryID > 0 && Country.ALL_COUNTRIES.containsKey(brewer.countryID)) {
			country = "\n" + Country.ALL_COUNTRIES.get(brewer.countryID).getName();
			if (brewer.stateID > 0 && State.ALL_STATES.containsKey(brewer.countryID)) {
				Map<Integer, State> states = State.ALL_STATES.get(brewer.countryID);
				if (states.containsKey(brewer.stateID)) {
					state = ", " + states.get(brewer.stateID).getName();
				}
			}
		}
		locationText.setText(brewer.address + "\n" + brewer.city + state + country);
		websiteButton.setText(brewer.website);
		//facebookButton.setText(brewer.facebook); // Just use the text 'Facebook', because often this isn't a nice looking name
		twitterButton.setText("@" + brewer.twitter);

		// Make fields visible too
		descriptionText.setVisibility(brewer.description.equals("") || brewer.description.equals("null")? View.GONE: View.VISIBLE);
		locationText.setVisibility(View.VISIBLE);
		websiteButton.setVisibility(brewer.website.equals("") || brewer.website.equals("null")? View.GONE: View.VISIBLE);
		facebookButton.setVisibility(brewer.facebook.equals("") || brewer.facebook.equals("null")? View.GONE: View.VISIBLE);
		twitterButton.setVisibility(brewer.twitter.equals("") || brewer.twitter.equals("null")? View.GONE: View.VISIBLE);

		// Get the activity-wide MapView to show on this fragment and center on this brewer's location
		MapView mapView = ((RateBeerActivity) getActivity()).requestMapViewInstance();
		try {
			// Use Geocoder to look up the coordinates
			try {
				List<Address> point = new Geocoder(getActivity()).getFromLocationName(details.address + " "
						+ details.city, 1);
				if (point.size() <= 0) {
					// Cannot find address: hide the map
					mapFrame.setVisibility(View.GONE);
				} else {
					// Found a location! Center the map here
					GeoPoint center = LocationUtils.getPoint(point.get(0).getLatitude(), point.get(0).getLongitude());
					mapView.getController().setCenter(center);
					mapFrame.setVisibility(View.VISIBLE);
					final SimpleItemizedOverlay to = PlacesFragment.getPlaceTypeMarker(mapView, 5, this);
					to.addOverlay(new OverlayItem(center, brewer.brewerName, brewer.city + state));
					mapView.getOverlays().add(to);
				}
			} catch (IOException e) {
				// Canot connect to geocoder server: hide the map
				mapFrame.setVisibility(View.GONE);
			}
		} catch (NoSuchMethodError e) {
			// Geocoder is not available at all: hide the map
			mapFrame.setVisibility(View.GONE);
		}
		mapFrame.addView(mapView);

	}

	@Override
	public void onBalloonClicked(OverlayItem item) {
		// No event, yet
	}

	private class BrewerBeersAdapter extends ArrayAdapter<BeerSearchResult> {

		public BrewerBeersAdapter(Context context, List<BeerSearchResult> objects) {
			super(context, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Get the right view, using a ViewHolder
			BeerViewHolder holder;
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_beersearchresult, null);
				holder = new BeerViewHolder();
				holder.beer = (TextView) convertView.findViewById(R.id.beer);
				holder.overall = (TextView) convertView.findViewById(R.id.overall);
				holder.count = (TextView) convertView.findViewById(R.id.count);
				holder.rated = (TextView) convertView.findViewById(R.id.rated);
				holder.retired = (TextView) convertView.findViewById(R.id.retired);
				holder.alias = (TextView) convertView.findViewById(R.id.alias);
				convertView.setTag(holder);
			} else {
				holder = (BeerViewHolder) convertView.getTag();
			}

			// Bind the data
			BeerSearchResult item = getItem(position);
			holder.beer.setText(item.beerName);
			holder.overall.setText((item.overallPerc >= 0 ? Integer.toString(item.overallPerc) : "?"));
			holder.count.setText(Integer.toString(item.rateCount) + " " + getString(R.string.details_ratings));
			holder.rated.setVisibility(item.isRated ? View.VISIBLE : View.GONE);
			holder.retired.setVisibility(item.isRetired ? View.VISIBLE : View.GONE);
			holder.alias.setVisibility(item.isAlias ? View.VISIBLE : View.GONE);

			return convertView;
		}

	}

	protected static class BeerViewHolder {
		TextView beer, overall, count, rated, retired, alias;
	}

	private class BrewerPagerAdapter extends PagerAdapter {

		private View pagerDetailsView;

		public BrewerPagerAdapter() {
			LayoutInflater inflater = getActivity().getLayoutInflater();
			pagerDetailsView = (ScrollView) inflater.inflate(R.layout.fragment_brewerdetails, null);
			beersView = (ListView) inflater.inflate(R.layout.fragment_pagerlist, null);

			nameText = (TextView) pagerDetailsView.findViewById(R.id.name);
			descriptionText = (TextView) pagerDetailsView.findViewById(R.id.description);
			locationText = (Button) pagerDetailsView.findViewById(R.id.location);
			websiteButton = (Button) pagerDetailsView.findViewById(R.id.website);
			facebookButton = (Button) pagerDetailsView.findViewById(R.id.facebook);
			twitterButton = (Button) pagerDetailsView.findViewById(R.id.twitter);
			mapFrame = (FrameLayout) pagerDetailsView.findViewById(R.id.map);
			locationText.setOnClickListener(onLocationClick);
			websiteButton.setOnClickListener(onWebsiteClick);
			facebookButton.setOnClickListener(onFacebookClick);
			twitterButton.setOnClickListener(onTwitterClick);

		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getActivity().getString(R.string.app_details).toUpperCase();
			case 1:
				return getActivity().getString(R.string.brewers_beers).toUpperCase();
			}
			return null;
		}

		@Override
		public Object instantiateItem(View container, int position) {
			switch (position) {
			case 0:
				((ViewPager) container).addView(pagerDetailsView, 0);
				return pagerDetailsView;
			case 1:
				((ViewPager) container).addView(beersView, 0);
				return beersView;
			}
			return null;
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == (View) object;
		}

	}

}
