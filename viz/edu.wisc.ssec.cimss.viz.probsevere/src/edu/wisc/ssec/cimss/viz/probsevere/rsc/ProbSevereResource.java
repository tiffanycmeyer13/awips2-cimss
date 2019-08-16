package edu.wisc.ssec.cimss.viz.probsevere.rsc;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.RGB;
import com.raytheon.uf.common.colormap.Color;
import com.raytheon.uf.common.colormap.ColorMapException;
import com.raytheon.uf.common.colormap.prefs.ColorMapParameters;
import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.datastorage.DataStoreFactory;
import com.raytheon.uf.common.datastorage.IDataStore;
import com.raytheon.uf.common.geospatial.ReferencedCoordinate;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.IGraphicsTarget.LineStyle;
import com.raytheon.uf.common.colormap.ColorMapLoader;
import com.raytheon.uf.viz.core.drawables.IWireframeShape;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.MapDescriptor;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.IResourceDataChanged;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.capabilities.ColorMapCapability;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.dataplugin.HDF5Util;

import edu.wisc.ssec.cimss.common.dataplugin.probsevere.ProbSevereRecord;

/**
 * NOAA/CIMSS ProbSevere Model Visualization Resource
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Mar 27, 2014 DCS 15298   lcronce     Initial Creation.
 * Jun 09, 2016 DR  18946   lcronce     Update to plugin addressing
 *                                      paint error associated with
 *                                      null pointer exception.
 * Dec 01, 2017 5863        mapeters    Change dataTimes to a NavigableSet
 * Nov 29, 2018 DCS 20816   lcronce     Updated plugin to address multiple data 
 *                                      file types and behavior. Also updated the 
 *                                      package name and methods to use ProbSevere 
 *                                      instead of ConvectProb to better reflect the 
 *                                      product origin.  Also added logic to address
 *                                      new timeAgnostic behavior of
 *                                      AbstractVizResource class.
 * Jul 23, 2019 DR 21469    lcronce     Updated plugin to remove unnecessary use of
 *              DR 21470                Collections and Lists of ProbSevereRecord type
 *                                      within HashMaps of unprocessedRecords and 
 *                                      frames.  Only one ProbSevereRecord exists per 
 *                                      DataTime instance.
 * </pre
 *
 * @author Lee Cronce
 * @version 1.0
 *
 */
