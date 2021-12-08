package edu.wisc.ssec.cimss.edex.plugin.probsevere.dao;

import com.raytheon.uf.common.dataplugin.PluginException;
import com.raytheon.uf.common.dataplugin.persist.IPersistable;
import com.raytheon.uf.common.datastorage.DataStoreFactory;
import com.raytheon.uf.common.datastorage.IDataStore;
import com.raytheon.uf.common.datastorage.records.DataUriMetadataIdentifier;
import com.raytheon.uf.common.datastorage.records.IDataRecord;
import com.raytheon.uf.common.datastorage.records.IMetadataIdentifier;
import com.raytheon.uf.edex.database.plugin.PluginDao;

import edu.wisc.ssec.cimss.common.dataplugin.probsevere.ProbSevereRecord;

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
 * Oct 13, 2021 8608        mapeters    Pass metadata ids to datastore
 *
 * </pre>
 *
 * @author Jordan Gerth
 */
public class ProbSevereDao extends PluginDao {

    /**
     * ProbSevereDao constructor
     *
     * @param pluginName
     * @throws PluginException
     */
    public ProbSevereDao(String pluginName) throws PluginException {
        super(pluginName);
    }

    @Override
    protected IDataStore populateDataStore(IDataStore dataStore,
            IPersistable obj) throws Exception {
        ProbSevereRecord psRec = (ProbSevereRecord) obj;
        IMetadataIdentifier metaId = new DataUriMetadataIdentifier(psRec);

        for (int i = 0; i < psRec.getDataArrays().length; i++) {
            IDataRecord record = DataStoreFactory.createStorageRecord(
                    psRec.getDataNames()[i], psRec.getDataURI(),
                    psRec.getDataArrays()[i]);
            record.setCorrelationObject(psRec);
            dataStore.addDataRecord(record, metaId);
        }

        return dataStore;
    }

}
