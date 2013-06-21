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
package com.ratebeer.android.api.command;

import java.util.LinkedHashMap;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;

public class Style implements Parcelable {

	private final int id;
	private final String code;
	private final String name;
	
	private Style(int id, String code, String name) {
		this.id = id;
		this.code = code;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	/**
	 * List of all the beer styles known ob RateBeer
	 */
	public static Map<Integer, Style> ALL_STYLES;

	/**
	 * Initialization of all the countries (which will run when the Country class is first used
	 */
	static {
		ALL_STYLES = new LinkedHashMap<Integer, Style>();
		addStyle("abbey-dubbel", 71, "Abbey Dubbel");
		addStyle("abbey-tripel", 72, "Abbey Tripel");
		addStyle("abt-quadrupel", 80, "Abt/Quadrupel");
		addStyle("altbier", 2, "Altbier");
		addStyle("amber-ale", 53, "Amber Ale");
		addStyle("amber-lager-vienna", 36, "Amber Lager/Vienna");
		addStyle("american-pale-ale", 18, "American Pale Ale");
		addStyle("american-strong-ale", 64, "American Strong Ale ");
		addStyle("baltic-porter", 63, "Baltic Porter");
		addStyle("barley-wine", 11, "Barley Wine");
		addStyle("belgian-ale", 12, "Belgian Ale");
		addStyle("belgian-strong-ale", 13, "Belgian Strong Ale");
		addStyle("belgian-white-witbier", 48, "Belgian White (Witbier)");
		addStyle("berliner-weisse", 61, "Berliner Weisse");
		addStyle("biere-de-garde", 58, "Bière de Garde");
		addStyle("bitter", 20, "Bitter");
		addStyle("black-ipa", 114, "Black IPA");
		addStyle("brown-ale", 15, "Brown Ale");
		addStyle("california-common", 42, "California Common");
		addStyle("cider", 10, "Cider");
		addStyle("cream-ale", 35, "Cream Ale");
		addStyle("czech-pilsner-sv283tly", 31, "Czech Pilsner/Světlý");
		addStyle("doppelbock", 26, "Doppelbock");
		addStyle("dortmunder-helles", 60, "Dortmunder/Helles");
		addStyle("dry-stout", 22, "Dry Stout");
		addStyle("dunkel-tmavy", 28, "Dunkel/Tmavý");
		addStyle("dunkelweizen", 100, "Dunkelweizen");
		addStyle("dunkler-bock", 9, "Dunkler Bock");
		addStyle("eisbock", 27, "Eisbock");
		addStyle("english-pale-ale", 16, "English Pale Ale");
		addStyle("english-strong-ale", 56, "English Strong Ale");
		addStyle("foreign-stout", 79, "Foreign Stout");
		addStyle("fruit-beer", 40, "Fruit Beer");
		addStyle("german-hefeweizen", 7, "German Hefeweizen");
		addStyle("german-kristallweizen", 82, "German Kristallweizen");
		addStyle("golden-ale-blond-ale", 54, "Golden Ale/Blond Ale");
		addStyle("grodziskie-gose-lichtenhainer", 117, "Grodziskie/Gose/Lichtenhainer");
		addStyle("heller-bock", 105, "Heller Bock");
		addStyle("ice-cider-perry", 112, "Ice Cider/Perry");
		addStyle("imperial-stout", 24, "Imperial Stout");
		addStyle("imperial-double-ipa", 81, "Imperial/Double IPA");
		addStyle("imperial-strong-porter", 113, "Imperial/Strong Porter");
		addStyle("india-pale-ale-ipa", 17, "India Pale Ale (IPA)");
		addStyle("irish-ale", 62, "Irish Ale");
		addStyle("kolsch", 39, "Kölsch");
		addStyle("lambic-style--faro", 77, "Lambic Style - Faro");
		addStyle("lambic-style--fruit", 14, "Lambic Style - Fruit");
		addStyle("lambic-style--gueuze", 73, "Lambic Style - Gueuze");
		addStyle("lambic-style--unblended", 78, "Lambic Style - Unblended");
		addStyle("low-alcohol", 75, "Low Alcohol");
		addStyle("malt-liquor", 8, "Malt Liquor");
		addStyle("mead", 44, "Mead");
		addStyle("mild-ale", 55, "Mild Ale");
		addStyle("oktoberfest-marzen", 37, "Oktoberfest/Märzen");
		addStyle("old-ale", 76, "Old Ale");
		addStyle("pale-lager", 3, "Pale Lager");
		addStyle("perry", 107, "Perry");
		addStyle("pilsener", 4, "Pilsener");
		addStyle("polotmavy", 115, "Polotmavý");
		addStyle("porter", 5, "Porter");
		addStyle("premium-bitter-esb", 101, "Premium Bitter/ESB");
		addStyle("premium-lager", 103, "Premium Lager");
		addStyle("sahti-gotlandsdricke-koduolu", 116, "Sahti/Gotlandsdricke/Koduõlu");
		addStyle("saison", 45, "Saison");
		addStyle("sake--daiginjo", 87, "Saké - Daiginjo");
		addStyle("sake--futsu-shu", 89, "Saké - Futsu-shu");
		addStyle("sake--genshu", 91, "Saké - Genshu");
		addStyle("sake--ginjo", 86, "Saké - Ginjo");
		addStyle("sake--honjozo", 85, "Saké - Honjozo");
		addStyle("sake--infused", 94, "Saké - Infused");
		addStyle("sake--junmai", 84, "Saké - Junmai");
		addStyle("sake--koshu", 92, "Saké - Koshu");
		addStyle("sake--namasake", 90, "Saké - Namasaké");
		addStyle("sake--nigori", 93, "Saké - Nigori");
		addStyle("sake--taru", 95, "Saké - Taru");
		addStyle("sake--tokubetsu", 88, "Saké - Tokubetsu");
		addStyle("schwarzbier", 29, "Schwarzbier");
		addStyle("scotch-ale", 102, "Scotch Ale");
		addStyle("scottish-ale", 21, "Scottish Ale");
		addStyle("smoked", 41, "Smoked");
		addStyle("sour-ale-wild-ale", 52, "Sour Ale/Wild Ale");
		addStyle("sour-red-brown", 118, "Sour Red/Brown");
		addStyle("specialty-grain", 106, "Specialty Grain");
		addStyle("spice-herb-vegetable", 57, "Spice/Herb/Vegetable");
		addStyle("stout", 6, "Stout");
		addStyle("strong-pale-lager-imperial-pils", 65, "Strong Pale Lager/Imperial Pils");
		addStyle("sweet-stout", 23, "Sweet Stout");
		addStyle("traditional-ale", 59, "Traditional Ale");
		addStyle("weizen-bock", 25, "Weizen Bock");
		addStyle("wheat-ale", 19, "Wheat Ale");
		addStyle("zwickel-keller-landbier", 74, "Zwickel/Keller/Landbier");
	}
	
	/**
	 * Helper method to add styles to the list of all known styles. The order corresponds 
	 * to that of the OPTION definition in RateBeer's HTML form at /beerstyles/s/#/
	 * @param code The style code (as used in URLs)
	 * @param id The style ID
	 * @param name The human readable name of the style
	 */
	private static void addStyle(String code, int id, String name) {
		ALL_STYLES.put(id, new Style(id, code, name));
	}

	public int describeContents() {
		return 0;
	}
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(id);
		out.writeString(code);
		out.writeString(name);
	}
	public static final Parcelable.Creator<Style> CREATOR = new Parcelable.Creator<Style>() {
		public Style createFromParcel(Parcel in) {
			return new Style(in);
		}
		public Style[] newArray(int size) {
			return new Style[size];
		}
	};
	private Style(Parcel in) {
		id = in.readInt();
		code = in.readString();
		name = in.readString();
	}
	
}
