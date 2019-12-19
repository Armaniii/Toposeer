package com.ciyrus.arman.toposeernew;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.tilequery.MapboxTilequery;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.turf.TurfMeta;
import android.widget.ScrollView;
import android.support.v7.widget.Toolbar;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

//import it.michelelacorte.swipeablecard.SwipeableCard;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

// classes needed to initialize map
// classes needed to add the location component
// classes needed to add a marker
// classes to calculate a route
// classes needed to launch navigation UI
//Line Chart classes

public class MapboxApplication extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener, PermissionsListener {
    // variables for adding location layer
    private MapView mapView;
    private MapboxMap mapboxMap;
    // variables for adding location layer
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    // variables for calculating and drawing a route
    private DirectionsRoute currentRoute;
    private static final String TAG = "DirectionsActivity";
    private NavigationMapRoute navigationMapRoute;
    // variables needed to initialize navigation
    private Button button;
    public FeatureCollection routeFeatureCollection;
    private List<Feature> routeExplodedFeature;
    private List<Point> listOfPoints  = new ArrayList<>();
    private List<Integer> elevationProfile = new ArrayList<>();
    private List<Feature> feat = new ArrayList<>();
    private double distance;
    private int newSize;
    private LineChart chart;
    private TextView tvX, tvY;
    private int size;
    RecyclerView recyclerView;
    ArrayList<DataModel> _listDataModel = new ArrayList<>();
    DataModel dm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        //SwipeableCard swipeableCard = (SwipeableCard) findViewById(R.id.swipeCard);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);


        dm = new DataModel();
        dm.setHighestElevation(10);
        dm.setLowestElevation(5);
        _listDataModel.add(dm);


        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);

        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(new MyCustomAdapter(_listDataModel));

    }

    public void setDefaultView()
    {
        _listDataModel.clear();

    }
    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(getString(R.string.navigation_guidance_day), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableLocationComponent(style);

                addDestinationIconSymbolLayer(style);

                mapboxMap.addOnMapClickListener(MapboxApplication.this);
//                button = findViewById(R.id.startButton);
//                button.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        boolean simulateRoute = true;
//                        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
//                                .directionsRoute(currentRoute)
//                                .shouldSimulateRoute(simulateRoute)
//                                .build();
//// Call this method with Context from within an Activity
//                        NavigationLauncher.startNavigation(MapboxApplication.this, options);
//                    }
//                });
            }
        });
        //drawGraph();
    }

    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("destination-icon-id", BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));
        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
        loadedMapStyle.addSource(geoJsonSource);
        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");
        destinationSymbolLayer.withProperties(
                iconImage("destination-icon-id"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );
        loadedMapStyle.addLayer(destinationSymbolLayer);
        //drawGraph();
    }

    public void drawGraph()
    {
        //setContentView(R.layout.activity_linechart);
        //tvX = findViewById(R.id.tvXMax);
//        tvY = findViewById(R.id.tvYMax);
        chart = findViewById(R.id.chart1);
        chart.setViewPortOffsets(15, 0, 0, 0);
        chart.setBackgroundColor(Color.rgb(104, 241, 175));
        // no description text
        chart.getDescription().setEnabled(false);

        // enable touch gestures
        chart.setTouchEnabled(true);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true);

        chart.setDrawGridBackground(false);

        XAxis x = chart.getXAxis();
        x.setEnabled(false);

        YAxis y = chart.getAxisLeft();
        //y.setTypeface(tfLight);
        y.setLabelCount(6, false);
        y.setTextColor(Color.WHITE);
        y.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        y.setDrawGridLines(false);
        y.setAxisLineColor(Color.WHITE);
        y.setEnabled(true);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);

        chart.animateXY(2000, 2000);

        chart.invalidate();
        //chart.invalidate();
    }

    private void setData()
    {
        System.out.println("GRAPH1: " + elevationProfile);
        LineDataSet set1;
        ArrayList<Entry> values = new ArrayList<>();
        for(int i = 0; i < elevationProfile.size(); i++)
        {
            float eleVal = (float) elevationProfile.get(i);
            values.add(new Entry(i,eleVal));
        }
        set1 = new LineDataSet(values, "Dataset 1");
        System.out.println("SET1:" + set1);

        if(chart.getData() != null && chart.getData().getDataSetCount() >0)
        {
            set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
            //System.out.println("GRAPH2");
        }
        else {
            System.out.println("GRAPH3");
            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set1.setCubicIntensity(0.2f);
            set1.setDrawFilled(true);
            set1.setDrawCircles(false);
            set1.setLineWidth(1.8f);
            set1.setCircleRadius(4f);
            set1.setCircleColor(Color.WHITE);
            set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setColor(Color.WHITE);
            set1.setFillColor(Color.WHITE);

            set1.setFillAlpha(75);
            set1.setDrawHorizontalHighlightIndicator(false);
            set1.setFillFormatter(new IFillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return chart.getAxisLeft().getAxisMinimum();
                }
            });
