package edu.wisc.ssec.cimss.viz.probsevere;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.viz.ui.BundleProductLoader;
import com.raytheon.viz.ui.EditorUtil;

import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.procedures.Bundle;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.ResourceType;
import com.raytheon.uf.viz.productbrowser.AbstractProductBrowserDataDefinition;
import com.raytheon.uf.viz.productbrowser.ProductBrowserLabel;
import com.raytheon.uf.viz.productbrowser.ProductBrowserPreference;

import edu.wisc.ssec.cimss.viz.probsevere.rsc.ProbSevereResourceData;

/**
 * NOAA/CIMSS ProbSevere Model Product Browser Data Definition
 *
 * Defines product browser access to NOAA/CIMSS ProbSevere Model data
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Mar 27, 2014 DCS 15298   jgerth      Initial Creation.
 * Nov 29, 2018 DCS 20816   lcronce     Updated plugin to address multiple data 
 *                                      file types and behavior. Also updated the 
 *                                      package name and methods to use ProbSevere 
 *                                      instead of ConvectProb to better reflect the 
 *                                      product origin.
 *
 * </pre
 *
 * @author Jordan Gerth
 * @version 1.0
 *
 */
public class ProbSevereProductBrowserDataDefinition extends
AbstractProductBrowserDataDefinition<ProbSevereResourceData> {

    IUFStatusHandler statusHandler = UFStatus.getHandler(ProbSevereProductBrowserDataDefinition.class);

    /**
     * Constructor defining new instance of this class
     */
    public ProbSevereProductBrowserDataDefinition() {
        displayName = "NOAA/CIMSS ProbSevere";
        loadProperties = new LoadProperties();
    }

    /**
     * @see com.raytheon.uf.viz.productbrowser.AbstractProductBrowserDataDefinition#getResourceData()
     */
    @Override
    public ProbSevereResourceData getResourceData() {
        return new ProbSevereResourceData();
    }

    /**
     * @see com.raytheon.uf.viz.productbrowser.AbstractProductBrowserDataDefinition#populateData(java.lang.String[])
     */
    @Override
    public List<ProductBrowserLabel> populateData(String[] selection) {
        List<ProductBrowserLabel> labels = new ArrayList<ProductBrowserLabel>();
        ProductBrowserLabel label = new ProductBrowserLabel("NOAA/CIMSS ProbSevere Model", "Probability of Severe");
        label.setProduct(true);
        labels.add(label);
        return labels;
    }

    /**
     * @see com.raytheon.uf.viz.productbrowser.AbstractProductBrowserDataDefinition#buildProductList(java.util.List)
     */
    @Override
    public List<String> buildProductList(List<String> historyList) {
        return historyList;
    }

    /**
     * @see com.raytheon.uf.viz.productbrowser.AbstractProductBrowserDataDefinition#constructResource(java.lang.String[], com.raytheon.uf.viz.core.rsc.ResourceType)
     */
    @Override
    public void constructResource(String[] selection, ResourceType type) {
        try {
            Bundle b = Bundle.unmarshalBundle(PathManagerFactory.getPathManager().getStaticFile("bundles/ProbSevere.xml"));
            new BundleProductLoader(EditorUtil.getActiveVizContainer(),b).run();
        } catch (VizException e1) {
            statusHandler.handle(Priority.PROBLEM, "Could not create resource for NOAA/CIMSS ProbSevere Model data", e1);
        }
    }

    /**
     * @see com.raytheon.uf.viz.productbrowser.AbstractProductBrowserDataDefinition#configurePreferences()
     */
    @Override
    public List<ProductBrowserPreference> configurePreferences() {
        return super.configurePreferences();
    }

}
