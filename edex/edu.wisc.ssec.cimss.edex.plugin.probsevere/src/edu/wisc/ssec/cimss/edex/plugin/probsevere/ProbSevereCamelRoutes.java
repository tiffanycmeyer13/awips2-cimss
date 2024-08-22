/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 *
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 *
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 *
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/

package edu.wisc.ssec.cimss.edex.plugin.probsevere;

import com.raytheon.uf.edex.esb.camel.EDEXRouteBuilder;

/**
 * Camel routes converted from file "probsevere-ingest.xml", context "probsevere-camel"
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 2024-08-22   2037701    lisa.singh   Initial creation (from auto-generated)
 *
 * </pre>
 */

// @formatter:off
/* Original XML context
 * <camelContext id="probsevere-camel"
        xmlns="http://camel.apache.org/schema/spring"
        errorHandlerRef="errorHandler"
        autoStartup="false">

        <!-- Begin probsevere routes -->
        <route id="probsevereIngestRoute">
            <from uri="jms-durable:queue:Ingest.probsevere"/>
            <setHeader name="pluginName">
                <constant>probsevere</constant>
            </setHeader>
            <doTry>
                <pipeline>
                    <bean ref="stringToFile" />
                    <bean ref="probsevereDecoder" method="decode" />
                    <to uri="direct-vm:persistIndexAlert" />
                </pipeline>
                <doCatch>
                    <exception>java.lang.Throwable</exception>
                    <to uri="log:probsevere?level=ERROR"/>
                </doCatch>
            </doTry>
        </route>
    </camelContext>
 */
// @formatter:on

public class ProbSevereCamelRoutes extends EDEXRouteBuilder {

    public ProbSevereCamelRoutes() {
    }

    @Override
    public void configure() throws Exception {
        // @formatter:off
        from("jms-durable:queue:Ingest.probsevere")
          .setHeader("pluginName", constant("probsevere"))
              .doTry()
                  .pipeline()
                      .bean("stringToFile")
                      .bean("probsevereDecoder", "decode")
                      .to("direct:persistIndexAlert")
              .endDoTry()
          .doCatch(Throwable.class)
              .to("log:probsevere?level=ERROR")
          .endDoTry()
          .end()
          .setId("probsevereIngestRoute");
        // @formatter:on
    }
}