public class ProbSevereResource extends
AbstractVizResource<ProbSevereResourceData, MapDescriptor> {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ProbSevereResource.class);

    // Place to store records that have not yet been processed
    private Map<DataTime, ProbSevereRecord> unprocessedRecords = new HashMap<DataTime, ProbSevereRecord>();

    private Map<DataTime, ProbSevereRecord> frames = new HashMap<DataTime, ProbSevereRecord>();

    private DataTime displayedDataTime;

    private static boolean isTimeAgnostic = false;

    /**
     * Constructor to define an instance of this resource
     *
     * @param ProbSevere plugin resource data
     * @param Display load properties
     */
    protected ProbSevereResource(ProbSevereResourceData resourceData,
            LoadProperties loadProperties) {
        super(resourceData, loadProperties, isTimeAgnostic);
        resourceData.addChangeListener(new IResourceDataChanged() {
            @Override
            public void resourceChanged(ChangeType type, Object object) {
                if (type == ChangeType.DATA_UPDATE) {
                    PluginDataObject[] pdo = (PluginDataObject[]) object;
                    for (PluginDataObject p : pdo) {
                        if (p instanceof ProbSevereRecord) {
                            addRecord((ProbSevereRecord) p);
                        }
                    }
                }
                issueRefresh();
            }
        });

    }

    /**
     * Method that declares if the resource is time agnostic. A time agnostic
     * resource does not pertain to time (e.g. static map backgrounds)
     *
     * @return true if resource is time agnostic
     */
    @Override
    public boolean isTimeAgnostic() {
        return isTimeAgnostic;
    }

    /**
     * @see com.raytheon.uf.viz.core.rsc.AbstractVizResource#disposeInternal()
     */
    @Override
    protected void disposeInternal() {
        clearDisplayFrames();
    }

    protected void clearDisplayFrames() {
        synchronized (frames) {
            if (!frames.isEmpty()){
                frames.clear();
            }
        }
    }

    /**
     * @see com.raytheon.uf.viz.core.rsc.AbstractVizResource#initInternal(com.raytheon.uf.viz.core.IGraphicsTarget)
     */
    @Override
    protected void initInternal(IGraphicsTarget target) throws VizException {
        // Create colormap parameters
        ColorMapParameters colorMapParams = getCapability(
                ColorMapCapability.class).getColorMapParameters();
        if (colorMapParams == null) {
            colorMapParams = new ColorMapParameters();
        }
        String name = "Grid/gridded data";
        if (colorMapParams.getColorMap() == null) {
            if (colorMapParams.getColorMapName() != null)
                name = colorMapParams.getColorMapName();
            try {
                colorMapParams.setColorMap(ColorMapLoader.loadColorMap(name));
            } catch (ColorMapException e) {
                statusHandler.handle(Priority.ERROR, "Error loading specified colormap: " + name, e);
            }
        }

        colorMapParams.setColorMapMax(100.0f);
        colorMapParams.setColorMapMin(0.0f);
        float[] cbi = {0.0f, 10.0f, 20.0f, 30.0f, 40.0f, 50.0f, 60.0f, 70.0f, 80.0f, 90.0f, 100.0f};
        colorMapParams.setColorBarIntervals(cbi);

    }

    /**
     * @see com.raytheon.uf.viz.core.rsc.AbstractVizResource#inspect(com.raytheon.uf.common.geospatial.ReferencedCoordinate)
     */
    @Override
    public String inspect(ReferencedCoordinate coord) throws VizException {
        ProbSevereRecord frameRec = null;
        synchronized (frames) {
            frameRec = frames.get(this.displayedDataTime);
        }
        if (frameRec == null) {
            return "";
        }
        StringBuilder sample = new StringBuilder();
        Coordinate latLon = new Coordinate();
        try {
            latLon = coord.asLatLon();
        } catch (Exception e1) {
            statusHandler.handle(Priority.ERROR, "Error converting ReferencedCoordinate to Lat/Lon", e1);
        }
        GeometryFactory geom = new GeometryFactory();
        Point point = geom.createPoint(latLon);
        // Check if we have an area we are rendering
        String[] propsArr = frameRec.getProperties();
        String[] propsArrKeys = frameRec.getPropertiesKeys();
        String[] modelArr = null;
        String[] modelArrKeys = null;
        if (resourceData.getModelType().equals("probsevere")) {
            modelArr = frameRec.getSevereModelProps();
            modelArrKeys = frameRec.getSevereModelKeys();
        } else if (resourceData.getModelType().equals("probtor")) {
            modelArr = frameRec.getTorModelProps();
            modelArrKeys = frameRec.getTorModelKeys();
        } else if (resourceData.getModelType().equals("probhail")) {
            modelArr = frameRec.getHailModelProps();
            modelArrKeys = frameRec.getHailModelKeys();
        } else if (resourceData.getModelType().equals("probwind")) {
            modelArr = frameRec.getWindModelProps();
            modelArrKeys = frameRec.getWindModelKeys();
        }
        try {
            Geometry[] pg = frameRec.getPolyGeoms();
            for (int i=0; i < pg.length; i++) {
                String[] props = propsArr[i].split("\\|");
                String[] propsKeys = propsArrKeys[i].split("\\|");
                String[] modelProps = modelArr[i].split("\\|");
                String[] modelPropsKeys = modelArrKeys[i].split("\\|");
                if (pg[i].contains(point)) {
                    for (int j=0;j < modelPropsKeys.length; j++) {
                        if (modelPropsKeys[j].substring(0,4).equalsIgnoreCase("line")) {
                            if (sample.length() == 0) {
                                sample.append(modelProps[j]);
                            } else {
                                sample.append("\n"+modelProps[j]);
                            }
                        }
                    }
                    if (resourceData.isShowObjectID()) {
                        for (int n=0; n < propsKeys.length; n++) {
                            if (propsKeys[n].equalsIgnoreCase("id")) {
                                sample.append("\nObjectID: "+props[n]);
                                break;
                            }
                        }
                    }
                    return sample.toString();
                }
            }
        } catch (Exception e) {
            statusHandler.handle(Priority.ERROR, "Error interogating ProbSevere data", e);
        }
        return "";
    }

    /**
     * Process all records for the displayedDataTime
     *
     * @param target
     * @param paintProps
     * @throws VizException
     */
    private void updateFrames(IGraphicsTarget target,
            PaintProperties paintProps) throws VizException {
        // Add any new records
        ProbSevereRecord newRecord = null;
        synchronized (unprocessedRecords) {
            newRecord = unprocessedRecords.remove(this.displayedDataTime);
        }
        if (newRecord != null) {
            // If record is incomplete, data is missing and it shouldn't be kept.
            if (newRecord.isRecordComplete()) {
                synchronized (frames) {
                    frames.put(this.displayedDataTime, newRecord);
                }
            }
        }
    }

    /**
     * @see com.raytheon.uf.viz.core.rsc.AbstractVizResource#paintInternal(com.raytheon.uf.viz.core.IGraphicsTarget, com.raytheon.uf.viz.core.drawables.PaintProperties)
     */
    @Override
    protected void paintInternal(IGraphicsTarget target,
            PaintProperties paintProps) throws VizException {

        this.displayedDataTime = paintProps.getDataTime();

        // First check to see if we need to process new data
        ProbSevereRecord unprocessed = null;
        synchronized (unprocessedRecords) {
            unprocessed = unprocessedRecords.get(this.displayedDataTime);
        }
        if (unprocessed != null) {
            updateFrames(target, paintProps);
        }

        // Hopefully we now have some data to display, if not bail
        ProbSevereRecord frameRec = null;
        synchronized (frames) {
            frameRec = frames.get(this.displayedDataTime);
        }
        if (frameRec == null) {
            this.displayedDataTime = null;
            return;
        } else {
            Geometry[] polyGeoms = frameRec.getPolyGeoms();
            String[] modelArr = null;
            String[] modelArrKeys = null;
            int[] probabilities = new int[polyGeoms.length];
            String[] torArr = null;
            String[] torArrKeys = null;
            int[] torProbs = new int[polyGeoms.length];
            if (resourceData.getModelType().equalsIgnoreCase("probsevere")) {
                modelArr = frameRec.getSevereModelProps();
                modelArrKeys = frameRec.getSevereModelKeys();
                torArr = frameRec.getTorModelProps();
                torArrKeys = frameRec.getTorModelKeys();
            } else if (resourceData.getModelType().equalsIgnoreCase("probtor")) {
                modelArr = frameRec.getTorModelProps();
                modelArrKeys = frameRec.getTorModelKeys();
            } else if (resourceData.getModelType().equalsIgnoreCase("probhail")) {
                modelArr = frameRec.getHailModelProps();
                modelArrKeys = frameRec.getHailModelKeys();
            } else if (resourceData.getModelType().equalsIgnoreCase("probwind")) {
                modelArr = frameRec.getWindModelProps();
                modelArrKeys = frameRec.getWindModelKeys();
            }
            for (int n=0; n < polyGeoms.length; n++) {
                String[] modelProps = modelArr[n].split("\\|");
                String[] modelPropsKeys = modelArrKeys[n].split("\\|");
                for (int p=0; p < modelPropsKeys.length; p++) {
                    if (modelPropsKeys[p].equalsIgnoreCase("prob")) {
                        probabilities[n] = Integer.parseInt(modelProps[p]);
                        break;
                    }
                }
                if (resourceData.getModelType().equalsIgnoreCase("probsevere")) {
                    String[] torProps = torArr[n].split("\\|");
                    String[] torPropsKeys = torArrKeys[n].split("\\|");
                    for (int p=0; p < torPropsKeys.length; p++) {
                        if (torPropsKeys[p].equalsIgnoreCase("prob")) {
                            torProbs[n] = Integer.parseInt(torProps[p]);
                            break;
                        }
                    }
                }
            }
            float thickline = 7.0f;
            float thinline = 4.0f;
            float bufferval = 0.05f;
            if (resourceData.getModelType().equalsIgnoreCase("probsevere")) {
                if (resourceData.getBaseShape().equalsIgnoreCase("tor")) {
                    for (int j=resourceData.getTorShapeThresh(); j < 101; j++) {
                        IWireframeShape iwfsTor = target.createWireframeShape(false, descriptor);
                        boolean drawShape = false;
                        for (int i=0; i < polyGeoms.length; i++) {
                            if (torProbs[i] == j) {
                                iwfsTor.addLineSegment(polyGeoms[i].getCoordinates());
                                drawShape = true;
                            }
                        }
                        if (drawShape) {
                            float lineval = thickline;
                            renderShapes(target, paintProps, iwfsTor, j, lineval);
                        }
                    }
                    for (int j=0; j < 101; j++) {
                        IWireframeShape iwfsSevere = target.createWireframeShape(false, descriptor);
                        boolean drawShape = false;
                        for (int i=0; i < polyGeoms.length; i++) {
                            if (probabilities[i] == j) {
                                iwfsSevere.addLineSegment(polyGeoms[i].buffer(bufferval,4).getCoordinates());
                                drawShape = true;
                            }
                        }
                        if (drawShape) {
                            float lineval = thickline;
                            if (j < resourceData.getProbThresh()) {
                                lineval = thinline;
                            }
                            renderShapes(target, paintProps, iwfsSevere, j, lineval);
                        }
                    }
                } else {
                    for (int j=0; j < 101; j++) {
                        IWireframeShape iwfsSevere = target.createWireframeShape(false, descriptor);
                        boolean drawShape = false;
                        for (int i=0; i < polyGeoms.length; i++) {
                            if (probabilities[i] == j) {
                                iwfsSevere.addLineSegment(polyGeoms[i].getCoordinates());
                                drawShape = true;
                            }
                        }
                        if (drawShape) {
                            float lineval = thickline;
                            if (j < resourceData.getProbThresh()) {
                                lineval = thinline;
                            }
                            renderShapes(target, paintProps, iwfsSevere, j, lineval);
                        }
                    }
                    for (int j=resourceData.getTorShapeThresh(); j < 101; j++) {
                        IWireframeShape iwfsTor = target.createWireframeShape(false, descriptor);
                        boolean drawShape = false;
                        for (int i=0; i < polyGeoms.length; i++) {
                            if (torProbs[i] == j) {
                                iwfsTor.addLineSegment(polyGeoms[i].buffer(bufferval,4).getCoordinates());
                                drawShape = true;
                            }
                        }
                        if (drawShape) {
                            float lineval = thickline;
                            renderShapes(target, paintProps, iwfsTor, j, lineval);
                        }
                    }
                }
            } else {
                for (int j=0; j < 101; j++) {
                    IWireframeShape iwfs = target.createWireframeShape(false, descriptor);
                    boolean drawShape = false;
                    for (int i=0; i < polyGeoms.length; i++) {
                        if (probabilities[i] == j) {
                            iwfs.addLineSegment(polyGeoms[i].getCoordinates());
                            drawShape = true;
                        }
                    }
                    if (drawShape) {
                        float lineval = thickline;
                        if (j < resourceData.getProbThresh()) {
                            lineval = thinline;
                        }
                        renderShapes(target, paintProps, iwfs, j, lineval);
                    }
                }
            }
        }
    }

    public void renderShapes(IGraphicsTarget target, PaintProperties paintProps, IWireframeShape iwfs, int prob, float thick) {

        Color color;
        RGB shapeColor = new RGB(255, 255, 255);
        float alpha;

        // Create colormap parameters
        ColorMapParameters colorMapParams = getCapability(
                ColorMapCapability.class).getColorMapParameters();

        color = colorMapParams.getColorByValue((float) prob);
        shapeColor.red = (int) (color.getRed() * 255);
        shapeColor.green = (int) (color.getGreen() * 255);
        shapeColor.blue = (int) (color.getBlue() * 255);
        alpha = color.getAlpha() * paintProps.getAlpha();

        try {
            target.drawWireframeShape(iwfs, shapeColor, thick, LineStyle.SOLID, alpha);
        } catch (VizException e) {
            statusHandler.handle(Priority.ERROR, "Error drawing given wireframe shape", e);
        }
        iwfs.dispose();

    }


    /**
     * Adds a new record to this resource
     *
     * @param new ProbSevere record
     */
    protected void addRecord(ProbSevereRecord newRec) {
        if (newRec != null) {
            DataTime dataTime = newRec.getDataTime();
            if (dataTime != null) {
                File f = HDF5Util.findHDF5Location(newRec);
                IDataStore ds = DataStoreFactory.getDataStore(f);
                try {
                    newRec.retrieveFromDataStore(ds);
                    synchronized (unprocessedRecords) {
                        unprocessedRecords.put(dataTime, newRec);
                    }
                } catch (Exception e) {
                    statusHandler.handle(Priority.ERROR, "Error retrieving new data from ProbSevere data store.", e);
                }
            }
        }
    }

    /**
     * @see com.raytheon.uf.viz.core.rsc.AbstractVizResource#getName()
     */
    @Override
    public String getName() {
        String name = "NOAA/CIMSS Prob";
        if (resourceData.getModelType().equalsIgnoreCase("probtor")) {
            name += "Tor";
        } else if (resourceData.getModelType().equalsIgnoreCase("probhail")) {
            name += "Hail";
        } else if (resourceData.getModelType().equalsIgnoreCase("probwind")) {
            name += "Wind";
        } else {
            name += "Severe";
        }
        name += " Model (%)";

        return name;
    }

    /**
     * @see com.raytheon.uf.viz.core.rsc.AbstractVizResource#remove(com.raytheon.uf.common.time.DataTime)
     */
    @Override
    public void remove(DataTime time) {
        super.remove(time);
        synchronized (frames) {
            frames.remove(time);
        }
        synchronized (unprocessedRecords) {
            unprocessedRecords.remove(time);
        }
    }

}
