package fun.services;

import fun.model.Customer;
import fun.model.dao.CustomerDAO;
import fun.model.Order;
import fun.model.dao.OrderDAO;
import fun.model.Part;
import fun.model.Sprocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.lang.String.format;

@Service
public class OrderService {
    @Autowired
    private OrderDAO orderDAO;

    @Autowired
    private CustomerDAO customerDAO;

    @Autowired
    private CustomerValidationHelper customerValidationHelper;

    @Transactional
    public Order createOrder(Map<Part, Integer> parts, String customerUuid) {
        Order order = new Order();
        order.setItemsOrdered(parts);
        setOrderedByOrThrowException(customerUuid, order);
        order.setTotalCost(parts.entrySet()
                        .stream()
                        .mapToDouble(this::costTimesQuantity)
                        .sum());
        order.setOrderedDate(new Date());
        return orderDAO.save(order);
    }

    private double costTimesQuantity(Map.Entry<Part, Integer> part) {
        return part.getKey().getCost() * part.getValue();
    }

    private void setOrderedByOrThrowException(String customerUuid, Order order) {
        Customer customer = customerDAO.findByUuid(customerUuid);
        customerValidationHelper.validateUserExistsByUuid(customer, customerUuid);
        order.setOrderedBy(customer);
    }

    @Transactional
    public List<Order> getOrderHistoryByCustomer(String customerUuid) {
        Customer customer = customerDAO.findByUuid(customerUuid);
        customerValidationHelper.validateUserExistsByUuid(customer, customerUuid);
        return orderDAO.findAllByOrderedBy(customer);
    }

    @Transactional
    public List<Order> getOrderHistoryByCustomerWithDateRange(String customerUuid, Date start, Date end) {
        Customer customer = customerDAO.findByUuid(customerUuid);
        customerValidationHelper.validateUserExistsByUuid(customer, customerUuid);
        return orderDAO.findByCustomerOrdersWithinDateRange(customer, start, end);
    }

    public Part getPartByName(String partName) {
        switch (partName.toLowerCase()) {
            case "sprocket":
                //TODO check part service to ensure there are enough in stock?
                //TODO Costs change based on supply/demand? Bulk discount?
                return new Sprocket(1.00, "waldo");
            default:
                throw new IllegalArgumentException(format("Part not found with name '%s'", partName));
        }
    }

    // Should most likely be moved to SQL as I would not expect this to be performant
    public Map<String, Integer> getMapOfCustomerSprocketOrderHistory(Date startDate, Date endDate) {
        Map<String, Integer> customerIntegerMap = new HashMap<>();
        Iterable<Customer> customers = customerDAO.findAll();
        StreamSupport.stream(customers.spliterator(), false)
                .forEach(customer -> {
                    Iterable<Order> orders = getOrdersByCustomer(startDate, endDate, customer);
                    Integer sprocketCount = getSprocketOrderCountByCustomer(orders);
                    customerIntegerMap.put(customer.getEmailAddress(), sprocketCount);
                });
        return customerIntegerMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(10) // limiting to top 10 results, adjustable pagination??
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    private Integer getSprocketOrderCountByCustomer(Iterable<Order> orders) {
        return StreamSupport.stream(orders.spliterator(), false)
                                .mapToInt(this::getSprocketSumFromOrders)
                                .sum(); // Sum all sprockets from all orders
    }

    private int getSprocketSumFromOrders(Order order) {
        return order.getItemsOrdered().entrySet()
                .stream()
                .filter(e -> e.getKey() instanceof Sprocket)
                .mapToInt(Map.Entry::getValue)
                .sum();
    }

    private Iterable<Order> getOrdersByCustomer(Date startDate, Date endDate, Customer customer) {
        Iterable<Order> orders;
        if (startDate == null && endDate == null) {
            orders = orderDAO.findAllByOrderedBy(customer);
        } else {
            orders = orderDAO.findByCustomerOrdersWithinDateRange(customer, startDate, endDate);
        }
        return orders;
    }
}
