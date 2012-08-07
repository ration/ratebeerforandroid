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

import com.ratebeer.android.R;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import com.actionbarsherlock.view.MenuInflater;
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
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
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
import com.ratebeer.android.app.location.SimpleItemizedOverlay;
import com.ratebeer.android.app.location.SimpleItemizedOverlay.OnBalloonClickListener;
import com.ratebeer.android.gui.components.ActivityUtil;
import com.ratebeer.android.gui.components.ArrayAdapter;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitleProvider;

public class BrewerViewFragment extends RateBeerFragment implements OnBalloonClickListener {

	protected static final String BASE_URI_FACEBOOK = "https://www.facebook.com/%1$s";
	protected static final String BASE_URI_TWITTER = "https://twitter.com/%1$s";

	private static final String STATE_BREWERID = "brewerId";
	private static final String STATE_BREWER = "brewer";
	private static final String STATE_BEERS = "beers";

	private static final int MENU_SHARE = 0;

	private LayoutInflater inflater;
	private ViewPager pager;
	private TextView nameText, descriptionText;
	private Button locationText, websiteButton, facebookButton, twitterButton;
	private FrameLayout mapFrame;
	private ListView beersView;

	private int brewerId;
	private BrewerDetails brewer;
	private ArrayList<BeerSearchResult> beers = new ArrayList<BeerSearchResult>();

	public BrewerViewFragment() {
	}

	public BrewerViewFragment(int placeId) {
		this.brewerId = placeId;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		this.inflater = inflater;
		return inflater.inflate(R.layout.fragment_brewerview, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		pager = (ViewPager) getView().findViewById(R.id.pager);
		BrewerPagerAdapter brewerPagerAdapter = new BrewerPagerAdapter();
		pager.setAdapter(brewerPagerAdapter);
		TabPageIndicator titles = (TabPageIndicator) getView().findViewById(R.id.titles);
		titles.setViewPager(pager);
		beersView.setOnItemClickListener(onBeerSelected);

		if (savedInstanceState != null) {
			brewerId = savedInstanceState.getInt(STATE_BREWERID);
			if (savedInstanceState.containsKey(STATE_BREWER)) {
				brewer = savedInstanceState.getParcelable(STATE_BREWER);
			}
			if (savedInstanceState.containsKey(STATE_BEERS)) {
				beers = savedInstanceState.getParcelableArrayList(STATE_BEERS);
			}
		} else {
			// Retrieve brewer details and beers
			refreshDetails();
			refreshBeers();
		}
		// Publish the current details, even when it is not loaded yet (and thus still empty)
		publishDetails(brewer);
		publishBeers(beers);

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem item = menu.add(RateBeerActivity.MENU_REFRESH, RateBeerActivity.MENU_REFRESH,
				RateBeerActivity.MENU_REFRESH, R.string.app_refresh);
		item.setIcon(R.drawable.ic_action_refresh);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		MenuItem item2 = menu.add(Menu.NONE, MENU_SHARE, MENU_SHARE, R.string.app_share);
		item2.setIcon(R.drawable.ic_action_share);
		item2.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case RateBeerActivity.MENU_REFRESH:
			refreshDetails();
			refreshBeers();
			break;
		case MENU_SHARE:
			if (brewer != null) {
				// Start a share intent for this event
				Intent s = new Intent(Intent.ACTION_SEND);
				s.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				s.setType("text/plain");
				s.putExtra(Intent.EXTRA_TEXT,
						getString(R.string.brewers_share, brewer.brewerName, Integer.toString(brewer.brewerId)));
				startActivity(Intent.createChooser(s, getString(R.string.brewers_sharebrewer)));
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_BREWERID, brewerId);
		if (brewer != null) {
			outState.putParcelable(STATE_BREWER, brewer);
		}
		if (beers != null) {
			outState.putParcelableArrayList(STATE_BEERS, beers);
		}
	}

	private void refreshDetails() {
		execute(new GetBrewerDetailsCommand(getRateBeerActivity().getApi(), brewerId));
	}

	private void refreshBeers() {
		execute(new GetBrewerBeersCommand(getRateBeerActivity().getApi(), brewerId,
				getRateBeerActivity().getUser() != null ? getRateBeerActivity().getUser().getUserID()
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
				Toast.makeText(getActivity(), getString(R.string.search_aliasedbeer), Toast.LENGTH_LONG).show();
				return;
			}
			getRateBeerActivity().load(new BeerViewFragment(item.beerName, item.beerId, item.rateCount));
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
		MapView mapView = getRateBeerActivity().requestMapViewInstance();
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
					GeoPoint center = getRateBeerActivity().getPoint(point.get(0).getLatitude(), point.get(0).getLongitude());
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
				convertView = inflater.inflate(R.layout.list_item_beersearchresult, null);
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
			if (getActivity() != null) {
				holder.beer.setText(item.beerName);
				holder.overall.setText((item.overallPerc >= 0 ? Integer.toString(item.overallPerc) : "?"));
				holder.count.setText(Integer.toString(item.rateCount) + " " + getString(R.string.details_ratings));
				holder.rated.setVisibility(item.isRated ? View.VISIBLE : View.GONE);
				holder.retired.setVisibility(item.isRetired ? View.VISIBLE : View.GONE);
				holder.alias.setVisibility(item.isAlias ? View.VISIBLE : View.GONE);
			}

			return convertView;
		}

	}

	protected static class BeerViewHolder {
		TextView beer, overall, count, rated, retired, alias;
	}

	private class BrewerPagerAdapter extends PagerAdapter implements TitleProvider {

		private View pagerDetailsView;
		private ListView pagerBeersView;

		public BrewerPagerAdapter() {
			LayoutInflater inflater = getActivity().getLayoutInflater();
			pagerDetailsView = (ScrollView) inflater.inflate(R.layout.fragment_brewerdetails, null);
			pagerBeersView = (ListView) inflater.inflate(R.layout.fragment_pagerlist, null);

			beersView = pagerBeersView;

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
		public String getTitle(int position) {
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
				((ViewPager) container).addView(pagerBeersView, 0);
				return pagerBeersView;
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

		@Override
		public void finishUpdate(View container) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View container) {
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

	}

}
