package com.utils;

import com.google.android.gms.maps.model.LatLng;

import com.maptracker.App;
import com.utils.AppFlags;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DirectionsJSONParser {
	
	/** Receives a JSONObject and returns a list of lists containing latitude and longitude */
	public List<List<HashMap<String,String>>> parse(JSONObject jObject){
		
		List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>() ;
		JSONArray jRoutes = null;
		JSONArray jLegs = null;
		JSONArray jSteps = null;	
		//my added for the distance
		JSONObject distance = null;
		try {			
			
			jRoutes = jObject.getJSONArray("routes");
			
			/** Traversing all   routes */
			for(int i=0;i<jRoutes.length();i++){
				jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");


				List path = new ArrayList<HashMap<String, String>>();
				
				/** Traversing all legs */
				for(int j=0;j<jLegs.length();j++){

					if(( (JSONObject)jRoutes.get(i)).toString() !=null && ((JSONObject)jRoutes.get(i)).toString().contains("distance")) {
						distance = ( (JSONObject)jLegs.get(j)).getJSONObject("distance");

						App.showLog("=====distance=======km=====");
						if( distance.getString("text") !=null) {
							String strKms = distance.getString("text");
							String strTemp[] = strKms.split(" ");
							App.showLog("======text=====km=====" + strKms);

							if(strKms.contains("km") && strKms.contains(" "))
							{
								if(strTemp !=null && strTemp.length > 0 && strTemp[0] !=null)
								{
									if(App.isNumeric(strTemp[0]))
									{
										AppFlags.totalMeterRoute = AppFlags.totalMeterRoute + ((Float.parseFloat(strTemp[0]) * 1000));
									}
								}

							}
							else if(strKms.contains("m") && strKms.contains(" "))
							{
								if(strTemp !=null && strTemp.length > 0 && strTemp[0] !=null)
								{
									if(App.isNumeric(strTemp[0]))
									{
										AppFlags.totalMeterRoute = AppFlags.totalMeterRoute + Float.parseFloat(strTemp[0]);
									}
								}

							}


							App.showLog("===meter===totalMeterRoute===" + AppFlags.totalMeterRoute);
							float totalKm = AppFlags.totalMeterRoute / 1000;
							App.showLog("===km===totalKm===" + totalKm);
						}



						//text
					}

					jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");
					
					/** Traversing all steps */
					for(int k=0;k<jSteps.length();k++){
						String polyline = "";
						polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
						List<LatLng> list = decodePoly(polyline);
						
						/** Traversing all points */
						for(int l=0;l<list.size();l++){
							HashMap<String, String> hm = new HashMap<String, String>();
							hm.put("lat", Double.toString(list.get(l).latitude) );
							hm.put("lng", Double.toString(list.get(l).longitude) );
							path.add(hm);						
						}								
					}
					routes.add(path);
				}
			}
			
		} catch (JSONException e) {			
			e.printStackTrace();
		}catch (Exception e){			
		}
		
		
		return routes;
	}	
	
	
	/**
	 * Method to decode polyline points 
	 * Courtesy : http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java 
	 * */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}