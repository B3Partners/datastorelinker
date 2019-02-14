/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.b3p.geotools.data.linker.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import nl.b3p.geotools.data.linker.DataStoreLinker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;

/**
 *
 * @author Erik van de Pol
 */
public class DataStoreUtil {
    protected static Log log = LogFactory.getLog(DataStoreUtil.class);

    public static DataTypeList getDataTypeList(Map<String, Object> inputParameters) throws Exception {
        List<String> good = new ArrayList<String>();
        List<String> bad = new ArrayList<String>();

        DataStore dataStore = null;
        try {
            dataStore = DataStoreLinker.openDataStore(inputParameters);

            if (dataStore == null) {
                return null;
            }
            log.debug("DataStore class: " + dataStore.getClass());

            good.addAll(Arrays.asList(dataStore.getTypeNames()));
            Collections.sort(good);

            /*for (String typename : dataStore.getTypeNames()) {
                FeatureIterator<SimpleFeature> iterator = null;
                FeatureCollection fc = null;
                try {
                    fc = dataStore.getFeatureSource(typename).getFeatures();
                    iterator = fc.features();
                    if (iterator.hasNext()) {
                        SimpleFeature feature = iterator.next();
                        if (feature != null) {
                            // we can also test for allowed geometries and such:
                            //String error = DataStoreLinker.testFeature(feature);
                            //if (error == null) ...
                            good.add(typename);
                        }
                    }

                } catch (DataSourceException e) {
                    bad.add(typename);
                    log.warn("Error reading features, cause: " + e.getLocalizedMessage());

                } catch (NoSuchElementException e) {
                    bad.add(typename);
                    log.warn("Table '" + typename + "' contains unsupported datatypes? " + e.getLocalizedMessage());

                } catch (Exception e) {
                    bad.add(typename);
                    log.warn("Table '" + typename + "' contains error: " + e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());

                } finally {
                    if (iterator != null) {
                        if (fc != null) {
                            fc.close(iterator);
                        } else {
                            iterator.close();
                        }
                    }
                }
            }*/
        } finally {
            if (dataStore != null)
                dataStore.dispose();
        }

        return new DataTypeList(good, bad);
    }
}