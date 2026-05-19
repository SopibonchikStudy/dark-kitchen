package edu.rutmiit.demo.kitchenservice.config;

import edu.rutmiit.demo.kitchenservice.grpc.KitchenGrpcService;
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

    @Value("${grpc.server.port:9092}")
    private int grpcPort;

    private Server server;
    private boolean running = false;
    private final KitchenGrpcService kitchenGrpcService;

    public GrpcServerLifecycle(KitchenGrpcService kitchenGrpcService) {
        this.kitchenGrpcService = kitchenGrpcService;
    }

    @Override
    public void start() {
        try {
            server = ServerBuilder.forPort(grpcPort)
                    .addService(kitchenGrpcService)
                    .build()
                    .start();

            running = true;
            log.info("gRPC сервер кухни запущен на порту {}", grpcPort);
        } catch (IOException e) {
            log.error("Ошибка запуска gRPC сервера", e);
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            log.info("Остановка gRPC сервера кухни...");
            server.shutdown();
            running = false;
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}