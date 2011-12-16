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

public class Country {

	private final int id;
	private final String code;
	private final String name;
	
	private Country(int id, String code, String name) {
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
	 * List of all the countries available in RateBeer
	 */
	public static Map<Integer, Country> ALL_COUNTRIES;

	/**
	 * Initialization of all the countries (which will run when the Country class is first used
	 */
	static {
		ALL_COUNTRIES = new LinkedHashMap<Integer, Country>();
		addCountry("united-states", 213, "United States"); 
		addCountry("belgium", 23, "Belgium"); 
		addCountry("netherlands", 144, "Netherlands"); 
		addCountry("england", 240, "England"); 
		addCountry("scotland", 241, "Scotland"); 
		addCountry("canada", 39, "Canada"); 
		addCountry("germany", 79, "Germany"); 
		addCountry("denmark", 58, "Denmark"); 
		addCountry("norway", 154, "Norway"); 
		addCountry("sweden", 190, "Sweden"); 
		addCountry("finland", 71, "Finland"); 
		addCountry("czech-republic", 56, "Czech Republic"); 
		
		addCountry("afghanistan", 1, "Afghanistan"); 
		addCountry("albania", 2, "Albania"); 
		addCountry("algeria", 4, "Algeria"); 
		addCountry("american-samoa", 5, "American Samoa"); 
		addCountry("andorra", 6, "Andorra"); 
		addCountry("angola", 7, "Angola"); 
		addCountry("anguilla", 8, "Anguilla"); 
		addCountry("antigua-barbuda", 9, "Antigua & Barbuda"); 
		addCountry("argentina", 10, "Argentina"); 
		addCountry("armenia", 11, "Armenia"); 
		addCountry("aruba", 12, "Aruba"); 
		addCountry("ascension-island", 13, "Ascension Island"); 
		addCountry("australia", 14, "Australia"); 
		addCountry("austria", 15, "Austria"); 
		addCountry("azerbaijan", 16, "Azerbaijan"); 
		addCountry("bahamas", 17, "Bahamas"); 
		addCountry("bahrain", 18, "Bahrain"); 
		addCountry("bangladesh", 19, "Bangladesh"); 
		addCountry("barbados", 20, "Barbados"); 
		addCountry("belarus", 22, "Belarus"); 
		//addCountry("belgium", 23, "Belgium"); 
		addCountry("belize", 24, "Belize"); 
		addCountry("benin", 25, "Benin"); 
		addCountry("bermuda", 26, "Bermuda"); 
		addCountry("bhutan", 27, "Bhutan"); 
		addCountry("bolivia", 28, "Bolivia"); 
		addCountry("bosnia", 29, "Bosnia"); 
		addCountry("botswana", 30, "Botswana"); 
		addCountry("brazil", 31, "Brazil"); 
		addCountry("british-virgin-islands", 32, "British Virgin Islands"); 
		addCountry("brunei", 33, "Brunei"); 
		addCountry("bulgaria", 34, "Bulgaria"); 
		addCountry("burkina-faso", 35, "Burkina Faso"); 
		addCountry("burundi", 36, "Burundi"); 
		addCountry("cambodia", 37, "Cambodia"); 
		addCountry("cameroon", 38, "Cameroon"); 
		//addCountry("canada", 39, "Canada"); 
		addCountry("cape-verde-islands", 40, "Cape Verde Islands"); 
		addCountry("cayman-islands", 41, "Cayman Islands"); 
		addCountry("central-african-republic", 42, "Central African Republic"); 
		addCountry("ceuta", 233, "Ceuta"); 
		addCountry("chad", 43, "Chad"); 
		addCountry("chile", 44, "Chile"); 
		addCountry("china", 45, "China"); 
		addCountry("christmas-island", 46, "Christmas Island"); 
		addCountry("cocos-keeling-islands", 47, "Cocos-Keeling Islands"); 
		addCountry("colombia", 48, "Colombia"); 
		addCountry("comoros", 49, "Comoros"); 
		addCountry("congo", 50, "Congo"); 
		addCountry("cook-islands", 51, "Cook Islands"); 
		addCountry("costa-rica", 52, "Costa Rica"); 
		addCountry("croatia", 53, "Croatia"); 
		addCountry("cuba", 54, "Cuba"); 
		addCountry("curacao", 257, "Curaçao"); 
		addCountry("cyprus", 55, "Cyprus"); 
		//addCountry("czech-republic", 56, "Czech Republic"); 
		addCountry("dem-rep-of-congo", 57, "Dem Rep of Congo"); 
		//addCountry("denmark", 58, "Denmark"); 
		addCountry("diego-garcia", 59, "Diego Garcia"); 
		addCountry("djibouti", 60, "Djibouti"); 
		addCountry("dominica", 61, "Dominica"); 
		addCountry("dominican-republic", 62, "Dominican Republic"); 
		addCountry("east-timor", 236, "East Timor"); 
		addCountry("ecuador", 63, "Ecuador"); 
		addCountry("egypt", 64, "Egypt"); 
		addCountry("el-salvador", 65, "El Salvador"); 
		//addCountry("england", 240, "England"); 
		addCountry("equatorial-guinea", 66, "Equatorial Guinea"); 
		addCountry("eritrea", 243, "Eritrea"); 
		addCountry("estonia", 67, "Estonia"); 
		addCountry("ethiopia", 68, "Ethiopia"); 
		addCountry("falkland-islands", 69, "Falkland Islands"); 
		addCountry("faroe-islands", 244, "Faroe Islands"); 
		addCountry("fiji-islands", 70, "Fiji Islands"); 
		//addCountry("finland", 71, "Finland"); 
		addCountry("france", 72, "France"); 
		addCountry("french-guiana", 74, "French Guiana"); 
		addCountry("french-polynesia", 75, "French Polynesia"); 
		addCountry("gabon", 76, "Gabon"); 
		addCountry("gambia", 77, "Gambia"); 
		addCountry("georgia", 78, "Georgia"); 
		//addCountry("germany", 79, "Germany"); 
		addCountry("ghana", 80, "Ghana"); 
		addCountry("gibraltar", 81, "Gibraltar"); 
		addCountry("greece", 82, "Greece"); 
		addCountry("greenland", 83, "Greenland"); 
		addCountry("grenada", 84, "Grenada"); 
		addCountry("guadeloupe", 85, "Guadeloupe"); 
		addCountry("guam", 86, "Guam"); 
		addCountry("guatemala", 88, "Guatemala"); 
		addCountry("guernsey", 225, "Guernsey"); 
		addCountry("guinea", 89, "Guinea"); 
		addCountry("guinea-bissau", 253, "Guinea-Bissau"); 
		addCountry("guyana", 90, "Guyana"); 
		addCountry("haiti", 91, "Haiti"); 
		addCountry("honduras", 92, "Honduras"); 
		addCountry("hong-kong", 93, "Hong Kong"); 
		addCountry("hungary", 94, "Hungary"); 
		addCountry("iceland", 95, "Iceland"); 
		addCountry("india", 96, "India"); 
		addCountry("indonesia", 97, "Indonesia"); 
		addCountry("iran", 98, "Iran"); 
		addCountry("iraq", 99, "Iraq"); 
		addCountry("ireland", 100, "Ireland"); 
		addCountry("isle-of-man", 224, "Isle of Man"); 
		addCountry("israel", 101, "Israel"); 
		addCountry("italy", 102, "Italy"); 
		addCountry("ivory-coast", 103, "Ivory Coast"); 
		addCountry("jamaica", 104, "Jamaica"); 
		addCountry("japan", 105, "Japan"); 
		addCountry("jersey", 226, "Jersey"); 
		addCountry("jordan", 106, "Jordan"); 
		addCountry("kazakhstan", 107, "Kazakhstan"); 
		addCountry("kenya", 108, "Kenya"); 
		addCountry("kiribati-republic", 109, "Kiribati Republic"); 
		addCountry("kosovo", 242, "Kosovo"); 
		addCountry("kuwait", 112, "Kuwait"); 
		addCountry("kyrgyz-republic", 113, "Kyrgyz Republic"); 
		addCountry("laos", 114, "Laos"); 
		addCountry("latvia", 115, "Latvia"); 
		addCountry("lebanon", 116, "Lebanon"); 
		addCountry("lesotho", 117, "Lesotho"); 
		addCountry("liberia", 118, "Liberia"); 
		addCountry("libya", 119, "Libya"); 
		addCountry("liechtenstein", 120, "Liechtenstein"); 
		addCountry("lithuania", 121, "Lithuania"); 
		addCountry("luxembourg", 122, "Luxembourg"); 
		addCountry("macau", 123, "Macau"); 
		addCountry("macedonia", 229, "Macedonia"); 
		addCountry("madagascar", 124, "Madagascar"); 
		addCountry("malawi", 125, "Malawi"); 
		addCountry("malaysia", 126, "Malaysia"); 
		addCountry("maldives", 127, "Maldives"); 
		addCountry("mali", 128, "Mali"); 
		addCountry("malta", 129, "Malta"); 
		addCountry("marshall-islands", 130, "Marshall Islands"); 
		addCountry("martinique", 131, "Martinique"); 
		addCountry("mauritania", 256, "Mauritania"); 
		addCountry("mauritius", 230, "Mauritius"); 
		addCountry("mayotte-island", 132, "Mayotte Island"); 
		addCountry("mexico", 133, "Mexico"); 
		addCountry("micronesia", 247, "Micronesia"); 
		addCountry("moldova", 134, "Moldova"); 
		addCountry("monaco", 135, "Monaco"); 
		addCountry("mongolia", 136, "Mongolia"); 
		addCountry("monserrat", 137, "Monserrat"); 
		addCountry("montenegro", 234, "Montenegro"); 
		addCountry("morocco", 138, "Morocco"); 
		addCountry("mozambique", 139, "Mozambique"); 
		addCountry("myanmar", 140, "Myanmar"); 
		addCountry("namibia", 141, "Namibia"); 
		addCountry("nauru", 142, "Nauru"); 
		addCountry("nepal", 143, "Nepal"); 
		//addCountry("netherlands", 144, "Netherlands"); 
		addCountry("new-caledonia", 147, "New Caledonia"); 
		addCountry("new-zealand", 148, "New Zealand"); 
		addCountry("nicaragua", 149, "Nicaragua"); 
		addCountry("niger", 150, "Niger"); 
		addCountry("nigeria", 151, "Nigeria"); 
		addCountry("niue", 152, "Niue"); 
		addCountry("norfolk-island", 153, "Norfolk Island"); 
		addCountry("north-korea", 110, "North Korea"); 
		addCountry("northern-ireland", 238, "Northern Ireland"); 
		addCountry("northern-marianas", 255, "Northern Marianas"); 
		//addCountry("norway", 154, "Norway"); 
		addCountry("oman", 155, "Oman"); 
		addCountry("pakistan", 156, "Pakistan"); 
		addCountry("palau", 157, "Palau"); 
		addCountry("palestine", 235, "Palestine"); 
		addCountry("panama", 158, "Panama"); 
		addCountry("papua-new-guinea", 159, "Papua New Guinea"); 
		addCountry("paraguay", 160, "Paraguay"); 
		addCountry("peru", 161, "Peru"); 
		addCountry("philippines", 162, "Philippines"); 
		addCountry("poland", 163, "Poland"); 
		addCountry("portugal", 164, "Portugal"); 
		addCountry("puerto-rico", 165, "Puerto Rico"); 
		addCountry("qatar", 166, "Qatar"); 
		addCountry("reunion", 246, "Réunion"); 
		addCountry("romania", 167, "Romania"); 
		addCountry("russia", 169, "Russia"); 
		addCountry("rwanda", 170, "Rwanda"); 
		addCountry("samoa", 219, "Samoa"); 
		addCountry("san-marino", 173, "San Marino"); 
		addCountry("sao-tome-principe", 254, "São Tomé & Principe"); 
		addCountry("saudi-arabia", 174, "Saudi Arabia"); 
		//addCountry("scotland", 241, "Scotland"); 
		addCountry("senegal-republic", 175, "Senegal Republic"); 
		addCountry("serbia", 221, "Serbia"); 
		addCountry("seychelles", 245, "Seychelles"); 
		addCountry("sierra-leone", 176, "Sierra Leone"); 
		addCountry("singapore", 177, "Singapore"); 
		addCountry("sint-maarten", 258, "Sint Maarten"); 
		addCountry("slovak-republic", 178, "Slovak Republic"); 
		addCountry("slovenia", 179, "Slovenia"); 
		addCountry("solomon-islands", 180, "Solomon Islands"); 
		addCountry("somalia", 181, "Somalia"); 
		addCountry("south-africa", 182, "South Africa"); 
		addCountry("south-korea", 111, "South Korea"); 
		addCountry("spain", 183, "Spain"); 
		addCountry("sri-lanka", 184, "Sri Lanka"); 
		addCountry("st-helena", 185, "St Helena"); 
		addCountry("st-kitts", 186, "St Kitts"); 
		addCountry("st-lucia", 171, "St Lucia"); 
		addCountry("st-vincent-the-grenadines", 227, "St Vincent & The Grenadines"); 
		addCountry("sudan", 187, "Sudan"); 
		addCountry("suriname", 188, "Suriname"); 
		addCountry("swaziland", 189, "Swaziland"); 
		//addCountry("sweden", 190, "Sweden"); 
		addCountry("switzerland", 191, "Switzerland"); 
		addCountry("syria", 192, "Syria"); 
		addCountry("taiwan", 193, "Taiwan"); 
		addCountry("tajikistan", 194, "Tajikistan"); 
		addCountry("tanzania", 195, "Tanzania"); 
		addCountry("thailand", 196, "Thailand"); 
		addCountry("tibet", 237, "Tibet"); 
		addCountry("tinian-island", 197, "Tinian Island"); 
		addCountry("togo", 198, "Togo"); 
		addCountry("tokelau", 199, "Tokelau"); 
		addCountry("tonga", 200, "Tonga"); 
		addCountry("trinidad-tobago", 201, "Trinidad & Tobago"); 
		addCountry("tunisia", 202, "Tunisia"); 
		addCountry("turkey", 203, "Turkey"); 
		addCountry("turkmenistan", 204, "Turkmenistan"); 
		addCountry("turks-and-caicos-islands", 205, "Turks and Caicos Islands"); 
		addCountry("tuvalu", 206, "Tuvalu"); 
		addCountry("uganda", 207, "Uganda"); 
		addCountry("ukraine", 208, "Ukraine"); 
		addCountry("united-arab-emirates", 209, "United Arab Emirates"); 
		//addCountry("united-states", 213, "United States"); 
		addCountry("united-states-virgin-islands", 211, "United States Virgin Islands"); 
		addCountry("uruguay", 212, "Uruguay"); 
		addCountry("uzbekistan", 214, "Uzbekistan"); 
		addCountry("vanuatu", 215, "Vanuatu"); 
		addCountry("vatican-city", 216, "Vatican City"); 
		addCountry("venezuela", 217, "Venezuela"); 
		addCountry("vietnam", 218, "Vietnam"); 
		addCountry("wales", 239, "Wales"); 
		addCountry("yemen", 248, "Yemen"); 
		addCountry("zambia", 222, "Zambia"); 
		addCountry("zimbabwe", 223, "Zimbabwe"); 
	}
	
	/**
	 * Helper method to add countries to the list of all available countries. The order corresponds 
	 * to that of the OPTION definition in RateBeer's HTML form at /beer/top-50/
	 * @param code The country code (as used in URLs)
	 * @param id The country ID
	 * @param name The human readable name of the country
	 */
	private static void addCountry(String code, int id, String name) {
		ALL_COUNTRIES.put(id, new Country(id, code, name));
	}

	public String toSinglePreference() {
		return id + "|" + code + "|" + name;
	}

	public static Country fromSinglePreference(String storedPreference) {
		if (storedPreference == null) {
			return null;
		}
		String[] parts = storedPreference.split("\\|");
		if (parts.length < 3) {
			return null;
		}
		try {
			return new Country(Integer.parseInt(parts[0]), parts[1], parts[2]);
		} catch (NumberFormatException e) {}
		return null;
	}

}
