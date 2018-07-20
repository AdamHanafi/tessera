package com.github.tessera.server;

import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class PrometheusResponseFormatterTest {

    private ResponseFormatter responseFormatter;

    private ArrayList<MBeanMetric> mockMetrics;

    @Before
    public void setUp() {
        this.responseFormatter = new PrometheusResponseFormatter();
        this.mockMetrics = new ArrayList<>();
    }

    @Test
    public void noArgResourceResponseCorrectlyFormatted() {
        mockMetrics.add(createMBeanResourceMetric("GET->upCheck()#a10a4f8d", "AverageTime[ms]_total", "100" ));

        String expectedResponse = "tessera_GET_upCheck_AverageTime_ms 100";

        assertThat(responseFormatter.createResponse(mockMetrics)).isEqualTo(expectedResponse);
    }

    @Test
    public void singleArgResourceResponseCorrectlyFormatted() {
        mockMetrics.add(createMBeanResourceMetric("POST->resend(ResendRequest)#8ca0a760", "RequestRate[requestsPerSeconds]", "1.3" ));

        String expectedResponse = "tessera_POST_resend_ResendRequest_RequestRate_requestsPerSeconds 1.3";

        assertThat(responseFormatter.createResponse(mockMetrics)).isEqualTo(expectedResponse);
    }

    @Test
    public void singleArrayArgResourceResponseCorrectlyFormatted() {
        mockMetrics.add(createMBeanResourceMetric("POST->push(byte[])#7f702b7e", "MinTime[ms]_total", "3.4"));

        String expectedResponse = "tessera_POST_push_byte_MinTime_ms 3.4";

        assertThat(responseFormatter.createResponse(mockMetrics)).isEqualTo(expectedResponse);
    }

    @Test
    public void multipleArgResourceResponseCorrectlyFormatted() {
        mockMetrics.add(createMBeanResourceMetric("GET->receiveRaw(String;String)#fc8f8357", "AverageTime[ms]_total", "5.2"));

        String expectedResponse = "tessera_GET_receiveRaw_StringString_AverageTime_ms 5.2";

        assertThat(responseFormatter.createResponse(mockMetrics)).isEqualTo(expectedResponse);
    }

    @Test
    public void multipleMetricsResponseCorrectlyFormatted() {
        mockMetrics.add(createMBeanResourceMetric("GET->upCheck()#a10a4f8d", "AverageTime[ms]_total", "100" ));
        mockMetrics.add(createMBeanResourceMetric("POST->resend(ResendRequest)#8ca0a760", "RequestRate[requestsPerSeconds]", "1.3" ));

        String expectedResponse = "tessera_GET_upCheck_AverageTime_ms 100" + "\n" +
            "tessera_POST_resend_ResendRequest_RequestRate_requestsPerSeconds 1.3";

        assertThat(responseFormatter.createResponse(mockMetrics)).isEqualTo(expectedResponse);
    }

    @Test
    public void noMetricsToFormatIsHandled() {
        assertThat(responseFormatter.createResponse(mockMetrics).isEmpty());
    }

    private MBeanMetric createMBeanResourceMetric(String resourceMethod, String name, String value) {
        MBeanResourceMetric mBeanResourceMetric = new MBeanResourceMetric();
        mBeanResourceMetric.setResourceMethod(resourceMethod);
        mBeanResourceMetric.setName(name);
        mBeanResourceMetric.setValue(value);

        return mBeanResourceMetric;
    }
}
