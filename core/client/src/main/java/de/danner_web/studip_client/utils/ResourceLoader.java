package de.danner_web.studip_client.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

import com.kitfox.svg.SVGCache;
import com.kitfox.svg.app.beans.SVGIcon;

public class ResourceLoader {

    /**
     * This method generates an URL to the resource specified by the input path
     * 
     * @param path
     * @return URL to the resource
     */
    public static URL getURL(String path) {
        return ResourceLoader.class.getClassLoader().getResource(path);
    }

    /**
     * This method generates a SVGIcon from the svg with the given path
     * 
     * @param path
     *            to a svg graphic
     * @return SVGIcon of the path
     */
    public static SVGIcon getSVGIcon(String path) {
        // Read SVG Image from jar File
        InputStream in = ResourceLoader.class.getClassLoader().getResourceAsStream(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        // Load SVG Image Stream to SVG Cache
        URI uri = SVGCache.getSVGUniverse().loadSVG(reader, path);
        SVGIcon svgicon = new SVGIcon();
        svgicon.setAntiAlias(true);
        svgicon.setScaleToFit(true);
        svgicon.setSvgURI(uri);

        return svgicon;
    }
}
