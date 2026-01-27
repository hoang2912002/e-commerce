package com.fashion.payment.common.util;

import java.lang.reflect.Field;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.jpa.domain.Specification;

import com.fashion.payment.common.annotation.Searchable;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.ManagedType;

public class SpecificationUtils<T> {
    private final List<Specification<T>> specs = new ArrayList<>();
    
    public SpecificationUtils<T> equal(String field, Object value){
        if(Objects.nonNull(value)){
            specs.add((root, query, builder) -> {
                return builder.equal(resolvePath(root,field), value);
            });
        }
        return this;
    }

    public SpecificationUtils<T> likeIgnoreCase(String field, String value) {
        if (value != null && !value.isBlank()) {

            String normalized = removeAccent(value.toLowerCase());

            specs.add((root, query, builder) -> {
                Path<String> path = root.get(field);
                Expression<String> dbField = builder.function("unaccent", String.class, builder.lower(path));
                return builder.like(dbField, "%" + normalized + "%");
            });
        }
        return this;
    }

    public Specification<T> build() {
        return specs.stream()
                .reduce((s1, s2) -> s1.and(s2))
                .orElse((root, query, builder) -> builder.conjunction());
    }

    public SpecificationUtils<T> likeAnyFieldIgnoreCase(String value, List<String> fields) {
        if (value != null && !value.isBlank() && fields.size() > 0) {
            String normalized = removeAccent(value.toLowerCase());

            Specification<T> spec = null;

            for (String field : fields) {
                Specification<T> fieldSpec = (root, query, builder) -> {
                    Path<String> path = root.get(field);
                    // unaccent là một dictionary (từ điển) chuyên dùng để loại bỏ các dấu phụ (diacritic marks).
                    // cho nên ở PostgreSQL cần chạy câu lệnh CREATE EXTENSION IF NOT EXISTS unaccent;
                    Expression<String> dbField = builder.function("unaccent", String.class, builder.lower(path));
                    return builder.like(dbField, "%" + normalized + "%");
                };

                spec = (spec == null) ? fieldSpec : spec.or(fieldSpec);
            }

            specs.add(spec);
        }
        return this;
    }

    /**
     * Lấy danh sách field có @searchable
     * @param classs
     * @return
     */
    public static List<String> getFieldsSearch(Class<?> classs){
        Class<?> c = classs;
        List<String> searchStr = new ArrayList<>();
        while (c != null) {
            for (Field field : c.getDeclaredFields()) {
                if (field.isAnnotationPresent(Searchable.class)) {
                    searchStr.add(field.getName());
                }
            }
            c = c.getSuperclass();
        }
        return searchStr;
    }


    /**
     * Hàm loại bỏ dấu ở Java để so sánh input với unaccent ở DB
     */
    private String removeAccent(String s) {
        if (s == null) return null;
        String normalized = Normalizer.normalize(s, Normalizer.Form.NFD);
        String result = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        // Xử lý riêng chữ đ
        return result.replace("đ", "d").replace("Đ", "D");
    }

    private Path<?> resolvePath(Root<T> root, String field){
        String[] parts = field.split("\\.");
        Path<?> path = root;
        for (String part : parts) {
            if(path instanceof From<?, ?> from){
                ManagedType<?> managedType = (ManagedType<?>) from.getModel();
                Attribute<?, ?> attr = managedType.getAttribute(part);

                if (attr.isAssociation()) {
                    path = from.join(part, JoinType.LEFT);
                } else {
                    path = from.get(part);
                }
            } else {
                path = path.get(part);
            }
        }
        return path;
    }
}
