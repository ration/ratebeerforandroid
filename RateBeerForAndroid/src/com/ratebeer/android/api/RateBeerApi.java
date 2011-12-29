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
package com.ratebeer.android.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.drawable.Drawable;

import com.ratebeer.android.api.command.AddAvailabilityCommand;
import com.ratebeer.android.api.command.AddToCellarCommand;
import com.ratebeer.android.api.command.CheckInCommand;
import com.ratebeer.android.api.command.DeleteBeerMailCommand;
import com.ratebeer.android.api.command.GetAllBeerMailsCommand;
import com.ratebeer.android.api.command.GetAllBeerMailsCommand.Mail;
import com.ratebeer.android.api.command.GetAvailableBeersCommand;
import com.ratebeer.android.api.command.GetAvailableBeersCommand.AvailableBeer;
import com.ratebeer.android.api.command.GetBeerAvailabilityCommand;
import com.ratebeer.android.api.command.GetBeerDetailsCommand;
import com.ratebeer.android.api.command.GetBeerDetailsCommand.BeerDetails;
import com.ratebeer.android.api.command.GetBeerImageCommand;
import com.ratebeer.android.api.command.GetBeerMailCommand;
import com.ratebeer.android.api.command.GetBeerMailCommand.MailDetails;
import com.ratebeer.android.api.command.GetCheckinsCommand;
import com.ratebeer.android.api.command.GetCheckinsCommand.CheckedInUser;
import com.ratebeer.android.api.command.GetEventDetailsCommand;
import com.ratebeer.android.api.command.GetEventDetailsCommand.Attendee;
import com.ratebeer.android.api.command.GetEventDetailsCommand.EventDetails;
import com.ratebeer.android.api.command.GetEventsCommand;
import com.ratebeer.android.api.command.GetEventsCommand.Event;
import com.ratebeer.android.api.command.GetFavouritePlacesCommand;
import com.ratebeer.android.api.command.GetPlaceDetailsCommand;
import com.ratebeer.android.api.command.GetPlacesCommand;
import com.ratebeer.android.api.command.GetPlacesCommand.Place;
import com.ratebeer.android.api.command.GetRatingsCommand;
import com.ratebeer.android.api.command.GetRatingsCommand.BeerRating;
import com.ratebeer.android.api.command.GetStyleDetailsCommand;
import com.ratebeer.android.api.command.GetStyleDetailsCommand.StyleDetails;
import com.ratebeer.android.api.command.GetTopBeersCommand;
import com.ratebeer.android.api.command.GetTopBeersCommand.TopBeer;
import com.ratebeer.android.api.command.GetUserCellarCommand;
import com.ratebeer.android.api.command.GetUserCellarCommand.CellarBeer;
import com.ratebeer.android.api.command.GetUserDetailsCommand;
import com.ratebeer.android.api.command.GetUserDetailsCommand.RecentBeerRating;
import com.ratebeer.android.api.command.GetUserDetailsCommand.UserDetails;
import com.ratebeer.android.api.command.GetUserIdCommand;
import com.ratebeer.android.api.command.GetUserImageCommand;
import com.ratebeer.android.api.command.GetUserRatingCommand;
import com.ratebeer.android.api.command.GetUserRatingCommand.OwnBeerRating;
import com.ratebeer.android.api.command.GetUserRatingsCommand;
import com.ratebeer.android.api.command.GetUserRatingsCommand.UserRating;
import com.ratebeer.android.api.command.PostRatingCommand;
import com.ratebeer.android.api.command.RemoveFromCellarCommand;
import com.ratebeer.android.api.command.SearchBeersCommand;
import com.ratebeer.android.api.command.SearchBeersCommand.BeerSearchResult;
import com.ratebeer.android.api.command.SearchPlacesCommand;
import com.ratebeer.android.api.command.SearchPlacesCommand.PlaceSearchResult;
import com.ratebeer.android.api.command.SearchUsersCommand;
import com.ratebeer.android.api.command.SearchUsersCommand.UserSearchResult;
import com.ratebeer.android.api.command.SendBeerMailCommand;
import com.ratebeer.android.api.command.SetDrinkingStatusCommand;
import com.ratebeer.android.api.command.SetEventAttendanceCommand;
import com.ratebeer.android.api.command.SignInCommand;
import com.ratebeer.android.app.ApplicationSettings;

public class RateBeerApi implements CommandService {

	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("M/d/yyyy");
	private static final String RB_KEY = HttpHelper.RB_KEY;
	private static final String UTF8 = HttpHelper.UTF8;

	private final ApplicationSettings applicationSettings;

	public RateBeerApi(ApplicationSettings applicationSettings) {
		this.applicationSettings = applicationSettings;
	}

