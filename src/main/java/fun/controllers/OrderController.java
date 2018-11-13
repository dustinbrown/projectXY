package fun.controllers;

import fun.model.dto.CreateOrderDTO;
import fun.model.Customer;
import fun.model.Order;
import fun.model.dto.OrderResponseDTO;
import fun.model.Part;
import fun.services.OrderService;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

//TODO create error handler to make error responses pretty and with meaningful status codes
@Controller
@RequestMapping("/api")
public class OrderController {
    private final static ModelMapper mapper = new ModelMapper();

    @Autowired
    private OrderService orderService;

    public OrderController() {
        Converter<Customer, String> toEmailAddress = ctx -> ctx.getSource() == null ? null : ctx.getSource().getEmailAddress();
        TypeMap<Order, OrderResponseDTO> typeMap = mapper.typeMap(Order.class, OrderResponseDTO.class);
        typeMap.addMappings(mapper -> mapper.using(toEmailAddress).map(Order::getOrderedBy, OrderResponseDTO::setCustomerEmailAddress));
    }

    @PostMapping(value = "/order", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public OrderResponseDTO createOrder(@Valid @RequestBody CreateOrderDTO createOrderDTO) {
        Map<Part, Integer> partMap = createOrderDTO.getParts()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(part -> orderService.getPartByName(part.getKey()),
                        Map.Entry::getValue));
        Order order = orderService.createOrder(partMap, createOrderDTO.getOrderByUuid());
        return mapper.map(order, OrderResponseDTO.class);
    }

    @GetMapping(value = "/orders/{customerUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<OrderResponseDTO> getOrdersByCustomer(@PathVariable String customerUuid,
                                                         @DateTimeFormat(iso = DATE_TIME) // ie, 2018-11-13T02:36:59.00Z
                                                         @RequestParam(value = "startDate", required = false) String startDate,
                                                         @DateTimeFormat(iso = DATE_TIME)
                                                         @RequestParam(value = "endDate", required = false) String endDate) {
        List<Order> orders;
        if (startDate == null && endDate == null) {
            orders = orderService.getOrderHistoryByCustomer(customerUuid);
        } else {
            orders = orderService.getOrderHistoryByCustomerWithDateRange(customerUuid, toDate(startDate),toDate(endDate));
        }
        return orders.stream()
                .map(order -> mapper.map(order, OrderResponseDTO.class))
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/orders/sprockets", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Integer> getMostSprockets(@DateTimeFormat(iso = DATE_TIME)
                                                     @RequestParam(value = "startDate", required = false) String startDate,
                                                  @DateTimeFormat(iso = DATE_TIME)
                                                  @RequestParam(value = "endDate", required = false) String endDate) {
        return orderService.getMapOfCustomerSprocketOrderHistory(toDate(startDate), toDate(endDate));
    }

    private Date toDate(String stringDate) {
        return Date.from(Instant.parse(stringDate));
    }

}
