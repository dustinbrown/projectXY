package fun.services;

import fun.model.dto.CreateCustomerDTO;
import fun.model.Customer;
import fun.model.dao.CustomerDAO;
import fun.model.dto.UpdateCustomerDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CustomerServiceTest {
    @InjectMocks
    private CustomerService subject;

    @Mock
    private CustomerDAO customerDAO;

    @Mock CustomerValidationHelper customerValidationHelper;

    @Test
    public void createCustomerFromDTO() {
        CreateCustomerDTO createCustomerDTO = getCreateCustomerDTO();

        subject.createCustomerFromDTO(createCustomerDTO);
        verify(customerValidationHelper).validateUserExistsByEmail("apples@bananas");
        ArgumentCaptor<Customer> argumentCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerDAO).save(argumentCaptor.capture());

        Customer savedCustomer = argumentCaptor.getValue();
        assertThat(savedCustomer.getEmailAddress(), is("apples@bananas"));
        assertThat(savedCustomer.getFirstName(), is("bobby"));
        assertThat(savedCustomer.getLastName(), is("sally"));
    }

    @Test
    public void createCustomerNotSavedWhenValidationFails() {
        CreateCustomerDTO createCustomerDTO = getCreateCustomerDTO();
        doThrow(new RuntimeException("fail")).when(customerValidationHelper).validateUserExistsByEmail("apples@bananas");

        try {
            subject.createCustomerFromDTO(createCustomerDTO);
            fail();
        } catch (Exception exception) {
            assertThat(exception, is(notNullValue()));
        }
        verifyZeroInteractions(customerDAO);
    }

    @Test
    public void updateCustomerEmailByUuid() {
        UpdateCustomerDTO updateCustomerDTO = getUpdateCustomerDTO();

        Customer customer = new Customer();
        customer.setEmailAddress("strawberries@grapes");
        customer.setFirstName("bobby");
        customer.setLastName("sally");

        when(customerDAO.findByUuid(anyString())).thenReturn(customer);

        subject.updateCustomerEmailByUuid(updateCustomerDTO);
        verify(customerValidationHelper).validateCustomer(updateCustomerDTO, customer);
        ArgumentCaptor<Customer> argumentCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerDAO).save(argumentCaptor.capture());

        Customer savedCustomer = argumentCaptor.getValue();
        assertThat(savedCustomer.getEmailAddress(), is("apples@bananas"));
        assertThat(savedCustomer.getFirstName(), is("bobby"));
        assertThat(savedCustomer.getLastName(), is("sally"));
    }

    @Test
    public void updateCustomerNotSavedWhenValidationFails() {
        UpdateCustomerDTO updateCustomerDTO = getUpdateCustomerDTO();
        when(customerDAO.findByUuid(anyString())).thenReturn(new Customer());
        doThrow(new RuntimeException("fail")).when(customerValidationHelper).validateCustomer(eq(updateCustomerDTO), any(Customer.class));

        try {
            subject.updateCustomerEmailByUuid(updateCustomerDTO);
            fail();
        } catch (Exception exception) {
            assertThat(exception, is(notNullValue()));
        }
        verify(customerDAO, times(0)).save(any(Customer.class));
    }

    private UpdateCustomerDTO getUpdateCustomerDTO() {
        UpdateCustomerDTO updateCustomerDTO = new UpdateCustomerDTO();
        updateCustomerDTO.setEmailAddress("apples@bananas");
        updateCustomerDTO.setUuid("blah-blah");
        return updateCustomerDTO;
    }

    private CreateCustomerDTO getCreateCustomerDTO() {
        CreateCustomerDTO createCustomerDTO = new CreateCustomerDTO();
        createCustomerDTO.setEmailAddress("apples@bananas");
        createCustomerDTO.setFirstName("bobby");
        createCustomerDTO.setLastName("sally");
        return createCustomerDTO;
    }
}