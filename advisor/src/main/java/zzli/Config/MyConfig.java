package zzli.Config;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;

/**
 * Created by catfish on 2016-12-1.
 * At 2016-12-1
 */
@Configuration
public class MyConfig {
    public String recByContentIndex="rec_by_content_";
    public String recByCFIndex="rec_by_cf_";
//    @Value("${elasticsearch.recByContentIndex}")
//    public String recByContentIndex;
//    @Value("${elasticsearch.recByCFIndex}")
//    public String recByCFIndex;
    @Value("${elasticsearch.cluster.name}")
    public String clusterName;
    @Value("${elasticsearch.server.name}")
    public String serverName;
    @Value("${elasticsearch.server.port}")
    public int serverPort;

    @Bean
    public TransportClient transportClient() {
        Settings settings = Settings.builder()
                .put("cluster.name", clusterName)
                .build();
        TransportClient client = null;
        try {
            client = new PreBuiltTransportClient(settings)
                    .addTransportAddresses(new InetSocketTransportAddress(InetAddress.getByName(serverName), serverPort));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return client;
    }
}