	@Override
	public CommandResult execute(Command command) {
		try {
			// User settings are retrieved every time from the application settings to be sure they reflect the current
			// user
			UserSettings userSettings = applicationSettings.getUserSettings();

			switch (command.getMethod()) {
			case SignIn:
				SignInCommand signInCommand = (SignInCommand) command;
				if (HttpHelper.signIn(signInCommand.getUsername(), signInCommand.getPassword())) {
					return new CommandSuccessResult(command);
				}
				throw new ApiException(ApiException.ExceptionType.AuthenticationFailed,
						"Tried to sign in but no (login) cookies were returned by the server");

			case SignOut:
				HttpHelper.makeRBGet("http://www.ratebeer.com/Signout.asp?v=1");
				if (!HttpHelper.isSignedIn()) {
					return new CommandSuccessResult(command);
				}
				throw new ApiException(ApiException.ExceptionType.CommandFailed,
						"Tried to log out but we still have session cookies");

			case GetUserId:
				ensureLogin(userSettings);
				return parseUserId((GetUserIdCommand) command, HttpHelper.makeRBGet("http://www.ratebeer.com/inbox"));

			case SearchUsers:
				SearchUsersCommand searchUsersCommand = (SearchUsersCommand) command;
				return parseUserSearchResults(searchUsersCommand,
						HttpHelper.makeRBPost("http://www.ratebeer.com/usersearch.php",
						Arrays.asList(new BasicNameValuePair("UserName", searchUsersCommand.getNormalizedQuery()))));

			case GetUserDetails:
				GetUserDetailsCommand getUserDetailsCommand = (GetUserDetailsCommand) command;
				return parseUserDetails(getUserDetailsCommand,
						HttpHelper.makeRBGet("http://www.ratebeer.com/user/" + getUserDetailsCommand.getUserId() + "/"));

			case GetUserRatings:
				GetUserRatingsCommand getUserRatingsCommand = (GetUserRatingsCommand) command;
				return parseUserRatings(
						getUserRatingsCommand,
						HttpHelper.makeRBGet("http://www.ratebeer.com/user/" + getUserRatingsCommand.getForUserId()
								+ "/ratings/" + getUserRatingsCommand.getPageNr() + "/"
								+ getUserRatingsCommand.getSortOrder() + "/"));

			case GetUserRating:
				ensureLogin(userSettings);
				GetUserRatingCommand getUserRatingCommand = (GetUserRatingCommand) command;
				return parseUserRating(
						getUserRatingCommand,
						HttpHelper.makeRBGet("http://www.ratebeer.com/beer/rate/" + getUserRatingCommand.getBeerId()
								+ "/"));

			case GetUserCellar:
				GetUserCellarCommand getUserCellarCommand = (GetUserCellarCommand) command;
				return parseUserCellar(
						getUserCellarCommand,
						HttpHelper.makeRBGet("http://www.ratebeer.com/user/" + getUserCellarCommand.getForUserId()
								+ "/cellar/"));

			case GetUserImage:
				GetUserImageCommand getUserImageCommand = (GetUserImageCommand) command;
				return parseUserImage(
						getUserImageCommand,
						HttpHelper.makeRawRBGet("http://www.ratebeer.com/UserPics/" + getUserImageCommand.getUsername()
								+ ".jpg"));

			case SearchBeers:
				SearchBeersCommand searchCommand = (SearchBeersCommand) command;
				return parseSearchResults(
						searchCommand,
						HttpHelper.makeRBGet("http://www.ratebeer.com/json/s.asp?k="
								+ RB_KEY
								+ "&b="
								+ URLEncoder.encode(searchCommand.getNormalizedQuery(), UTF8)
								+ (searchCommand.getUserId() != SearchBeersCommand.NO_USER ? "&u="
										+ searchCommand.getUserId() : "")));

			case GetBeerDetails:
				GetBeerDetailsCommand getBeerDetailsCommand = (GetBeerDetailsCommand) command;
				return parseBeerDetails(
						getBeerDetailsCommand,
						HttpHelper.makeRBGet("http://www.ratebeer.com/json/bff.asp?k=" + RB_KEY + "&bd="
								+ getBeerDetailsCommand.getBeerId()));

			case GetBeerImage:
				GetBeerImageCommand getBeerImageCommand = (GetBeerImageCommand) command;
				return parseBeerImage(
						getBeerImageCommand,
						HttpHelper.makeRawRBGet("http://www.ratebeer.com/beerimages/" + getBeerImageCommand.getBeerId()
								+ ".jpg"));

			case GetBeerRatings:
				GetRatingsCommand getBeerRatingsCommand = (GetRatingsCommand) command;
				return parseRatings(
						getBeerRatingsCommand,
						HttpHelper.makeRBGet("http://www.ratebeer.com/json/gr.asp?k=" + RB_KEY + "&bid="
								+ getBeerRatingsCommand.getBeerId()));

			case GetStyleDetails:
				GetStyleDetailsCommand getStyleDetailsCommand = (GetStyleDetailsCommand) command;
				return parseStyleBeers(
						getStyleDetailsCommand,
						HttpHelper.makeRBGet("http://www.ratebeer.com/beerstyles/s/"
								+ getStyleDetailsCommand.getStyleId() + "/"));

			case GetTopBeers:
				GetTopBeersCommand getTopBeersCommand = (GetTopBeersCommand) command;
				switch (getTopBeersCommand.getTopList()) {
				case Top50:
					// TODO: Replace with API call http://www.ratebeer.com/json/tb.asp?m=top50&k=<KEY>
					return parseTop50Beers(getTopBeersCommand,
							HttpHelper.makeRBGet("http://www.ratebeer.com/beer/top-50/"));
				case TopByCountry:
					// TODO: Replace with API call http://www.ratebeer.com/json/tb.asp?m=country&c=<COUNTRYID>&k=<KEY>
					return parseTopByCountryBeers(
							getTopBeersCommand,
							HttpHelper.makeRBGet("http://www.ratebeer.com/beer/country/"
									+ getTopBeersCommand.getCountry().getCode() + "/"
									+ getTopBeersCommand.getCountry().getId() + "/"));
				}
				return null;

			case SearchPlaces:
				SearchPlacesCommand searchPlacesCommand = (SearchPlacesCommand) command;
				return parsePlaceSearchResults(
						searchPlacesCommand,
						HttpHelper.makeRBGet("http://www.ratebeer.com/json/psstring.asp?k="
								+ RB_KEY
								+ "&s="
								+ URLEncoder.encode(searchPlacesCommand.getNormalizedQuery(), UTF8)));

			case GetPlacesAround:
				GetPlacesCommand getPlacesCommand = (GetPlacesCommand) command;
				return parsePlaces(
						getPlacesCommand,
						HttpHelper.makeRBGet("http://ratebeer.com/json/beerme.asp?k=" + RB_KEY + "&mi="
								+ getPlacesCommand.getRadius() + "&la=" + getPlacesCommand.getLatitude() + "&lo="
								+ getPlacesCommand.getLongitude()));

			case GetAvailableBeers:
				GetAvailableBeersCommand getAvailableBeersCommand = (GetAvailableBeersCommand) command;
				return parseAvailableBeers(
						getAvailableBeersCommand,
						HttpHelper.makeRBGet("http://ratebeer.com/json/beershere.asp?k=" + RB_KEY + "&pid="
								+ getAvailableBeersCommand.getPlaceId()));

			case GetBeerAvailability:
				GetBeerAvailabilityCommand getBeerAvailabilityCommand = (GetBeerAvailabilityCommand) command;
				return parseBeerAvailability(
						getBeerAvailabilityCommand,
						HttpHelper.makeRBGet("http://ratebeer.com/json/where?k=" + RB_KEY + "&bd="
								+ getBeerAvailabilityCommand.getBeerID()));

			case GetEvents:
				ensureLogin(userSettings);
				GetEventsCommand getEventsCommand = (GetEventsCommand) command;
				return parseEvents(
						getEventsCommand,
						HttpHelper.makeRBGet("http://www.ratebeer.com/FestsInMyArea.asp?CountryID="
								+ getEventsCommand.getCountry().getId()
								+ (getEventsCommand.getState() == null ? "" : "&StateID="
										+ getEventsCommand.getState().getId())));

			case GetEventDetails:
				GetEventDetailsCommand getEventDetailsCommand = (GetEventDetailsCommand) command;
				return parseEventDetails(
						getEventDetailsCommand,
						HttpHelper.makeRBGet("http://www.ratebeer.com/Events-Detail.asp?EventID="
								+ getEventDetailsCommand.getEventId()));

			case SetEventAttendance:
				ensureLogin(userSettings);
				SetEventAttendanceCommand setEventAttendanceCommand = (SetEventAttendanceCommand) command;
				HttpHelper.makeRBPost("http://www.ratebeer.com/eventprocess-attend.asp", Arrays.asList(
						new BasicNameValuePair("EventID", Integer.toString(setEventAttendanceCommand.getEventId())),
						new BasicNameValuePair("IsGoing", setEventAttendanceCommand.isGoing() ? "1" : "0")),
				// Note that we get an HTTP 500 response even when the request is successfull...
						HttpStatus.SC_INTERNAL_SERVER_ERROR);
				return new CommandSuccessResult(setEventAttendanceCommand);

			case SetDrinkingStatus:
				ensureLogin(userSettings);
				SetDrinkingStatusCommand setDrinkingStatusCommand = (SetDrinkingStatusCommand) command;
				HttpHelper.makeRBPost("http://www.ratebeer.com/userstatus-process.asp",
						Arrays.asList(new BasicNameValuePair("MyStatus", setDrinkingStatusCommand.getNewStatus())),
						// Note that we get an HTTP 500 response even when the request is successfull...
						HttpStatus.SC_INTERNAL_SERVER_ERROR);
				return new CommandSuccessResult(setDrinkingStatusCommand);

			case PostRating:
				ensureLogin(userSettings);
				PostRatingCommand postRatingCommand = (PostRatingCommand) command;
				// NOTE: Maybe use the API call to http://www.ratebeer.com/m/m_saverating.asp, but it should be checked
				// whether this allows updating of a rating too
				HttpHelper.makeRBPost(postRatingCommand.getRatingID() <= 0 ? "http://www.ratebeer.com/saverating.asp"
						: "http://www.ratebeer.com/updaterating.asp", Arrays.asList(new BasicNameValuePair("BeerID",
						Integer.toString(postRatingCommand.getBeerId())), new BasicNameValuePair("RatingID",
						postRatingCommand.getRatingID() <= 0 ? "" : Integer.toString(postRatingCommand.getRatingID())),
						new BasicNameValuePair("OrigDate", postRatingCommand.getOriginalPostDate() == null ? ""
								: postRatingCommand.getOriginalPostDate()),
						new BasicNameValuePair("aroma", Integer.toString(postRatingCommand.getAroma())),
						new BasicNameValuePair("appearance", Integer.toString(postRatingCommand.getAppearance())),
						new BasicNameValuePair("flavor", Integer.toString(postRatingCommand.getTaste())),
						new BasicNameValuePair("palate", Integer.toString(postRatingCommand.getPalate())),
						new BasicNameValuePair("overall", Integer.toString(postRatingCommand.getOverall())),
						new BasicNameValuePair("totalscore", Float.toString(postRatingCommand.getTotal())),
						new BasicNameValuePair("Comments", postRatingCommand.getComment())));
				return new CommandSuccessResult(postRatingCommand);

			case GetPlaceDetails:
				GetPlaceDetailsCommand getPlaceDetailsCommand = (GetPlaceDetailsCommand) command;
				return parsePlaceDetails(
						getPlaceDetailsCommand,
						HttpHelper.makeRBGet("http://www.ratebeer.com/json/pss.asp?pid="
								+ Integer.toString(getPlaceDetailsCommand.getPlaceId()) + "&k=" + RB_KEY));

			case CheckIn:
				ensureLogin(userSettings);
				CheckInCommand checkInCommand = (CheckInCommand) command;
				HttpHelper.makeRBGet("http://www.ratebeer.com/json/ci.asp?t=Log&p="
						+ Integer.toString(checkInCommand.getPlaceID()) + "&k=" + RB_KEY);
				return new CommandSuccessResult(checkInCommand);

			case GetCheckins:
				GetCheckinsCommand getCheckinsCommand = (GetCheckinsCommand) command;
				return parseCheckins(
						getCheckinsCommand,
						HttpHelper.makeRBGet("http://www.ratebeer.com/json/ci.asp?t=View&p="
								+ Integer.toString(getCheckinsCommand.getPlaceID()) + "&k=" + RB_KEY));

			case GetFavouritePlaces:
				ensureLogin(userSettings);
				GetFavouritePlacesCommand getFavPlacesCommand = (GetFavouritePlacesCommand) command;
				return parseFavouritePlaces(
						getFavPlacesCommand,
						HttpHelper.makeRBGet("http://www.ratebeer.com/beer/availability-add/"
								+ getFavPlacesCommand.getBeerId() + "/"));

			case AddAvailability:
				ensureLogin(userSettings);
				AddAvailabilityCommand addAvCommand = (AddAvailabilityCommand) command;
				List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>(Arrays.asList(
						new BasicNameValuePair("UserID", Integer.toString(userSettings.getUserID())),
						new BasicNameValuePair("BeerID", Integer.toString(addAvCommand.getBeerId())),
						new BasicNameValuePair("PlaceName",
								addAvCommand.getExtraPlaceId() == AddAvailabilityCommand.NO_EXTRA_PLACE ? ""
										: addAvCommand.getExtraPlaceName()),
						new BasicNameValuePair("PlaceName2",
								addAvCommand.getExtraPlaceId() == AddAvailabilityCommand.NO_EXTRA_PLACE ? ""
										: addAvCommand.getExtraPlaceName()),
						new BasicNameValuePair("PlaceID",
								addAvCommand.getExtraPlaceId() == AddAvailabilityCommand.NO_EXTRA_PLACE ? "" : Integer
										.toString(addAvCommand.getExtraPlaceId()))));
				for (int p = 0; p < addAvCommand.getSelectedFavourites().length; p++) {
					params.add(new BasicNameValuePair("placeid",
							Integer.toString(addAvCommand.getSelectedFavourites()[p])));
				}
				if (addAvCommand.isOnBottleCan()) {
					params.add(new BasicNameValuePair("ServedBottle", "on"));
				}
				if (addAvCommand.isOnTap()) {
					params.add(new BasicNameValuePair("ServedTap", "on"));
				}
				HttpHelper.makeRBPost("http://www.ratebeer.com/Ratings/Beer/Avail-Save.asp", params);
				return new CommandSuccessResult(addAvCommand);

			case AddToCellar:
				ensureLogin(userSettings);
				AddToCellarCommand addToCellarCommand = (AddToCellarCommand) command;
				if (addToCellarCommand.isWant()) {
					HttpHelper.makeRBPost("http://www.ratebeer.com/beerlistwant-process.asp", Arrays.asList(
							new BasicNameValuePair("BeerID", Integer.toString(addToCellarCommand.getBeerId())),
							new BasicNameValuePair("memo", addToCellarCommand.getMemo()), new BasicNameValuePair(
									"submit", "Add")));
				} else {
					HttpHelper.makeRBPost("http://www.ratebeer.com/beerlisthave-process.asp", Arrays.asList(
							new BasicNameValuePair("BeerID", Integer.toString(addToCellarCommand.getBeerId())),
							new BasicNameValuePair("Update", "0"),
							new BasicNameValuePair("vintage", addToCellarCommand.getVintage()), new BasicNameValuePair(
									"memo", addToCellarCommand.getMemo()), new BasicNameValuePair("quantity",
									addToCellarCommand.getQuantity()), new BasicNameValuePair("submit", "Add")));
				}
				return new CommandSuccessResult(addToCellarCommand);

			case RemoveFromCellar:
				ensureLogin(userSettings);
				RemoveFromCellarCommand removeFromCellarCommand = (RemoveFromCellarCommand) command;
				HttpHelper.makeRBGet("http://www.ratebeer.com/WishList-Delete.asp?WishID="
						+ removeFromCellarCommand.getBeerId() + "&UserID=" + userSettings.getUserID());
				return new CommandSuccessResult(removeFromCellarCommand);

			case GetBeerMail:
				ensureLogin(userSettings);
				GetBeerMailCommand getBeerMailCommand = (GetBeerMailCommand) command;
				return parseBeerMail(
						(GetBeerMailCommand) command,
						HttpHelper.makeRBGet("http://www.ratebeer.com/json/msg.asp?k=" + RB_KEY + "&u="
								+ Integer.toString(userSettings.getUserID()) + "&mid="
								+ Integer.toString(getBeerMailCommand.getMessageID())));

			case GetAllBeerMails:
				ensureLogin(userSettings);
				GetAllBeerMailsCommand getAllBeerMailsCommand = (GetAllBeerMailsCommand) command;
				return parseAllBeerMails(
						getAllBeerMailsCommand,
						HttpHelper.makeRBGet("http://www.ratebeer.com/json/msg.asp?k=" + RB_KEY + "&u="
								+ Integer.toString(userSettings.getUserID()) + "&max="
								+ Integer.toString(getAllBeerMailsCommand.getCount())));

			case DeleteBeerMail:
				ensureLogin(userSettings);
				DeleteBeerMailCommand deleteBeerMailCommand = (DeleteBeerMailCommand) command;
				HttpHelper.makeRBGet("http://www.ratebeer.com/DeleteMessage.asp?MessageID="
						+ Integer.toString(deleteBeerMailCommand.getMail().getMessageId()));
				return new CommandSuccessResult(deleteBeerMailCommand);

			case SendBeerMail:
				ensureLogin(userSettings);
				SendBeerMailCommand sendBeerMailCommand = (SendBeerMailCommand) command;
				HttpHelper.makeRBPost("http://www.ratebeer.com/savemessage/",
						Arrays.asList(new BasicNameValuePair("nSource", Integer.toString(userSettings.getUserID())),
								new BasicNameValuePair("Referrer", "http://www.ratebeer.com/user/messages/0/"),
								new BasicNameValuePair("UserName", "0"),
								new BasicNameValuePair("RecipientName", sendBeerMailCommand.getSendTo()),
								new BasicNameValuePair("Subject", sendBeerMailCommand.getSubject()),
								new BasicNameValuePair("Body", sendBeerMailCommand.getBody())));
				return new CommandSuccessResult(sendBeerMailCommand);

			default:
				return null;
			}
		} catch (ApiException e) {
			return new CommandFailureResult(command, e);
		} catch (UnknownHostException e) {
			return new CommandFailureResult(command, new ApiException(ApiException.ExceptionType.Offline, e.toString()));
		} catch (HttpHostConnectException e) {
			return new CommandFailureResult(command, new ApiException(ApiException.ExceptionType.Offline, e.toString()));
		} catch (Exception e) {
			return new CommandFailureResult(command, new ApiException(ApiException.ExceptionType.CommandFailed,
					e.toString()));
		}
	}

