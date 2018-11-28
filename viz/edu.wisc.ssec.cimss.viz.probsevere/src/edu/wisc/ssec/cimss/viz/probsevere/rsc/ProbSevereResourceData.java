package edu.wisc.ssec.cimss.viz.probsevere.rsc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.AbstractRequestableResourceData;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.LoadProperties;

import edu.wisc.ssec.cimss.common.dataplugin.probsevere.ProbSevereRecord;

/**
 * NOAA/CIMSS ProbSevere Model Visualization Resource Data
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
 *
 * </pre
 *
 * @author Lee Cronce
 * @version 1.0
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
public class ProbSevereResourceData extends AbstractRequestableResourceData {

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ProbSevereResourceData.class);

    // Sets the default probability threshold for thickening lines of shapes
    @XmlAttribute
    private int probThresh = 50;

    // Sets the default probability threshold for showing an outer contour for the ProbTor model
    @XmlAttribute
    private int torShapeThresh = 3;

    // Sets what model the base (inside) shape will rely upon 
    @XmlAttribute
    private String baseShape = "severe";

    // Sets the default model type to display
    @XmlAttribute
    private String modelType = "probsevere";

    // Sets the display of the object ID to false by default
    @XmlAttribute
    private boolean showObjectID = false;

    /**
     * @see com.raytheon.uf.viz.core.rsc.AbstractRequestableResourceData#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }

        if (obj instanceof ProbSevereResourceData == true) {
            return false;
        }

        return true;
    }

    /**
     * @see com.raytheon.uf.viz.core.rsc.AbstractRequestableResourceData#constructResource(com.raytheon.uf.viz.core.rsc.LoadProperties, com.raytheon.uf.common.dataplugin.PluginDataObject[])
     */
    @Override
    protected AbstractVizResource<?, ?> constructResource(
            LoadProperties loadProperties, PluginDataObject[] objects)
                    throws VizException {
        ProbSevereResource rsc = new ProbSevereResource(this, loadProperties);
        for (PluginDataObject o : objects) {
            if (o instanceof ProbSevereRecord) {
                ProbSevereRecord rec = (ProbSevereRecord) o;
                rsc.addRecord(rec);
            } else {
                statusHandler.handle(Priority.PROBLEM,
                        "Received wrong type of data.  Got: " + o.getClass()
                        + " Expected: " + ProbSevereRecord.class);
            }
        }
        return rsc;
    }

    /**
     * Threshold for thickening shape outline
     *
     * @return probability threshold
     */
    public int getProbThresh() {
        return probThresh;
    }

    /**
     * Set threshold for thickening shape outline
     *
     * @param probability threshold
     */
    public void setProbThresh(int probThresh) {
        this.probThresh = probThresh;
    }

    /**
     * Threshold for displaying secondary shape for ProbTor model
     *
     * @return probability threshold
     */
    public int getTorShapeThresh() {
        return torShapeThresh;
    }

    /**
     * Set threshold for displaying secondary shape for ProbTor model
     *
     * @param probability threshold
     */
    public void setTorShapeThresh(int torShapeThresh) {
        this.torShapeThresh = torShapeThresh;
    }

    /**
     * Determines what model the base shape should rely upon
     *
     * @return model used for base shape
     */
    public String getBaseShape() {
        return baseShape;
    }

    /**
     * Set what model the base shape should rely upon
     *
     * @param model used for base shape
     */
    public void setBaseShape(String baseShape) {
        this.baseShape = baseShape;
    }

    /**
     * Determines what model type should be displayed
     *
     * @return model type to display
     */
    public String getModelType() {
        return modelType;
    }

    /**
     * Set what model type should be displayed
     *
     * @param model type to display
     */
    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    /**
     * Determine whether or not to display object ID in sampling
     *
     * @return boolean
     */
    public boolean isShowObjectID() {
        return showObjectID;
    }

    /**
     * Set whether or not to display object ID in sampling
     *
     * @param boolean
     */
    public void setShowObjectID(boolean showObjectID) {
        this.showObjectID = showObjectID;
    }

}