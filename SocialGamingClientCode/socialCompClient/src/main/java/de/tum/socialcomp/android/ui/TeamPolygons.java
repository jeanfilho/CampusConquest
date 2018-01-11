package de.tum.socialcomp.android.ui;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.tum.socialcomp.android.MainActivity;
import de.tum.socialcomp.android.R;

/**
 * This class represents a polygon/attack mesh to be drawn on the map
 *
 * TODO: Note: not (fully) functional yet
 *
 * Created by Jonas on 26/06/2015.
 */
public class TeamPolygons {
    public class PlayerPolyStruct{
        Polygon poly;
        String ID;
        Marker[] nodes;
    }
    Faculty fac = new Faculty();
    private GoogleMap map;
    public ArrayList<PlayerPolyStruct> teamPolys = new ArrayList<PlayerPolyStruct>();

    public TeamPolygons(GoogleMap mapz)
    {
        map = mapz;
    }

    /**
     * add a polygon to the current list of polygons (needs the markers for the node positions, a string to identify the polygon, and a color index to know which color to use
     *
     * @param markers
     * @param ID
     * @param color
     */
    public void add(Marker[] markers, String ID, int color)
    {
        PlayerPolyStruct poly = new PlayerPolyStruct();
        poly.ID = ID;
        poly.nodes = markers;

        PolygonOptions polOps = new PolygonOptions();
        for(Marker marker : markers) {
            polOps.add(marker.getPosition());
        }
        poly.poly = map.addPolygon(polOps
                .strokeColor(color)
                .fillColor(Color.TRANSPARENT));

        teamPolys.add(poly);
    }

    public class compactCoordinate {
        double x;
        double y;

        public compactCoordinate(double xC, double yC) {
            x = xC;
            y = yC;
        }
    }