	private void ensureLogin(UserSettings userSettings) throws ClientProtocolException, IOException, ApiException {
		// Make sure we are logged in
		if (!HttpHelper.isSignedIn()) {
			if (!HttpHelper.signIn(userSettings.getUsername(), userSettings.getPassword())) {
				throw new ApiException(ApiException.ExceptionType.AuthenticationFailed,
						"Tried to sign in but no (login) cookies were returned by the server");
			}
		}

	}

	private CommandResult parseUserId(GetUserIdCommand command, String response) {

		// Searches for a unique text where the user ID can be found
		// Text looks something like: <div class="sitename"><a href="/user/101051/">erickok</a>
		// but can also be: <div class="sitename"><a href="http://www.ratebeer.com/user/101051/">erickok</a>
		String userIdText = "<a href=\"/user/";
		int userIdIndex = response.indexOf(userIdText);
		if (userIdIndex < 0) {
			return new CommandFailureResult(command, new ApiException(ApiException.ExceptionType.CommandFailed,
					"The user ID begin HTML string was not found..."));
		}
		// Get the end of the user URL, which is a /
		int userIdEnd = response.indexOf("/", userIdIndex + userIdText.length());
		int userId = Integer.parseInt(response.substring(userIdIndex + userIdText.length(), userIdEnd));

		// Also look for the drinking status
		/*
		 * String drinkingText = "TODO"; int drinkingStart = response.indexOf(drinkingText); int drinkingEnd =
		 * response.indexOf("TODO", drinkingStart + drinkingText.length());
		 */
		String drinkingStatus = "";
		/*
		 * if (drinkingStart >= 0 && drinkingEnd > 0) { drinkingStatus = response.substring(drinkingStart +
		 * drinkingText.length(), drinkingEnd); int linkEnd = drinkingStatus.indexOf(">") + 1; if (linkEnd > 0 &&
		 * drinkingStatus.indexOf("<", linkEnd) > 0) { // Strip the 'a href' tag from this drinking status, leaving only
		 * the actual beer name drinkingStatus = drinkingStatus.substring(linkEnd, drinkingStatus.indexOf("<",
		 * linkEnd)); } drinkingStatus = HttpHelper.cleanHtml(drinkingStatus); }
		 */

		// And whether this user has a premium account
		int premiumStart = response.indexOf("<span class=premie>&nbsp;P&nbsp;</span>");

		// Get the user ID from the raw HTML and add it as result to the original command
		command.setUserId(userId);
		command.setDrinkingStatus(drinkingStatus);
		command.setPremium(premiumStart >= 0);
		return new CommandSuccessResult(command);
	}

