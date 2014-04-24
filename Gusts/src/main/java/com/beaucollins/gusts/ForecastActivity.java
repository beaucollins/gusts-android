package com.beaucollins.gusts;

import android.app.Activity;
import android.os.Bundle;

import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class ForecastActivity extends Activity
implements Response.Listener<Document>, Response.ErrorListener {

    public static final String TAG="Gusts";

    public static final String FORECAST_URL="http://graphical.weather.gov/xml/sample_products/browser_interface/ndfdXMLclient.php?whichClient=NDFDgen&lat=47.65&lon=-122.29&listLatLon=&lat1=&lon1=&lat2=&lon2=&resolutionSub=&listLat1=&listLon1=&listLat2=&listLon2=&resolutionList=&endPoint1Lat=&endPoint1Lon=&endPoint2Lat=&endPoint2Lon=&listEndPoint1Lat=&listEndPoint1Lon=&listEndPoint2Lat=&listEndPoint2Lon=&zipCodeList=&listZipCodeList=&centerPointLat=&centerPointLon=&distanceLat=&distanceLon=&resolutionSquare=&listCenterPointLat=&listCenterPointLon=&listDistanceLat=&listDistanceLon=&listResolutionSquare=&citiesLevel=&listCitiesLevel=&sector=&gmlListLatLon=&featureType=&requestedTime=&startTime=&endTime=&compType=&propertyName=&product=time-series&begin=2004-01-01T00%3A00%3A00&end=2018-04-24T00%3A00%3A00&Unit=e&maxt=maxt&temp=temp&wspd=wspd&wdir=wdir&wx=wx&wgust=wgust";

    private RequestQueue mRequestQueue;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRequestQueue = Volley.newRequestQueue(this);

        setContentView(R.layout.forecast);

    }

    public void onResume() {

        super.onResume();

        Request<?> request = new XMLRequest(FORECAST_URL, this, this);
        mRequestQueue.add(request);
    }

    @Override
    public void onResponse(Document response) {
        try {
            android.util.Log.d(TAG, "Response: " + response);
            XPath xPath = XPathFactory.newInstance().newXPath();
            Node root = response.getDocumentElement();

            android.util.Log.d(TAG, "Root node: " + root.getNodeName());
            Node node = (Node)xPath.evaluate("//title", root, XPathConstants.NODE);

            NodeList sustained = (NodeList)xPath.evaluate("//wind-speed[@type='sustained']/value", root, XPathConstants.NODESET);
            NodeList gusts = (NodeList)xPath.evaluate("//wind-speed[@type='gust']/value", root, XPathConstants.NODESET);
            NodeList directions = (NodeList)xPath.evaluate("//direction/value", root, XPathConstants.NODESET);


            for (int i=0; i<sustained.getLength(); i++) {
                Node speed = sustained.item(i);
                Node gust = gusts.item(i);
                Node direction = directions.item(i);

                android.util.Log.d(TAG, "Speed: " + speed.getTextContent() + " Gust: " + gust.getTextContent() + " Direction: " + direction.getTextContent());

            }

            android.util.Log.d(TAG, "Title: " + node.getTextContent());
        } catch (XPathExpressionException e) {
            android.util.Log.e(TAG, "Invalid expression", e);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        android.util.Log.e(TAG, "Request failed", error);
    }

}

class XMLRequest extends Request<Document> {

    final private Response.Listener<Document> mListener;

    public XMLRequest(String url, Response.Listener<Document> listener, Response.ErrorListener errorListener) {
        super(Request.Method.GET, url, errorListener);
        mListener = listener;
    }

    @Override
    protected Response<Document> parseNetworkResponse(NetworkResponse response) {
        try {
            InputStream stream = new ByteArrayInputStream(response.data);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(stream, null);

            return Response.success(document, HttpHeaderParser.parseCacheHeaders(response));
        } catch (ParserConfigurationException e) {
            return Response.error(new VolleyError(e));
        } catch (SAXException e) {
            return Response.error(new VolleyError(e));
        } catch (IOException e) {
            return Response.error(new VolleyError(e));
        }
    }

    @Override
    protected void deliverResponse(Document response) {
        mListener.onResponse(response);
    }

}