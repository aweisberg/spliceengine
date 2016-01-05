package com.splicemachine.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.service.Service;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.server.MiniYARNCluster;
import org.apache.hadoop.yarn.server.nodemanager.NodeManager;
import org.apache.hadoop.yarn.server.nodemanager.containermanager.ContainerManagerImpl;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.ResourceScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.fifo.FifoScheduler;
import org.apache.log4j.Logger;

/**
 * Starts Yarn server
 */
public class SpliceTestYarnPlatform {
    public static final int DEFAULT_HEARTBEAT_INTERVAL = 100;
    public static int DEFAULT_NODE_COUNT = 1;

    private static final Logger LOG = Logger.getLogger(SpliceTestYarnPlatform.class);

    private URL yarnSiteConfigURL = null;
    private MiniYARNCluster yarnCluster = null;
    private Configuration conf = null;

    public SpliceTestYarnPlatform() {
        // for testing
        try {
            configForTesting();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error trying to config.", e);
        }
    }

    public static void main(String[] args) throws Exception {
        String classPathRoot;
        int nodeCount = DEFAULT_NODE_COUNT;
        if (args != null && args.length > 0) {
            classPathRoot = args[0];
        } else {
            throw new RuntimeException("Use main method for testing with splice yarn client. First arg is required " +
                                           "is the path to the root of the server classpath. This is required so that " +
                                           "splice clients can find the server configuration (yarn-site.xml) in order " +
                                           "to connect.");
        }
        if (args.length > 1) {
            nodeCount = Integer.parseInt(args[1]);
        }

        SpliceTestYarnPlatform yarnParticipant = new SpliceTestYarnPlatform();
        yarnParticipant.configForSplice(classPathRoot);
        yarnParticipant.start(nodeCount);
    }

    public Configuration getConfig() {
        return conf;
    }

    public MiniYARNCluster getYarnCluster() {
        return yarnCluster;
    }

    public void stop() {
        if (yarnCluster != null && yarnCluster.getServiceState() == Service.STATE.STARTED) {
            yarnCluster.stop();
        }
    }

    public void start(int nodeCount) throws Exception {
        if (yarnCluster == null) {
            LOG.info("Starting up YARN cluster with "+nodeCount+" nodes. Server yarn-site.xml is: "+yarnSiteConfigURL);

            yarnCluster = new MiniYARNCluster(SpliceTestYarnPlatform.class.getSimpleName(), nodeCount, 1, 1);
            yarnCluster.init(conf);
            yarnCluster.start();

            NodeManager nm = yarnCluster.getNodeManager(0);
            waitForNMToRegister(nm);

            // save the server config to classpath so yarn clients can read it
            Configuration yarnClusterConfig = yarnCluster.getConfig();
            yarnClusterConfig.set("yarn.application.classpath", new File(yarnSiteConfigURL.getPath()).getParent());
            //write the document to a buffer (not directly to the file, as that
            //can cause the file being written to get read -which will then fail.
            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
            yarnClusterConfig.writeXml(bytesOut);
            bytesOut.close();
            //write the bytes to the file in the classpath
            OutputStream os = new FileOutputStream(new File(yarnSiteConfigURL.getPath()));
            os.write(bytesOut.toByteArray());
            os.close();
        }
        LOG.info("YARN cluster started.");
    }

    private void configForSplice(String classPathRoot) throws URISyntaxException, MalformedURLException {
        LOG.info("Classpath root: "+classPathRoot);
        if (classPathRoot == null || classPathRoot.isEmpty()) {
            throw new RuntimeException("Can't find path to classpath root: "+classPathRoot);
        }
        File cpRootFile = new File(classPathRoot);
        if (! cpRootFile.exists()) {
            throw new RuntimeException("Can't find path to classpath root: "+classPathRoot);
        }
        cpRootFile = new File(classPathRoot, "/yarn-site.xml");
        yarnSiteConfigURL = cpRootFile.toURI().toURL();
    }

    private void configForTesting() throws URISyntaxException {
        yarnSiteConfigURL = Thread.currentThread().getContextClassLoader().getResource("yarn-site.xml");
        if (yarnSiteConfigURL == null) {
            throw new RuntimeException("Could not find 'yarn-site.xml' file in classpath");
        } else {
            LOG.info("Found 'yarn-site.xml' at "+ yarnSiteConfigURL.toURI().toString());
        }

        conf = new YarnConfiguration();
        conf.setInt(YarnConfiguration.RM_NM_HEARTBEAT_INTERVAL_MS, DEFAULT_HEARTBEAT_INTERVAL);
        conf.setInt(YarnConfiguration.RM_SCHEDULER_MINIMUM_ALLOCATION_MB, 128);
        conf.setClass(YarnConfiguration.RM_SCHEDULER, FifoScheduler.class, ResourceScheduler.class);
        conf.set("yarn.application.classpath", new File(yarnSiteConfigURL.getPath()).getParent());
    }

    private static void waitForNMToRegister(NodeManager nm)
        throws Exception {
        int attempt = 60;
        ContainerManagerImpl cm =
            ((ContainerManagerImpl) nm.getNMContext().getContainerManager());
        while (cm.getBlockNewContainerRequestsStatus() && attempt-- > 0) {
            Thread.sleep(2000);
        }
    }
}