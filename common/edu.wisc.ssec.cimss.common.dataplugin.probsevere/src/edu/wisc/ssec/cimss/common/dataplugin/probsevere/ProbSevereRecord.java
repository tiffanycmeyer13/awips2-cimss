package edu.wisc.ssec.cimss.common.dataplugin.probsevere;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Index;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.persist.IPersistable;
import com.raytheon.uf.common.dataplugin.persist.PersistablePluginDataObject;
import com.raytheon.uf.common.datastorage.IDataStore;
import com.raytheon.uf.common.datastorage.records.IDataRecord;
import com.raytheon.uf.common.datastorage.records.StringDataRecord;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

import edu.wisc.ssec.cimss.common.dataplugin.probsevere.impl.ProbSevereGeometry;
import edu.wisc.ssec.cimss.common.dataplugin.probsevere.impl.ProbSevereModelType;
import edu.wisc.ssec.cimss.common.dataplugin.probsevere.impl.ProbSevereObject;
import edu.wisc.ssec.cimss.common.dataplugin.probsevere.impl.ProbSevereShape;

/**
 * NOAA/CIMSS ProbSevere Model Data Record Definition
 *
 * Data record that stores attributes of NOAA/CIMSS ProbSevere Model shapes
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Mar 27, 2014 DCS 15298   lcronce     Initial Creation.
 * Nov 29, 2018 DCS 20816   lcronce     Updated plugin to address multiple data 
 *                                      file types and behavior. Also updated the 
 *                                      package name and methods to use ProbSevere 
 *                                      instead of ConvectProb to better reflect the 
 *                                      product origin.
 * Jul 23, 2019 DR 21469    lcronce     Added boolean method isRecordComplete() for
 *              DR 21470                use within the visualization resource to make
 *                                      sure we have a complete record to work with 
 *                                      prior to drawing shapes.
 * </pre
 *
 * @author Lee Cronce
 * @version 1.0
 *
 */
@Entity
@SequenceGenerator(initialValue = 1, name = PluginDataObject.ID_GEN, sequenceName = "probsevereseq")
@Table(name = ProbSevereRecord.PLUGIN_NAME, uniqueConstraints = { @UniqueConstraint(columnNames = { "refTime" }) })
/*
 * Both refTime and forecastTime are included in the refTimeIndex since
 * forecastTime is unlikely to be used.
 */
@org.hibernate.annotations.Table(appliesTo = ProbSevereRecord.PLUGIN_NAME, indexes = { @Index(name = "probsevere_refTimeIndex", columnNames = {
        "refTime", "forecastTime" }) })
