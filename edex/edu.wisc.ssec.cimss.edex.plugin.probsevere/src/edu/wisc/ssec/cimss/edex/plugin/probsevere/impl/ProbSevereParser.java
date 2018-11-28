package edu.wisc.ssec.cimss.edex.plugin.probsevere.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import edu.wisc.ssec.cimss.common.dataplugin.probsevere.impl.ProbSevereGeometry;
import edu.wisc.ssec.cimss.common.dataplugin.probsevere.impl.ProbSevereModelType;
import edu.wisc.ssec.cimss.common.dataplugin.probsevere.impl.ProbSevereObject;
import edu.wisc.ssec.cimss.common.dataplugin.probsevere.impl.ProbSevereShape;

/**
 * NOAA/CIMSS ProbSevere Model Data Parser
 *
 * Data parser that parses shapefile records of NOAA/CIMSS ProbSevere Model
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
public class ProbSevereParser {

    private final IUFStatusHandler statusHandler = UFStatus.getHandler(ProbSevereParser.class);

    public ProbSevereObject psObject;

    /**
     * Default empty constructor
     */
    public ProbSevereParser() {
    }

    /**
     * Calls setData method for defining shape data
     *
     * @param File object passed on from EDEX
     */
    public ProbSevereParser(File file) {

        String fileFormat = file.getName().split("\\.")[1];

        if (fileFormat.equals("ascii")) {

            psObject = setASCIIData(file);
            psObject.setFileFormat("ascii");

        } else {

            ObjectMapper mapper = new ObjectMapper();
            psObject = setJSONData(file, mapper);
            psObject.setFileFormat("json");

        }

    }

    /**
     * Calls findShapes method to define shape data
     *
     * @param File object passed on from EDEX
     */
    private ProbSevereObject setJSONData(File file, ObjectMapper mapper) {

        ProbSevereObject object = null;

        try {

            object = mapper.readValue(file, ProbSevereObject.class);

        } catch (IOException e) {

            statusHandler.error("Problem reading ProbSevere JSON data file: "+file.getName(), e);

        }

        return object;

    }

    /**
     * Calls findShapes method to define shape data
     *
     * @param File object passed on from EDEX
     */
    private ProbSevereObject setASCIIData(File file) {

        ProbSevereObject object = null;

        List<String> fileLines = separateLines(file);

        String validTime = findTime(fileLines, file);

        if (validTime != null) {

            object = new ProbSevereObject();
            object.setValidTime(validTime);
            List<ProbSevereShape> features = getShapeFeatures(fileLines);
            object.setFeatures(features);

        }

        return object;

    }

    /**
     * Separates out lines of data from the input ASCII data file
     *
     * @param File passed on by EDEX
     * @return ArrayList of lines of data from data file
     */
    private List<String> separateLines(File file) {

        List<String> fileLines = null;

        try {

            if (file != null) {

                BufferedReader reader = new BufferedReader(new FileReader(file));
                fileLines = new ArrayList<String>();
                String line;

                while ((line = reader.readLine()) != null) {

                    fileLines.add(line);

                }

                reader.close();

            }

        } catch (Exception e) {

            statusHandler.error("Problem with reading lines of the ProbSevere ASCII input file: " + file.getName(), e);

        }

        return fileLines;

    }

    /**
     * Parses out the data from the passed file object
     *
     * @param ArrayList of String type containing individual lines of a ProbSevere data file
     * @return String object containing ProbSevere data valid time
     */
    private String findTime(List<String> fileLines, File file) {

        String validTime = null;

        if (fileLines != null) {

            try {

                String vTime = fileLines.get(0);

                if (!vTime.substring(0,5).equals("Valid")) {

                    if (file.getName().substring(0,4).equals("SSEC")) {

                        validTime = file.getName().substring(23,38)+" UTC";

                    }

                } else {

                    validTime = vTime.split(" ")[1]+" UTC";

                }

            } catch (Exception e) {

                statusHandler.error("Problem acquiring ProbSevere data valid date and time", e);

            }

        }

        return validTime;
    }

    /**
     * Parses out the data from the passed file object
     *
     * @param File object passed on from EDEX
     * @return ArrayList containing the shape data
     */
    private List<ProbSevereShape> getShapeFeatures(List<String> fileLines) {

        List<ProbSevereShape> features = new ArrayList<ProbSevereShape>();

        if (fileLines != null) {

            for (String line : fileLines) {

                if (!line.substring(0,5).equals("Valid")) {

                    try {

                        ProbSevereShape feature = new ProbSevereShape();
                        Map<String, String> properties = new LinkedHashMap<String, String>();
                        ProbSevereGeometry geometry = new ProbSevereGeometry();
                        ProbSevereModelType modelType = new ProbSevereModelType();
                        Map<String, String> severeModelProps = new LinkedHashMap<String, String>();
                        Map<String, String> torModelProps = new LinkedHashMap<String, String>();
                        Map<String, String> hailModelProps = new LinkedHashMap<String, String>();
                        Map<String, String> windModelProps = new LinkedHashMap<String, String>();

                        String[] shapeAttributes = line.split(":");

                        String type = shapeAttributes[0];
                        properties.put("TYPE", type);

                        String probability = shapeAttributes[1];
                        severeModelProps.put("PROB", probability);
                        severeModelProps.put("LINE01", "ProbSevere: "+probability+"%");

                        String mucape = shapeAttributes[2];
                        severeModelProps.put("LINE02", "- MUCAPE: "+mucape);

                        String ebshear = shapeAttributes[3];
                        severeModelProps.put("LINE03", "- EBShear: "+ebshear);

                        String mesh = shapeAttributes[4];
                        severeModelProps.put("LINE04", "- MESH: "+mesh);

                        String rcemiss = shapeAttributes[5];
                        severeModelProps.put("LINE05", "- MaxRC Emiss: "+rcemiss);

                        String rcicecf = shapeAttributes[6];
                        severeModelProps.put("LINE06", "- MaxRC IceCF: "+rcicecf);

                        String[] points = shapeAttributes[7].split(",");
                        float[][][] coordinates = new float[1][points.length/2][2];
                        for (int i=0; i < points.length; i++) {
                            if ((i == 0) || ((i%2) == 0)) {
                                coordinates[0][i/2][1] = Float.parseFloat(points[i]);
                            } else {
                                coordinates[0][(i-1)/2][0] = Float.parseFloat(points[i]);
                            }
                        }
                        geometry.setCoordinates(coordinates);

                        String objectprops = shapeAttributes[8];
                        String[] props = objectprops.split(";");
                        properties.put("ID", props[0]);
                        if (props.length > 1) {
                            severeModelProps.put("LINE07", "-"+props[1]);
                        }

                        String message = "currently not available.\nPlease choose ProbSevere or check with your\nITO for ProbSevere (All Hazards) data feed capability.";
                        torModelProps.put("PROB", "0");
                        torModelProps.put("LINE01", "¯\\_(ツ)_/¯");
                        torModelProps.put("LINE02", "ProbTor "+message);

                        hailModelProps.put("PROB", "0");
                        hailModelProps.put("LINE01", "¯\\_(ツ)_/¯");
                        hailModelProps.put("LINE02", "ProbHail "+message);

                        windModelProps.put("PROB", "0");
                        windModelProps.put("LINE01", "¯\\_(ツ)_/¯");
                        windModelProps.put("LINE02", "ProbWind "+message);

                        modelType.setProbsevere(severeModelProps);
                        modelType.setProbtor(torModelProps);
                        modelType.setProbhail(hailModelProps);
                        modelType.setProbwind(windModelProps);

                        feature.setProperties(properties);
                        feature.setGeometry(geometry);
                        feature.setModels(modelType);

                        features.add(feature);

                    } catch (Exception e) {

                        statusHandler.error("Problem defining ProbSevere shape object from read line: " + line, e);

                    }

                }

            }

        }

        return features;
    }

}