	private CommandResult parseUserSearchResults(SearchUsersCommand command, String html) throws ParseException {

		// Results?
		int foundStart = html.indexOf("No users found");
		if (foundStart >= 0) {
			// Return an empty list as result
			command.setSearchResults(new ArrayList<UserSearchResult>());
			return new CommandSuccessResult(command);
		}

		// We are either send to a search results page or directly to the only found user's page
		int tableStart = html.indexOf("Found more than one user");
		if (tableStart < 0) {
			
			// Probably send directly to the single found user: parse that instead
			String idText = "<div class=\"sitename\">";
			int idStart = html.indexOf(idText);
			if (idStart < 0) {
				return new CommandFailureResult(command, new ApiException(ApiException.ExceptionType.CommandFailed,
						"The response HTML did not contain the unique identifiable HTML string"));
			}
			
			// Indeed a user page was found: return just the user name, id and number of ratings
			idStart += idText.length();
			String userName = html.substring(idStart, html.indexOf("<", idStart));

			int ratingsStart1 = html.indexOf("Last seen", idStart);
			int ratingsStart2 = html.indexOf("<b>", ratingsStart1) + "<b>".length();
			int ratings = Integer.parseInt(html.substring(ratingsStart2, html.indexOf("</b> beer", ratingsStart2)));

			int userIdStart = html.indexOf("/user/") + "/user/".length();
			int userId = Integer.parseInt(html.substring(userIdStart, html.indexOf("/", userIdStart)));
			
			// Make a list of just this user
			ArrayList<UserSearchResult> results = new ArrayList<UserSearchResult>();
			results.add(new UserSearchResult(userId, userName, ratings));
			command.setSearchResults(results);
			return new CommandSuccessResult(command);
			
		}
		
		// We are given a table of users and their number of ratings
		String rowText = "<TD class=\"beer\"><A HREF=\"/user/";
		int rowStart = html.indexOf(rowText, tableStart) + rowText.length();
		ArrayList<UserSearchResult> results = new ArrayList<UserSearchResult>();

		while (rowStart > 0 + rowText.length()) {

			int userId = Integer.parseInt(html.substring(rowStart, html.indexOf("/", rowStart)));

			int nameStart = html.indexOf(">", rowStart) + 1;
			String userName = HttpHelper.cleanHtml(html.substring(nameStart, html.indexOf("<", nameStart)));

			int ratingsStart = html.indexOf("<B>", nameStart) + "<B>".length();
			String ratingsRaw = html.substring(ratingsStart, html.indexOf("</B>", ratingsStart));
			int ratings = 0;
			if (ratingsRaw.length() > 0) {
				ratings = Integer.parseInt(html.substring(ratingsStart, html.indexOf("</B>", ratingsStart)));
			}

			results.add(new UserSearchResult(userId, userName, ratings));
			rowStart = html.indexOf(rowText, ratingsStart) + rowText.length();
		}

		// Set the list of found users on the original command as result
		command.setSearchResults(results);
		return new CommandSuccessResult(command);

	}

	private CommandResult parseUserDetails(GetUserDetailsCommand command, String html) throws ParseException {

		// Parse the user's details and recent ratings
		int userStart = html.indexOf("class=\"selected\">profile</a><br>");
		if (userStart < 0) {
			return new CommandFailureResult(command, new ApiException(ApiException.ExceptionType.CommandFailed,
					"The response HTML did not contain the unique event content string"));
		}

		int nameStart = html.indexOf("<div class=\"sitename\">", userStart) + "<div class=\"sitename\">".length();
		String name = html.substring(nameStart, html.indexOf("</div>", nameStart));

		int locationStart = html.indexOf("<span>", nameStart) + "<span>".length();
		String location = html.substring(locationStart, html.indexOf("<br>", locationStart)).trim();
		int ofEnd = location.indexOf("/>");
		if (ofEnd >= 0 && location.indexOf("<", ofEnd) > 0) {
			location = "of " + location.substring(ofEnd + 2, location.indexOf("<", ofEnd));
		}

		int joinedStart = html.indexOf("class=\"GrayItalic\">", locationStart) + "class=\"GrayItalic\">".length();
		String joined = html.substring(joinedStart, html.indexOf("<", joinedStart)).trim();

		int lastSeenStart = html.indexOf("class=\"GrayItalic\">", joinedStart) + "class=\"GrayItalic\">".length();
		String lastSeen = html.substring(lastSeenStart, html.indexOf("<", lastSeenStart));

		int beerRateCountStart = html.indexOf("<b>", lastSeenStart) + "<b>".length();
		int beerRateCount = Integer.parseInt(html.substring(beerRateCountStart,
				html.indexOf("</b>", beerRateCountStart)));

		int placeRateCountStart = html.indexOf("<b>", beerRateCountStart) + "<b>".length();
		int placeRateCount = Integer.parseInt(html.substring(placeRateCountStart,
				html.indexOf("</b>", placeRateCountStart)));

		int avgScoreGivenPresent = html.indexOf("Avg Score Given: ", placeRateCountStart);
		String avgScoreGiven = null;
		if (avgScoreGivenPresent >= 0) {
			int avgScoreGivenStart = avgScoreGivenPresent + "Avg Score Given: ".length();
			avgScoreGiven = HttpHelper.cleanHtml(html.substring(avgScoreGivenStart, html.indexOf(" ", avgScoreGivenStart)));
		}

		int avgBeerRatedPresent = html.indexOf("Avg Beer Rated: ", avgScoreGivenPresent);
		String avgBeerRated = null;
		if (avgBeerRatedPresent >= 0) {
			int avgBeerRatedStart = avgBeerRatedPresent + "Avg Beer Rated: ".length();
			avgBeerRated = HttpHelper.cleanHtml(html.substring(avgBeerRatedStart, html.indexOf("<", avgBeerRatedStart)));
		}

		String styleText = "Favorite style: <a href=\"/beerstyles/";
		int styleStart = html.indexOf(styleText, placeRateCountStart);
		int styleId = -1;
		String styleName = null;
		if (styleStart >= 0) {
			styleStart += styleText.length();
			int styleIdStart = html.indexOf("/", styleStart) + 1;
			styleId = Integer.parseInt(html.substring(styleIdStart, html.indexOf("/", styleIdStart)));
			int styleNameStart = html.indexOf("<b>", styleStart) + "<b>".length();
			styleName = HttpHelper.cleanHtml(html.substring(styleNameStart, html.indexOf("</b>", styleNameStart)));
		}

		List<RecentBeerRating> ratings = new ArrayList<RecentBeerRating>();
		String ratingText = "style=\"height: 21px;\"><A HREF=\"/beer/";
		int ratingStart = html.indexOf(ratingText, styleStart);
		while (ratingStart >= 0) {
			int beerIdStart = html.indexOf("/", ratingStart + ratingText.length()) + 1;
			int beerId = Integer.parseInt(html.substring(beerIdStart, html.indexOf("/", beerIdStart)));

			int beerNameStart = html.indexOf(">", beerIdStart) + ">".length();
			String beerName = HttpHelper.cleanHtml(html.substring(beerNameStart, html.indexOf("<", beerNameStart)));

			int beerStyleStart = html.indexOf("smallGray\">", beerNameStart) + "smallGray\">".length();
			String beerStyle = HttpHelper.cleanHtml(html.substring(beerStyleStart, html.indexOf("<", beerStyleStart)));

			int beerRatingStart = html.indexOf("bold;\">", beerStyleStart) + "bold;\">".length();
			String beerRating = html.substring(beerRatingStart, html.indexOf("<", beerRatingStart));

			int beerDateStart = html.indexOf("smallGray\">", beerRatingStart) + "smallGray\">".length();
			String beerDate = html.substring(beerDateStart, html.indexOf("<", beerDateStart));

			ratings.add(new RecentBeerRating(beerId, beerName, beerStyle, beerRating, beerDate));
			ratingStart = html.indexOf(ratingText, beerDateStart);
		}

		// Set the user's rating on the original command as result
		command.setDetails(new UserDetails(name, joined, lastSeen, location, beerRateCount, placeRateCount,
				avgScoreGiven, avgBeerRated, styleName, styleId, ratings));
		return new CommandSuccessResult(command);

	}

