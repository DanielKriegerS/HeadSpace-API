package com.headspace.identity.infrastructure.security.principal;

import com.headspace.identity.domain.model.RoleName;
import com.headspace.identity.domain.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class HeadSpaceUserPrincipal implements OidcUser, UserDetails {
    private final User user;
    private final Map<String, Object> attributes;
    private final OidcIdToken idToken;

    public HeadSpaceUserPrincipal(User user, Map<String, Object> attributes, OidcIdToken idToken) {
        this.user = user;
        this.attributes = attributes;
        this.idToken = idToken;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(RoleName::name)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return user.getUsername();
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == com.headspace.identity.domain.model.UserStatus.ACTIVE;
    }

    @Override
    public OidcIdToken getIdToken() {
        return idToken;
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return null;
    }

    @Override
    public Map<String, Object> getClaims() {
        return attributes;
    }
}
