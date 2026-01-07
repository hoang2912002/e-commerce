package com.fashion.identity.service;

import java.util.List;
import java.util.function.Function;

import com.fashion.identity.entity.Address;
import com.fashion.identity.entity.User;

public interface AddressService {
    <T> List<T> handleAddressesForUser(User user, List<Address> addresses, Function<Address, T> mapper);
}
