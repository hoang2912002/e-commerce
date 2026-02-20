package com.fashion.shipping.service.strategy.impl;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.fashion.shipping.common.enums.EnumError;
import com.fashion.shipping.common.enums.ShippingProvider;
import com.fashion.shipping.common.util.AsyncUtils;
import com.fashion.shipping.common.util.FormatTime;
import com.fashion.shipping.common.util.NormalizeString;
import com.fashion.shipping.dto.request.ghn.GhnEstimateTimeRequest;
import com.fashion.shipping.dto.request.ghn.GhnOrderRequest;
import com.fashion.shipping.dto.request.ghn.GhnShippingFeeRequest;
import com.fashion.shipping.dto.response.AddressResponse.InnerAddressResponse;
import com.fashion.shipping.dto.response.ShippingResponse.InnerInternalShippingResponse;
import com.fashion.shipping.dto.response.ShippingResponse.InnerTempShippingFeeResponse;
import com.fashion.shipping.dto.response.ghn.GhnOrderResponse;
import com.fashion.shipping.dto.response.ghn.GhnProvinceResponse;
import com.fashion.shipping.dto.response.ghn.GhmDistrictResponse;
import com.fashion.shipping.dto.response.ghn.GhnAddressIdResponse;
import com.fashion.shipping.dto.response.ghn.GhnAddressIdResponse.WarmResultGhn;
import com.fashion.shipping.dto.response.ghn.GhmDistrictResponse.APIResponseGhnDistrict;
import com.fashion.shipping.dto.response.ghn.GhnEstimateTimeResponse;
import com.fashion.shipping.dto.response.ghn.GhnEstimateTimeResponse.APIResponseGhnLeadTime;
import com.fashion.shipping.dto.response.ghn.GhnProvinceResponse.APIResponseGhnProvince;
import com.fashion.shipping.dto.response.ghn.GhnShippingFeeResponse.APIResponseGhnShippingFee;
import com.fashion.shipping.dto.response.ghn.GhnWardResponse;
import com.fashion.shipping.dto.response.ghn.GhnWardResponse.APIResponseGhnWard;
import com.fashion.shipping.exception.ServiceException;
import com.fashion.shipping.integration.ShippingGhnClient;
import com.fashion.shipping.properties.ShippingGhnProperties;
import com.fashion.shipping.properties.cache.ShippingServiceGhnCacheProperties;
import com.fashion.shipping.service.provider.CacheProvider;
import com.fashion.shipping.service.strategy.ShippingStrategy;
import com.fashion.shipping.service.strategy.ThirdPartyAddressKey;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class ShippingGhnStrategy implements ShippingStrategy{
    final ShippingGhnClient shippingGhnClient;
    final Executor virtualExecutor;
    final ShippingGhnProperties shippingGhnProperties;
    final ShippingServiceGhnCacheProperties shippingServiceGhnCacheProperties;

    /**
     * @implNote StringRedisTemplate mặc định dùng StringRedisSerializer -> lưu thuần text
     * @implSpec RedisTemplate mặc định dùng JdkSerializationRedisSerializer -> khi lưu ký tự khác chuỗi sẽ tự mã hóa -> khó đọc
     */
    final StringRedisTemplate stringRedisTemplate;
    final CacheProvider cacheProvider;
    
    @Override
    public InnerTempShippingFeeResponse shippingFee(InnerInternalShippingResponse req) {
        if (req.getDistrict() == null || req.getWard() == null || req.getProvince() == null) {
            return null;
        }

        Long startTime = System.currentTimeMillis();
        try {
            GhnAddressIdResponse addressIds = getAddressIdsFromRedisHash(req);

            if (addressIds == null) {
                log.warn("SHIPPING: Address not found in Redis Hash, using API fallback");
                return shippingFeeWithApiFallback(req);
            }

            String feeLeadTimeKey = buildFeeLeadTimeKey(
                addressIds.getDistrictId(), 
                addressIds.getWardCode()
            );
            String lockKey = buildFeeLeadTimeLockKey(
                addressIds.getDistrictId(), 
                addressIds.getWardCode()
            );

            InnerTempShippingFeeResponse result = cacheProvider.getDataResponse(
                feeLeadTimeKey,
                lockKey,
                req.getVersion(),
                InnerTempShippingFeeResponse.class,
                () -> calculateShippingFee(addressIds.getDistrictId(), addressIds.getWardCode())
            );

            long duration = System.currentTimeMillis() - startTime;
            log.info("SHIPPING: Completed in {}ms", duration);

        return result;   
        } catch (Exception e) {
            throw new ServiceException(
                EnumError.SHIPPING_INTERNAL_ERROR_CALL_API,
                "server.error.internal"
            );
        }
    }

    @Override
    public <GhnOrderRequest,GhnOrderResponse> GhnOrderResponse createOrder(GhnOrderRequest ghnOrderRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createOrder'");
    }

    @Override
    public String getProviderName() {
        return ShippingProvider.GHN.toString();
    }

    @Override
    public ThirdPartyAddressKey cacheThirdPartyAddressKey() {
        return new ThirdPartyAddressKey() {
            // private final ShippingServiceGhnCacheProperties shippingServiceCacheProperties = new ShippingServiceGhnCacheProperties();
            @Override
            public String getProvince(String provinceName) {
                return shippingServiceGhnCacheProperties.createCacheKeyNotPrefix(
                    shippingServiceGhnCacheProperties.getKeys().getShippingThirdPartyGhnDistrict(),
                    provinceName
                );
            }

            @Override
            public String getDistrict(Long provinceNo) {
                return shippingServiceGhnCacheProperties.createCacheKeyNotPrefix(
                    shippingServiceGhnCacheProperties.getKeys().getShippingThirdPartyGhnDistrict(),
                    provinceNo
                );
            }

            @Override
            public String getWard(Long districtNo) {
                return shippingServiceGhnCacheProperties.createCacheKeyNotPrefix(
                    shippingServiceGhnCacheProperties.getKeys().getShippingThirdPartyGhnWard(),
                    districtNo
                );
            }
            
        };
    }

    @Override
    public ThirdPartyAddressKey lockThirdPartyAddressKey() {
        return new ThirdPartyAddressKey() {
            @Override
            public String getProvince(String provinceName) {
                return shippingServiceGhnCacheProperties.createLockKey(
                    shippingServiceGhnCacheProperties.getKeys().getShippingThirdPartyGhnProvince(),
                    provinceName
                );
            }

            @Override
            public String getDistrict(Long provinceNo) {
                return shippingServiceGhnCacheProperties.createLockKey(
                    shippingServiceGhnCacheProperties.getKeys().getShippingThirdPartyGhnDistrict(),
                    provinceNo
                );
            }

            @Override
            public String getWard(Long districtNo) {
                return shippingServiceGhnCacheProperties.createLockKey(
                    shippingServiceGhnCacheProperties.getKeys().getShippingThirdPartyGhnWard(),
                    districtNo
                );
            }
            
        };
    }

    @Override
    // @EventListener(ApplicationReadyEvent.class)
    @Async("virtualExecutor")
    public void warmAddressCache(){
        try {
            // String token = shippingGhnProperties.getApiTokenDev();
            String token = shippingGhnProperties.getApiTokenProduction();
            APIResponseGhnProvince provinceResponse = this.shippingGhnClient.getThirdPartyProvince(token);

            if (provinceResponse == null || provinceResponse.getCode() != 200 || provinceResponse.getData() == null) {
                log.error("GHN: Failed to fetch provinces - Invalid response");
                return;
            }

            List<GhnProvinceResponse> provinces = provinceResponse.getData();
            if (provinces.isEmpty()) {
                log.warn("GHN: No provinces returned from API");
                return;
            }

            // Cache provinces
            int provinceCount = cacheProvinces(provinces);
            log.info("GHN: Cached {} provinces", provinceCount);

            // ✅ STEP 2: Cache districts and wards
            int totalDistricts = 0;
            int totalWards = 0;
            int failedDistricts = 0;

            for (GhnProvinceResponse province : provinces) {
                try {
                    WarmResultGhn result = cacheDistrictsAndWards(province, token);
                    totalDistricts += result.getDistrictCount();
                    totalWards += result.getWardCount();
                    failedDistricts += result.getFailedCount();

                    // Rate limiting - avoid overwhelming GHN API
                    // Thread.sleep(100);

                } catch (Exception e) {
                    log.warn("GHN: Failed to cache for province {} ({}): {}", 
                        province.getProvinceId(), 
                        province.getProvinceName(),
                        e.getMessage());
                    failedDistricts++;
                }
            }

            log.info("GHN: Cache warming completed - Provinces: {}, Districts: {}, Wards: {}, Failed: {}", 
                provinceCount, totalDistricts, totalWards, failedDistricts);

        } catch (Exception e) {
            throw new ServiceException(
                EnumError.SHIPPING_INTERNAL_ERROR_CALL_API,
                "server.error.internal"
            );
        }
    }

    private int cacheProvinces(List<GhnProvinceResponse> provinces) {
        Map<String, String> provinceMap = new HashMap<>();
        int count = 0;

        for (GhnProvinceResponse province : provinces) {
            // ✅ Null safety
            if (province == null || province.getProvinceName() == null || province.getProvinceId() == null) {
                log.warn("GHN: Skipping invalid province: {}", province);
                continue;
            }

            String normalizedName = NormalizeString.toNormalize(province.getProvinceName());
            if (normalizedName == null || normalizedName.isEmpty()) {
                log.warn("GHN: Skipping province with empty normalized name: {}", 
                    province.getProvinceName());
                continue;
            }

            provinceMap.put(normalizedName, province.getProvinceId().toString());

            // Cache name extensions (aliases)
            if (province.getNameExtension() != null && !province.getNameExtension().isEmpty()) {
                province.getNameExtension().stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(ext -> !ext.isEmpty())
                    .forEach(ext -> {
                        String normalizedExt = NormalizeString.toNormalize(ext);
                        if (normalizedExt != null && !normalizedExt.isEmpty()) {
                            provinceMap.put(normalizedExt, province.getProvinceId().toString());
                        }
                    });
            }

            count++;
        }

        // Batch write to Redis
        if (!provinceMap.isEmpty()) {
            String provinceHashKey = this.shippingServiceGhnCacheProperties.getKeys().getShippingThirdPartyGhnProvince();
            stringRedisTemplate.opsForHash().putAll(provinceHashKey, provinceMap);
            log.debug("GHN: Written {} province mappings to Redis", provinceMap.size());
        }

        return count;
    }

    /**
     * ✅ Cache districts and wards for a province
     */
    private WarmResultGhn cacheDistrictsAndWards(GhnProvinceResponse province, String token) {
        WarmResultGhn result = new WarmResultGhn();

        try {
            // Fetch districts
            APIResponseGhnDistrict districtResponse = shippingGhnClient.getThirdPartyDistrict(
                token,
                Map.of("province_id", province.getProvinceId())
            );

            // ✅ Null safety check
            if (districtResponse == null || 
                districtResponse.getCode() != 200 || 
                districtResponse.getData() == null ||
                districtResponse.getData().isEmpty()) {
                
                log.debug("GHN: No districts for province {} ({})", 
                    province.getProvinceId(), province.getProvinceName());
                return result;
            }

            List<GhmDistrictResponse> districts = districtResponse.getData();
            Map<String, String> districtMap = new HashMap<>();
            String districtHashKey = this.cacheThirdPartyAddressKey().getDistrict(province.getProvinceId());

            for (GhmDistrictResponse district : districts) {
                if (district == null || 
                    district.getDistrictName() == null || 
                    district.getDistrictId() == null) {
                    log.debug("GHN: Skipping invalid district in province {}", 
                        province.getProvinceId());
                    continue;
                }

                String normalizedName = NormalizeString.toNormalize(district.getDistrictName());
                if (normalizedName != null && !normalizedName.isEmpty()) {
                    districtMap.put(normalizedName, district.getDistrictId().toString());
                }

                // Cache name extensions
                if (district.getNameExtension() != null && !district.getNameExtension().isEmpty()) {
                    district.getNameExtension().stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(ext -> !ext.isEmpty())
                        .forEach(ext -> {
                            String normalizedExt = NormalizeString.toNormalize(ext);
                            if (normalizedExt != null && !normalizedExt.isEmpty()) {
                                districtMap.put(normalizedExt, district.getDistrictId().toString());
                            }
                        });
                }

                result.setDistrictCount(result.getDistrictCount() + 1);

                // ✅ Cache wards for this district (with error handling)
                try {
                    Integer wardCount = cacheWardsForDistrict(district.getDistrictId(), token);
                    result.setWardCount(result.getWardCount() + wardCount);
                } catch (Exception e) {
                    log.debug("GHN: Failed to cache wards for district {} ({}): {}", 
                        district.getDistrictId(), 
                        district.getDistrictName(),
                        e.getMessage());
                    result.setFailedCount(result.getFailedCount() + 1);
                }
            }

            // Batch write districts
            if (!districtMap.isEmpty()) {
                stringRedisTemplate.opsForHash().putAll(districtHashKey, districtMap);
                log.debug("GHN: Written {} districts for province {}", 
                    districtMap.size(), province.getProvinceId());
            }

        } catch (Exception e) {
            log.warn("GHN: Error caching districts for province {}: {}", 
                province.getProvinceId(), e.getMessage());
            result.setFailedCount(result.getFailedCount() + 1);
        }

        return result;
    }

    /**
     * ✅ FIXED: Cache wards with proper null safety
     */
    private int cacheWardsForDistrict(Long districtId, String token) {
        try {
            APIResponseGhnWard wardResponse = shippingGhnClient.getThirdPartyWard(
                token,
                Map.of("district_id", districtId)
            );

            // ✅ NULL SAFETY CHECK - This was the bug!
            if (wardResponse == null || 
                wardResponse.getCode() != 200 || 
                wardResponse.getData() == null ||
                wardResponse.getData().isEmpty()) {
                
                log.debug("GHN: No wards for district {}", districtId);
                return 0;
            }

            List<GhnWardResponse> wards = wardResponse.getData();
            Map<String, String> wardMap = new HashMap<>();
            String wardHashKey = this.cacheThirdPartyAddressKey().getWard(districtId);
            int count = 0;

            for (GhnWardResponse ward : wards) {
                // ✅ Null safety
                if (ward == null || ward.getWardName() == null || ward.getWardCode() == null) {
                    log.debug("GHN: Skipping invalid ward in district {}", districtId);
                    continue;
                }

                String normalizedName = NormalizeString.toNormalize(ward.getWardName());
                if (normalizedName != null && !normalizedName.isEmpty()) {
                    wardMap.put(normalizedName, ward.getWardCode());
                }

                // Cache name extensions
                if (ward.getNameExtension() != null && !ward.getNameExtension().isEmpty()) {
                    ward.getNameExtension().stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(ext -> !ext.isEmpty())
                        .forEach(ext -> {
                            String normalizedExt = NormalizeString.toNormalize(ext);
                            if (normalizedExt != null && !normalizedExt.isEmpty()) {
                                wardMap.put(normalizedExt, ward.getWardCode());
                            }
                        });
                }

                count++;
            }

            // Batch write wards
            if (!wardMap.isEmpty()) {
                stringRedisTemplate.opsForHash().putAll(wardHashKey, wardMap);
                log.debug("GHN: Written {} wards for district {}", wardMap.size(), districtId);
            }

            return count;

        } catch (Exception e) {
            log.debug("GHN: Error caching wards for district {}: {}", districtId, e.getMessage());
            return 0;
        }
    }

    private String buildFeeLeadTimeKey(Long districtId, String wardCode) {
        return shippingServiceGhnCacheProperties.createCacheKey(
            shippingServiceGhnCacheProperties.getKeys().getShippingThirdPartyGhnFeeLeadTime(),
            districtId + ":" + wardCode
        );
    }

    private String buildFeeLeadTimeLockKey(Long districtId, String wardCode) {
        return shippingServiceGhnCacheProperties.createLockKey(
            shippingServiceGhnCacheProperties.getKeys().getShippingThirdPartyGhnFeeLeadTime(),
            districtId + ":" + wardCode
        );
    }

    private InnerTempShippingFeeResponse calculateShippingFee(
        Long districtId,
        String wardCode
    ){
        // String token = shippingGhnProperties.getApiTokenDev();
        String token = shippingGhnProperties.getApiTokenProduction();
        // Long shopId = shippingGhnProperties.getShopIdDev();
        Long shopId = shippingGhnProperties.getShopIdProduction();
        Long shopDistrictId = shippingGhnProperties.getShopAddressDistrictId();
        String shopWardCode = shippingGhnProperties.getShopAddressWardId();
        Long shopProvinceId = shippingGhnProperties.getShopAddressProvinceId();

        GhnShippingFeeRequest feeReq = GhnShippingFeeRequest.builder()
            .service_type_id(2)
            .from_district_id(shopDistrictId)
            .from_ward_code(shopWardCode)
            .to_district_id(districtId)
            .to_ward_code(wardCode)
            .length(30)
            .width(40)
            .height(30)
            .weight(4000)
            .build();

        GhnEstimateTimeRequest leadTimeReq = GhnEstimateTimeRequest.builder()
            .service_type_id(2)
            .from_district_id(shopDistrictId)
            .from_ward_code(shopWardCode)
            .to_district_id(districtId)
            .to_ward_code(wardCode)
            .build();
        
        CompletableFuture<BigDecimal> feeFuture = AsyncUtils.fetchAsyncWThread(
            () -> getShippingFee(feeReq, token, shopId),
            virtualExecutor
        );

        CompletableFuture<LocalDateTime> leadTimeFuture = AsyncUtils.fetchAsyncWThread(
            () -> getLeadTime(leadTimeReq, token, shopId),
            virtualExecutor
        );

        CompletableFuture.allOf(feeFuture, leadTimeFuture).join();
        InnerTempShippingFeeResponse addResponse = InnerTempShippingFeeResponse.builder()
            .provider(ShippingProvider.GHN)
            .shippingFee(feeFuture.join())
            .estimatedDate(leadTimeFuture.join())
            .build();
        addResponse.innerTempShippingFeeResponse(System.currentTimeMillis());
        return addResponse;
    }

    
    private InnerTempShippingFeeResponse shippingFeeWithApiFallback(InnerInternalShippingResponse req) {
        // String token = shippingGhnProperties.getApiTokenDev();
        String token = shippingGhnProperties.getApiTokenProduction();

        // Call APIs sequentially
        GhnProvinceResponse province = getProvince(req.getProvince(), token);
        if (province == null) return null;

        GhmDistrictResponse district = getDistrict(
            province.getProvinceId(), 
            req.getDistrict(), 
            token
        );
        if (district == null) return null;

        GhnWardResponse ward = getWard(
            district.getDistrictId(), 
            req.getWard(), 
            token
        );
        if (ward == null) return null;

        // ✅ Cache to Redis Hash for next time
        cacheAddressToRedisHash(req, province, district, ward);

        // Calculate fee + lead time
        return calculateShippingFee(district.getDistrictId(), ward.getWardCode());
    }

    private GhnAddressIdResponse getAddressIdsFromRedisHash(InnerInternalShippingResponse req) {
        try {
            // Normalize names
            String normalizedProvince = NormalizeString.toNormalize(req.getProvince());
            String normalizedDistrict = NormalizeString.toNormalize(req.getDistrict());
            String normalizedWard = NormalizeString.toNormalize(req.getWard());

            // ✅ Get Province ID from Redis Hash (1ms)
            String provinceHashKey = this.shippingServiceGhnCacheProperties.getKeys().getShippingThirdPartyGhnProvince();
            String provinceIdStr = (String) stringRedisTemplate.opsForHash()
                .get(provinceHashKey, normalizedProvince);
            
            if (provinceIdStr == null) {
                log.debug("SHIPPING: Province '{}' not in Redis Hash", normalizedProvince);
                return null;
            }

            Long provinceId = Long.parseLong(provinceIdStr);
            String districtHashKey = this.cacheThirdPartyAddressKey().getDistrict(provinceId);
            String districtIdStr = (String) stringRedisTemplate.opsForHash()
                .get(districtHashKey, normalizedDistrict);
            
            if (districtIdStr == null) {
                log.debug("SHIPPING: District '{}' not in Redis Hash", normalizedDistrict);
                return null;
            }
            Long districtId = Long.parseLong(districtIdStr);

            String wardHashKey = this.cacheThirdPartyAddressKey().getWard(districtId);
            String wardCode = (String) stringRedisTemplate.opsForHash()
                .get(wardHashKey, normalizedWard);
            
            if (wardCode == null) {
                log.debug("SHIPPING: Ward '{}' not in Redis Hash", normalizedWard);
                return null;
            }

            log.debug("SHIPPING: Got address IDs from Redis Hash - Province: {}, District: {}, Ward: {}", 
                provinceId, districtId, wardCode);

            return GhnAddressIdResponse.builder().provinceId(provinceId).districtId(districtId).wardCode(wardCode).build();
        } catch (Exception e) {
            log.error("SHIPPING: Error getting address IDs from Redis Hash", e);
            return null;
        }
    }

    private InnerTempShippingFeeResponse getCachedFeeLeadTime(String key) {
        try {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (json == null) return null;

            // Parse JSON to object
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, InnerTempShippingFeeResponse.class);
        } catch (Exception e) {
            log.warn("SHIPPING: Error getting cached fee/leadtime", e);
            return null;
        }
    }
    
    private void cacheAddressToRedisHash(
            InnerInternalShippingResponse req,
            GhnProvinceResponse province,
            GhmDistrictResponse district,
            GhnWardResponse ward) {
        
        CompletableFuture.runAsync(() -> {
            try {
                String normalizedProvince = NormalizeString.toNormalize(req.getProvince());
                String normalizedDistrict = NormalizeString.toNormalize(req.getDistrict());
                String normalizedWard = NormalizeString.toNormalize(req.getWard());

                // Cache province
                stringRedisTemplate.opsForHash().put(
                    shippingServiceGhnCacheProperties.getKeys().getShippingThirdPartyGhnProvince(),
                    normalizedProvince,
                    province.getProvinceId().toString()
                );

                // Cache district
                String districtHashKey = shippingServiceGhnCacheProperties.getKeys().getShippingThirdPartyGhnDistrict() + province.getProvinceId();
                stringRedisTemplate.opsForHash().put(
                    districtHashKey,
                    normalizedDistrict,
                    district.getDistrictId().toString()
                );

                // Cache ward
                String wardHashKey = shippingServiceGhnCacheProperties.getKeys().getShippingThirdPartyGhnWard() + district.getDistrictId();
                stringRedisTemplate.opsForHash().put(
                    wardHashKey,
                    normalizedWard,
                    ward.getWardCode()
                );

                log.info("SHIPPING: Cached address mapping to Redis Hash");
            } catch (Exception e) {
                log.error("SHIPPING: Failed to cache address", e);
            }
        }, virtualExecutor);
    }

     private void cacheFeeLeadTime(String key, InnerTempShippingFeeResponse response) {
        CompletableFuture.runAsync(() -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(response);
                
                stringRedisTemplate.opsForValue().set(key, json, 
                    Duration.ofMinutes(30));
                
                log.debug("SHIPPING: Cached fee/leadtime");
            } catch (Exception e) {
                log.error("SHIPPING: Failed to cache fee/leadtime", e);
            }
        }, virtualExecutor);
    }

    /**
     * 
     * @param provinceName
     * @param token
     * @return call third party
     */

    private GhnProvinceResponse getProvince(String provinceName, String token) {
        try {
            // Gọi Feign Client (Chỗ này sẽ block Virtual Thread, nhưng không sao vì VT rất rẻ)
            APIResponseGhnProvince province = this.shippingGhnClient.getThirdPartyProvince(token);

            if (province != null && province.getCode() == 200 && province.getData() != null) {
                return province.getData().stream()
                    .filter(p -> p.getProvinceName().contains(provinceName) 
                            || (p.getNameExtension() != null && p.getNameExtension().contains(provinceName)))
                    .findFirst()
                    .orElse(null);
            } else {
                throw new ServiceException(EnumError.SHIPPING_SHIPPING_CALL_THIRD_API_ERROR, "GHN API returned error");
            }
        } catch (Exception e) {
            log.error("[getProvince] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.SHIPPING_INTERNAL_ERROR_CALL_API, "sys.internal.error");
        }
    }
    
    /**
     * 
     * @param province_id
     * @param districtName
     * @param token
     * @return call third party
     */
    private GhmDistrictResponse getDistrict(Long province_id, String districtName, String token) {
        try {
            // Gọi Feign Client (Chỗ này sẽ block Virtual Thread, nhưng không sao vì VT rất rẻ)
            APIResponseGhnDistrict district = this.shippingGhnClient.getThirdPartyDistrict(token,Map.of("province_id", province_id));

            if(district != null && district.getCode() == 200 && district.getData() != null){
                return district.getData().stream()
                    .filter(p -> p.getProvinceId().equals(province_id) && (p.getDistrictName().contains(districtName) || (p.getNameExtension() != null && p.getNameExtension().contains(districtName))))
                    .findFirst()
                    .orElse(null);
            } else {
                throw new ServiceException(EnumError.SHIPPING_SHIPPING_CALL_THIRD_API_ERROR, "GHN API returned error");
            }
        } catch (Exception e) {
            log.error("[getDistrict] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.SHIPPING_INTERNAL_ERROR_CALL_API, "sys.internal.error");
        }
    }

    /**
     * 
     * @param districtId
     * @param wardName
     * @param token
     * @return call third party
     */
    private GhnWardResponse getWard(Long districtId, String wardName, String token) {
        try {
            APIResponseGhnWard ward = this.shippingGhnClient.getThirdPartyWard(token,Map.of("district_id", districtId));

            if(ward != null && ward.getCode() == 200 && ward.getData() != null){
                return ward.getData().stream().filter(p -> p.getDistrictId().equals(districtId) && (p.getWardName().contains(wardName) || (p.getNameExtension() != null && p.getNameExtension().contains(wardName))))
                    .findFirst()
                    .orElse(null);
            } else {
                throw new ServiceException(EnumError.SHIPPING_SHIPPING_CALL_THIRD_API_ERROR, "GHN API returned error");
            }
        } catch (Exception e) {
            log.error("[getWard] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.SHIPPING_INTERNAL_ERROR_CALL_API, "sys.internal.error");
        }
    }

    /**
     * 
     * @param shippingFeeRequest
     * @param shopId
     * @param token
     * @return call third party
     */
    private BigDecimal getShippingFee(GhnShippingFeeRequest shippingFeeRequest, String token, Long shopId){
        try {
            Map<String, Object> requestBody = toMap(shippingFeeRequest);
            APIResponseGhnShippingFee shippingFee = this.shippingGhnClient.getThirdPartyShippingFee(token, shopId, requestBody);
            if(shippingFee != null && shippingFee.getCode() == 200 && shippingFee.getData() != null){
                BigDecimal fee = shippingFee.getData().getServiceFee();
                return fee;
            }
            else{
                return null;
            }
        } catch (Exception e) {
            log.error("[getShippingFee] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.SHIPPING_INTERNAL_ERROR_CALL_API, "sys.internal.error");
        }
    }

    /**
     * 
     * @param estimateTimeRequest
     * @param shopId
     * @param token
     * @return call third party
     */
    private LocalDateTime getLeadTime(GhnEstimateTimeRequest estimateTimeRequest, String token, Long shopId){
        try {
            Map<String, Object> requestBody = toMap(estimateTimeRequest);
            APIResponseGhnLeadTime leadTime = this.shippingGhnClient.getThirdPartyLeadTime(token, shopId, requestBody);
            if(leadTime != null && leadTime.getCode() == 200 && leadTime.getData() != null){
                GhnEstimateTimeResponse lT = leadTime.getData();
                return FormatTime.formatUnixToLocalDateTime(lT.getLeadtime());
            }
            else{
                return null;
            }
        } catch (Exception e) {
            log.error("[getLeadTime] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.SHIPPING_INTERNAL_ERROR_CALL_API, "sys.internal.error");
        }

        
    }

    private Map<String, Object> toMap(Object obj) {
        Map<String, Object> map = new HashMap<>();
        try {
            for (Field field : obj.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                map.put(field.getName(), field.get(obj));
            }
        } catch (Exception e) {
            log.error("toMap", e.getMessage(), e);
            throw new ServiceException(EnumError.SHIPPING_INTERNAL_ERROR_CALL_API, "sys.internal.error");
        }
        return map;
    }
}
