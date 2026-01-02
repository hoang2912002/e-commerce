package com.fashion.identity.mapper;

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
 */
public interface EntityMapper<D, E, I> {
    D toDto(E entity);
    E toEntity(D dto);
    I toInnerEntity(E entity);

    List<E> toEntity (List<D> d);
    List<D> toDto (List<E> e);
    List<I> toInnerEntity (List<E> e);
    
    @Named("partialUpdate")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(@MappingTarget E entity, D dto);
}
