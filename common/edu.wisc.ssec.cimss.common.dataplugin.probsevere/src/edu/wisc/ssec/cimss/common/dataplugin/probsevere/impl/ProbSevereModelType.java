package edu.wisc.ssec.cimss.common.dataplugin.probsevere.impl;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * NOAA/CIMSS ProbSevere Model Type Definition
 *
 * Data object that defines attributes of a NOAA/CIMSS ProbSevere Model type
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
public class ProbSevereModelType {

    private Map<String, String> severeModelProps;

    private Map<String, String> torModelProps;

    private Map<String, String> hailModelProps;

    private Map<String, String> windModelProps;

    /**
     * Default empty constructor
     */
    public ProbSevereModelType(){
    }

    /**
     * Retrieve properties of main ProbSevere model type
     *
     * @return Main ProbSevere model type properties
     */
    public Map<String, String> getProbsevere() {
        return severeModelProps;
    }

    /**
     * Set properties of main ProbSevere model type
     *
     * @param main ProbSevere model type properties
     */
    public void setProbsevere(Map<String, String> severeModelProps) {
        this.severeModelProps = severeModelProps;
    }

    /**
     * Retrieve properties of ProbTor model type
     *
     * @return ProbTor model type properties
     */
    public Map<String, String> getProbtor() {
        return torModelProps;
    }

    /**
     * Set properties of ProbTor model type
     *
     * @param ProbTor model type properties
     */
    public void setProbtor(Map<String, String> torModelProps) {
        this.torModelProps = torModelProps;
    }

    /**
     * Retrieve properties of ProbHail model type
     *
     * @return ProbHail model type properties
     */
    public Map<String, String> getProbhail() {
        return hailModelProps;
    }

    /**
     * Set properties of ProbHail model type
     *
     * @param ProbHail model type properties
     */
    public void setProbhail(Map<String, String> hailModelProps) {
        this.hailModelProps = hailModelProps;
    }

    /**
     * Retrieve properties of ProbWind model type
     *
     * @return ProbWind model type properties
     */
    public Map<String, String> getProbwind() {
        return windModelProps;
    }

    /**
     * Set properties of ProbWind model type
     *
     * @param ProbWind model type properties
     */
    public void setProbwind(Map<String, String> windModelProps) {
        this.windModelProps = windModelProps;
    }

}