	private CommandResult parseUserRatings(GetUserRatingsCommand command, String html) throws ParseException {

		// Parse the beer ratings table
		int tableStart = html.indexOf("<!-- RATINGS -->");
		if (tableStart < 0) {
			return new CommandFailureResult(command, new ApiException(ApiException.ExceptionType.CommandFailed,
					"The response HTML did not contain the unique ratings table begin HTML string"));
		}
		String rowText = "\"><A HREF=\"/beer/";
		int rowStart = html.indexOf(rowText, tableStart) + rowText.length();
		ArrayList<UserRating> ratings = new ArrayList<UserRating>();

		while (rowStart > 0 + rowText.length()) {

			int idStart = html.indexOf("/", rowStart) + 1;
			int beerId = Integer.parseInt(html.substring(idStart, html.indexOf("/", idStart)));

			int beerStart = html.indexOf(">", idStart) + 1;
			String beerName = HttpHelper.cleanHtml(html.substring(beerStart, html.indexOf("<", beerStart)));

			int brewerStart = html.indexOf("/\">", beerStart) + 3;
			String brewerName = HttpHelper.cleanHtml(html.substring(brewerStart, html.indexOf("<", brewerStart)));

			int styleStart = html.indexOf("\">", brewerStart) + "\">".length();
			String styleName = HttpHelper.cleanHtml(html.substring(styleStart, html.indexOf("<", styleStart)));

			int scoreStart = html.indexOf("center>", styleStart) + "center>".length();
			float score = Float.parseFloat(html.substring(scoreStart, html.indexOf("<", scoreStart)));

			int myRatingStart = html.indexOf("center>", scoreStart) + "center>".length();
			float myRating = Float.parseFloat(html.substring(myRatingStart, html.indexOf("<", myRatingStart)));

			int dateStart = html.indexOf("align=right>", myRatingStart) + "align=right>".length();
			Date date = DATE_FORMATTER.parse(html.substring(dateStart, html.indexOf("&", dateStart)));

			ratings.add(new UserRating(beerId, beerName, brewerName, styleName, score, myRating, date));
			rowStart = html.indexOf(rowText, dateStart) + rowText.length();
		}

		// Set the list of user ratings on the original command as result
		command.setUserRatings(ratings);
		return new CommandSuccessResult(command);

	}

	private CommandResult parseUserRating(GetUserRatingCommand command, String html) throws ParseException {

		// Parse the user's existing rating
		int ratingStart = html.indexOf("name=OrigDate id=OrigDate value=\"");
		if (ratingStart < 0) {
			return new CommandFailureResult(command, new ApiException(ApiException.ExceptionType.CommandFailed,
					"The response HTML did not contain the unique rating content string"));
		}

		int origDateStart = ratingStart + "name=OrigDate id=OrigDate value=\"".length();
		String origDate = html.substring(origDateStart, html.indexOf("\"", origDateStart));
		if (origDate.length() <= 0) {
			// Not yet rated!
			command.setRating(null);
			return new CommandSuccessResult(command);
		}
		int ratingIDStart = html.indexOf("name=RatingID id=RatingID value=\"", ratingStart)
				+ "name=RatingID id=RatingID value=\"".length();
		int ratingID = Integer.parseInt(html.substring(ratingIDStart, html.indexOf("\"", ratingIDStart)));

		int numbersStart = html.indexOf("<strong>Aroma</strong>");
		String selectedText = "SELECTED";

		int aromaStart = html.indexOf(selectedText, numbersStart) - 3;
		int aroma = Integer.parseInt(html.substring(aromaStart, html.indexOf(" ", aromaStart)).replace("=", ""));

		int appearanceStart = html.indexOf(selectedText, aromaStart + selectedText.length()) - 3;
		int appearance = Integer.parseInt(html.substring(appearanceStart, html.indexOf(" ", appearanceStart)).replace(
				"=", ""));

		int tasteStart = html.indexOf(selectedText, appearanceStart + selectedText.length()) - 3;
		int taste = Integer.parseInt(html.substring(tasteStart, html.indexOf(" ", tasteStart)).replace("=", ""));

		int palateStart = html.indexOf(selectedText, tasteStart + selectedText.length()) - 3;
		int palate = Integer.parseInt(html.substring(palateStart, html.indexOf(" ", palateStart)).replace("=", ""));

		int overallStart = html.indexOf(selectedText, palateStart + selectedText.length()) - 3;
		int overall = Integer.parseInt(html.substring(overallStart, html.indexOf(" ", overallStart)).replace("=", ""));

		String commentsText = " class=\"normBack\">";
		int commentsStart = html.indexOf(commentsText, overallStart) + commentsText.length();
		String comments = HttpHelper.cleanHtml(html.substring(commentsStart, html.indexOf("<", commentsStart)));

		// Set the user's rating on the original command as result
		command.setRating(new OwnBeerRating(ratingID, origDate, appearance, aroma, taste, palate, overall, comments));
		return new CommandSuccessResult(command);

	}

	private CommandResult parseUserCellar(GetUserCellarCommand command, String html) throws ParseException {

		// Parse the user's cellar
		int wantsStart = html.indexOf("Wants");
		int havesStart = html.indexOf("Haves");
		if (wantsStart < 0 || havesStart < 0) {
			return new CommandFailureResult(command, new ApiException(ApiException.ExceptionType.CommandFailed,
					"The response HTML did not contain the unique wants/haves content string"));
		}

		ArrayList<CellarBeer> wants = new ArrayList<CellarBeer>();
		ArrayList<CellarBeer> haves = new ArrayList<CellarBeer>();
		String wantRowText = "/wishlist/have/";
		int wantRowStart = html.indexOf(wantRowText, wantsStart) + wantRowText.length();
		while (wantRowStart > 0 + wantRowText.length() && wantRowStart < havesStart) {

			int beerId = Integer.parseInt(html.substring(wantRowStart, html.indexOf("/", wantRowStart)));

			int beerStart = html.indexOf("/\">", wantRowStart + 10) + 3;
			String beerName = HttpHelper.cleanHtml(html.substring(beerStart, html.indexOf("<", beerStart)));

			int memoStart = html.indexOf(" height=1 width=45>", beerStart) + " height=1 width=45>".length();
			String memo = HttpHelper.cleanHtml(html.substring(memoStart, html.indexOf("<", memoStart))).trim();

			wants.add(new CellarBeer(beerId, beerName, memo, null, null));
			wantRowStart = html.indexOf(wantRowText, memoStart) + wantRowText.length();
		}
		String haveRowText = "/wishlist/want/";
		int haveRowStart = html.indexOf(haveRowText, havesStart) + haveRowText.length();
		if (wantRowStart < haveRowStart) {
			wantRowStart = haveRowStart;
		}
		while (haveRowStart > 0 + haveRowText.length()) {

			int beerId = Integer.parseInt(html.substring(haveRowStart, html.indexOf("/", haveRowStart)));

			int beerStart = html.indexOf("/\">", haveRowStart + 10) + 3;
			String beerName = HttpHelper.cleanHtml(html.substring(beerStart, html.indexOf("<", beerStart)));

			int quantityStart = html.indexOf("valign=bottom align=center>", beerStart)
					+ "valign=bottom align=center>".length();
			String quantity = HttpHelper.cleanHtml(html.substring(quantityStart, html.indexOf("</td>", quantityStart))).trim();
			if (quantity.equals(" <span class=beerfoot>available</span>")) {
				quantity = "available";
			}

			int memoStart = html.indexOf("beerfoot valign=top>", quantityStart) + "beerfoot valign=top>".length();
			String memo = HttpHelper.cleanHtml(html.substring(memoStart, html.indexOf("<br><span", memoStart))).trim();

			int vintageStart = html.indexOf("class=beerfoot><i>", beerStart) + "class=beerfoot><i>".length();
			String vintage = HttpHelper.cleanHtml(html.substring(vintageStart, html.indexOf("</i>", vintageStart))).trim();

			haves.add(new CellarBeer(beerId, beerName, memo, vintage, quantity));
			haveRowStart = html.indexOf(haveRowText, vintageStart) + haveRowText.length();
		}

		// Set the user's wants and haves on the original command as result
		command.setWantsAndHaves(wants, haves);
		return new CommandSuccessResult(command);

	}

	private CommandResult parseTop50Beers(GetTopBeersCommand command, String html) {

		// Parse the top beers table
		int tableStart = html.indexOf("<h1>the ratebeer top 50");
		if (tableStart < 0) {
			return new CommandFailureResult(command, new ApiException(ApiException.ExceptionType.CommandFailed,
					"The response HTML did not contain the unique top beers table begin HTML string"));
		}
		String rowText = "</TR><TR";
		int rowStart = html.indexOf(rowText, tableStart) + rowText.length();
		int tableEnd = html.indexOf("</TABLE>", rowStart) + "</TABLE>".length();
		ArrayList<TopBeer> beers = new ArrayList<TopBeer>();

		while (rowStart > 0 + rowText.length() && tableEnd > rowStart) {

			int orderStart = html.indexOf("class=\"dlo\">", rowStart) + "class=\"dlo\">".length();
			int orderNr = Integer.parseInt(html.substring(orderStart, html.indexOf("&", orderStart)));

			int idStart1 = html.indexOf("<A HREF=\"/beer/", orderStart) + "<A HREF=\"/beer/".length();
			int idStart2 = html.indexOf("/", idStart1) + "/".length();
			int beerId = Integer.parseInt(html.substring(idStart2, html.indexOf("/", idStart2)));

			int beerStart = html.indexOf(">", idStart2) + ">".length();
			String beerName = HttpHelper.cleanHtml(html.substring(beerStart, html.indexOf("<", beerStart)));

			int scoreStart = html.indexOf("<b>", beerStart) + "<b>".length();
			float score = Float.parseFloat(html.substring(scoreStart, html.indexOf("<", scoreStart)));

			int countStart = html.indexOf("#999999\">", scoreStart) + "#999999\">".length();
			int count = Integer.parseInt(html.substring(countStart, html.indexOf("&", countStart)));

			int styleStart = html.indexOf("/\">", countStart) + "/\">".length();
			String style = html.substring(styleStart, html.indexOf("<", styleStart));

			beers.add(new TopBeer(orderNr, beerId, beerName, score, count, style));
			rowStart = html.indexOf(rowText, styleStart) + rowText.length();
		}

		// Set the list of top beers on the original command as result
		command.setBeers(beers);
		return new CommandSuccessResult(command);

	}

