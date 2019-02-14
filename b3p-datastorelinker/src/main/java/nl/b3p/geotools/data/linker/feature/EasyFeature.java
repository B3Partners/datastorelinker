package nl.b3p.geotools.data.linker.feature;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import nl.b3p.geotools.data.linker.FeatureException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 *
 * @author Gertjan Al, B3Partners
 */
public class EasyFeature {

    private static final Log log = LogFactory.getLog(EasyFeature.class);
    private SimpleFeature feature;
    private boolean skipped = false;

    /**
     * Constructor of this easy feature, define the SimpleFeature here
     *
     * @param feature The SimpleFeature to use
     */
    public EasyFeature(SimpleFeature feature) {
        this.feature = feature;
    }

    /**
     * Release a usable OpenGIS feature
     *
     * @return The transformed feature
     */
    public SimpleFeature getFeature() {
        return feature;
    }

    /**
     * Get attribute using attributeID as location in attribute array
     *
     * @param attributeID the id of the attribute to retrieve
     * @return the feature
     */
    public AttributeType getAttributeType(int attributeID) {
        return feature.getFeatureType().getType(attributeID);
    }

    /**
     * Get attribute with given atrributename
     *
     * @param name the name of the attribute to retrieve
     * @return the attributeType
     */
    public AttributeType getAttributeType(String name) {
        return feature.getFeatureType().getType(name);
    }

    /**
     * Insert default attribute at a given postition, with name and classtype
     *
     * @param attributeID the id of the attrubute
     * @param name the name of the attribute
     * @param binding the binding
     */
    public void insertAttributeDescriptor(int attributeID, String name, Class binding) {
        // Create new attribute
        AttributeTypeBuilder attributeTypeBuilder = new AttributeTypeBuilder();
        attributeTypeBuilder.setName(name);
        attributeTypeBuilder.setBinding(binding);

        insertAttributeDescriptor(attributeID, attributeTypeBuilder.buildDescriptor(name));
    }

    /**
     * Used for extended AttributeType inserting. Build your own AttributeType
     * instead of using the default method
     *
     * @param attributeID the id of the attribute
     * @param attributeDescriptor the descriptor
     */
    public void insertAttributeDescriptor(int attributeID, AttributeDescriptor attributeDescriptor) {
        // save userdata
        Map<Object, Object> ud = feature.getUserData();

        // Add attributeType to current attributeList
        List<AttributeDescriptor> attributeDescriptors = new ArrayList<AttributeDescriptor>(feature.getFeatureType().getAttributeDescriptors());
        attributeDescriptors.add(attributeID, attributeDescriptor);

        // Build FeatureType
        SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder();
        featureTypeBuilder.init(feature.getFeatureType());
        featureTypeBuilder.setAttributes(attributeDescriptors);

        // Create new Feature
        SimpleFeatureBuilder simpleFeatureBuilder = new SimpleFeatureBuilder(featureTypeBuilder.buildFeatureType());

        // Create feature attributes list
        List<Object> attributes = feature.getAttributes();
        attributes.add(attributeID, null);
        // Build new feature with new values array
        feature = simpleFeatureBuilder.buildFeature(getID(), attributes.toArray(new Object[attributes.size()]));
        feature.getUserData().putAll(ud);
    }

    /**
     * Used for extended AttributeType adding. Build your own AttributeType
     * instead of using the default method
     *
     * @param attributeDescriptor the descriptor
     */
    public void addAttributeDescriptor(AttributeDescriptor attributeDescriptor) {
        insertAttributeDescriptor(feature.getAttributeCount(), attributeDescriptor);
    }

    public void addAttributeDescriptor(String name, Class binding) {
        insertAttributeDescriptor(feature.getAttributeCount(), name, binding);
    }

