package fun.services;

import fun.model.Customer;
import fun.model.dao.CustomerDAO;
import fun.model.dto.UpdateCustomerDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static java.lang.String.format;

@Component
public class CustomerValidationHelper {
    private final static String DUPLICATE_EMAIL_ERROR_TEMPLATE = "Duplicate email address found using '%s'";
    private final static String EXISTING_EMAIL_TEMPLATE = "Customer with email address '%s' already exists";
    private final static String CUSTOMER_DOES_NOT_EXIST_TEMPLATE = "Customer with uuid '%s' does not exist";

    @Autowired
    private CustomerDAO customerDAO;

    public void validateUserExistsByEmail(String emailAddress) {
        Customer existingCustomer = customerDAO.findByEmailAddressIgnoreCase(emailAddress);
        if (existingCustomer != null) {
            // Potential security hole identifying resources
            throw new IllegalArgumentException(format(EXISTING_EMAIL_TEMPLATE, emailAddress));
        }
    }

    public void validateCustomer(UpdateCustomerDTO updateCustomerDTO, Customer existingCustomer) {
        validateUserExistsByUuid(existingCustomer, updateCustomerDTO.getUuid());
        String emailAddress = updateCustomerDTO.getEmailAddress();
        if (customerAlreadyExistsWithEmail(emailAddress, existingCustomer.getUuid())) {
            // Potential security hole identifying resources
            throw new IllegalArgumentException(format(DUPLICATE_EMAIL_ERROR_TEMPLATE, emailAddress));
        }
    }

    public void validateUserExistsByUuid(Customer existingCustomer, String customerUuid) {
        if (existingCustomer ==  null) {
            throw new IllegalArgumentException(format(CUSTOMER_DOES_NOT_EXIST_TEMPLATE, customerUuid));
        }
    }

    private boolean customerAlreadyExistsWithEmail(String emailAddress, String customerUuid) {
        Customer customerWithExistingEmail = customerDAO.findByEmailAddressIgnoreCase(emailAddress);
        return !Objects.equals(customerWithExistingEmail.getUuid(), customerUuid);
    }
}
