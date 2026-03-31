package com.mahajan.order_service.service;

import com.mahajan.order_service.client.ProductClient;
import com.mahajan.order_service.client.UserClient;
import com.mahajan.order_service.dto.OrderDto;
import com.mahajan.order_service.dto.ProductDto;
import com.mahajan.order_service.dto.UserDto;
import com.mahajan.order_service.entity.Order;
import com.mahajan.order_service.exception.ExternalServiceException;
import com.mahajan.order_service.exception.ResourceNotFoundException;
import com.mahajan.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;
    private final UserClient userClient;
    private final ProductClient productClient;

    @Override
    public OrderDto createOrder(OrderDto orderDto) {
        // Validate User
        try {
            UserDto userDto = userClient.getUserById(orderDto.getUserId());
            if (userDto == null) {
                throw new ExternalServiceException("User validation failed.");
            }
        } catch (Exception e) {
            throw new ExternalServiceException("User ID " + orderDto.getUserId() + " not found or User Service is down.");
        }

        // Validate Product
        try {
            ProductDto productDto = productClient.getProductById(orderDto.getProductId());
            if (productDto == null) {
                throw new ExternalServiceException("Product validation failed.");
            }
        } catch (Exception e) {
            throw new ExternalServiceException("Product ID " + orderDto.getProductId() + " not found or Product Service is down.");
        }

        Order order = modelMapper.map(orderDto, Order.class);
        Order savedOrder = orderRepository.save(order);
        return modelMapper.map(savedOrder, OrderDto.class);
    }

    @Override
    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Order", "id", id)
        );
        return modelMapper.map(order, OrderDto.class);
    }

    @Override
    public List<OrderDto> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(order -> modelMapper.map(order, OrderDto.class))
                .collect(Collectors.toList());
    }
}
