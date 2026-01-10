package com.fashion.product.service.impls;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.product.common.enums.EnumError;
import com.fashion.product.common.util.PageableUtils;
import com.fashion.product.common.util.SlugUtil;
import com.fashion.product.common.util.SpecificationUtils;
import com.fashion.product.dto.request.search.SearchModel;
import com.fashion.product.dto.request.search.SearchOption;
import com.fashion.product.dto.request.search.SearchRequest;
import com.fashion.product.dto.response.OptionResponse;
import com.fashion.product.dto.response.OptionValueResponse;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.entity.Option;
import com.fashion.product.entity.OptionValue;
import com.fashion.product.exception.ServiceException;
import com.fashion.product.mapper.OptionValueMapper;
import com.fashion.product.repository.OptionRepository;
import com.fashion.product.repository.OptionValueRepository;
import com.fashion.product.service.OptionValueService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OptionValueServiceImpl implements OptionValueService{
    OptionValueRepository optionValueRepository;
    OptionValueMapper optionValueMapper;
    OptionRepository optionRepository;
    
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public OptionValueResponse createOptionValue(OptionValue optionValue){
        log.info("[createOptionValue] start create option value ....");
        try {
            final String slug =  SlugUtil.toSlug(optionValue.getValue());
            this.getRawOptionValueBySlug(slug,null);
            
            Option option = null;
            if(optionValue.getOption().getId() != null){
                option = this.optionRepository.findById(optionValue.getOption().getId()).orElseThrow(
                    () -> new ServiceException(EnumError.PRODUCT_OPTION_ERR_NOT_FOUND_ID, "option.not.found.id",Map.of("id", optionValue.getOption().getId()))
                );
            } else {
                option = new Option();
            }
            final OptionValue createOptionValue = OptionValue.builder()
                .value(optionValue.getValue())
                .slug(slug)
                .activated(true)
                .option(option)
                .build();
            return optionValueMapper.toDto(this.optionValueRepository.saveAndFlush(createOptionValue));
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [createOptionValue] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public OptionValueResponse updateOptionValue(OptionValue optionValue){
        log.info("[updateOptionValue] start update option value ....");
        try {
            final OptionValue updateOptionValue = getRawOptionValueById(optionValue.getId());
            if(updateOptionValue == null){
                throw new ServiceException(EnumError.PRODUCT_OPTION_VALUE_ERR_NOT_FOUND_ID, "option.value.not.found.id",Map.of("id", optionValue.getId()));
            }
            final String slug =  SlugUtil.toSlug(optionValue.getValue());
            this.getRawOptionValueBySlug(slug,optionValue.getId());
            
            Option option = null;
            if(optionValue.getOption().getId() != null){
                option = this.optionRepository.findById(optionValue.getOption().getId()).orElseThrow(
                    () -> new ServiceException(EnumError.PRODUCT_OPTION_ERR_NOT_FOUND_ID, "option.not.found.id",Map.of("id", optionValue.getOption().getId()))
                );
            } else {
                option = new Option();
            }
            updateOptionValue.setValue(optionValue.getValue());
            updateOptionValue.setSlug(slug);
            updateOptionValue.setOption(option);
            updateOptionValue.setActivated(true);
            return optionValueMapper.toDto(this.optionValueRepository.saveAndFlush(updateOptionValue));
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [updateOptionValue] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OptionValue> getRawListOptionValueBySlug(List<String> slugs){
        try {
            return this.optionValueRepository.findAllBySlugIn(slugs);
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getRawListOptionValueBySlug] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OptionValue> getRawListOptionValueById(List<Long> id){
        try {
            return this.optionValueRepository.findAllByIdIn(id);
            
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getRawListOptionValueById] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OptionValueResponse getOptionValueById(Long id){
        try {
            OptionValue optionValue = getRawOptionValueById(id);
            return optionValueMapper.toDto(optionValue);
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getRawListOptionValueById] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<List<OptionValueResponse>> getAllOptionValue(
        SearchRequest request
    ){
        try {
            SearchOption searchOption = request.getSearchOption();
            SearchModel searchModel = request.getSearchModel();

            List<String> allowedField = List.of("createdAt");

            PageRequest pageRequest = PageableUtils.buildPageRequest(
                searchOption.getPage(), 
                searchOption.getSize(), 
                searchOption.getSort(), 
                allowedField, 
                "createdAt", 
                Sort.Direction.DESC
            );
            List<String> fields = SpecificationUtils.getFieldsSearch(OptionValue.class);

            Specification<OptionValue> spec = new SpecificationUtils<OptionValue>()
                .equal("activated", searchModel.getActivated())
                .likeAnyFieldIgnoreCase(searchModel.getQ(), fields)
                .build();

            Page<OptionValue> options = this.optionValueRepository.findAll(spec, pageRequest);
            List<OptionValueResponse> optionResponses = this.optionValueMapper.toDto(options.getContent());
            return PageableUtils.<OptionValue, OptionValueResponse>buildPaginationResponse(pageRequest, options, optionResponses);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getAllOptionValue] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private void getRawOptionValueBySlug(String slug, Long excludeId){
        try {
            Optional<OptionValue> duplicate;
            if (excludeId == null) {
                duplicate = this.optionValueRepository.findBySlug(slug);
            } else {
                duplicate = this.optionValueRepository.findBySlugAndIdNot(slug,excludeId);
            }
            duplicate.ifPresent(cateExist -> {
                throw new ServiceException(EnumError.PRODUCT_OPTION_DATA_EXISTED_SLUG,"option.exist.slug", Map.of("slug", slug));
            });
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getRawOptionValueBySlug] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private OptionValue getRawOptionValueById(Long id){
        try {
            // Optional<OptionValue> opValue = this.optionValueRepository.findById(id);
            // return opValue.isPresent() ? opValue.get() : null;
            return this.optionValueRepository.findById(id).orElseThrow(
                () -> new ServiceException(EnumError.PRODUCT_OPTION_ERR_NOT_FOUND_ID, "option.value.not.found.id",Map.of("id", id))
            );
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getRawOptionValueById] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
}
