package com.lorries.hub.security;

import com.lorries.hub.entity.Employee;
import com.lorries.hub.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 用户详情服务实现
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeMapper employeeMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Employee employee = employeeMapper.findByUsername(username);
        if (employee == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        if (!"ACTIVE".equals(employee.getStatus())) {
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }

        return new User(
                employee.getUsername(),
                employee.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + employee.getRoleCode()))
        );
    }
}
