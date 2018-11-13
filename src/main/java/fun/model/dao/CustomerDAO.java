package fun.model.dao;

import fun.model.Customer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerDAO extends CrudRepository<Customer, String> {
    Customer findByUuid(String uuid);

    Customer findByEmailAddressIgnoreCase(String emailAddress);
}
