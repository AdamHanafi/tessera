
package com.github.nexus.socket;

import com.github.nexus.config.ServerConfig;
import com.github.nexus.config.SslAuthenticationMode;
import com.github.nexus.config.SslConfig;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sun.security.ssl.SSLSocketFactoryImpl;

import javax.net.SocketFactory;
import java.io.File;
import java.net.URI;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpProxyFactoryTest {

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    @Test
    public void secureHttpProxy() throws Exception {

        final URI uri = new URI("http://bogus.com");

        final File tmpFile = new File(tmpDir.getRoot(), "keystores");

        final ServerConfig configuration = mock(ServerConfig.class);
        when(configuration.getServerUri()).thenReturn(uri);
        
        SslConfig sslConfig = mock(SslConfig.class);
        when(sslConfig.getTls()).thenReturn(SslAuthenticationMode.STRICT);
        when(configuration.getSslConfig()).thenReturn(sslConfig);
        
        when(sslConfig.getTls()).thenReturn(SslAuthenticationMode.STRICT);
        when(sslConfig.getClientKeyStore()).thenReturn(tmpFile.toPath());
        when(sslConfig.getClientKeyStorePassword()).thenReturn("somepwd");

        when(sslConfig.getClientTrustStore()).thenReturn(tmpFile.toPath());
        when(sslConfig.getClientTrustStorePassword()).thenReturn("somepwd");
        
        when(sslConfig.getKnownServersFile())
                .thenReturn(tmpFile.toPath());


        HttpProxyFactory proxyFactory = new HttpProxyFactory(configuration);
        HttpProxy proxy = proxyFactory.create();

        assertThat(proxy)
            .isNotNull()
            .extracting("socketFactory")
            .extracting("class")
            .containsExactly(SSLSocketFactoryImpl.class);

    }

    @Test
    public void insecureHttpProxy() throws Exception {
        final URI uri = new URI("http://bogus.com");

        final ServerConfig configuration = mock(ServerConfig.class);
        when(configuration.getServerUri()).thenReturn(uri);
        when(configuration.isSsl()).thenReturn(false);


        final HttpProxyFactory proxyFactory = new HttpProxyFactory(configuration);
        final HttpProxy proxy = proxyFactory.create();

        assertThat(proxy)
            .isNotNull()
            .extracting("socketFactory")
            .containsExactly(SocketFactory.getDefault());

    }

}