    /**
     * Remove all AttributeTypes with values
     *
     * @param keepGeom boolean do not remove geometry column
     * @throws Exception general exception
     */
    public void removeAllAttributeDescriptors(boolean keepGeom) throws Exception {
        // save userdata
        Map<Object, Object> ud = feature.getUserData();

        String geometryName = null;
        Integer geometryID = null;
        GeometryAttribute ga = null;
        AttributeDescriptor gad = null;
        if (keepGeom) {
            ga = feature.getDefaultGeometryProperty();
            if (ga != null) {
                geometryName = ga.getDescriptor().getLocalName();
                geometryID = getAttributeDescriptorIDbyName(geometryName);
            }
        }

        SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder();
        featureTypeBuilder.init(feature.getFeatureType());

        List<AttributeDescriptor> attributeDescriptors = new ArrayList<AttributeDescriptor>(feature.getFeatureType().getAttributeDescriptors());
        if (geometryID != null) {
            gad = attributeDescriptors.get(geometryID);
        }
        attributeDescriptors = new ArrayList<AttributeDescriptor>();
        if (keepGeom && gad != null) {
            attributeDescriptors.add(gad);
        }
        featureTypeBuilder.setAttributes(attributeDescriptors);

        SimpleFeatureBuilder simpleFeatureBuilder = new SimpleFeatureBuilder(featureTypeBuilder.buildFeatureType());
        List<Object> attributes = feature.getAttributes();
        Object[] values = null;
        if (keepGeom && gad != null) {
            Object value = null;
            if (geometryID != null) {
                value = attributes.get(geometryID);
            }
            values = new Object[]{value};
        }
        if (values == null) {
            feature = simpleFeatureBuilder.buildFeature(getID());
        } else {
            feature = simpleFeatureBuilder.buildFeature(getID(), values);
        }
        feature.getUserData().putAll(ud);
    }

    /**
     * Remove AttributeType at attributeTypeID
     *
     * @param attributeID the id of the attribute
     * @throws Exception general exception
     */
    public void removeAttributeDescriptor(int attributeID) throws Exception {
        // save userdata
        Map<Object, Object> ud = feature.getUserData();

        SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder();
        featureTypeBuilder.init(feature.getFeatureType());

        List<AttributeDescriptor> attributeDescriptors = new ArrayList<AttributeDescriptor>(feature.getFeatureType().getAttributeDescriptors());
        attributeDescriptors.remove(attributeID);
        featureTypeBuilder.setAttributes(attributeDescriptors);

        SimpleFeatureBuilder simpleFeatureBuilder = new SimpleFeatureBuilder(featureTypeBuilder.buildFeatureType());
        List<Object> attributes = feature.getAttributes();
        attributes.remove(attributeID);

        feature = simpleFeatureBuilder.buildFeature(getID(), attributes.toArray(new Object[attributes.size()]));
        feature.getUserData().putAll(ud);
    }

    /**
     * Remove AttributeType by name
     *
     * @param name the name of the attribute
     * @throws Exception general exception
     */
    public void removeAttributeDescriptor(String name) throws Exception {
        removeAttributeDescriptor(getAttributeDescriptorIDbyName(name));
    }

    /**
     * Default way to set AttributeType at specified attributeID, overwrites the
     * current AttributeType at that index
     *
     * @param attributeID the id of the attribute
     * @param name the name of the attribute type
     * @param binding the binding
     * @param keepValue keep the value or not
     */
    public void setAttributeDescriptor(int attributeID, String name, Class binding, boolean keepValue) {
        // Create new attribute
        AttributeTypeBuilder attributeTypeBuilder = new AttributeTypeBuilder();
        attributeTypeBuilder.setName(name);
        attributeTypeBuilder.setBinding(binding);

        setAttributeDescriptor(attributeID, attributeTypeBuilder.buildDescriptor(name), keepValue);
    }

    /**
     * Extended way to set a AttributeType; overwrites the current AttributeType
     * at that index
     *
     * @param attributeID the id of the attribute
     * @param attributeDescriptor the descriptor
     * @param keepValue keep the value or not
     */
    public void setAttributeDescriptor(int attributeID, AttributeDescriptor attributeDescriptor, boolean keepValue) {
        // save userdata
        Map<Object, Object> ud = feature.getUserData();

        // Add attributeType to current attributeList
        List<AttributeDescriptor> attributeDescriptors = new ArrayList<AttributeDescriptor>(feature.getFeatureType().getAttributeDescriptors());
        attributeDescriptors.set(attributeID, attributeDescriptor);

        // Build FeatureType
        SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder();
        featureTypeBuilder.init(feature.getFeatureType());
        featureTypeBuilder.setAttributes(attributeDescriptors);

        // Create new Feature
        SimpleFeatureBuilder simpleFeatureBuilder = new SimpleFeatureBuilder(featureTypeBuilder.buildFeatureType());

        // Create feature attributes list
        List<Object> attributes = feature.getAttributes();
        if (!keepValue) {
            attributes.set(attributeID, null);
        }

        feature = simpleFeatureBuilder.buildFeature(getID(), attributes.toArray(new Object[attributes.size()]));
        feature.getUserData().putAll(ud);
    }

