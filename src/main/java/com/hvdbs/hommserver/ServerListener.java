package com.hvdbs.hommserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.tcp.serializer.ByteArrayRawSerializer;
import org.springframework.integration.ip.tcp.serializer.TcpCodecs;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

@Component
public class ServerListener {
    private static final int listenerPort = 47624;
    private final Logger logger = LoggerFactory.getLogger(ServerListener.class);

    @Bean
    public IntegrationFlow receiveMessages() {
        return IntegrationFlows
                .from(
                        Tcp.inboundGateway(
                                Tcp.netServer(listenerPort)
                                .deserializer(TcpCodecs.crlf())
                                        .serializer(TcpCodecs.crlf())
                                        .deserializer(inputStream -> {
                                            byte[] message = new byte[0];
                                            if (inputStream.available() > 0) {
                                                message = inputStream.readAllBytes();
                                            }
                                            return message;
                                        })
                                        .serializer(new ByteArrayRawSerializer() {
                                            @Override
                                            public void serialize(byte[] bytes, OutputStream outputStream) throws IOException {
                                                byte[] response = new byte[] {1,1,1,1,1,1,1,1,1,1};
                                                outputStream.write(bytes);
                                                outputStream.flush();
                                            }
                                        })))
                .log()
                .handle((byte[] message, MessageHeaders messageHeaders) -> {
                            String string = new String(message);
                            logger.info(Arrays.toString(message));
                           // int val = ((message[0] & 0xff)) | (message[1] & 0xff << 8);
                            return string.toUpperCase();
                        }
                )

                .get();
    }
}
