package edu.wisc.ssec.cimss.common.dataplugin.probsevere.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * NOAA/CIMSS ProbSevere Model Shape Geometry
 *
 * Data object that defines attributes of a NOAA/CIMSS ProbSevere Model shape geometry
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Nov 29, 2018 DCS 20816   lcronce     Initial creation.
 *
 * </pre
 *
 * @author Lee Cronce
 * @version 1.0
 *
 */

@JsonIgnoreProperties(ignoreUnknown=true)
public class ProbSevereGeometry {

    private float[][][] coordinates;

    /**
     * Default empty constructor
     */
    public ProbSevereGeometry(){
    }

    /**
     * Retrieve coordinates of shape geometry
     *
     * @return shape geometry coordinates
     */
    public float[][][] getCoordinates() {
        return coordinates;
    }

    /**
     * Set coordinates of shape geometry
     *
     * @param shape geometry coordinates
     */
    public void setCoordinates(float[][][] coordinates) {
        this.coordinates = coordinates;
    }

}
