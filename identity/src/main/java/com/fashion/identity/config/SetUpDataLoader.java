package com.fashion.identity.config;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.fashion.identity.common.enums.GenderEnum;
import com.fashion.identity.common.util.SplitCamelCase;
import com.fashion.identity.entity.Permission;
import com.fashion.identity.entity.Role;
import com.fashion.identity.entity.User;
import com.fashion.identity.exception.ServiceException;
import com.fashion.identity.repository.PermissionRepository;
import com.fashion.identity.repository.RoleRepository;
import com.fashion.identity.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class SetUpDataLoader implements ApplicationListener<ContextRefreshedEvent>{

    @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping;
    PermissionRepository permissionRepository;
    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;

    // @Value("${admin.gmail}")
    // String adminGmail;

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Map<RequestMappingInfo, HandlerMethod> map = handlerMapping.getHandlerMethods();

        List<Permission> listPermission = new ArrayList<>();
        map.forEach((info, handlerMethod) -> {
            Set<String> patterns = info.getPathPatternsCondition().getPatternValues();
            String path = patterns.stream().findFirst().orElse("");

            List<String> range = List.of("USER", "ADMIN");
            //Module
            String[] arrPath = path.split("/"); 
            String module = "UNKNOWN";
            if (arrPath.length > 1 && !arrPath[arrPath.length - 1].contains("error") ) {
                String second = arrPath[1].toUpperCase();
                // module = arrPath.length > 3 ? arrPath[2].toUpperCase() : second;
                module = second;

                Set<RequestMethod> methods = info.getMethodsCondition().getMethods();
                String method = (methods == null || methods.isEmpty()) ? "GET" : methods.iterator().next().name();

                String endpointName = handlerMethod.getMethod().getName();
                String readableName = SplitCamelCase.convertCamelCase(endpointName);

                listPermission.add(
                    Permission.builder()
                    .apiPath(path)
                    .method(method)
                    .module(module)
                    .name(readableName)
                    .service("identity-service")
                    .build()
                );
            }
        });
        List<Permission> permissionsDB = this.permissionRepository.findAll();
        final Set<String> existEndpoint = permissionsDB.stream()
            .map(p -> p.getApiPath() + "::" + p.getModule() + "::" + p.getMethod())
            .collect(Collectors.toSet());

        final List<Permission> insertPermissions = listPermission.stream()
            .filter(p -> !existEndpoint.contains(p.getApiPath() + "::" + p.getModule() + "::" + p.getMethod()))
            .toList();
        
        if(!insertPermissions.isEmpty()){
            List<Permission> insertedPermissions = this.permissionRepository.saveAllAndFlush(insertPermissions);
            permissionsDB = Stream.concat(permissionsDB.stream(), insertedPermissions.stream())
                .distinct().toList();
        }

        Role roleDB = this.roleRepository.findBySlug("admin");
        if (roleDB == null) {
            roleDB = roleRepository.save(
                Role.builder()
                    .name("Admin")
                    .slug("admin")
                    .permissions(new ArrayList<>(permissionsDB)) // 🔥 mutable
                    .build()
            );
        } else {
            if (roleDB.getPermissions().size() != permissionsDB.size()) {
                roleDB.getPermissions().clear();
                roleDB.getPermissions().addAll(permissionsDB);
                roleRepository.save(roleDB);
            }
        }

        String email = "hoang2912002@gmail.com";
        Optional<User> userDB = this.userRepository.findByUserName(email);
        if(!userDB.isPresent() || Objects.isNull(userDB)){
            this.userRepository.save(
                User.builder()
                .fullName("Ngô Công Hoàng")
                .email(email)
                .password(this.passwordEncoder.encode("123456"))
                .phoneNumber("0987654321")
                .gender(GenderEnum.MALE)
                .dob(LocalDate.of(2002, 1, 29))
                .userName(email)
                .refreshToken(null)
                .addresses(null)
                .role(roleDB)
                .build()
            );
        }

        if(insertPermissions.isEmpty() && Objects.nonNull(roleDB) && Objects.nonNull(userDB)){
            System.out.println(">>> SKIP INIT DATABASE ALREADY HAVE DATA....");
        }
        else{
            System.out.println(">>> END INIT DATABASE");
        }
    }
    

}
