package com.markerdemo.utils;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.oguzbabaoglu.fancymarkers.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarkerManager<T extends CustomMarker> implements
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener {

    private final GoogleMap googleMap;

    private OnMarkerClickListener<T> onMarkerClickListener;
    private OnInfoWindowClickListener<T> onInfoWindowClickListener;

    private T lastSelected;

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (onInfoWindowClickListener != null) {
            onInfoWindowClickListener.onInfoWindowClick(markerCache.get(marker));
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        if (lastSelected != null) {
            lastSelected.setSelected(false);
        }

        final T item = markerCache.get(marker);

        if (item != null) {
            item.setSelected(true);
            lastSelected = item;
        }

        return onMarkerClickListener != null
                && onMarkerClickListener.onMarkerClick(markerCache.get(marker));
    }

    public interface OnMarkerClickListener<T extends  CustomMarker> {

        boolean onMarkerClick(T marker);
    }

    public interface OnInfoWindowClickListener<T extends  CustomMarker> {

        void onInfoWindowClick(T marker);
    }

    public void setOnMarkerClickListener(OnMarkerClickListener<T> onMarkerClickListener) {
        this.onMarkerClickListener = onMarkerClickListener;
    }

    public void setOnInfoWindowClickListener(OnInfoWindowClickListener<T> onInfoWindowClickListener) {
        this.onInfoWindowClickListener = onInfoWindowClickListener;
    }

    /**
     * Marker cache for on screen markers.
     */
    private MarkerCache<T> markerCache = new MarkerCache<>();

    public MarkerManager(GoogleMap map) {
        googleMap = map;
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnInfoWindowClickListener(this);
    }

    /**
     * A cache of markers.
     */
    private static class MarkerCache<T> {
        private Map<T, Marker> cache = new HashMap<>();
        private Map<Marker, T> cacheReverse = new HashMap<>();

        public Marker get(T item) {
            return cache.get(item);
        }

        public T get(Marker m) {
            return cacheReverse.get(m);
        }

        public void put(T item, Marker m) {
            cache.put(item, m);
            cacheReverse.put(m, item);
        }

        public void clear() {
            cache.clear();
            cacheReverse.clear();
        }

        public void remove(T item) {
            Marker m = cache.get(item);
            cacheReverse.remove(m);
            cache.remove(item);
        }
    }

    /**
     * Add a marker to the map.
     *
     * @param marker marker to add
     */
    public void addMarker(T marker) {

        final MarkerOptions markerOptions = new MarkerOptions();
        marker.setMarkerManager(this);
        marker.prepareMarker(markerOptions);

        markerCache.put(marker, googleMap.addMarker(markerOptions));

        marker.onAdd();
    }

    /**
     * Add all markers to the map.
     *
     * @param markers markers to add
     */
    public void addMarkers(List<T> markers) {

        for (T marker : markers) {
            addMarker(marker);
        }
    }

    /**
     * Remove a marker from the map.
     *
     * @param marker marker to remove
     */
    public void removeMarker(T marker) {

        final Marker realMarker = markerCache.get(marker);

        if (realMarker != null) {
            realMarker.remove();
        }

        markerCache.remove(marker);
    }

    /**
     * Clear the map.
     */
    public void clear() {

        googleMap.clear();
        markerCache.clear();
    }

    /**
     * Get the the real marker object.
     *
     * @param marker marker which to obtain its real marker
     * @return real marker
     */
    public Marker getMarker(T marker) {
        return markerCache.get(marker);
    }
}

