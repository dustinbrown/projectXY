package fun.services;

import fun.model.Customer;
import fun.model.dao.CustomerDAO;
import fun.model.dto.UpdateCustomerDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CustomerValidationHelperTest {
    @InjectMocks
    private CustomerValidationHelper subject;

    @Mock
    private CustomerDAO customerDAO;

    @Test
    public void validateUserExistsByEmail() {
        when(customerDAO.findByEmailAddressIgnoreCase("foo@bar")).thenReturn(null);
        subject.validateUserExistsByEmail("foo@bar");
    }

    @Test
    public void validateUserExistsByEmailThrowsExpectedException() {
        when(customerDAO.findByEmailAddressIgnoreCase("foo@bar")).thenReturn(new Customer());
        try {
            subject.validateUserExistsByEmail("foo@bar");
            fail();
        } catch (Exception exception) {
           assertThat(exception.getMessage(), is("Customer with email address 'foo@bar' already exists"));
        }
    }

    @Test
    public void validateCustomerWithSameEmail() {
        UpdateCustomerDTO updateCustomerDTO = getUpdateCustomerDTO("foo@bar.com");
        Customer customer = getCustomer("blah blah", "foo@bar.com");
        when(customerDAO.findByEmailAddressIgnoreCase("foo@bar.com")).thenReturn(customer);

        subject.validateCustomer(updateCustomerDTO, customer);
    }

    @Test
    public void validateCustomerWithNewEmail() {
        UpdateCustomerDTO updateCustomerDTO = getUpdateCustomerDTO("bananas@bar.com");
        Customer customer = getCustomer("blah blah", "foo@bar.com");
        when(customerDAO.findByEmailAddressIgnoreCase("bananas@bar.com")).thenReturn(customer);

        subject.validateCustomer(updateCustomerDTO, customer);
    }

    @Test
    public void validateCustomerWithSameEmailAsAnotherCustomer() {
        UpdateCustomerDTO updateCustomerDTO = getUpdateCustomerDTO("me@me.com");
        Customer customer = getCustomer("blah blah", "foo@bar.com");
        Customer existingCustomer = getCustomer("nope nope", "me@me.com");
        when(customerDAO.findByEmailAddressIgnoreCase("me@me.com")).thenReturn(existingCustomer);

        try {
            subject.validateCustomer(updateCustomerDTO, customer);
        } catch (Exception exception) {
            assertThat(exception.getMessage(), is("Duplicate email address found using 'me@me.com'"));
        }
    }

    @Test
    public void validateUserExistsByUuid() {
        subject.validateUserExistsByUuid(new Customer(), "apples");
    }

    @Test
    public void validateUserExistsByUuidFailsAsExpectedWithNull() {
        try {
            subject.validateUserExistsByUuid(null, "apples");
            fail();
        } catch (Exception exception) {
            assertThat(exception.getMessage(), is("Customer with uuid 'apples' does not exist"));
        }
    }

    private UpdateCustomerDTO getUpdateCustomerDTO(String email) {
        UpdateCustomerDTO updateCustomerDTO = new UpdateCustomerDTO();
        updateCustomerDTO.setEmailAddress(email);
        updateCustomerDTO.setUuid("blah blah");
        return updateCustomerDTO;
    }

    private Customer getCustomer(String uuid, String email) {
        Customer customer = new Customer();
        customer.setUuid(uuid);
        customer.setEmailAddress(email);
        return customer;
    }
}