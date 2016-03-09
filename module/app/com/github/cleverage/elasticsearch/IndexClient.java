package com.github.cleverage.elasticsearch;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsException;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.NodeBuilder;
import play.Logger;
import play.api.Configuration;
import play.api.Environment;

import java.net.InetAddress;
import java.nio.file.Paths;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

public class IndexClient {

    public static org.elasticsearch.node.Node node = null;

    public static org.elasticsearch.client.Client client = null;

    public static IndexConfig config;

    public IndexClient(Environment environment, Configuration configuration) {
        // ElasticSearch config load from application.conf
        this.config = new IndexConfig(environment, configuration);
    }

    public void start() throws Exception {

        // Load Elasticsearch Settings
        Settings.Builder settings = loadSettings();

        // Check Model
        if (this.isLocalMode()) {
            Logger.info("ElasticSearch : Starting in Local Mode");

            NodeBuilder nb = nodeBuilder().settings(settings).local(true).client(false).data(true);
            node = nb.node();
            client = node.client();
            Logger.info("ElasticSearch : Started in Local Mode");
        } else {
            Logger.info("ElasticSearch : Starting in Client Mode");
            TransportClient c = TransportClient.builder().settings(settings).build();
            if (config.client == null) {
                throw new Exception("Configuration required - elasticsearch.client when local model is disabled!");
            }

            String[] hosts = config.client.trim().split(",");
            boolean done = false;
            for (String host : hosts) {
                String[] parts = host.split(":");
                if (parts.length != 2) {
                    throw new Exception("Invalid Host: " + host);
                }
                Logger.info("ElasticSearch : Client - Host: " + parts[0] + " Port: " + parts[1]);
                c.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(parts[0]), Integer.valueOf(parts[1])));
                done = true;
            }
            if (!done) {
                throw new Exception("No Hosts Provided for ElasticSearch!");
            }
            client = c;
            Logger.info("ElasticSearch : Started in Client Mode");
        }

        // Check Client
        if (client == null) {
            throw new Exception("ElasticSearch Client cannot be null - please check the configuration provided and the health of your ElasticSearch instances.");
        }
    }

    /**
     * Checks if is local mode.
     *
     * @return true, if is local mode
     */
    private boolean isLocalMode() {
        try {
            if (config.client == null) {
                return true;
            }
            if (config.client.equalsIgnoreCase("false") || config.client.equalsIgnoreCase("true")) {
                return true;
            }

            return config.local;
        } catch (Exception e) {
            Logger.error("Error! Starting in Local Model: %s", e);
            return true;
        }
    }

    /**
     * Load settings from resource file
     *
     * @return
     * @throws Exception
     */
    private Settings.Builder loadSettings() throws Exception {
        Settings.Builder settings = Settings.settingsBuilder();

        // set default settings
        settings.put("client.transport.sniff", config.sniffing);
        
        if (config.clusterName != null && !config.clusterName.isEmpty()) {
            settings.put("cluster.name", config.clusterName);
        }

        // load settings
        if (config.localConfig != null && !config.localConfig.isEmpty()) {
            Logger.debug("Elasticsearch : Load settings from " + config.localConfig);
            try {
                settings.loadFromPath(Paths.get(this.getClass().getClassLoader().getResource(config.localConfig).toURI()));
            } catch (SettingsException settingsException) {
                Logger.error("Elasticsearch : Error when loading settings from " + config.localConfig);
                throw new Exception(settingsException);
            }
        }
        settings.build();
        Logger.info("Elasticsearch : Settings  " + settings.internalMap().toString());
        return settings;
    }

    public void stop() throws Exception {
        if (client != null) {
            client.close();
        }
        if (node != null) {
            node.close();
        }
    }
}
