package fun.services;

import fun.model.Customer;
import fun.model.dao.CustomerDAO;
import fun.model.Order;
import fun.model.dao.OrderDAO;
import fun.model.Part;
import fun.model.Sprocket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrderServiceTest {
    @InjectMocks
    private OrderService subject;

    @Mock
    private OrderDAO orderDAO;

    @Mock
    private CustomerDAO customerDAO;

    @Mock
    private CustomerValidationHelper customerValidationHelper;

    @Test
    public void createOrder() {
        Map<Part, Integer> parts = new HashMap<>();
        parts.put(new Sprocket(1.0, "me"), 10);
        Customer customer = getCustomer("foo@bar");
        when(customerDAO.findByUuid("apples")).thenReturn(customer);
        subject.createOrder(parts, "apples");

        ArgumentCaptor<Order> argumentCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderDAO).save(argumentCaptor.capture());

        Order savedOrder = argumentCaptor.getValue();
        assertThat(savedOrder.getItemsOrdered(), is(parts));
        assertThat(savedOrder.getOrderedBy().getEmailAddress(), is("foo@bar"));
        assertThat(savedOrder.getTotalCost(), is(10.0));
    }

    @Test
    public void getOrderHistoryByCustomer() {
        Customer customer = getCustomer("foo@bar");
        when(customerDAO.findByUuid("apples")).thenReturn(customer);

        subject.getOrderHistoryByCustomer("apples");
        verify(customerValidationHelper).validateUserExistsByUuid(customer, "apples");
        verify(orderDAO).findAllByOrderedBy(customer);
    }

    @Test
    public void getOrderHistoryByCustomerFailsAsExpected() {
        Customer customer = getCustomer("foo@bar");
        when(customerDAO.findByUuid("apples")).thenReturn(customer);
        doThrow(new RuntimeException("fail")).when(customerValidationHelper).validateUserExistsByUuid(customer, "apples");
        try {
            subject.getOrderHistoryByCustomer("apples");
            fail();
        } catch (Exception exception) {
           assertThat(exception, is(notNullValue()));
        }
        verifyZeroInteractions(orderDAO);
    }

    @Test
    public void getOrderHistoryByCustomerWithDateRange() {
        Customer customer = getCustomer("foo@bar");
        when(customerDAO.findByUuid("apples")).thenReturn(customer);
        Date start = new Date();
        Date end = new Date();

        subject.getOrderHistoryByCustomerWithDateRange("apples", start, end);
        verify(customerValidationHelper).validateUserExistsByUuid(customer, "apples");
        verify(orderDAO).findByCustomerOrdersWithinDateRange(customer, start, end);
    }

    @Test
    public void getOrderHistoryByCustomerWithDateRangeFailsAsExpected() {
        Customer customer = getCustomer("foo@bar");
        when(customerDAO.findByUuid("apples")).thenReturn(customer);
        doThrow(new RuntimeException("fail")).when(customerValidationHelper).validateUserExistsByUuid(customer, "apples");
        Date start = new Date();
        Date end = new Date();

        try {
            subject.getOrderHistoryByCustomerWithDateRange("apples", start, end);
            fail();
        } catch (Exception exception) {
            assertThat(exception, is(notNullValue()));
        }
        verifyZeroInteractions(orderDAO);
    }

    @Test
    public void getPartByName() {
        Part actual = subject.getPartByName("sprocket");
        assertThat(actual, instanceOf(Sprocket.class));
    }

    @Test
    public void getPartByNameWithUpperCase() {
        Part actual = subject.getPartByName("Sprocket");
        assertThat(actual, instanceOf(Sprocket.class));
    }

    @Test
    public void getPartByNameFailsWhenPartNotFound() {
        try {
            subject.getPartByName("wheels");
        } catch (Exception exception) {
            assertThat(exception.getMessage(), is("Part not found with name 'wheels'"));
        }
    }

    @Test
    public void getMapOfCustomerSprocketOrderHistoryWithNoDate() {
        Customer customer = getCustomer("foo");
        Customer customer1 = getCustomer("bar");

        Iterable<Customer> customerIterable = Arrays.asList(customer, customer1);
        when(customerDAO.findAll()).thenReturn(customerIterable);

        setOrder(customer, 10);
        setOrder(customer1, 100);

        Map<String, Integer> actual = subject.getMapOfCustomerSprocketOrderHistory(null, null);
        assertThat(actual, hasEntry(is("foo"), is(10)));
        assertThat(actual, hasEntry(is("bar"), is(100)));

    }

    @Test
    public void getMapOfCustomerSprocketOrderHistoryWithDateAndNoSprockets() {
        Customer customer = getCustomer("foo");
        Customer customer1 = getCustomer("bar");

        Iterable<Customer> customerIterable = Arrays.asList(customer, customer1);
        when(customerDAO.findAll()).thenReturn(customerIterable);

        setOrder(customer, 10);
        setOrder(customer1, 100);

        Map<String, Integer> actual = subject.getMapOfCustomerSprocketOrderHistory(new Date(), new Date());
        assertThat(actual, hasEntry(is("foo"), is(0)));
        assertThat(actual, hasEntry(is("bar"), is(0)));

    }

    @Test
    public void getMapOfCustomerSprocketOrderHistoryWithDate() {
        Customer customer = getCustomer("foo");
        Customer customer1 = getCustomer("bar");

        Iterable<Customer> customerIterable = Arrays.asList(customer, customer1);
        when(customerDAO.findAll()).thenReturn(customerIterable);
        Date startDate = new Date();
        Date endDate = new Date();

        setOrderWithDate(customer, 10, startDate, endDate);
        setOrderWithDate(customer1, 100, startDate, endDate);

        Map<String, Integer> actual = subject.getMapOfCustomerSprocketOrderHistory(startDate, endDate);
        assertThat(actual, hasEntry(is("foo"), is(10)));
        assertThat(actual, hasEntry(is("bar"), is(100)));

    }

    private void setOrder(Customer customer, int amountOfSprockets) {
        Order order = new Order();
        Map<Part, Integer> orderMap = new HashMap<>();
        orderMap.put(new Sprocket(1.0, "m"), amountOfSprockets);
        order.setItemsOrdered(orderMap);
        List<Order> orders = Arrays.asList(order);
        when(orderDAO.findAllByOrderedBy(customer)).thenReturn(orders);
    }

    private void setOrderWithDate(Customer customer, int amountOfSprockets, Date start, Date end) {
        Order order = new Order();
        Map<Part, Integer> orderMap = new HashMap<>();
        orderMap.put(new Sprocket(1.0, "m"), amountOfSprockets);
        order.setItemsOrdered(orderMap);
        List<Order> orders = Arrays.asList(order);
        when(orderDAO.findByCustomerOrdersWithinDateRange(customer, start, end)).thenReturn(orders);
    }

    private Customer getCustomer(String foo) {
        Customer customer = new Customer();
        customer.setEmailAddress(foo);
        return customer;
    }

    @Test
    public void happyPath() {

    }

}