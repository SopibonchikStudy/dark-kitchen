import os

def create_directory_structure():
    """Создает структуру папок и файлов для Dark Kitchen проекта"""
    
    # Структура проекта в виде словаря: путь к папке -> список файлов
    structure = {
        "dark-kitchen-events-contract/src/main/java/edu/rutmiit/demo/darkkitchen/events": [
            "EventMetadata.java",
            "EventEnvelope.java",
            "RoutingKeys.java",
            "OrderEvent.java",
            "KitchenEvent.java",
            "DeliveryEvent.java"
        ],
        "dark-kitchen-events-contract/src/main/resources": [
            "application.properties"
        ],
        "dark-kitchen-events-contract": [
            "pom.xml"
        ],
        
        "dark-kitchen-api-contract/src/main/java/edu/rutmiit/demo/darkkitchenapi/dto": [
            "OrderRequest.java",
            "OrderResponse.java",
            "OrderItemRequest.java",
            "OrderItemResponse.java",
            "StatusUpdateRequest.java",
            "CourierInfo.java",
            "StatusHistoryEntry.java",
            "ErrorResponse.java"
        ],
        "dark-kitchen-api-contract/src/main/java/edu/rutmiit/demo/darkkitchenapi/endpoints": [
            "OrderApi.java"
        ],
        "dark-kitchen-api-contract/src/main/resources": [
            "application.properties"
        ],
        "dark-kitchen-api-contract": [
            "pom.xml"
        ],
        
        "order-service/src/main/java/edu/rutmiit/demo/orderservice": [
            "OrderServiceApplication.java"
        ],
        "order-service/src/main/java/edu/rutmiit/demo/orderservice/config": [
            "RabbitMQConfig.java"
        ],
        "order-service/src/main/java/edu/rutmiit/demo/orderservice/controller": [
            "OrderController.java",
            "RootController.java"
        ],
        "order-service/src/main/java/edu/rutmiit/demo/orderservice/service": [
            "OrderService.java"
        ],
        "order-service/src/main/java/edu/rutmiit/demo/orderservice/storage": [
            "OrderStorage.java"
        ],
        "order-service/src/main/java/edu/rutmiit/demo/orderservice/assembler": [
            "OrderModelAssembler.java"
        ],
        "order-service/src/main/java/edu/rutmiit/demo/orderservice/event": [
            "OrderEventPublisher.java"
        ],
        "order-service/src/main/java/edu/rutmiit/demo/orderservice/listener": [
            "OrderStatusListener.java"
        ],
        "order-service/src/main/java/edu/rutmiit/demo/orderservice/exception": [
            "GlobalExceptionHandler.java",
            "ResourceNotFoundException.java"
        ],
        "order-service/src/main/java/edu/rutmiit/demo/orderservice/graphql/fetcher": [
            "OrderDataFetcher.java",
            "OrderMutationFetcher.java"
        ],
        "order-service/src/main/java/edu/rutmiit/demo/orderservice/graphql/types": [
            "OrderFilterGql.java",
            "CreateOrderInputGql.java",
            "OrderConnectionGql.java"
        ],
        "order-service/src/main/java/edu/rutmiit/demo/orderservice/graphql/exception": [
            "GraphQLExceptionHandler.java"
        ],
        "order-service/src/main/resources": [
            "application.properties"
        ],
        "order-service": [
            "pom.xml"
        ],
        
        "kitchen-service/src/main/java/edu/rutmiit/demo/kitchenservice": [
            "KitchenServiceApplication.java"
        ],
        "kitchen-service/src/main/java/edu/rutmiit/demo/kitchenservice/config": [
            "RabbitMQConfig.java"
        ],
        "kitchen-service/src/main/java/edu/rutmiit/demo/kitchenservice/listener": [
            "KitchenOrderListener.java"
        ],
        "kitchen-service/src/main/java/edu/rutmiit/demo/kitchenservice/event": [
            "KitchenEventPublisher.java"
        ],
        "kitchen-service/src/main/resources": [
            "application.properties"
        ],
        "kitchen-service": [
            "pom.xml"
        ],
        
        "delivery-service/src/main/java/edu/rutmiit/demo/deliveryservice": [
            "DeliveryServiceApplication.java"
        ],
        "delivery-service/src/main/java/edu/rutmiit/demo/deliveryservice/config": [
            "RabbitMQConfig.java"
        ],
        "delivery-service/src/main/java/edu/rutmiit/demo/deliveryservice/listener": [
            "DeliveryOrderListener.java"
        ],
        "delivery-service/src/main/java/edu/rutmiit/demo/deliveryservice/event": [
            "DeliveryEventPublisher.java"
        ],
        "delivery-service/src/main/resources": [
            "application.properties"
        ],
        "delivery-service": [
            "pom.xml"
        ],
        
        "notification-service/src/main/java/edu/rutmiit/demo/notificationservice": [
            "NotificationServiceApplication.java"
        ],
        "notification-service/src/main/java/edu/rutmiit/demo/notificationservice/config": [
            "RabbitMQConfig.java"
        ],
        "notification-service/src/main/java/edu/rutmiit/demo/notificationservice/listener": [
            "NotificationListener.java"
        ],
        "notification-service/src/main/java/edu/rutmiit/demo/notificationservice/controller": [
            "NotificationController.java"
        ],
        "notification-service/src/main/resources": [
            "application.properties"
        ],
        "notification-service": [
            "pom.xml"
        ],
        
        "gateway/src/main/java/edu/rutmiit/demo/gateway": [
            "GatewayApplication.java"
        ],
        "gateway/src/main/java/edu/rutmiit/demo/gateway/config": [
            "GatewayConfig.java"
        ],
        "gateway/src/main/resources": [
            "application.properties"
        ],
        "gateway": [
            "pom.xml"
        ]
    }
    
    # Создаем директории и файлы
    for dir_path, files in structure.items():
        # Создаем директорию (рекурсивно)
        os.makedirs(dir_path, exist_ok=True)
        
        # Создаем файлы в директории
        for file_name in files:
            file_path = os.path.join(dir_path, file_name)
            if not os.path.exists(file_path):
                with open(file_path, 'w', encoding='utf-8') as f:
                    # Добавляем комментарий-заглушку в Java файлы
                    if file_name.endswith('.java'):
                        f.write(f"// TODO: Implement {file_name}\n")
                    elif file_name == 'pom.xml':
                        f.write("""<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>edu.rutmiit.demo</groupId>
    <artifactId>""" + os.path.basename(os.path.dirname(os.path.dirname(os.path.dirname(file_path)))) + """</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
    </properties>
</project>""")
                    elif file_name == 'application.properties':
                        f.write("# Application properties\n")
                    else:
                        f.write(f"# TODO: Implement {file_name}\n")
                print(f"Создан файл: {file_path}")
            else:
                print(f"Файл уже существует: {file_path}")
    
    print("\n✅ Структура проекта успешно создана!")

if __name__ == "__main__":
    try:
        create_directory_structure()
    except Exception as e:
        print(f"❌ Ошибка при создании структуры: {e}")