	private CommandResult parseTopByCountryBeers(GetTopBeersCommand command, String html) {

		// Parse the top beers table
		int tableStart = html.indexOf("<h1>the best beers of ");
		if (tableStart < 0) {
			return new CommandFailureResult(command, new ApiException(ApiException.ExceptionType.CommandFailed,
					"The response HTML did not contain the unique top beers table begin HTML string"));
		}
		String rowText = "</TR><TR";
		int rowStart = html.indexOf(rowText, tableStart) + rowText.length();
		int tableEnd = html.indexOf("</TABLE>", rowStart) + "</TABLE>".length();
		ArrayList<TopBeer> beers = new ArrayList<TopBeer>();

		while (rowStart > 0 + rowText.length() && tableEnd > rowStart) {

			int orderStart = html.indexOf("#999999\">", rowStart) + "#999999\">".length();
			int orderNr = Integer.parseInt(html.substring(orderStart, html.indexOf("<", orderStart)));

			int idStart1 = html.indexOf("<A HREF=\"/beer/", orderStart) + "<A HREF=\"/beer/".length();
			int idStart2 = html.indexOf("/", idStart1) + "/".length();
			int beerId = Integer.parseInt(html.substring(idStart2, html.indexOf("/", idStart2)));

			int beerStart = html.indexOf(">", idStart2) + ">".length();
			String beerName = HttpHelper.cleanHtml(html.substring(beerStart, html.indexOf("<", beerStart)));

			int scoreStart = html.indexOf("<b>", beerStart) + "<b>".length();
			float score = Float.parseFloat(html.substring(scoreStart, html.indexOf("<", scoreStart)));

			int countStart = html.indexOf("#999999\">", scoreStart) + "#999999\">".length();
			int count = Integer.parseInt(html.substring(countStart, html.indexOf("&", countStart)));

			int styleStart = html.indexOf("/\">", countStart) + "/\">".length();
			String style = html.substring(styleStart, html.indexOf("<", styleStart));

			beers.add(new TopBeer(orderNr, beerId, beerName, score, count, style));
			rowStart = html.indexOf(rowText, styleStart) + rowText.length();
		}

		// Set the list of top beers on the original command as result
		command.setBeers(beers);
		return new CommandSuccessResult(command);

	}

	private CommandResult parseStyleBeers(GetStyleDetailsCommand command, String html) {

		// Parse the top beers table
		int tableStart = html.indexOf("<small><b>MORE BEER STYLES</b></small><br>");
		if (tableStart < 0) {
			return new CommandFailureResult(command, new ApiException(ApiException.ExceptionType.CommandFailed,
					"The response HTML did not contain the unique top beers of the style table begin HTML string"));
		}

		int nameStart = html.indexOf("<h1>") + "<h1>".length();
		String name = HttpHelper.cleanHtml(html.substring(nameStart, html.indexOf("</h1>", nameStart)));

		int descriptionStart = html.indexOf("\">", nameStart) + "\">".length();
		String description = HttpHelper.cleanHtml(html.substring(descriptionStart, html.indexOf("</div>", descriptionStart))
				.trim());

		String servedInText = ".gif\">&nbsp;";
		List<String> servedIn = new ArrayList<String>();
		int servedInStart = html.indexOf(servedInText, descriptionStart);
		while (servedInStart >= 0) {
			servedIn.add(HttpHelper.cleanHtml(html.substring(servedInStart, html.indexOf("<br>", servedInStart))));
			servedInStart = html.indexOf(servedInText, servedInStart + 1);
		}

		String rowText = "<td class=\"beer\"><font color=\"#999999\">";
		int rowStart = html.indexOf(rowText, servedInStart) + rowText.length();
		ArrayList<TopBeer> beers = new ArrayList<TopBeer>();

		while (rowStart > 0 + rowText.length()) {

			int orderNr = Integer.parseInt(html.substring(rowStart, html.indexOf("<", rowStart)));

			int idStart1 = html.indexOf("<A HREF=\"/beer/", rowStart) + "<A HREF=\"/beer/".length();
			int idStart2 = html.indexOf("/", idStart1) + "/".length();
			int beerId = Integer.parseInt(html.substring(idStart2, html.indexOf("/", idStart2)));

			int beerStart = html.indexOf(">", idStart2) + ">".length();
			String beerName = HttpHelper.cleanHtml(html.substring(beerStart, html.indexOf("<", beerStart)));

			int scoreStart = html.indexOf("<b>", beerStart) + "<b>".length();
			float score = Float.parseFloat(html.substring(scoreStart, html.indexOf("<", scoreStart)));

			int countStart = html.indexOf("#999999\">", scoreStart) + "#999999\">".length();
			int count = Integer.parseInt(html.substring(countStart, html.indexOf("&", countStart)));

			beers.add(new TopBeer(orderNr, beerId, beerName, score, count, Integer.toString(command.getStyleId())));
			rowStart = html.indexOf(rowText, countStart) + rowText.length();
		}

		// Set the list of top beers on the original command as result
		command.setDetails(new StyleDetails(name, description, servedIn, beers));
		return new CommandSuccessResult(command);

	}

	private CommandResult parseEvents(GetEventsCommand command, String html) {

		// Parse the events table
		int tableStart = html.indexOf("REGIONAL EVENTS");
		if (tableStart < 0) {
			return new CommandFailureResult(command, new ApiException(ApiException.ExceptionType.CommandFailed,
					"The response HTML did not contain the unique top beers table begin HTML string"));
		}
		String rowText = "Events-Detail.asp?EventID=";
		int rowPresent = html.indexOf(rowText, tableStart);
		if (rowPresent < 0) {
			command.setEvents(new ArrayList<GetEventsCommand.Event>());
			return new CommandSuccessResult(command);
		}
		int rowStart = rowPresent + rowText.length();
		ArrayList<Event> events = new ArrayList<Event>();
		SimpleDateFormat americanDate = new SimpleDateFormat("M/d/y");

		while (rowStart > 0 + rowText.length()) {

			int eventId = Integer.parseInt(html.substring(rowStart, html.indexOf("\"", rowStart)));

			int nameStart = html.indexOf(">", rowStart) + ">".length();
			String event = HttpHelper.cleanHtml(html.substring(nameStart, html.indexOf("<", nameStart)));

			int cityStart = html.indexOf("#999999>&nbsp;", nameStart) + "#999999>&nbsp;".length();
			String city = html.substring(cityStart, html.indexOf("&", cityStart));

			int dateStart = html.indexOf("#999999>&nbsp;", cityStart) + "#999999>&nbsp;".length();
			String dateString = html.substring(dateStart, html.indexOf("&", dateStart));
			Date date = null;
			try {
				date = americanDate.parse(dateString);
			} catch (ParseException e) {
			}

			events.add(new Event(eventId, event, city, date));
			rowStart = html.indexOf(rowText, dateStart) + rowText.length();
		}

		// Set the list of top beers on the original command as result
		command.setEvents(events);
		return new CommandSuccessResult(command);

	}

