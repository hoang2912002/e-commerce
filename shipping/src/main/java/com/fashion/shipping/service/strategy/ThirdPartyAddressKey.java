package com.fashion.shipping.service.strategy;

public interface ThirdPartyAddressKey {
    String getProvince(String provinceName);
    String getDistrict(Long provinceNo);
    String getWard(Long districtNo);
}