@DynamicSerialize
public class ProbSevereRecord extends PersistablePluginDataObject implements
IPersistable {

    /** Serializable id * */
    private static final long serialVersionUID = 1L;

    public static final String PLUGIN_NAME = "probsevere";

    // Data store data item names
    @Transient
    private static final String[] DATA_NAMES = { "polygons", "propertiesKeys", "properties", "severeModelKeys", "severeModelProps", 
            "torModelKeys", "torModelProps", "hailModelKeys", "hailModelProps", "windModelKeys", "windModelProps"};

    private final static transient IUFStatusHandler statusHandler = UFStatus.getHandler(ProbSevereRecord.class);

    @Transient
    private String[] polygons = null;

    @Transient
    private String[] propertiesKeys = null;

    @Transient
    private String[] properties = null;

    @Transient
    private String[] severeModelKeys = null;

    @Transient
    private String[] severeModelProps = null;

    @Transient
    private String[] torModelKeys = null;

    @Transient
    private String[] torModelProps = null;

    @Transient
    private String[] hailModelKeys = null;

    @Transient
    private String[] hailModelProps = null;

    @Transient
    private String[] windModelKeys = null;

    @Transient
    private String[] windModelProps = null;

    @Transient
    private Object[] dataArrays = null;

    @Transient
    private int insertIndex = 0;

    // Used to track
    @Transient
    long persistTime = 0L;

    /**
     * Required empty constructor.
     */
    public ProbSevereRecord() {
    }

    /**
     * Constructs a data record from a dataURI
     *
     * @param data URI
     */
    public ProbSevereRecord(String uri) {
        super(uri);
    }

    /**
     * Constructs a ProbSevere record with a
     * given amount of shapes
     *
     * @param size of arrays based on number of shapes
     */
    public ProbSevereRecord(ProbSevereObject psObject) {
        List<ProbSevereShape> psShapes = psObject.getFeatures();
        polygons = new String[psShapes.size()];
        propertiesKeys = new String[psShapes.size()];
        properties = new String[psShapes.size()];
        severeModelKeys = new String[psShapes.size()];
        severeModelProps = new String[psShapes.size()];
        torModelKeys = new String[psShapes.size()];
        torModelProps = new String[psShapes.size()];
        hailModelKeys = new String[psShapes.size()];
        hailModelProps = new String[psShapes.size()];
        windModelKeys = new String[psShapes.size()];
        windModelProps = new String[psShapes.size()];
        dataArrays = new Object[] { polygons, propertiesKeys, properties, severeModelKeys, severeModelProps,
                torModelKeys, torModelProps, hailModelKeys, hailModelProps, windModelKeys, windModelProps};
        insertIndex = 0;
        for (ProbSevereShape shape : psShapes) {
            addShape(shape);
        }
    }

    /**
     * Adds a ProbSevere shape to the record collection.
     *
     * @param shape to add
     */
    public void addShape(ProbSevereShape psShape) {
        try {
            Map<String, String> propertiesMap = psShape.getProperties();
            ProbSevereGeometry geometry = psShape.getGeometry();
            polygons[insertIndex] = createPolygonWKTString(geometry);
            propertiesKeys[insertIndex] = createPropsKeysArray(propertiesMap);
            properties[insertIndex] = createPropsArray(propertiesMap);
            ProbSevereModelType model = psShape.getModels();
            severeModelKeys[insertIndex] = createPropsKeysArray(model.getProbsevere());
            severeModelProps[insertIndex] = createPropsArray(model.getProbsevere());
            torModelKeys[insertIndex] = createPropsKeysArray(model.getProbtor());
            torModelProps[insertIndex] = createPropsArray(model.getProbtor());
            hailModelKeys[insertIndex] = createPropsKeysArray(model.getProbhail());
            hailModelProps[insertIndex] = createPropsArray(model.getProbhail());
            windModelKeys[insertIndex] = createPropsKeysArray(model.getProbwind());
            windModelProps[insertIndex] = createPropsArray(model.getProbwind());
            insertIndex++;
        } catch (Exception e) {
            statusHandler.error("insertIndex is out of bounds: " + Integer.toString(insertIndex), e);
        }
    }

    private String createPropsArray(Map<String, String> propsMap) {
        Collection<String> props = propsMap.values();
        StringBuffer propsString = new StringBuffer();
        int i = 0;
        for (String prop : props) {
            if (i == 0) {
                propsString.append(prop);
            } else {
                propsString.append("|"+prop);
            }
            i++;
        }
        return propsString.toString();
    }

    private String createPropsKeysArray(Map<String, String> propsMap) {
        Set<String> propsKeys = propsMap.keySet();
        StringBuffer propsKeysString = new StringBuffer();
        int i = 0;
        for (String key : propsKeys) {
            if (i == 0) {
                propsKeysString.append(key);
            } else {
                propsKeysString.append("|"+key);
            }
            i++;
        }
        return propsKeysString.toString();
    }

    private String createPolygonWKTString(ProbSevereGeometry geometry) {
        StringBuffer polygon = new StringBuffer();
        float [][][] coordinates = geometry.getCoordinates();
        polygon.append("POLYGON((");
        for (int i = 0; i < coordinates[0].length; i++) {
            String lon = Float.toString(coordinates[0][i][0]);
            String lat = Float.toString(coordinates[0][i][1]);
            polygon.append(lon);
            polygon.append(" ");
            polygon.append(lat);
            if (i < coordinates[0].length-1) {
                polygon.append(",");
                polygon.append(" ");
            }
        }
        polygon.append("))");
        return polygon.toString();
    }

    /**
     * Retrieve names of data contained in data record
     *
     * @return string array of data record field names
     */
    public String[] getDataNames() {
        return DATA_NAMES;
    }

    /**
     * Retrieve data contained within the data record
     *
     * @return array of data objects stored in record
     */
    public Object[] getDataArrays() {
        return dataArrays;
    }

    /**
     * Set data to be contained within the data record
     *
     * @param array of data objects to be set
     */
    public void setDataArrays(Object[] dataArrays) {
        this.dataArrays = dataArrays;
    }

    /**
     * Retrieves the shape polygons
     *
     * @return polygons defining the data record shapes
     */
    public String[] getPolygons() {
        return polygons;
    }

    /**
     * Creating Geometry objects from String representation
     * of shape polygons
     *
     * @return Geometry objects of shape polygons
     */
    public Geometry[] getPolyGeoms() {
        Geometry[] polyGeoms = new Geometry [polygons.length];
        WKTReader reader = new WKTReader();
        int i = 0;
        for (String poly : polygons) {
            try {
                polyGeoms[i] = reader.read(poly);
                i++;
            } catch (Exception e) {
                statusHandler.error("Well Known Text reader could not read selected text: " + poly, e);
            }
        }
        return polyGeoms;
    }

    /**
     * Retrieves the shape properties keys
     *
     * @return map defining the data record shape properties
     */
    public String[] getPropertiesKeys() {
        return propertiesKeys;
    }

    /**
     * Retrieves the shape properties
     *
     * @return properties defining the data record shapes
     */
    public String[] getProperties() {
        return properties;
    }

    /**
     * Retrieves the ProbSevere properties keys
     *
     * @return map defining the data record shape properties
     */
    public String[] getSevereModelKeys() {
        return severeModelKeys;
    }

    /**
     * Retrieves the ProbSevere properties
     *
     * @return properties defining the data record shapes
     */
    public String[] getSevereModelProps() {
        return severeModelProps;
    }

    /**
     * Retrieves the ProbTor properties keys
     *
     * @return map defining the data record shape properties
     */
    public String[] getTorModelKeys() {
        return torModelKeys;
    }

    /**
     * Retrieves the ProbTor properties
     *
     * @return properties defining the data record shapes
     */
    public String[] getTorModelProps() {
        return torModelProps;
    }

    /**
     * Retrieves the ProbHail properties keys
     *
     * @return map defining the data record shape properties
     */
    public String[] getHailModelKeys() {
        return hailModelKeys;
    }

    /**
     * Retrieves the ProbHail properties
     *
     * @return properties defining the data record shapes
     */
    public String[] getHailModelProps() {
        return hailModelProps;
    }

    /**
     * Retrieves the ProbWind properties keys
     *
     * @return map defining the data record shape properties
     */
    public String[] getWindModelKeys() {
        return windModelKeys;
    }

    /**
     * Retrieves the ProbWind properties
     *
     * @return properties defining the data record shapes
     */
    public String[] getWindModelProps() {
        return windModelProps;
    }

    /**
     * Sets the data arrays from the store.
     *
     * @param dataStore
     * @throws Exception
     */
    public void retrieveFromDataStore(IDataStore dataStore) throws Exception {
        IDataRecord[] dataRec = dataStore.retrieve(getDataURI());
        Object[] dataArrays = new Object[dataRec.length];
        for (int i = 0; i < dataRec.length; i++) {
            if (dataRec[i].getName().equals("polygons")) {
                polygons = ((StringDataRecord) dataRec[i]).getStringData();
                dataArrays[i] = polygons;
            } else if (dataRec[i].getName().equals("propertiesKeys")) {
                propertiesKeys = ((StringDataRecord) dataRec[i]).getStringData();
                dataArrays[i] = propertiesKeys;
            } else if (dataRec[i].getName().equals("properties")) {
                properties = ((StringDataRecord) dataRec[i]).getStringData();
                dataArrays[i] = properties;
            } else if (dataRec[i].getName().equals("severeModelKeys")) {
                severeModelKeys = ((StringDataRecord) dataRec[i]).getStringData();
                dataArrays[i] = severeModelKeys;
            } else if (dataRec[i].getName().equals("severeModelProps")) {
                severeModelProps = ((StringDataRecord) dataRec[i]).getStringData();
                dataArrays[i] = severeModelProps;
            } else if (dataRec[i].getName().equals("torModelKeys")) {
                torModelKeys = ((StringDataRecord) dataRec[i]).getStringData();
                dataArrays[i] = torModelKeys;
            } else if (dataRec[i].getName().equals("torModelProps")) {
                torModelProps = ((StringDataRecord) dataRec[i]).getStringData();
                dataArrays[i] = torModelProps;
            } else if (dataRec[i].getName().equals("hailModelKeys")) {
                hailModelKeys = ((StringDataRecord) dataRec[i]).getStringData();
                dataArrays[i] = hailModelKeys;
            } else if (dataRec[i].getName().equals("hailModelProps")) {
                hailModelProps = ((StringDataRecord) dataRec[i]).getStringData();
                dataArrays[i] = hailModelProps;
            } else if (dataRec[i].getName().equals("windModelKeys")) {
                windModelKeys = ((StringDataRecord) dataRec[i]).getStringData();
                dataArrays[i] = windModelKeys;
            } else if (dataRec[i].getName().equals("windModelProps")) {
                windModelProps = ((StringDataRecord) dataRec[i]).getStringData();
                dataArrays[i] = windModelProps;
            }
        }
        setDataArrays(dataArrays);
    }

    /**
     * Determines if a record is complete with data
     *
     * @return boolean defining if the record is complete
     */
    public boolean isRecordComplete() {
        for (Object arr : dataArrays) {
            if (arr == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * @see com.raytheon.uf.common.dataplugin.PluginDataObject#getPluginName()
     */
    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }
}
