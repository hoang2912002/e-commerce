package com.fashion.identity.service.impls;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.identity.common.enums.EnumError;
import com.fashion.identity.dto.request.search.user.UserSearchRequest;
import com.fashion.identity.dto.response.AddressResponse;
import com.fashion.identity.dto.response.PaginationResponse;
import com.fashion.identity.entity.Address;
import com.fashion.identity.entity.User;
import com.fashion.identity.exception.ServiceException;
import com.fashion.identity.mapper.AddressMapper;
import com.fashion.identity.repository.AddressRepository;
import com.fashion.identity.repository.UserRepository;
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
    AddressMapper addressMapper;
    UserRepository userRepository;
    
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

    @Override
    @Transactional(rollbackFor= ServiceException.class)
    public AddressResponse createAddress(Address address) {
        try {
            this.checkExistAddress(address.getUser().getId(),address.getShopManagementId(), address.getAddress(), address.getDistrict(), address.getProvince(), address.getWard(), null, false);
            return this.addressMapper.toDto(this.addressRepository.save(address));
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("IDENTITY-SERVICE: createAddress: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(rollbackFor= ServiceException.class)
    public AddressResponse updateAddress(Address address) {
        try {
            this.checkExistAddress(address.getUser().getId(),address.getShopManagementId(), address.getAddress(), address.getDistrict(), address.getProvince(), address.getWard(), address.getId(), false);
            User user = this.userRepository.findById(address.getUser().getId()).orElseThrow(
                () -> new ServiceException(EnumError.IDENTITY_USER_ERR_NOT_FOUND_ID,"user.not.found.id", Map.of("user.id", address.getUser().getId()))
            );
            Address uAddress = this.addressRepository.lockAddressById(address.getId());
            this.changeField(uAddress,address);
            uAddress.setUser(user);
            return this.addressMapper.toDto(this.addressRepository.saveAndFlush(uAddress));
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("IDENTITY-SERVICE: updateAddress: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddressById(UUID id) {
        try {
            Address address = this.addressRepository.findById(id).orElseThrow(
                () -> new ServiceException(EnumError.IDENTITY_ADDRESS_ERR_NOT_FOUND_ID, "address.not.found.id",Map.of("id", id))
            );
            return this.addressMapper.toDto(address);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("IDENTITY-SERVICE: getAddressById: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void upSertShopManagementAddress(Address address) {
        try {
            this.checkExistAddress(null,address.getShopManagementId(), address.getAddress(), address.getDistrict(), address.getProvince(), address.getWard(), null, true);
            Address save = this.addressRepository.lockAddressByShopId(address.getShopManagementId());
            if(Objects.isNull(save)){
                save = new Address();
            }
            this.changeField(save,address);
            save.setUser(null);
            this.addressRepository.save(save);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("IDENTITY-SERVICE: upSertShopManagementAddress: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.IDENTITY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public PaginationResponse<List<AddressResponse>> getAllAddresses(UserSearchRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllAddresses'");
    }

    @Override
    public void deleteAddressById(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAddressById'");
    }

    private void checkExistAddress(UUID userId, UUID smId, String address, String district, String province, String ward, UUID excludeId, boolean fromProductService){
        if (userId != null && smId != null) {
            throw new ServiceException(EnumError.IDENTITY_ADDRESS_INVALID_OWNER, "address.cannot.have.both.owners");
        }
        if (userId == null && smId == null) {
            throw new ServiceException(EnumError.IDENTITY_ADDRESS_INVALID_OWNER, "address.must.have.owner");
        }
        Optional<Address> duplicate;
    
        if (excludeId == null) {
            duplicate = this.addressRepository.findDuplicateForCreate(userId, smId, address, district, province, ward);
        } else {
            duplicate = this.addressRepository.findDuplicateForUpdate(userId, smId, address, district, province, ward, excludeId);
        }

        duplicate.ifPresent(user -> {
            if(!fromProductService){
                throw new ServiceException(EnumError.IDENTITY_ADDRESS_DATA_EXISTED,"user.exist.address.ward.district.province", Map.of(
                    "address", address,
                    "ward", ward,
                    "district", district,
                    "province", province
                ));
            }
        });
    }

    private void updateAddressData(Address target, Address source) {
        target.setAddress(source.getAddress());
        target.setProvince(source.getProvince());
        target.setDistrict(source.getDistrict());
        target.setWard(source.getWard());
    }

    private void changeField(Address target, Address source){
        target.setAddress(source.getAddress());
        target.setDistrict(source.getDistrict());
        target.setProvince(source.getProvince());
        target.setWard(source.getWard());
        target.setShopManagementId(source.getShopManagementId());
        target.setCurrentUserAddress(source.getCurrentUserAddress());
        target.setActivated(true);
    }
}
