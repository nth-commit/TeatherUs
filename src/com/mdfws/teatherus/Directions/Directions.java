package com.mdfws.teatherus.directions;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import com.mdfws.teatherus.util.Geom;

public class Directions {
	
	private ArrayList<Direction> directions;
	private ArrayList<Point> points;
	
	public Directions(String jsonString) throws JSONException {
		JSONObject route = new JSONObject(jsonString).getJSONArray("routes").getJSONObject(0); // Only one route supported
		JSONObject leg = route.getJSONArray("legs").getJSONObject(0); // Only one leg supported
		createDirections(leg.getJSONArray("steps"));
		createPoints();
	}
	
	private void createDirections(JSONArray steps) throws JSONException {
		directions = new ArrayList<Direction>();
		int length = steps.length();
		for (int i = 0; i < length; i++) {
			directions.add(new Direction(steps.getJSONObject(i)));
		}
	}
	
	private void createPoints() {
		Direction currentDirection = null;
		Direction nextDirection = null;
		
		points = new ArrayList<Point>();
		Point currentPoint = null;
		Point prevPoint = createLastPoint();
		points.add(prevPoint);
		
		for (int i = directions.size() - 1; i >= 0; i--) {
			currentDirection = directions.get(i);
			List<LatLng> currentDirectionPoints = currentDirection.getPoints();
			for (int j = currentDirectionPoints.size() - 1; j >= 0; j--) {
				currentPoint = createPoint(currentDirectionPoints.get(j), prevPoint, currentDirection);
				points.add(0, currentPoint);
				prevPoint = currentPoint;
			}
			nextDirection = currentDirection;
		}
	}
	
	private Point createLastPoint() {
		return new Point() {{
			distanceToCurrentDirectionMeters = 0;
			timeToCurrentDirectionMinutes = 0;
			distanceToNextDirectionMeters = 0;
			timeToNextDirectionMinutes = 0;
			distanceToArrivalMeters = 0;
			timeToArrivalMinutes = 0;
			direction = directions.get(directions.size() - 1);
			nextDirection = null;
			nextPoint = null;
		}};
	}
	
	private Point createPoint(LatLng location, final Point next, final Direction currentDirection) {
		final double distanceToNext = Geom.calculateDistance(location, next.location);
		final boolean isNewDirection = next.direction != currentDirection;
		return new Point() {{
			distanceToCurrentDirectionMeters = isNewDirection ? 0 : nextPoint.distanceToCurrentDirectionMeters + distanceToNext;
			distanceToNextDirectionMeters = isNewDirection ? 0 : nextPoint.distanceToNextDirectionMeters + distanceToNext;
			distanceToArrivalMeters = nextPoint.distanceToArrivalMeters + distanceToNext;
			direction = currentDirection;
			nextDirection = isNewDirection ? nextPoint.direction : nextPoint.nextDirection;
			nextPoint = next;
		}};
	}
	
	public List<Direction> getDirectionsList() {
		return directions;
	}
	
	public List<Point> getPoints() {
		return points;
	}
}