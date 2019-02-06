package com.jpmorgan.quorum.enclave.websockets;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.RawTransaction;
import com.quorum.tessera.encryption.PublicKey;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.List;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnclaveRequestCodec extends JsonCodec<EnclaveRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnclaveRequestCodec.class);

    private static final Encoder BASE64_ENCODER = Base64.getEncoder();

    private static final Decoder BASE64_DECODER = Base64.getDecoder();

    @Override
    public JsonObjectBuilder doEncode(EnclaveRequest request) throws Exception {

        EnclaveRequestType enclaveRequestType = request.getType();

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (int i = 0; i < enclaveRequestType.getParamTypes().size(); i++) {
            Object value = request.getArgs().get(i);

            ArgType type = enclaveRequestType.getParamTypes().get(i);

            if (type == ArgType.PUBLIC_KEY_LIST) {
                JsonArrayBuilder nestedBuilder = Json.createArrayBuilder();
                List<PublicKey> publicKeys = List.class.cast(value);

                publicKeys.forEach((k) -> {
                    nestedBuilder.add(BASE64_ENCODER.encodeToString(k.getKeyBytes()));
                });
                jsonArrayBuilder.add(nestedBuilder);
                continue;
            }

            if (type == ArgType.BYTE_ARRAY) {
                String encodedValue = BASE64_ENCODER.encodeToString((byte[]) value);
                jsonArrayBuilder.add(encodedValue);
                continue;
            }

            if (type == ArgType.PUBLIC_KEY) {
                PublicKey publicKey = PublicKey.class.cast(value);
                String encodedKey = BASE64_ENCODER.encodeToString(publicKey.getKeyBytes());
                jsonArrayBuilder.add(encodedKey);
                continue;
            }

            if (type == ArgType.RAW_TRANSACTION) {
                RawTransaction txn = RawTransaction.class.cast(value);
                JsonObjectBuilder encoded = new RawTransactionCodec().doEncode(txn);
                jsonArrayBuilder.add(encoded);
                continue;
            }

            if (type == ArgType.ENCODED_PAYLOAD) {
                EncodedPayload encodedPayload = EncodedPayload.class.cast(value);
                JsonObjectBuilder encoded = new EncodedPayloadCodec().doEncode(encodedPayload);
                jsonArrayBuilder.add(encoded);
                continue;
            }

        }

        return Json.createObjectBuilder()
                .add("type", request.getType().name())
                .add("args", jsonArrayBuilder);
    }

    @Override
    public EnclaveRequest doDecode(JsonObject json) throws Exception {

        EnclaveRequestType enclaveRequestType = EnclaveRequestType.valueOf(json.getString("type"));

        JsonArray args = json.getJsonArray("args");

        EnclaveRequest.Builder requestBuilder = EnclaveRequest.Builder.create()
                .withType(enclaveRequestType);

        for (int i = 0; i < args.size(); i++) {
            ArgType type = enclaveRequestType.getParamTypes().get(i);
            if (type == ArgType.BYTE_ARRAY) {
                String encodedValue = args.getString(i);
                byte[] decodedValue = Base64.getDecoder().decode(encodedValue);
                requestBuilder.withArg(decodedValue);
            }

            if (type == ArgType.PUBLIC_KEY) {
                String encodedValue = args.getString(i);
                byte[] decodedValue = Base64.getDecoder().decode(encodedValue);
                requestBuilder.withArg(PublicKey.from(decodedValue));
            }

            if (type == ArgType.PUBLIC_KEY_LIST) {

                List<PublicKey> publicKeys = args.getJsonArray(i).stream()
                        .map(JsonString.class::cast)
                        .map(JsonString::getString)
                        .map(BASE64_DECODER::decode)
                        .map(PublicKey::from)
                        .collect(Collectors.toList());

                requestBuilder.withArg(publicKeys);
            }

            if (type == ArgType.RAW_TRANSACTION) {
                RawTransaction txn = new RawTransactionCodec().doDecode(args.getJsonObject(i));
                requestBuilder.withArg(txn);
            }

            if (type == ArgType.ENCODED_PAYLOAD) {
                EncodedPayload encodedPayoad = new EncodedPayloadCodec().doDecode(args.getJsonObject(i));
                requestBuilder.withArg(encodedPayoad);

            }
        }

        return requestBuilder.build();

    }
}
