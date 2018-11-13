package fun.model.dao;

import fun.model.Customer;
import fun.model.Order;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface OrderDAO extends CrudRepository<Order, String> {
    Order findByUuid(String uuid);

    // select * from order where order.orderedBy = '<customerUuid>'
    List<Order> findAllByOrderedBy(Customer customer);

    @Query("SELECT o FROM Order o WHERE o.orderedBy = :customer AND o.orderedDate BETWEEN :start AND :end")
    List<Order> findByCustomerOrdersWithinDateRange(@Param("customer") Customer customer,
                                                    @Param("start") Date start,
                                                    @Param("end") Date end);
}
