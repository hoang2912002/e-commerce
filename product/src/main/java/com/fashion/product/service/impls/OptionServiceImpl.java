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
import com.fashion.product.dto.response.CategoryResponse;
import com.fashion.product.dto.response.OptionResponse;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.entity.Category;
import com.fashion.product.entity.Option;
import com.fashion.product.exception.ServiceException;
import com.fashion.product.mapper.OptionMapper;
import com.fashion.product.repository.OptionRepository;
import com.fashion.product.service.OptionService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OptionServiceImpl implements OptionService{
    OptionRepository optionRepository;
    OptionMapper optionMapper;
    
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public OptionResponse createOption(Option option){
        log.info("PRODUCT-SERVICE: [createOption] start create option ....");
        try {
            final String slug = SlugUtil.toSlug(option.getName());
            this.getRawOptionBySlug(slug,null);
            final Option createOption = Option.builder()
            .name(option.getName())
            .slug(slug)
            .activated(true)
            .build();
            return optionMapper.toDto(this.optionRepository.saveAndFlush(createOption));   
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [createOption] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public OptionResponse updateOption(Option option){
        log.info("PRODUCT-SERVICE: [updateOption] start update option ....");
        try {
            Option updateOption = getRawOptionById(option.getId());
            if(updateOption == null){
                throw new ServiceException(EnumError.PRODUCT_OPTION_ERR_NOT_FOUND_ID, "option.not.found.id",Map.of("id", option.getId()));
            }
            String slug = SlugUtil.toSlug(option.getName());
            
            this.getRawOptionBySlug(slug,option.getId());

            updateOption.setSlug(slug);
            updateOption.setName(option.getName());
            updateOption.setActivated(true);
            updateOption = this.optionRepository.saveAndFlush(updateOption);
            return optionMapper.toDto(updateOption);   
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [updateOption] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Option getRawOptionById(Long id){
        try {
            Optional<Option> opValue = this.optionRepository.findById(id);
            return opValue.isPresent() ? opValue.get() : null;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getRawOptionById] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OptionResponse getOptionById(Long id){
        try {
            Option option = getRawOptionById(id);
            if(option == null){
                throw new ServiceException(EnumError.PRODUCT_OPTION_ERR_NOT_FOUND_ID, "option.not.found.id",Map.of("id", id));
            }
            return optionMapper.toDto(option);   
        } catch (ServiceException e) {
            throw e;
        }
        catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getOptionById] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<List<OptionResponse>> getAllOption(SearchRequest request){
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
            List<String> fields = SpecificationUtils.getFieldsSearch(Option.class);

            Specification<Option> spec = new SpecificationUtils<Option>()
                .equal("activated", searchModel.getActivated())
                .likeAnyFieldIgnoreCase(searchModel.getQ(), fields)
                .build();

            Page<Option> options = this.optionRepository.findAll(spec, pageRequest);
            List<OptionResponse> optionResponses = this.optionMapper.toDto(options.getContent());
            return PageableUtils.<Option, OptionResponse>buildPaginationResponse(pageRequest, options, optionResponses);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getAllOption] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private void getRawOptionBySlug(String slug, Long excludeId){
        try {
            Optional<Option> duplicate;
            if (excludeId == null) {
                duplicate = this.optionRepository.findBySlug(slug);
            } else {
                duplicate = this.optionRepository.findBySlugAndIdNot(slug,excludeId);
            }
            duplicate.ifPresent(cateExist -> {
                throw new ServiceException(EnumError.PRODUCT_OPTION_DATA_EXISTED_SLUG,"option.exist.slug", Map.of("slug", slug));
            });
            
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getRawOptionBySlug] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
}
