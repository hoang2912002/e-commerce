package com.fashion.product.service.impls;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.kafka.common.Uuid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.product.common.enums.EnumError;
import com.fashion.product.common.util.ConvertUuidUtil;
import com.fashion.product.common.util.PageableUtils;
import com.fashion.product.common.util.SlugUtil;
import com.fashion.product.common.util.SpecificationUtils;
import com.fashion.product.dto.request.search.SearchModel;
import com.fashion.product.dto.request.search.SearchOption;
import com.fashion.product.dto.request.search.SearchRequest;
import com.fashion.product.dto.response.CategoryResponse;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.entity.Category;
import com.fashion.product.exception.ServiceException;
import com.fashion.product.mapper.CategoryMapper;
import com.fashion.product.repository.CategoryRepository;
import com.fashion.product.service.CategoryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService{
    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public CategoryResponse createCategory(Category category) {
        log.info("[createCategory] start create category ....");
        try {
            return upSertCategory(category,null, categoryMapper::toDto);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [createUser] Error: {}", e.getMessage());
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public CategoryResponse updateCategory(Category category){
        log.info("[updateCategory] start update category ....");
        try {
            return upSertCategory(category,category.getId(), categoryMapper::toDto);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [createUser] Error: {}", e.getMessage());
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID id){
        try {
            Category category = findRawCategoryById(id);
            if(category == null){
                throw new ServiceException(EnumError.PRODUCT_CATEGORY_ERR_NOT_FOUND_ID, "category.not.found.id",Map.of("id", id));
            }
            return categoryMapper.toDto(category);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getCategoryById] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<List<CategoryResponse>> getAllCategory(SearchRequest request) {
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

            List<String> fields = SpecificationUtils.getFieldsSearch(Category.class);

            Specification<Category> spec = new SpecificationUtils<Category>()
                .equal("activated", searchModel.getActivated())
                .likeAnyFieldIgnoreCase(searchModel.getQ(), fields)
                .build();
            Page<Category> categories = this.categoryRepository.findAll(spec, pageRequest);
            List<CategoryResponse> categoryResponses = this.categoryMapper.toDto(categories.getContent());
            return PageableUtils.<Category, CategoryResponse>buildPaginationResponse(pageRequest, categories, categoryResponses);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getAllCategory] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getCategoryTreeStartByListId(List<UUID> ids){
        try {
            List<Category> allCategories = categoryRepository.findAll();

            // 2️⃣ Build map id → Category để lookup nhanh
            Map<UUID, Category> categoryMap = allCategories.stream()
                    .collect(Collectors.toMap(Category::getId, Function.identity()));

            // 3️⃣ Build map parentId → list childId để traverse cây bằng id
            Map<UUID, List<UUID>> childrenMapById = allCategories.stream()
                    .filter(c -> c.getParent() != null)
                    .collect(Collectors.groupingBy(
                            c -> c.getParent().getId(),
                            Collectors.mapping(Category::getId, Collectors.toList())
                    ));

            // 4️⃣ Duyệt từng rootId, lấy tất cả các node con (subtree)
            Set<UUID> allSubtreeIds = new HashSet<>();
            for (UUID rootId : ids) {
                //Dùng stack để thực hiện DFS (Depth-First Search).
                //ArrayDeque với push và pop theo cơ chế LIFO → Last In First Out.
                Deque<UUID> stack = new ArrayDeque<>();
                stack.push(rootId);

                while (!stack.isEmpty()) {
                    UUID currentId = stack.pop();
                    if (allSubtreeIds.add(currentId)) { // add xong mới push
                        List<UUID> childrenIds = childrenMapById.get(currentId);
                        if (childrenIds != null) {
                            stack.addAll(childrenIds);
                        }
                    }
                }
            }

            // 5️⃣ Lọc leaf nodes (không có con)
            List<Category> leafCategories = allSubtreeIds.stream()
                    .map(categoryMap::get)
                    .filter(cat -> !childrenMapById.containsKey(cat.getId()) || childrenMapById.get(cat.getId()).isEmpty())
                    .toList();

            return leafCategories;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getCategoryTreeStartByListId] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public boolean isLeaf(Category category) {
        return category.getChildren() == null || category.getChildren().isEmpty();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Category findRawCategoryById(UUID id){
        try {
            // Optional<Category> cOptional = this.categoryRepository.findById(id);
            return this.categoryRepository.findById(id).orElseGet(null);
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [findRawCategoryById] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private <T> T upSertCategory(
        Category category, 
        UUID checkId, 
        //Callback function — nhận Category sau khi save, trả về một DTO (T) mà bạn muốn
        Function<Category, T> mapper
    ){
        try {
            final String slug = SlugUtil.toSlug(category.getName());
            this.findCategoryBySlug(slug,checkId);
            
            //parent
            if (category.getParent() != null && category.getParent().getId() != null) {
                category.setParent(findRawCategoryById(category.getParent().getId()));
            }

            category.setActivated(true);
            category.setSlug(slug);
            return mapper.apply(categoryRepository.save(category));
            
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [upSertCategory] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private void findCategoryBySlug(String slug, UUID excludeId){
        try {
            Optional<Category> duplicate;
            if (excludeId == null) {
                duplicate = this.categoryRepository.findBySlug(slug);
            } else {
                duplicate = this.categoryRepository.findBySlugAndIdNot(slug,excludeId);
            }
            duplicate.ifPresent(cateExist -> {
                throw new ServiceException(EnumError.PRODUCT_CATEGORY_DATA_EXISTED_SLUG,"category.exist.slug", Map.of("slug", slug));
            });
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [findCategoryBySlug] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
}
