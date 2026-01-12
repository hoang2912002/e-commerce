package com.fashion.identity.service;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import com.fashion.identity.dto.request.search.user.UserSearchRequest;
import com.fashion.identity.dto.response.AddressResponse;
import com.fashion.identity.dto.response.PaginationResponse;
import com.fashion.identity.entity.Address;
import com.fashion.identity.entity.User;

public interface AddressService {
    <T> List<T> handleAddressesForUser(User user, List<Address> addresses, Function<Address, T> mapper);
    AddressResponse createAddress(Address address);
    void upSertShopManagementAddress(Address address);
    AddressResponse updateAddress(Address address);
    AddressResponse getAddressById(UUID id);
    PaginationResponse<List<AddressResponse>> getAllAddresses(UserSearchRequest request);
    void deleteAddressById(UUID id);
}
