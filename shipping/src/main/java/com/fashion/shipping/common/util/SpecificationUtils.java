package com.fashion.shipping.common.util;

import java.lang.reflect.Field;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.jpa.domain.Specification;

import com.fashion.shipping.common.annotation.Searchable;

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
        /**
         * Specification là 1 interface để tạo điều kiện tìm kiếm 
         * 1️⃣ Vì sao Specification.where() / and() yêu cầu Specification<T> other?
         *      Trong interface đó có:
         *          @Nullable Predicate toPredicate(
         *              Root<T> root, 
         *              CriteriaQuery<?> query, 
         *              CriteriaBuilder criteriaBuilder
         *          );
         *      Đây là hàm để tạo predicate khi gọi đến Specification<T> thì thực chất là tạo predicate cho T
         * 
         * 2️⃣ unrestricted() là gì? Vì sao return null?
         *      static <T> Specification<T> unrestricted() {
         *          return (root, query, builder) -> null;
         *      }
         *      👉 null Predicate trong Criteria API nghĩa là: không áp điều kiện nào
         * 
         * 3️⃣ root, query, builder là gì?     
         *      root: Root<T> root
         *          👉 Đại diện cho Entity gốc trong FROM
         *          ➡ root = FROM user u
         *      query: CriteriaQuery<?> query
         *          👉 Đại diện cho toàn bộ câu query: select, district, order by, group by
         *      builder: CriteriaBuilder builder
         *          👉 Factory để tạo: equal, like, and, or, greaterThan
         *          👉 Nó là tool tạo Predicate không phải callback
         * 
         * 4️⃣ Giải thích đoạn Path<?> path = root
         *      VD: field là "user.address.city"
         *          Sau khi split: user -> address -> city
         *      Do root là Root<User>
         *      root.get("address")
         *          JPA nhìn metadata của User
         *          Thấy address có annotation @ManyToOne
         *          → return Path<Address>
         *      path.get("name")
         *          Thấy name là String
         *          → map tới column
         */
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

    /**
     * 1️⃣ Mục tiêu:
     *      Gom tất cả Specification<T> trong specs
     *      Nối chúng bằng AND
     *      Nếu không có spec nào → trả về điều kiện TRUE
     * 
     * 2️⃣ pecs.stream().reduce((s1, s2) -> s1.and(s2))
     *      return (root, query, cb) -> {
                Predicate p1 = s1.toPredicate(root, query, cb);
                Predicate p2 = s2.toPredicate(root, query, cb);
                return cb.and(p1, p2);
            };
     *      s1, s2 chưa chạy
     *      Chỉ đến lúc repository gọi → toPredicate() mới chạy => Lazy
     * 
     * 3️⃣ .orElse((root, query, cb) -> cb.conjunction())
     *      cb.conjunction() = Predicate luôn TRUE
     *      Lý do ko dùng null như unrestricted: dễ lỗi, ko rõ nghĩa, return null -> không Where
     */
    public Specification<T> build() {
        return specs.stream()
                .reduce((s1, s2) -> s1.and(s2))
                .orElse((root, query, builder) -> builder.conjunction());
    }


    /**
     * Tìm kiếm LIKE trên nhiều field, nối bằng OR
     * 👉 Mục tiêu:
     *      Search 1 keyword.
     *      Áp dụng cho N field.
     *      Nối các điều kiện bằng OR
     *      Ví dụ:
     *          likeAnyFieldIgnoreCase("admin", "username", "email", "fullName")
     *          SQL:    
     *              WHERE
                        username LIKE '%admin%'
                    OR email    LIKE '%admin%'
                    OR full_name LIKE '%admin%'
     * spec = (spec == null) ? fieldSpec : spec.or(fieldSpec);
     *      👉 Sau vòng for:
     *          spec = (field1 LIKE ...) OR (field2 LIKE ...) OR ...
     * 📌 Ví dụ cuối cùng:
     *      WHERE status = 'ACTIVE'
            AND (
                    username LIKE '%admin%'
                OR email LIKE '%admin%'
                OR full_name LIKE '%admin%'
            )
     */
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
            /**
             * Path<?> path = root;
             *      root là Root<User>
             *      Root extends From
             *      👉 path đang đứng ở bảng User
             * instanceof From<?, ?>
             *      path hiện tại có phải là entity / join node không?
             * Khi loop "address.city"
             *      Lần 1 – "address"
             *          path instanceof From  // TRUE (User)
             *          ➡ Sang bảng Address
             *      Lần 2 – "city"
             *          path instanceof From  // TRUE (Address)
             *          ➡ .get("city") (vì city là column)
             *      Lý do join "address" mà ko join "city"
             *          city là String, không phải entity, không có bảng để JOIN
             */
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
