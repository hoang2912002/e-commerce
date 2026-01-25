package com.fashion.order.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** 
 * @Definition: Generic interface for mapping between DTO and Entity and Inner Entity
 * @Type Parameters:
 * D - Data Transfer Object type
 * E - Entity type
 * I - Inner Entity type
 * R - Request
 */
public interface EntityMapper<D, E, I, R> {
    D toDto(E entity);
    E toEntity(D dto);
    I toInnerEntity(E entity);
    E toValidated (R request);

    List<E> toEntity (List<D> d);
    List<D> toDto (List<E> e);
    List<I> toInnerEntity (List<E> e);
    
    @Named("toUpdate")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void toUpdate(@MappingTarget E entity, R request);
}
