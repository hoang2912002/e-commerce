package com.fashion.identity.service.impls;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.identity.common.enums.EnumError;
import com.fashion.identity.entity.Address;
import com.fashion.identity.entity.User;
import com.fashion.identity.exception.ServiceException;
import com.fashion.identity.repository.AddressRepository;
import com.fashion.identity.service.AddressService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AddressServiceImpl implements AddressService{
    AddressRepository addressRepository;

    @Override
    @Transactional(rollbackFor= ServiceException.class)
    public <T> List<T> handleAddressesForUser(
        User updateUser, 
        List<Address> inputAddresses,
        Function<Address, T> mapper
    ) {
        try {
            final List<Address> save = new ArrayList<>();
            if (inputAddresses == null)
                return new ArrayList<>();

            for (Address addr : inputAddresses) {
                Address currentAddress;

                if (addr.getId() != null) {
                    currentAddress = this.addressRepository.findById(addr.getId()).orElseGet(
                        () -> new Address()
                    );
                } else {
                    currentAddress = new Address();
                }
                updateAddressData(currentAddress, addr);
                currentAddress.setActivated(true);
                currentAddress.setUser(updateUser);
                save.add(currentAddress);
            }
            List<T> result = this.addressRepository.saveAllAndFlush(save).stream().map(a ->
                mapper.apply(a)
            ).toList();
            return result;
        } catch (ServiceException e) {
            throw e;
        }
        catch (Exception e) {
            log.error("IDENTITY-SERVICE: handleAddressesForUser: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private void updateAddressData(Address target, Address source) {
        target.setAddress(source.getAddress());
        target.setProvince(source.getProvince());
        target.setDistrict(source.getDistrict());
        target.setWard(source.getWard());
    }
}