	private CommandResult parseEventDetails(GetEventDetailsCommand command, String html) throws ParseException {

		// Parse the user's existing rating
		int eventStart = html.indexOf("<a href=/events.php>Beer Events and Festivals</a>");
		if (eventStart < 0) {
			return new CommandFailureResult(command, new ApiException(ApiException.ExceptionType.CommandFailed,
					"The response HTML did not contain the unique event content string"));
		}

		int nameStart = html.indexOf("<h1>", eventStart) + "<h1>".length();
		String name = HttpHelper.cleanHtml(html.substring(nameStart, html.indexOf("</h1>", nameStart)));

		int daysStart = html.indexOf("<h2>", nameStart) + "<h2>".length();
		String days = html.substring(daysStart, html.indexOf("</h2>", daysStart));

		int timesStart = html.indexOf("<br>", daysStart) + "<br>".length();
		String times = html.substring(timesStart, html.indexOf("<br>", timesStart)).trim();

		int locationStart = html.indexOf("<br>", timesStart) + "<br>".length();
		String location = HttpHelper.cleanHtml(html.substring(locationStart, html.indexOf("<br>", locationStart)));

		int addressStart = html.indexOf("\">", locationStart) + "\">".length();
		String address = HttpHelper.cleanHtml(html.substring(addressStart, html.indexOf(" [ map ]", addressStart)).trim());

		String detailsText = "<strong><h3>Details</h3></strong><br>";
		int detailsStart = html.indexOf(detailsText, addressStart) + detailsText.length();
		String details = HttpHelper.cleanHtml(html.substring(detailsStart, html.indexOf("<b>Cost:</b>", detailsStart))).trim();

		String contactText = "<h3>Contact Info</h3><br>";
		int contactStart = html.indexOf(contactText, detailsStart) + contactText.length();
		String contact = HttpHelper.cleanHtml(html.substring(contactStart, html.indexOf("</i>", contactStart))).trim();

		List<Attendee> attendees = new ArrayList<Attendee>();
		String attendeeText = "<br><a href=/user/";
		int attendeeStart = html.indexOf(attendeeText, contactStart);
		while (attendeeStart >= 0) {
			int attendeeIdStart = attendeeStart + attendeeText.length();
			int attendeeIdEnd = html.indexOf("/", attendeeIdStart);
			int attendeeId = Integer.parseInt(html.substring(attendeeIdStart, attendeeIdEnd));
			String attendeeName = html.substring(attendeeIdEnd + 2, html.indexOf("<", attendeeIdEnd));
			attendees.add(new Attendee(attendeeName, attendeeId));
			attendeeStart = html.indexOf(attendeeText, attendeeIdStart);
		}

		// Set the user's rating on the original command as result
		command.setDetails(new EventDetails(name, days, times, location, address, null, details, contact, attendees));
		return new CommandSuccessResult(command);

	}

	private CommandResult parseFavouritePlaces(GetFavouritePlacesCommand command, String html) throws ParseException {

		// Parse the favourite places table
		int tableStart = html.indexOf("<div id=\"likely\"");
		if (tableStart < 0) {
			return new CommandFailureResult(command, new ApiException(ApiException.ExceptionType.CommandFailed,
					"The response HTML did not contain the unique favourites table begin HTML string"));
		}
		String rowText = "<INPUT name=\"placeid\" type=checkbox value=";
		int rowStart = html.indexOf(rowText, tableStart) + rowText.length();
		ArrayList<PlaceSearchResult> places = new ArrayList<PlaceSearchResult>();

		while (rowStart > 0 + rowText.length()) {

			int placeId = Integer.parseInt(html.substring(rowStart, html.indexOf(" ", rowStart)));

			int placeNameStart = html.indexOf(".click()\">", rowStart) + ".click()\">".length();
			String placeName = HttpHelper.cleanHtml(html.substring(placeNameStart, html.indexOf("<", placeNameStart)));

			int cityStart = html.indexOf("><em>in ", placeNameStart) + "><em>in ".length();
			String city = html.substring(cityStart, html.indexOf("<", cityStart));

			places.add(new PlaceSearchResult(placeId, placeName, city));
			rowStart = html.indexOf(rowText, cityStart) + rowText.length();
		}

		// Set the list of places on the original command as result
		command.setPlaces(places);
		return new CommandSuccessResult(command);

	}
/*
	private CommandResult parseFindPlaceResults(FindPlacesCommand command, String html) throws ParseException {

		// Parse the find places table
		int noResultsStart = html.indexOf("NO PLACES FOUND");
		if (noResultsStart >= 0) {
			command.setPlaces(new ArrayList<PlaceForAvailability>());
			return new CommandSuccessResult(command);
		}
		int tableStart = html.indexOf("places found.");
		if (tableStart < 0) {
			return new CommandFailureResult(command, new ApiException(ApiException.ExceptionType.CommandFailed,
					"The response HTML did not contain the unique search results table begin HTML string"));
		}
		String rowText = "',";
		int rowStart = html.indexOf(rowText, tableStart) + rowText.length();
		ArrayList<PlaceForAvailability> places = new ArrayList<PlaceForAvailability>();

		while (rowStart > 0 + rowText.length()) {

			int placeId = Integer.parseInt(html.substring(rowStart, html.indexOf(")", rowStart)));

			int placeNameStart = html.indexOf(";\">", rowStart) + ";\">".length();
			String placeName = html.substring(placeNameStart, html.indexOf("<", placeNameStart));

			int cityStart = html.indexOf("</A></TD><TD>", placeNameStart) + "</A></TD><TD>".length();
			String city = html.substring(cityStart, html.indexOf("<", cityStart));

			places.add(new PlaceForAvailability(placeId, placeName, city));
			rowStart = html.indexOf(rowText, cityStart) + rowText.length();
		}

		// Set the list of places on the original command as result
		command.setPlaces(places);
		return new CommandSuccessResult(command);

	}
*/
	private CommandResult parseSearchResults(SearchBeersCommand command, String html) {

		try {

			// Parse the JSON response
			JSONArray json = new JSONArray(html);
			ArrayList<BeerSearchResult> results = new ArrayList<BeerSearchResult>();
			for (int i = 0; i < json.length(); i++) {
				JSONObject result = json.getJSONObject(i);
				String pctl = result.getString("OverallPctl");
				results.add(new BeerSearchResult(Integer.parseInt(result.getString("BeerID")), HttpHelper.cleanHtml(result
						.getString("BeerName")),
				// TODO: This should parse as a double and be displayed as integer instead
						(pctl.equals("null") ? -1 : (int) Double.parseDouble(pctl)), Integer.parseInt(result
								.getString("RateCount")), result.getInt("IsRated") == 1, result.getBoolean("IsAlias"),
						result.getBoolean("Retired")));
			}

			// Set the list of search results on the original command as result
			command.setSearchResults(results);
			return new CommandSuccessResult(command);

		} catch (JSONException e) {
			return new CommandFailureResult(command, new ApiException(ApiException.ExceptionType.CommandFailed,
					"JSON parsing error: " + e.toString()));
		}
	}

	private CommandResult parseRatings(GetRatingsCommand command, String html) {

		try {

			// Parse the JSON response
			JSONArray json = new JSONArray(html);
			SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy h:mm:ss a");
			ArrayList<BeerRating> results = new ArrayList<BeerRating>();
			for (int i = 0; i < json.length(); i++) {
				JSONObject result = json.getJSONObject(i);
				String entered = result.getString("TimeEntered");
				Date timeEntered = null;
				try {
					timeEntered = dateFormat.parse(entered);
				} catch (ParseException e) {
				}
				String updated = result.getString("TimeUpdated");
				Date timeUpdated = null;
				try {
					timeUpdated = dateFormat.parse(updated);
				} catch (ParseException e) {
				}
				results.add(new BeerRating(result.getString("resultNum"), result.getString("RatingID"), result
						.getString("Appearance"), result.getString("Aroma"), result.getString("Flavor"), result
						.getString("Mouthfeel"), result.getString("Overall"), result.getString("TotalScore"),
						HttpHelper.cleanHtml(result.getString("Comments")), timeEntered, timeUpdated, Integer.parseInt(result
								.getString("UserID")), HttpHelper.cleanHtml(result.getString("UserName")), HttpHelper.cleanHtml(result
								.getString("City")), result.getString("StateID"), HttpHelper.cleanHtml(result.getString("State")),
						result.getString("CountryID"), HttpHelper.cleanHtml(result.getString("Country")), result
								.getString("RateCount")));
			}

			// Set the list of ratings on the original command as result
			command.setRatings(results);
			return new CommandSuccessResult(command);

		} catch (JSONException e) {
			return new CommandFailureResult(command, new ApiException(ApiException.ExceptionType.CommandFailed,
					"JSON parsing error: " + e.toString()));
		}
	}

	private CommandResult parsePlaceSearchResults(SearchPlacesCommand command, String html) {

		try {

			// Parse the JSON response
			JSONArray json = new JSONArray(html);
			ArrayList<PlaceSearchResult> results = new ArrayList<PlaceSearchResult>();
			for (int i = 0; i < json.length(); i++) {
				JSONObject result = json.getJSONObject(i);
				results.add(new PlaceSearchResult(Integer.parseInt(result.getString("PlaceID")), HttpHelper.cleanHtml(result
						.getString("PlaceName")), HttpHelper.cleanHtml(result.getString("City"))));
			}

			// Set the list of search results on the original command as result
			command.setSearchResults(results);
			return new CommandSuccessResult(command);

		} catch (JSONException e) {
			return new CommandFailureResult(command, new ApiException(ApiException.ExceptionType.CommandFailed,
					"JSON parsing error: " + e.toString()));
		}
	}

	private CommandResult parseCheckins(GetCheckinsCommand command, String html) {

		try {

			// Parse the JSON response
			JSONArray json = new JSONArray(html);
			ArrayList<CheckedInUser> results = new ArrayList<CheckedInUser>();
			for (int i = 0; i < json.length(); i++) {
				JSONObject result = json.getJSONObject(i);
				results.add(new CheckedInUser(result.getInt("UserID"), HttpHelper.cleanHtml(result.getString("Username"))));
			}

			// Set the list of checked in users on the original command as result
			command.setCheckins(results);
			return new CommandSuccessResult(command);

		} catch (JSONException e) {
			return new CommandFailureResult(command, new ApiException(ApiException.ExceptionType.CommandFailed,
					"JSON parsing error: " + e.toString()));
		}
	}

