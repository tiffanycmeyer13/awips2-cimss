package edu.wisc.ssec.cimss.common.dataplugin.probsevere.impl;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * NOAA/CIMSS ProbSevere Model Shape Definition
 *
 * Data object that defines attributes of a NOAA/CIMSS ProbSevere Model shape
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Nov 29, 2018 DCS 20816   lcronce     Initial Creation.
 *
 * </pre
 *
 * @author Lee Cronce
 * @version 1.0
 *
 */

@JsonIgnoreProperties(ignoreUnknown=true)
public class ProbSevereShape {

    private ProbSevereGeometry geometry;

    private Map<String, String> properties;

    private ProbSevereModelType modelTypes;

    /**
     * Default empty constructor
     */
    public ProbSevereShape(){
    }

    /**
     * Retrieve geometry of shape
     *
     * @return shape geometry
     */
    public ProbSevereGeometry getGeometry() {
        return geometry;
    }

    /**
     * Set geometry of shape
     *
     * @param shape geometry
     */
    public void setGeometry(ProbSevereGeometry geometry) {
        this.geometry = geometry;
    }

    /**
     * Retrieve properties of shape
     *
     * @return shape properties
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Set properties of shape
     *
     * @param shape properties
     */
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    /**
     * Retrieve properties of shape
     *
     * @return shape properties
     */
    public ProbSevereModelType getModels() {
        return modelTypes;
    }

    /**
     * Set properties of shape
     *
     * @param shape properties
     */
    public void setModels(ProbSevereModelType modelTypes) {
        this.modelTypes = modelTypes;
    }

}
