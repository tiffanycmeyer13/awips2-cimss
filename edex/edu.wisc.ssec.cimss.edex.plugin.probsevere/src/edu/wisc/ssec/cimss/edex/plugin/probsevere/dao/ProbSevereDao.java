package edu.wisc.ssec.cimss.edex.plugin.probsevere.dao;

import edu.wisc.ssec.cimss.common.dataplugin.probsevere.ProbSevereRecord;
import com.raytheon.uf.common.dataplugin.PluginException;
import com.raytheon.uf.common.dataplugin.persist.IPersistable;
import com.raytheon.uf.common.datastorage.DataStoreFactory;
import com.raytheon.uf.common.datastorage.IDataStore;
import com.raytheon.uf.common.datastorage.records.IDataRecord;
import com.raytheon.uf.edex.database.plugin.PluginDao;

/**
 * NOAA/CIMSS ProbSevere Model Data Acquisition Object
 *
 * Defines access to persisted data from NOAA/CIMSS ProbSevere Model
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Mar 27, 2014 DCS 15298   jgerth      Initial Creation.
 * Nov 29, 2018 DCS 20816   lcronce     Updated package name and methods 
 *                                      to use ProbSevere instead of ConvectProb 
 *                                      to better reflect the product origin.
 *
 * </pre
 *
 * @author Jordan Gerth
 * @version 1.0
 *
 */

public class ProbSevereDao extends PluginDao {

    /**
     * ProbSevereDao constructor
     * @param Plugin name
     * @throws PluginException
     */
    public ProbSevereDao(String pluginName) throws PluginException {
        super(pluginName);
    }

    /**
     * Copy data from a Persistable object into a given DataStore container.
     * @param dataStore DataStore instance to receive the Persistable data.
     * @param obj The Persistable object to be stored.
     * @throws Exception Any general exception thrown in this method.
     */
    protected IDataStore populateDataStore(IDataStore dataStore,
            IPersistable obj) throws Exception {
        ProbSevereRecord psRec = (ProbSevereRecord) obj;

        for (int i = 0; i < psRec.getDataArrays().length; i++) {
            IDataRecord record = DataStoreFactory.createStorageRecord(
                    psRec.getDataNames()[i], psRec.getDataURI(), psRec.getDataArrays()[i]);
            record.setCorrelationObject(psRec);
            dataStore.addDataRecord(record);
        }

        return dataStore;
    }

}
