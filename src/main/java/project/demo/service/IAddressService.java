package project.demo.service;

import java.util.List;

import project.demo.model.Address;

public interface IAddressService {
    Address findById(Integer addressId);
    List<Address> findByCustomerId(Integer customerId);
    Address save(Address address);
    Address update(Address address);
    void delete(Integer addressId);
    Address setAsDefault(Integer addressId, Integer customerId);
}
