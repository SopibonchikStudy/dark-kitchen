// assembler/OrderModelAssembler.java
package edu.rutmiit.demo.orderservice.assembler;

import edu.rutmiit.demo.darkkitchenapi.dto.OrderResponse;
import edu.rutmiit.demo.orderservice.controller.OrderController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class OrderModelAssembler implements RepresentationModelAssembler<OrderResponse, EntityModel<OrderResponse>> {

    @Override
    public EntityModel<OrderResponse> toModel(OrderResponse order) {
        return EntityModel.of(order,
                linkTo(methodOn(OrderController.class).getOrderById(order.getOrderId())).withSelfRel(),
                linkTo(methodOn(OrderController.class).getAllOrders(null)).withRel("collection")
        );
    }
}