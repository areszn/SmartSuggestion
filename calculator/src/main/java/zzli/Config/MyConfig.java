package zzli.Config;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

/**
 * Created by catfish on 2016-12-1.
 */
@Component
public class MyConfig {
    public String recByContentIndex="rec_by_content_";
    public String recByCFIndex="rec_by_cf_";
    @Value("${elasticsearch.cluster.name}")
    public String cluster_name;
    @Value("${elasticsearch.server.name}")
    public String server_name;
    @Value("${elasticsearch.server.port}")
    public int server_port;

    @Bean
    public TransportClient transportClient() {
        Settings settings = Settings.builder()
                .put("cluster.name", cluster_name)
                .build();
        TransportClient client = null;
        try {
            client = new PreBuiltTransportClient(settings)
                    .addTransportAddresses(new InetSocketTransportAddress(InetAddress.getByName(server_name), server_port));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return client;
    }
}
