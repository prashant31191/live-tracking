package drawpath.model;

/**
 * Created by prashant.chovatiya on 3/8/2018.
 */

public class RouteListModel {

    public String title = "";
    public String detail = "";
    public String timestamp = "";

    public SelectPlaceModel selectPlaceModel;

    public RouteListModel(String title, String detail)
    {
        this.title = title;
        this.detail = detail;
    }
}