	private CommandResult parsePlaceDetails(GetPlaceDetailsCommand command, String html) {

		try {

			// Parse the JSON response
			JSONArray json = new JSONArray(html);
			JSONObject result = json.getJSONObject(0);
			String avgRating = result.getString("AvgRating");

			// Get the details directly form the JSON object
			command.setDetails(new Place(result.getInt("PlaceID"), HttpHelper.cleanHtml(result.getString("PlaceName")), result
					.getInt("PlaceType"), HttpHelper.cleanHtml(result.getString("Address")), HttpHelper.cleanHtml(result.getString("City")),
					result.getString("StateID"), result.getInt("CountryID"), HttpHelper.cleanHtml(result.getString("PostalCode")),
					HttpHelper.cleanHtml(result.getString("PhoneNumber")), avgRating.equals("null") ? -1 : (int) Float
							.parseFloat(avgRating), HttpHelper.cleanHtml(result.getString("PhoneAC")), result
							.getDouble("Latitude"), result.getDouble("Longitude"), -1D));
			return new CommandSuccessResult(command);

		} catch (JSONException e) {
			return new CommandFailureResult(command, new ApiException(ApiException.ExceptionType.CommandFailed,
					"JSON parsing error: " + e.toString()));
		}
	}

	private CommandResult parsePlaces(GetPlacesCommand command, String html) {

		try {

			// Parse the JSON response
			JSONArray json = new JSONArray(html);
			ArrayList<Place> results = new ArrayList<Place>();
			for (int i = 0; i < json.length(); i++) {
				JSONObject result = json.getJSONObject(i);
				String avgRating = result.getString("AvgRating");
				results.add(new Place(result.getInt("PlaceID"), HttpHelper.cleanHtml(result.getString("PlaceName")), result
						.getInt("PlaceType"), HttpHelper.cleanHtml(result.getString("Address")), HttpHelper.cleanHtml(result
						.getString("City")), result.getString("StateID"), result.getInt("CountryID"), HttpHelper.cleanHtml(result
						.getString("PostalCode")), HttpHelper.cleanHtml(result.getString("PhoneNumber")),
						avgRating.equals("null") ? -1 : (int) Float.parseFloat(avgRating), HttpHelper.cleanHtml(result
								.getString("PhoneAC")), result.getDouble("Latitude"), result.getDouble("Longitude"),
						result.getDouble("Distance")));
			}

			// Set the list of search results on the original command as result
			command.setPlaces(results);
			return new CommandSuccessResult(command);

		} catch (JSONException e) {
			return new CommandFailureResult(command, new ApiException(ApiException.ExceptionType.CommandFailed,
					"JSON parsing error: " + e.toString()));
		}
	}

	private CommandResult parseBeerDetails(GetBeerDetailsCommand command, String html) throws ApiException {

		try {

			// Parse the JSON response
			JSONArray json = new JSONArray(html);
			BeerDetails details = null;
			if (json.length() > 0) {
				JSONObject result = json.getJSONObject(0);
				String overall = result.getString("OverallPctl");
				String style = result.getString("StylePctl");
				details = new BeerDetails(result.getInt("BeerID"), HttpHelper.cleanHtml(result.getString("BeerName")),
						result.getInt("BrewerID"), HttpHelper.cleanHtml(result.getString("BrewerName")),
						HttpHelper.cleanHtml(result.getString("BeerStyleName")), (float) result.getDouble("Alcohol"),
						overall.equals("null") ? GetBeerDetailsCommand.NO_SCORE_YET : Float.parseFloat(overall),
						style.equals("null") ? GetBeerDetailsCommand.NO_SCORE_YET : Float.parseFloat(style),
						HttpHelper.cleanHtml(result.getString("Description")));
			}

			// Set the list of search results on the original command as result
			command.setDetails(details);
			return new CommandSuccessResult(command);

		} catch (JSONException e) {
			return new CommandFailureResult(command, new ApiException(ApiException.ExceptionType.CommandFailed,
					"JSON parsing error: " + e.toString()));
		}
	}

	private CommandResult parseBeerMail(GetBeerMailCommand command, String html) {

		try {

			// Parse the JSON response
			JSONArray json = new JSONArray(html);
			JSONObject result = json.getJSONObject(0);
			MailDetails mail = new MailDetails(result.getInt("MessageID"), result.getString("Body"));

			// Set the mail details on the original command as result
			command.setMail(mail);
			return new CommandSuccessResult(command);

		} catch (JSONException e) {
			return new CommandFailureResult(command, new ApiException(ApiException.ExceptionType.CommandFailed,
					"JSON parsing error: " + e.toString()));
		}
	}

	private CommandResult parseAvailableBeers(GetAvailableBeersCommand command, String html) {

		try {

			// Parse the JSON response
			SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy h:mm:ss a");
			JSONArray json = new JSONArray(html);
			ArrayList<AvailableBeer> results = new ArrayList<AvailableBeer>();
			for (int i = 0; i < json.length(); i++) {
				JSONObject result = json.getJSONObject(i);
				String pctl = result.getString("AverageRating");
				String entered = result.getString("TimeEntered");
				Date timeEntered = null;
				try {
					timeEntered = dateFormat.parse(entered);
				} catch (ParseException e) {
				}
				results.add(new AvailableBeer(Integer.parseInt(result.getString("BeerID")), HttpHelper.cleanHtml(result
						// TODO: This should parse as a double and be displayed as integer instead
						.getString("BeerName")), (pctl.equals("null") ? -1 : (int) Double.parseDouble(pctl)), 
						timeEntered));
			}

			// Set the list of available beers on the original command as result
			command.setAvailableBeers(results);
			return new CommandSuccessResult(command);

		} catch (JSONException e) {
			return new CommandFailureResult(command, new ApiException(ApiException.ExceptionType.CommandFailed,
					"JSON parsing error: " + e.toString()));
		}
	}

	private CommandResult parseBeerAvailability(GetBeerAvailabilityCommand command, String html) {

		try {

			// Parse the JSON response
			JSONArray json = new JSONArray(html);
			ArrayList<PlaceSearchResult> results = new ArrayList<PlaceSearchResult>();
			for (int i = 0; i < json.length(); i++) {
				// {"PlaceID":4413,"PlaceName":"Keg Liquors","Latitude":38.312,"Longitude":-85.766,"PostalCode":"47129",
				// "Abbrev":"IN ","Country":"United States ","ServedBottle":false,"ServedTap":false}
				JSONObject result = json.getJSONObject(i);
				results.add(new PlaceSearchResult(Integer.parseInt(result.getString("PlaceID")), HttpHelper
						.cleanHtml(result.getString("PlaceName")), HttpHelper.cleanHtml(result.getString("Country"))));
			}

			// Set the list of available beers on the original command as result
			command.setPlaces(results);
			return new CommandSuccessResult(command);

		} catch (JSONException e) {
			return new CommandFailureResult(command, new ApiException(ApiException.ExceptionType.CommandFailed,
					"JSON parsing error: " + e.toString()));
		}
	}

	private CommandResult parseAllBeerMails(GetAllBeerMailsCommand command, String html) {

		try {

			// Parse the JSON response
			JSONArray json = new JSONArray(html);
			ArrayList<Mail> results = new ArrayList<Mail>();
			for (int i = 0; i < json.length(); i++) {
				JSONObject result = json.getJSONObject(i);
				results.add(new Mail(result.getInt("MessageID"), HttpHelper.cleanHtml(result.getString("UserName")), result
						.getBoolean("MessageRead"), HttpHelper.cleanHtml(result.getString("Subject")), result.getInt("Source"),
						HttpHelper.cleanHtml(result.getString("Sent")), result.getString("Reply").equals("1")));
			}

			// Set the list of beer mails on the original command as result
			command.setMail(results);
			return new CommandSuccessResult(command);

		} catch (JSONException e) {
			return new CommandFailureResult(command, new ApiException(ApiException.ExceptionType.CommandFailed,
					"JSON parsing error: " + e.toString()));
		}
	}

	private CommandResult parseBeerImage(GetBeerImageCommand command, InputStream rawStream) {
		// Read the raw response stream as Drawable image and return this in a success result
		command.setImage(Drawable.createFromStream(rawStream, "tmp"));
		return new CommandSuccessResult(command);
	}

	private CommandResult parseUserImage(GetUserImageCommand command, InputStream rawStream) {
		// Read the raw response stream as Drawable image and return this in a success result
		command.setImage(Drawable.createFromStream(rawStream, "tmp"));
		return new CommandSuccessResult(command);
	}

}
