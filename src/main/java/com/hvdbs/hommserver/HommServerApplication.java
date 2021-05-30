package com.hvdbs.hommserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.tcp.serializer.AbstractByteArraySerializer;
import org.springframework.integration.ip.tcp.serializer.ByteArrayRawSerializer;
import org.springframework.integration.ip.tcp.serializer.TcpCodecs;

import java.io.IOException;
import java.io.OutputStream;

@SpringBootApplication
public class HommServerApplication {

    @Autowired
    ServerSocketHandler serverSocketHandler;

    public static void main(String[] args) {
        SpringApplication.run(HommServerApplication.class, args);
    }

    @Bean
    public IntegrationFlow server() {
        return IntegrationFlows.from(Tcp.inboundGateway(
                Tcp.netServer(47624)
                        .serializer(codec()) // default is CRLF
                        .deserializer(codec()).soTcpNoDelay(true))) // default is CRLF
                .handle(serverSocketHandler::handleMessage)
                .get();
    }

    @Bean
    public IntegrationFlow client() {
        return IntegrationFlows.from(MyGateway.class)
                .handle(Tcp.outboundGateway(
                        Tcp.netClient("localhost", 1234)
                                .serializer(new ByteArrayRawSerializer() {
                                    @Override
                                    public void serialize(byte[] bytes, OutputStream outputStream) throws IOException {
                                        outputStream.write(bytes);
                                        outputStream.flush();
                                    }
                                }) // default is CRLF
                                .deserializer(inputStream -> {
                                    byte[] message = new byte[0];
                                    if (inputStream.available() > 0) {
                                        message = inputStream.readAllBytes();
                                    }
                                    return message;
                                }))) // default is CRLF
                .transform(Transformers.objectToString()) // byte[] -> String
                .get();
    }

    @Bean
    public AbstractByteArraySerializer codec() {
        System.out.println("11111111111111");
        return TcpCodecs.crlf();
    }
/*
    @Bean
    @DependsOn("client")
    ApplicationRunner runner(MyGateway gateway) {
        return args -> {
            System.out.println(gateway.exchange("foo"));
            System.out.println(gateway.exchange("bar"));
        };
    }*/

    public interface MyGateway {

        String exchange(String out);
    }
}
