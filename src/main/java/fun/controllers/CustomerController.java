package fun.controllers;

import fun.model.dto.CreateCustomerDTO;
import fun.model.dto.CustomerResponseDTO;
import fun.model.Customer;
import fun.model.dto.UpdateCustomerDTO;
import fun.services.CustomerService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;

//TODO create error handler to make error responses pretty and with meaningful status codes
@Controller
@RequestMapping("/api")
public class CustomerController {
    private final static ModelMapper mapper = new ModelMapper();
    @Autowired
    private CustomerService customerService;

    @PostMapping(value = "/customer", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CustomerResponseDTO createCustomer(@Valid @RequestBody CreateCustomerDTO createCustomerDTO) {
        Customer newCustomer = customerService.createCustomerFromDTO(createCustomerDTO);
        return mapper.map(newCustomer, CustomerResponseDTO.class);
    }

    @PutMapping(value = "/customer", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CustomerResponseDTO createCustomer(@Valid @RequestBody UpdateCustomerDTO updateCustomerDTO) {
        Customer newCustomer = customerService.updateCustomerEmailByUuid(updateCustomerDTO);
        return mapper.map(newCustomer, CustomerResponseDTO.class);
    }
}
