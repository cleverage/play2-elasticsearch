package elasticsearch;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import play.Logger;
import play.Play;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * User: nboire
 * Date: 19/04/12
 */
public class IndexClient {

    /**
     * The client.
     */
    private static org.elasticsearch.client.Client client = null;

    /**
     * Client.
     *
     * @return the client
     */
    public static org.elasticsearch.client.Client client() {
        return client;
    }

    /**
     * Checks if is local mode.
     *
     * @return true, if is local mode
     */
    private boolean isLocalMode() {
        try {
            String clientConfig = Play.application().configuration().getString("elasticsearch.client");
            Boolean local = Play.application().configuration().getBoolean("elasticsearch.local");

            if (clientConfig == null) {
                return true;
            }
            if (clientConfig.equalsIgnoreCase("false") || clientConfig.equalsIgnoreCase("true")) {
                return true;
            }

            return local;
        } catch (Exception e) {
            Logger.error("Error! Starting in Local Model: %s", e);
            return true;
        }
    }

    /**
     * Gets the hosts.
     *
     * @return the hosts
     */
    public static String getHosts() {
        String s = Play.application().configuration().getString("elasticsearch.client");
        if (s == null) {
            return "";
        }
        return s;
    }

    public void start() throws Exception {
        // Start Node Builder
        ImmutableSettings.Builder settings = ImmutableSettings.settingsBuilder();
        settings.put("client.transport.sniff", true);
        settings.build();

        // Check Model
        if (this.isLocalMode()) {
            Logger.info("Starting Elastic Search for Play! in Local Mode");
            NodeBuilder nb = nodeBuilder().settings(settings).local(true).client(false).data(true);
            Node node = nb.node();
            client = node.client();

        } else {
            Logger.info("Connecting Play! to Elastic Search in Client Mode");
            TransportClient c = new TransportClient(settings);
            if (Play.application().configuration().getString("elasticsearch.client") == null) {
                throw new Exception("Configuration required - elasticsearch.client when local model is disabled!");
            }
            String[] hosts = getHosts().trim().split(",");
            boolean done = false;
            for (String host : hosts) {
                String[] parts = host.split(":");
                if (parts.length != 2) {
                    throw new Exception("Invalid Host: " + host);
                }
                Logger.info("Transport Client - Host: " + parts[0] + " Port: "+ parts[1]);
                c.addTransportAddress(new InetSocketTransportAddress(parts[0], Integer.valueOf(parts[1])));
                done = true;
            }
            if (done == false) {
                throw new Exception("No Hosts Provided for Elastic Search!");
            }
            client = c;
        }
        Logger.info("Starting Elastic Search for Play! is ready");

        // Check Client
        if (client == null) {
            throw new Exception("Elastic Search Client cannot be null - please check the configuration provided and the health of your Elastic Search instances.");
        }
    }
}