// create a data object with the data sets
            LineData data = new LineData(set1);
            //data.setValueTypeface(tfLight);
            data.setValueTextSize(9f);
            data.setDrawValues(false);


            dm = new DataModel();
            dm.setHighestElevation(15);
            dm.setLowestElevation(0);
            dm.setLineChart(data);
            _listDataModel.add(dm);


            // set data
            //chart.setData(data);
            //chart.invalidate();

        }
    }

    private void drawNavigationPolylineRoute(final DirectionsRoute route) {
        if (mapboxMap != null) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    List<Feature> directionsRouteFeatureList = new ArrayList<>();
                    LineString lineString = LineString.fromPolyline(route.geometry(), PRECISION_6);
                    List<Point> coordinates = lineString.coordinates();
                    for (int i = 0; i < coordinates.size(); i++) {
                        directionsRouteFeatureList.add(Feature.fromGeometry(LineString.fromLngLats(coordinates)));
                    }
                    routeFeatureCollection = FeatureCollection.fromFeatures(directionsRouteFeatureList);
                    GeoJsonSource source = style.getSourceAs("SOURCE_ID");
                    if (source != null) {
                        source.setGeoJson(routeFeatureCollection);
                    }
                }
            });
            routeExplodedFeature = explode(routeFeatureCollection);

            //System.out.println("OIOIOIOIOIOI" + routeExplodedFeature);
            //System.out.println("testsize  " + routeExplodedFeature.size());
            int size = routeExplodedFeature.size();
            int max = 1;
            if (size <= 250)
            {
                max = 50;
                MapboxApplication.this.newSize = size/max;
            }
            if(size>250 && size<1000)
            {
                max = 50;
            }
            if (size >= 1000 && size<10000){
                max = 500;
                MapboxApplication.this.newSize=size/max;
            }
            if (size >= 10000 && size <50000) {
                max = 1000;
                MapboxApplication.this.newSize = size/max;
            }
            if(size >=50000 && size <100000)
            {
                max = 10000;
                MapboxApplication.this.newSize=size/max;
            }
            if(size>=100000 && size <300000)
            {
                max = 15000;
                MapboxApplication.this.newSize=size/max;
            }
            if(size>=300000 && size <400000)
            {
                max = 20000;
                MapboxApplication.this.newSize=size/max;
            }

            System.out.println("MAX:"+max);
            System.out.println("ARRAY SIZE:" +routeExplodedFeature.size());
            //Converts points to Lat Long objects
            for (int j = 0; j < routeExplodedFeature.size(); j++) {
                Point temp = (Point) routeExplodedFeature.get(j).geometry();
                //LatLng latsLongs = new LatLng(temp.latitude(), temp.longitude());
                listOfPoints.add(temp);
            }
            System.out.println("LIST SIZE: " + listOfPoints.size());
            //Elevation query request, retrieves the max elev for a Lat/Long object

            for (int i = 0; i < listOfPoints.size(); i += max) {
                makeElevationRequestToTilequeryApi(listOfPoints.get(i));
            }
        }

    }

    public void retrieveProfile(int el)
    {
        this.elevationProfile.add(el);
        //System.out.println("retrieve: "+elevationProfile);
        System.out.print("ele size = "+this.newSize);
        System.out.println("Other size = "+ this.elevationProfile.size());
        if(this.elevationProfile.size() >= newSize)
        {
            drawGraph();
            setData();
            chart.invalidate();
        }
    }

    public static List<Feature> explode(@NonNull FeatureCollection featureCollection)
    {
        List<Feature> finalFeatureList = new ArrayList<>();
        for (Point singlePoint : TurfMeta.coordAll(featureCollection, true))
        {
            finalFeatureList.add(Feature.fromGeometry(singlePoint));
        }

        return finalFeatureList;
    }

    @SuppressWarnings( {"MissingPermission"})
    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        MapboxApplication.this.elevationProfile.clear();
        MapboxApplication.this.listOfPoints.clear();
        System.out.println("ELEVATION PROFILE: "+ elevationProfile);
        Point destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                locationComponent.getLastKnownLocation().getLatitude());

        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
        if (source != null) {
            source.setGeoJson(Feature.fromGeometry(destinationPoint));
        }

        getRoute(originPoint, destinationPoint);
        //button.setEnabled(true);
       // button.setBackgroundResource(R.color.mapboxBlue);
        return true;
    }

    private void getRoute(Point origin, Point destination) {

        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
// You can get the generic HTTP info about the response
                        Log.d(TAG, "Response code: " + response.code());
                        if (response.body() == null) {
                            Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Log.e(TAG, "No routes found");
                            return;
                        }

                        currentRoute = response.body().routes().get(0);
                        drawNavigationPolylineRoute(currentRoute);
                        //System.out.println(currentRoute);
// Draw the route on the map
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                        //System.out.println("New" +elevationProfile);
                        distance = currentRoute.distance();
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Log.e(TAG, "Error: " + throwable.getMessage());
                    }
                });

        //GeoJsonSource source = mapboxMap.getStyle().getSourceAs("navigation-guidance-day");
        //source.setGeoJson(NavigationMapRoute);
    }

    private void makeElevationRequestToTilequeryApi(@NonNull Point point)
    {
        //System.out.println("CAME HERE");
        final MapboxTilequery elevationQuery = MapboxTilequery.builder()
                .accessToken(getString(R.string.mapbox_access_token))
                .mapIds("mapbox.mapbox-terrain-v2")
                .query(point)
                .geometry("polygon")
                .layers("contour")
                .build();

        elevationQuery.enqueueCall(new Callback<FeatureCollection>()
        {
            @Override
            public void onResponse(Call<FeatureCollection> call, Response<FeatureCollection> response)
            {
                //response


                if (response.body().features() != null) {
                    List<Feature> featureList = response.body().features();

                    List<Integer> listOfElevationNumbers = new ArrayList<Integer>() {};

                    // Build a list of the elevation numbers in the response.
                    for (Feature singleFeature : featureList)
                    {
                        listOfElevationNumbers.add(Integer.parseInt(singleFeature.getStringProperty("ele")));
                    }
                    int max = Collections.max(listOfElevationNumbers);
                    //MapboxApplication.this.elevationProfile.add(max);

                    retrieveProfile(max);



                    //resultSource.setGeoJson(featureList.get(featureList.size() - 1));
                    //System.out.println("ELEVATION:   "+featureList.get(featureList.size()-1).getStringProperty("ele"));
                    //elevation = Integer.parseInt(featureList.get(featureList.size()-1).getStringProperty("ele"));
                }
                else
                {
                    Toast.makeText(MapboxApplication.this, R.string.no_elevation, Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<FeatureCollection> call, Throwable throwable) {
                Toast.makeText(MapboxApplication.this, R.string.no_elevation, Toast.LENGTH_SHORT).show();
                Timber.d("Request failed: %s", throwable.getMessage());
//                System.out.println("NULL #2");
                //Toast.makeText(ElevationQueryActivity.this,
                  //      R.string.elevation_tilequery_api_response_error, Toast.LENGTH_SHORT).show();
            }
        });

        }



    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
// Activate the MapboxMap LocationComponent to show user location
// Adding in LocationComponentOptions is also an optional parameter
            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(this, loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);
// Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent(mapboxMap.getStyle());
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public ArrayList<DataModel> listArray() {

        ArrayList<DataModel> objList = new ArrayList<DataModel>();
        DataModel dm;

        dm = new DataModel();
        dm.setHighestElevation(50);
        dm.setLowestElevation(30);
        objList.add(dm);
        return objList;
    }

}

