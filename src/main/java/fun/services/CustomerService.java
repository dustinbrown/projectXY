package fun.services;

import fun.model.dto.CreateCustomerDTO;
import fun.model.Customer;
import fun.model.dao.CustomerDAO;
import fun.model.dto.UpdateCustomerDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {
    private final static ModelMapper mapper = new ModelMapper();

    @Autowired
    private CustomerDAO customerDAO;

    @Autowired
    private CustomerValidationHelper customerValidationHelper;

    @Transactional
    public Customer createCustomerFromDTO(CreateCustomerDTO createCustomerDTO) {
        customerValidationHelper.validateUserExistsByEmail(createCustomerDTO.getEmailAddress());
        Customer newCustomer = mapper.map(createCustomerDTO, Customer.class);
        return customerDAO.save(newCustomer);
    }

    @Transactional
    public Customer updateCustomerEmailByUuid(UpdateCustomerDTO updateCustomerDTO) {
        Customer existingCustomer = customerDAO.findByUuid(updateCustomerDTO.getUuid());
        customerValidationHelper.validateCustomer(updateCustomerDTO, existingCustomer);

        existingCustomer.setEmailAddress(updateCustomerDTO.getEmailAddress());
        return customerDAO.save(existingCustomer);
    }

}
