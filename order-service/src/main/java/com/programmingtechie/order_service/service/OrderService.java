package com.programmingtechie.order_service.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.programmingtechie.order_service.dto.InventoryResponse;
import com.programmingtechie.order_service.dto.OrderLineItemsDto;
import com.programmingtechie.order_service.dto.OrderRequest;
import com.programmingtechie.order_service.model.Order;
import com.programmingtechie.order_service.model.OrderLineItems;
import com.programmingtechie.order_service.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;

    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemDtoList()
            .stream()
            .map(this::mapToDto)
            .toList();

        order.setOrderlineItemList(orderLineItems);

        List<String> skuCodes = order.getOrderlineItemList().stream()
            .map(OrderLineItems::getSkuCode)
            .toList();

        String skuCodesParam = String.join(",", skuCodes);

        InventoryResponse[] inventoryResponseArray = webClient.get()
            .uri("http://localhost:8082/api/inventory?skuCode={skuCodes}", skuCodesParam)
            .retrieve()
            .bodyToMono(InventoryResponse[].class)
            .block();

        if (inventoryResponseArray == null || inventoryResponseArray.length == 0) {
            log.warn("No response from inventory service");
            throw new IllegalArgumentException("Unable to check inventory");
        }

        boolean allProductInStock = Arrays.stream(inventoryResponseArray)
            .allMatch(response -> response.getIsInStock());

        if (allProductInStock) {
            orderRepository.save(order);
        } else {
            throw new IllegalArgumentException("Product is not in stock, please try again later");
        }
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemDto.getPrice());
        orderLineItems.setQuantity(orderLineItemDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemDto.getSkuCode());
        return orderLineItems;
    }
}