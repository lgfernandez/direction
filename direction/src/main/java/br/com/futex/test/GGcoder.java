package br.com.futex.test;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;

public class GGcoder {

	private static final String URL = "http://maps.google.com/maps/geo?output=json";
	private static final String DEFAULT_KEY = "AIzaSyBK4HR8dvW-6ED2maXC3dQc4uAYLicNpuQ";

	public static GAddress geocode(String address, String key) throws Exception {
		URL url = new URL(URL + "&q=" + URLEncoder.encode(address, "UTF-8")+"&key="+DEFAULT_KEY);
		URLConnection conn = url.openConnection();
		ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
		IOUtils.copy(conn.getInputStream(), output);
		output.close();

		GAddress gaddr = new GAddress();
		JSONObject json = JSONObject.fromObject(output.toString());
		JSONObject placemark = (JSONObject) query(json, "Placemark[0]");

		final String commonId = "AddressDetails.Country.AdministrativeArea";

		gaddr.setFullAddress(query(placemark, "address").toString());
		gaddr.setZipCode(query(placemark, commonId + ".SubAdministrativeArea.Locality.PostalCode.PostalCodeNumber")
				.toString());
		gaddr.setAddress(query(placemark, commonId + ".SubAdministrativeArea.Locality.Thoroughfare.ThoroughfareName")
				.toString());
		gaddr.setCity(query(placemark, commonId + ".SubAdministrativeArea.SubAdministrativeAreaName").toString());
		gaddr.setState(query(placemark, commonId + ".AdministrativeAreaName").toString());
		gaddr.setLat(Double.parseDouble(query(placemark, "Point.coordinates[1]").toString()));
		gaddr.setLng(Double.parseDouble(query(placemark, "Point.coordinates[0]").toString()));
		return gaddr;
	}

	public static GAddress geocode(String address) throws Exception {
		return geocode(address, DEFAULT_KEY);
	}

	/* allow query for json nested objects, ie. Placemark[0].address */
	private static Object query(JSONObject jo, String query) {
		try {
			String[] keys = query.split("\\.");
			Object r = queryHelper(jo, keys[0]);
			for (int i = 1; i < keys.length; i++) {
				r = queryHelper(JSONObject.fromObject(r), keys[i]);
			}
			return r;
		} catch (JSONException e) {
			return "";
		}
	}

	/* help in query array objects: Placemark[0] */
	private static Object queryHelper(JSONObject jo, String query) {
		int openIndex = query.indexOf('[');
		int endIndex = query.indexOf(']');
		if (openIndex > 0) {
			String key = query.substring(0, openIndex);
			int index = Integer.parseInt(query.substring(openIndex + 1, endIndex));
			return jo.getJSONArray(key).get(index);
		}
		return jo.get(query);
	}

	public static void main(String[] args) throws Exception {
		System.out.println(GGcoder.geocode("SQN 311 bloco I, Brasilia").getFullAddress());
		System.out.println(GGcoder.geocode("7066041").getFullAddress());
	}

}
