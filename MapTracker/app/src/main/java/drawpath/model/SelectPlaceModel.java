package drawpath.model;

import com.google.android.gms.maps.model.LatLng;


public class SelectPlaceModel {

    public String strPoint="1";
    public String strAddress="";
    public String strHint="Place ";
    public LatLng latLng = null;

    public SelectPlaceModel()
    {}
    public SelectPlaceModel(String strPoint, String strAddress, String strHint, LatLng latLng)
    {
        this.strPoint = strPoint;
        this.strAddress = strAddress;
        this.strHint = strHint;
        this.latLng = latLng;
    }

}
