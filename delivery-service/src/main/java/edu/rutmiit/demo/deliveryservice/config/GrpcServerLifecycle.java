package edu.rutmiit.demo.deliveryservice.config;

import edu.rutmiit.demo.deliveryservice.grpc.DeliveryGrpcService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GrpcServerLifecycle implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(GrpcServerLifecycle.class);

    @Value("${grpc.server.port:9093}")
    private int grpcPort;

    private Server server;
    private boolean running = false;
    private final DeliveryGrpcService deliveryGrpcService;

    public GrpcServerLifecycle(DeliveryGrpcService deliveryGrpcService) {
        this.deliveryGrpcService = deliveryGrpcService;
    }

    @Override
    public void start() {
        try {
            server = ServerBuilder.forPort(grpcPort)
                    .addService(deliveryGrpcService)
                    .build()
                    .start();

            running = true;
            log.info("gRPC сервер доставки запущен на порту {}", grpcPort);
        } catch (IOException e) {
            log.error("Ошибка запуска gRPC сервера доставки", e);
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            log.info("Остановка gRPC сервера доставки...");
            server.shutdown();
            running = false;
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}