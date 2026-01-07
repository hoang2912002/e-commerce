package com.fashion.identity.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.fashion.identity.dto.response.PaginationResponse;

public class PageableUtils {
    public static <E, D> PaginationResponse buildPaginationResponse(PageRequest pageRequest, Page<E> data, List<D> listData){
        PaginationResponse rs = new PaginationResponse();
        PaginationResponse.InnerMetaPaginationResponse meta = new PaginationResponse.InnerMetaPaginationResponse(1,10,1,0L);
        
        if (data == null) {
            rs.setMeta(meta);
            rs.setData(null);
            return rs;
        }

        meta.setPage(pageRequest.getPageNumber());
        meta.setPageSize(pageRequest.getPageSize());
        meta.setPages(data.getTotalPages());
        meta.setTotal(data.getTotalElements());

        rs.setMeta(meta);
        rs.setData(listData);
        return rs;
    }

    public static Sort buildSort(
        String sortStr,
        List<String> allowedFields,
        String defaultField, 
        Sort.Direction defaultDirection
    ) {
        List<Sort.Order> orders = new ArrayList<>();

        if(Objects.nonNull(sortStr) && !sortStr.isEmpty()){
            String[] parts = sortStr.split(",");
            for (int i = 0; i < parts.length; i += 2) {
                String field = parts[i].trim();
                if (!allowedFields.contains(field)) continue;

                Sort.Direction direction = Sort.Direction.ASC;
                if (i + 1 < parts.length && parts[i + 1].trim().equalsIgnoreCase("desc")) {
                    direction = Sort.Direction.DESC;
                }
                orders.add(new Sort.Order(direction, field));
            }
        }

        if (orders.isEmpty()) {
            orders.add(new Sort.Order(defaultDirection, defaultField));
        }

        return Sort.by(orders);
    }

    public static PageRequest buildPageRequest(
        int page,
        int size,
        String sortString,
        List<String> allowedFields,
        String defaultField, 
        Sort.Direction defaultDirection
    ){
        int pageNumber = page > 0 ? page : 0;
        int pageSize = size > 0 ? size : 10;


        return PageRequest.of(
            pageNumber, 
            pageSize, 
            buildSort(sortString, allowedFields, defaultField, defaultDirection));
    }
}