    public void set(JSONObject data)
    {
        /*
        for(PlayerPolyStruct anus : teamPolys)
        {Log.e("feggit1",anus.ID);}
        try {
            //pull the all meshes array
            JSONArray allMeshes = data.getJSONArray("allMeshes");
            JSONObject facMeshes;
            int i = 0;
            ArrayList<TeamPolygons.PlayerPolyStruct> shlong  = new ArrayList<PlayerPolyStruct>();
            Faculty fac = new Faculty();


            //for each mesh grab its facultyID, then add each mesh to the TeamPolygons object of that faculty
            while((facMeshes = allMeshes.getJSONObject(i)) != null) {
                int facID = facMeshes.getInt("facultyID");
                JSONArray meshes = facMeshes.getJSONArray("meshes");
                JSONArray mesh;
                int j = 0;
                while((mesh = meshes.getJSONArray(j)) != null) {
                    String ID = mesh.getJSONObject(0).getString("facebookID"); //replace 1337 with whatever will be the actual string ID to get
                    Log.e("Feggit", ID);
                    //check if mesh/polygon already exists (what's the ID?)
                    //if so:
                    //	put into a list of existing polygons
                    if(exists(ID))
                    {
                        Log.e("Feggit", "esists");
                        PlayerPolyStruct playerPoly = find(ID);
                        shlong.add(playerPoly);
                        delete(ID);

                    } else { //if not:
                        Log.e("Feggit", "nope");
                        int len = mesh.length();
                        Marker[] markers = new Marker[len];
                        MapSectionFragment mapfrag = MapSectionFragment.getInstance();
                        for(int k = 0; k < len; k++) {
                            JSONObject point = mesh.getJSONObject(k);
                            Marker marker = mapfrag.getPlayerMarker(point.getString("facebookID"));
                            if(marker == null)
                                marker = map.addMarker(new MarkerOptions().position(new LatLng(point.getDouble("x"), point.getDouble("y"))));
                            markers[k]=marker;
                        }
                        PlayerPolyStruct poly = new PlayerPolyStruct();
                        poly.ID = ID;
                        poly.nodes = markers;

                        PolygonOptions polOps = new PolygonOptions();
                        for(Marker macker : markers) {
                            polOps.add(macker.getPosition());
                        }
                        poly.poly = map.addPolygon(polOps
                                .strokeColor(mapfrag.getResources().getColor(fac.getFacColor(facID)))
                                .fillColor(Color.TRANSPARENT));

                        teamPolys.add(poly);
                    }
                    j++;
                }
                i++;
            }

            //now here replace existing Polygons with newly grabbed ones...
            teamPolys = shlong;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        */
        for(PlayerPolyStruct enus : teamPolys)
        {
            enus.poly.remove();
        }
        try {
            //pull the all meshes array
            JSONArray allMeshes = data.getJSONArray("allMeshes");
            JSONObject facMeshes;
            int i = 0;
            ArrayList<TeamPolygons.PlayerPolyStruct> shlong = new ArrayList<PlayerPolyStruct>();
            Faculty fac = new Faculty();
            int allMeshesNum = allMeshes.length();

            //for each mesh grab its facultyID, then add each mesh to the TeamPolygons object of that faculty
            while (i < allMeshesNum) {
                Log.e("Feggit i loop: ", i + "");
                facMeshes = allMeshes.getJSONObject(i);
                int facID = facMeshes.getInt("facultyID");
                JSONArray meshes = facMeshes.getJSONArray("meshes");
                JSONArray mesh;
                int j = 0;
                int meshNum = meshes.length();
                while(j < meshNum) {
                    Log.e("Feggit j loop: ", j + "");
                    mesh = meshes.getJSONArray(j);
                    String ID = mesh.getJSONObject(0).getString("facebookID"); //replace 1337 with whatever will be the actual string ID to get
                    Log.e("Feggit", ID);
                    int len = mesh.length();
                    Marker[] markers = new Marker[len];
                    MapSectionFragment mapfrag = MapSectionFragment.getInstance();
                    for(int k = 0; k < len; k++) {
                        Log.e("Feggit k loop: ", k + "");
                        JSONObject point = mesh.getJSONObject(k);
                        Marker marker = mapfrag.getPlayerMarker(point.getString("facebookID"));
                        if(marker == null)
                            Log.e("Feggit3","pimml");
                            marker = map.addMarker(new MarkerOptions().visible(false).position(new LatLng(point.getDouble("longitude"), point.getDouble("latitude"))));
                        markers[k]=marker;
                    }
                    Log.e("Feggit: ", "checked through markers. psych!");
                    PlayerPolyStruct poly = new PlayerPolyStruct();
                    poly.ID = ID;
                    poly.nodes = markers;

                    PolygonOptions polOps = new PolygonOptions();
                    for(Marker macker : markers) {
                        polOps.add(macker.getPosition());
                    }
                    Log.e("Feggit: ", "added to polOps markers. psych!");
                    poly.poly = map.addPolygon(polOps
                            .strokeColor(mapfrag.getResources().getColor(fac.getFacColor(facID))) //mapfrag.getResources().getColor(fac.getFacColor(facID)
                            .fillColor(Color.TRANSPARENT));
                    Log.e("Feggit: Added poly", poly.poly.toString());
                    //map.addMarker(new MarkerOptions().position(markers[0].getPosition()));
                    //MainActivity.mapsectionfrag.testPolyMarker(new MarkerOptions().position(markers[i].getPosition()));

                    shlong.add(poly);
                    j++;
                }
                i++;
            }
        teamPolys = shlong;
        }
        catch(Exception e)
        {
            Log.e("Feggit Exception:",e.toString());
            e.printStackTrace();
        }
    }

    //update all polygons, basically just resets each polygon to the nodes it's set to?
    public void update()
    {
        for(PlayerPolyStruct poly : teamPolys)
        {
            ArrayList<LatLng> nodes = new ArrayList<LatLng>();
            for(Marker marker : poly.nodes)
            {
                nodes.add(marker.getPosition());
            }
            poly.poly.setPoints(nodes);
        }
    }

    //delete the first polygon with the given identifier string
    public boolean delete(String ID)
    {
        for(PlayerPolyStruct poly : teamPolys)
        {
            if(poly.ID.equals(ID))
            {
                poly.poly.remove();
                teamPolys.remove(poly);
                return true;
            }
        }
        return false;
    }

    //check whether at least one polygon with the given identifier string exists
    public boolean exists(String ID)
    {
        for(PlayerPolyStruct poly : teamPolys)
        {
            if(poly.ID.equals(ID))
            {
                return true;
            }
        }
        return false;
    }
    public PlayerPolyStruct find(String ID)
    {
        for(PlayerPolyStruct poly : teamPolys)
        {
            if(poly.ID.equals(ID))
            {
                return poly;
            }
        }
        return null;
    }
}
