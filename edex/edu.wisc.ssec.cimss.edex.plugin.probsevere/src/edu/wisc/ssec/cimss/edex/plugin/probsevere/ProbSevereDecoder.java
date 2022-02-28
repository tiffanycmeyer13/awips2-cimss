package edu.wisc.ssec.cimss.edex.plugin.probsevere;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.common.time.util.TimeUtil;

import edu.wisc.ssec.cimss.common.dataplugin.probsevere.ProbSevereRecord;
import edu.wisc.ssec.cimss.common.dataplugin.probsevere.impl.ProbSevereObject;
import edu.wisc.ssec.cimss.edex.plugin.probsevere.impl.ProbSevereParser;

/**
 * NOAA/CIMSS ProbSevere Model Data Decoder
 *
 * Data decoder that reads shapefile records of the NOAA/CIMSS ProbSevere Model
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 27, 2014 DCS 15298  lcronce     Initial creation.
 * Nov 29, 2018 DCS 20816  lcronce     Updated plugin to address multiple
 *                                     data file types and behavior. Also
 *                                     updated the package name and methods to
 *                                     use ProbSevere instead of ConvectProb
 *                                     to better reflect the product origin.
 * Jan 27, 2021 DCS 22416  lcronce     Fixes plugin to work with data files
 *                                     with or without WMO headers.
 * Feb 16, 2022 8608       mapeters    Handle PDO.traceId changes
 *
 * </pre>
 *
 * @author Lee Cronce
 */

public class ProbSevereDecoder {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ProbSevereDecoder.class);

    private String traceId = null;

    /**
     * Default empty constructor
     */
    public ProbSevereDecoder() {
    }

    /**
     * Creates the data object that will be persisted to the database and hdf5
     * repository
     *
     * @param File
     *            object passed by EDEX
     * @return PluginDataObject[] object of shape data
     * @throws Throwable
     */
    public PluginDataObject[] decode(File file) throws Throwable {

        ProbSevereParser psParser = new ProbSevereParser(file);
        ProbSevereObject psObject = psParser.psObject;
        ProbSevereRecord psRecord = null;

        if (psObject != null && !psObject.getFeatures().isEmpty()) {
            psRecord = new ProbSevereRecord(psObject);
        } else {
            return new PluginDataObject[0];
        }

        String validTime = psObject.getValidTime();

        if (validTime != null) {
            Calendar c = TimeUtil.newCalendar();
            psRecord.setInsertTime(c);
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(
                        "yyyyMMdd_HHmmss 'UTC'");
                psRecord.setDataTime(new DataTime(dateFormat.parse(validTime)));
            } catch (Exception e) {
                statusHandler.error(
                        "Problem defining valid ProbSevere file time information using: "
                                + validTime,
                        e);
                return new PluginDataObject[0];
            }
        } else {
            return new PluginDataObject[0];
        }

        if (psRecord != null) {
            psRecord.setSourceTraceId(traceId);
        }

        return new PluginDataObject[] { psRecord };

    }

    /**
     * Set a trace identifier for the source data.
     *
     * @param traceId
     *            A unique identifier associated with the input data.
     */
    public void setTraceId(String traceId) {

        this.traceId = traceId;

    }

}