    public void setAttributeDescriptor(String attributeName, AttributeDescriptor attributeDescriptor) throws Exception {
        if (containsAttributeDescriptor(attributeName)) {
            int attributeID = getAttributeDescriptorIDbyName(attributeName);
            setAttributeDescriptor(attributeID, attributeDescriptor, true);
        } else {
            addAttributeDescriptor(attributeDescriptor);
        }
    }

    /**
     * Lookup attributeID of AttributeType name
     *
     * @param name the name of the descriptor
     * @return index of the attribute descriptor
     * @throws FeatureException general FeatureException
     */
    public int getAttributeDescriptorIDbyName(String name) throws FeatureException {
        List<AttributeDescriptor> attributeDescriptors = feature.getFeatureType().getAttributeDescriptors();
        for (int i = 0; i < attributeDescriptors.size(); i++) {
            if (attributeDescriptors.get(i).getLocalName().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get name of AttributeType at given position
     *
     * @param attributeID the id of the attribute
     * @return the attribute descriptor
     * @throws java.lang.Exception general exception
     */
    public String getAttributeDescriptorNameByID(int attributeID) throws Exception {
        if (attributeID < 0 || attributeID >= getAttributeCount()) {
            throw new Exception("AttributeType attributeID " + attributeID + " not allowed");
        }
        return feature.getFeatureType().getAttributeDescriptors().get(attributeID).getLocalName();
    }

    /**
     * Get number of Attributes
     *
     * @return the number of attributes found
     */
    public int getAttributeCount() {
        return feature.getAttributeCount();
    }

    /**
     * Set Attribute at a specified position
     *
     * @param attributeID id of the attribute
     * @param attribute the attribute in question
     */
    public void setAttribute(int attributeID, Object attribute) {
        feature.setAttribute(attributeID, attribute);
    }

    /**
     * Set Attribute at a specified name
     *
     * @param name the name of the attribute
     * @param attribute the attribute
     */
    public void setAttribute(String name, Object attribute) {
        feature.setAttribute(name, attribute);
    }

    public Object getAttribute(int attributeID) {
        return feature.getAttribute(attributeID);
    }

    public Object getAttribute(String name) {
        return feature.getAttribute(name);
    }

    public List<Object> getAttributes() {
        return feature.getAttributes();
    }

    @Override
    public String toString() {
        String attributes = "";
        for (AttributeType attribute : feature.getFeatureType().getTypes()) {
            attributes += ", " + attribute.getName().toString();
        }

        if (attributes.length() > 2) {
            attributes = attributes.substring(2);
        } else {
            attributes = "[empty]";
        }

        return "Feature(" + attributes + ")";
    }

    public String getTypeName() {
        return feature.getFeatureType().getTypeName();
    }

    public void setTypeName(String name) {
        // save userdata
        Map<Object, Object> ud = feature.getUserData();

        SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder();
        featureTypeBuilder.addAll(feature.getFeatureType().getAttributeDescriptors());
        featureTypeBuilder.setName(name);

        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureTypeBuilder.buildFeatureType());
        feature = featureBuilder.buildFeature(feature.getID(), feature.getAttributes().toArray(new Object[feature.getAttributeCount()]));

        feature.getUserData().putAll(ud);
    }

    public EasyFeature copy() {
        return copy(null, null);
    }

    public EasyFeature copy(String ID) {
        return copy(null, ID);
    }

    public EasyFeature copy(String name, String ID) {
        SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder();
        // Use typename and ID of source if name or ID is empty
        if (name == null) {
            name = feature.getFeatureType().getTypeName();
        }
        if (ID == null) {
            ID = feature.getID();
        }
        featureTypeBuilder.setName(name);

        // Build FeatureType
        List<AttributeDescriptor> attributeDescriptors = new ArrayList<AttributeDescriptor>(feature.getFeatureType().getAttributeDescriptors());
        //featureTypeBuilder.init(feature.getFeatureType());
        featureTypeBuilder.setAttributes(attributeDescriptors);

        // Create new Feature
        SimpleFeatureBuilder simpleFeatureBuilder = new SimpleFeatureBuilder(featureTypeBuilder.buildFeatureType());

        // Create feature attributes list
        List<Object> attributes = feature.getAttributes();

        // Build new feature with new values array
        SimpleFeature f = simpleFeatureBuilder.buildFeature(ID, attributes.toArray(new Object[attributes.size()]));

        // copy user data
        f.getUserData().putAll(feature.getUserData());

        return new EasyFeature(f);
    }

    public String getID() {
        return feature.getID();
    }

    public SimpleFeatureType getFeatureType() {
        return feature.getFeatureType();
    }

    /**
     * Check if attributeID is legal (above zero and above attributeCount)
     *
     * @param attributeID the id of the attribute
     * @return boolean whether the descriptor is present
     */
    public boolean containsAttributeDescriptor(int attributeID) {
        return (attributeID >= 0) && (attributeID < getAttributeCount());
    }

    public boolean containsAttributeDescriptor(String attributeName) {
        for (AttributeDescriptor descriptor : feature.getFeatureType().getAttributeDescriptors()) {
            if (descriptor.getName().getLocalPart().equals(attributeName)) {
                return true;
            }
        }
        return false;
    }

    public static AttributeDescriptor buildGeometryAttributeDescriptor(String attributeName, Class binding, boolean isNillable, CoordinateReferenceSystem crs) {
        AttributeTypeBuilder attributeTypeBuilder = new AttributeTypeBuilder();
        attributeTypeBuilder.setBinding(binding);
        attributeTypeBuilder.setCRS(crs);
        attributeTypeBuilder.setName(attributeName);
        attributeTypeBuilder.setNillable(isNillable);

        // Prevent warnings; save as VARCHAR(256)
        attributeTypeBuilder.setLength(256);

        return attributeTypeBuilder.buildDescriptor(attributeName);
    }

    public void setCRS(CoordinateReferenceSystem crs) throws Exception {
        GeometryAttribute ga = feature.getDefaultGeometryProperty();
        if (ga == null) {
            return;
        }
        String geometryName = ga.getDescriptor().getLocalName();
        boolean isNillable = feature.getFeatureType().getGeometryDescriptor().isNillable();
        Class binding = feature.getFeatureType().getGeometryDescriptor().getType().getBinding();
        int attributeID = getAttributeDescriptorIDbyName(geometryName);

        // Create new geometryColumn with previous settings
        setAttributeDescriptor(attributeID, buildGeometryAttributeDescriptor(geometryName, binding, isNillable, crs), true);
    }

    public void repairGeometry() throws FeatureException {
        Class binding = feature.getDefaultGeometry().getClass();
        FeatureType ft = feature.getFeatureType();
        Class typeBinding = ft.getGeometryDescriptor().getType().getBinding();

        if (!binding.equals(typeBinding)) {

            log.debug("feature binding: " + binding.toString()
                    + " for feature: " + feature.getID());
            log.debug("feature type binding: " + typeBinding.toString()
                    + " for feature type: " + ft.getName().getLocalPart());

            GeometryAttribute ga = feature.getDefaultGeometryProperty();
            CoordinateReferenceSystem crs =
                    ga.getDescriptor().getCoordinateReferenceSystem();
            String geometryName = ga.getDescriptor().getLocalName();
            boolean isNillable =
                    feature.getFeatureType().getGeometryDescriptor().isNillable();
            int attributeID = getAttributeDescriptorIDbyName(geometryName);

            // Create new geometryColumn with repaired binding
            setAttributeDescriptor(attributeID,
                    buildGeometryAttributeDescriptor(geometryName, binding, isNillable, crs), true);

        }
    }

    public void convertGeomTo2D() {
        if (feature != null && feature.getDefaultGeometry() != null) {
            Geometry g = (Geometry) feature.getDefaultGeometry();
            Coordinate[] cs = g.getCoordinates();

            boolean hasZCoord = false;
            for (int t = 0; t < cs.length; t++) {
                if (!(Double.isNaN(cs[t].z))) {
                    hasZCoord = true;
                }
            }

            if (hasZCoord) {
                WKBWriter writer = new WKBWriter(2);
                WKBReader reader = new WKBReader();
                Geometry geom2D = null;

                byte[] binary = writer.write(g);
                try {
                    geom2D = reader.read(binary);
                } catch (ParseException parsEx) {
                    log.error("Error reading wkb", parsEx);
                }

                feature.setDefaultGeometry(geom2D);
            }
        }

    }

    /**
     * @return the skipped
     */
    public boolean isSkipped() {
        return skipped;
    }

    /**
     * @param skipped the skipped to set
     */
    public void setSkipped(boolean skipped) {
        this.skipped = skipped;
    }
}
