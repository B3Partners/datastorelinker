/**
 * $Id$
 */

package nl.b3p.geotools.data.linker.blocks;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nl.b3p.geotools.data.linker.ActionFactory;
import nl.b3p.geotools.data.linker.feature.EasyFeature;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.Map;
import nl.b3p.geotools.data.linker.Status;


/**
 * Deze Action verwijdert een geometrie waarvan alle vertexen onder een bepaalde
 * precisie-threshold vallen
 *
 * Hiermee kan worden voorkomen dat ArcGIS hierop crasht.
 * @author matthijsln
 */
public class ActionGeometry_VertexPrecisionThreshold extends Action {
    private static final Log log = LogFactory.getLog(ActionGeometry_VertexPrecisionThreshold.class);

    private static final RoundingMode rm = RoundingMode.FLOOR;

    private int scale = 2;
    private boolean floatPrecision = false;

    public void setScale(int scale) {
        this.scale = scale;
    }

    public void setFloatPrecision(boolean floatPrecision) {
        this.floatPrecision = floatPrecision;
    }

    public static List<List<String>> getConstructors() {
        List<List<String>> constructors = new ArrayList<List<String>>();

        constructors.add(Arrays.asList(new String[]{
                ActionFactory.SCALE,
                ActionFactory.FLOAT_PRECISION
        }));
        
        return constructors;
    }

    @Override
    public EasyFeature execute(EasyFeature feature) throws Exception {

        Geometry geometry = (Geometry)feature.getFeature().getDefaultGeometry();

        if(geometry == null) {
            return feature;
        }

        if((geometry instanceof Point) || (geometry instanceof MultiPoint)) {
            return feature;
        }

        if(!(geometry instanceof GeometryCollection)) {
            if(verticesBelowPrecisionThreshold(geometry.getCoordinates(), scale, floatPrecision)) {
                if(log.isDebugEnabled()) {
                    log.debug("feature below precision threshold, removing: " + geometry.toString());
                }
                return null;
            } else {
                return feature;
            }
        } else {
            GeometryCollection gc = (GeometryCollection)geometry;

            if(gc.isEmpty()) {
                return feature;
            }

            List features = new ArrayList();
            boolean haveFeaturesToRemove = false;
            for(int i = 0; i < gc.getNumGeometries(); i++) {
                Geometry g = gc.getGeometryN(i);
                if(!verticesBelowPrecisionThreshold(g.getCoordinates(), scale, floatPrecision)) {
                    features.add(g);
                } else {
                    haveFeaturesToRemove = true;
                    if(log.isDebugEnabled()) {
                        log.debug("geometry #" + (i+1) + " from geometrycollection is below precision threshold: " + g.toString());
                    }
                }
            }

            if(!haveFeaturesToRemove) {
                return feature;
            }

            if(features.isEmpty()) {
                if(log.isDebugEnabled()) {
                    log.debug("all features from geometrycollection below precision threshold, removing entire feature: " + geometry.toString());
                }
                return null;
            }

            if(log.isDebugEnabled()) {
                log.debug("rebuilding geometrycollection without features below precision threshold; " + features.size() + " of " + gc.getNumGeometries() + " remain");
                log.debug("original geometrycollection: " + geometry.toString());
            }

            if(gc instanceof MultiPolygon) {
                geometry = geometry.getFactory().createMultiPolygon((Polygon[])features.toArray(new Polygon[] {}));
            } else if(gc instanceof MultiLineString) {
                geometry = geometry.getFactory().createMultiLineString((LineString[])features.toArray(new LineString[] {}));
            }

            if(log.isDebugEnabled()) {
                log.debug("rebuilt geometrycollection: " + geometry.toString());
            }

            String geometryName = feature.getFeature().getDefaultGeometryProperty().getDescriptor().getLocalName();
            feature.setAttribute(geometryName, geometry);            
            return feature;
        }
    }

    public static boolean verticesBelowPrecisionThreshold(Coordinate[] vertices, int scale, boolean floatPrecision) {
        BigDecimal x = null; BigDecimal y = null;

        for(int i = 0; i < vertices.length; i++) {
            Coordinate c = vertices[i];
            BigDecimal cx, cy;
            if(floatPrecision) {
                cx = new BigDecimal((float)c.x).setScale(scale, rm);
                cy = new BigDecimal((float)c.y).setScale(scale, rm);
            } else {
                cx = new BigDecimal(c.x).setScale(scale, rm);
                cy = new BigDecimal(c.y).setScale(scale, rm);
            }
            if(x == null) {
                x = cx;
                y = cy;
                //System.out.printf("1st  coord: %.1f, %.1f\n", x, y);
            } else {
                if(!cx.equals(x) || !cy.equals(y)) {
                    /* Een vertex heeft een coordinaat die verschilt van de eerste
                     * vertex bij de schaal, dus feature ongewijzigd doorlaten
                     */
                    //System.out.printf("diff coord: %.1f, %.1f\n", cx, cy);
                    return false;
                }
                //System.out.printf("same coord: %.1f, %.1f\n", cx, cy);
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "vertex precision threshold";
    }

    @Override
    public String getDescription_NL() {
        return "Verwijder geometrie waarvan de coordinaten van alle vertices bij een bepaalde precisie niet verschillen";
    }

    @Override
    public void flush(Status status, Map properties) throws Exception {
    }
    
    @Override
    public void processPostCollectionActions(Status status, Map properties) throws Exception {
    }
}
