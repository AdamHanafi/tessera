package com.quorum.tessera.p2p;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.node.PartyInfoParser;
import com.quorum.tessera.node.PartyInfoService;
import com.quorum.tessera.node.model.Party;
import com.quorum.tessera.node.model.PartyInfo;
import com.quorum.tessera.node.model.Recipient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonReader;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PartyInfoResourceTest {

    private PartyInfoService partyInfoService;

    private PartyInfoResource partyInfoResource;

    private PartyInfoParser partyInfoParser;

    @Before
    public void onSetup() {
        this.partyInfoService = mock(PartyInfoService.class);
        this.partyInfoParser = mock(PartyInfoParser.class);

        this.partyInfoResource = new PartyInfoResource(partyInfoService, partyInfoParser);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(partyInfoService, partyInfoParser);
    }

    @Test
    public void partyInfoPost() throws IOException {

        byte[] data = "{}".getBytes();

        PartyInfo partyInfo = mock(PartyInfo.class);
        when(partyInfoParser.from(data)).thenReturn(partyInfo);
        when(partyInfoService.updatePartyInfo(partyInfo)).thenReturn(partyInfo);

        byte[] resultData = "Returned Party Info Data".getBytes();

        when(partyInfoParser.to(partyInfo)).thenReturn(resultData);

        Response response = partyInfoResource.partyInfo(data);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        StreamingOutput o = (StreamingOutput) response.getEntity();
        o.write(mock(OutputStream.class));

        assertThat(o).isNotNull();

        verify(partyInfoParser).from(data);
        verify(partyInfoService).updatePartyInfo(partyInfo);
        verify(partyInfoParser).to(partyInfo);
    }

    @Test
    public void partyInfoGet() {

        final String partyInfoJson = "{\"url\":\"http://localhost:9001/\",\"peers\":[\"http://localhost:9006/\",\"http://localhost:9005/\"],\"keys\":[{\"key\":\"BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=\",\"url\":\"http://localhost:9001/\"},{\"key\":\"QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc=\",\"url\":\"http://localhost:9002/\"}]}";

        final PartyInfo partyInfo = new PartyInfo(
            "http://localhost:9001/",
            new HashSet<>(Arrays.asList(
                new Recipient(PublicKey.from(Base64.getDecoder().decode("BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=")), "http://localhost:9001/"),
                new Recipient(PublicKey.from(Base64.getDecoder().decode("QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc=")), "http://localhost:9002/"))
            ),
            new HashSet<>(Arrays.asList(new Party("http://localhost:9005/"), new Party("http://localhost:9006/")))
        );

        when(partyInfoService.getPartyInfo()).thenReturn(partyInfo);

        final Response response = partyInfoResource.getPartyInfo();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        final String output = response.getEntity().toString();
        final JsonReader expected = Json.createReader(new StringReader(partyInfoJson));
        final JsonReader actual = Json.createReader(new StringReader(output));

        assertThat(expected.readObject()).isEqualTo(actual.readObject());

        verify(partyInfoService).getPartyInfo();
    }

}
