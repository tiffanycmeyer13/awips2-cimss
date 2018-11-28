package edu.wisc.ssec.cimss.common.dataplugin.probsevere.impl;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * NOAA/CIMSS ProbSevere Model Object Definition
 *
 * Data object that defines attributes of a NOAA/CIMSS ProbSevere Model object
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
public class ProbSevereObject {

    private String fileFormat;

    private String validTime;

    private List<ProbSevereShape> features;

    /**
     * Default empty constructor
     */
    public ProbSevereObject() {
    }

    /**
     * Retrieve file format of data file
     *
     * @return file format
     */
    public String getFileFormat() {
        return fileFormat;
    }

    /**
     * Set file format of data file
     *
     * @param file format
     */
    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    /**
     * Retrieve valid time of object
     *
     * @return object valid time
     */
    public String getValidTime() {
        return validTime;
    }

    /**
     * Set valid time of object
     *
     * @param object valid time
     */
    public void setValidTime(String validTime) {
        this.validTime = validTime;
    }

    /**
     * Retrieve shapes of object
     *
     * @return object shapes
     */
    public List<ProbSevereShape> getFeatures() {
        return features;
    }

    /**
     * Set shapes of object
     *
     * @param object shapes
     */
    public void setFeatures(List<ProbSevereShape> features) {
        this.features = features;
    }

}
