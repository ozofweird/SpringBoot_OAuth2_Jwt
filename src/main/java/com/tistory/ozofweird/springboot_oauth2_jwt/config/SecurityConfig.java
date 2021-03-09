package com.tistory.ozofweird.springboot_oauth2_jwt.config;

import com.tistory.ozofweird.springboot_oauth2_jwt.domain.user.Role;
import com.tistory.ozofweird.springboot_oauth2_jwt.security.CustomUserDetailsService;
import com.tistory.ozofweird.springboot_oauth2_jwt.security.TokenAuthenticationFilter;
import com.tistory.ozofweird.springboot_oauth2_jwt.security.oauth2.CustomOAuth2UserService;
import com.tistory.ozofweird.springboot_oauth2_jwt.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.tistory.ozofweird.springboot_oauth2_jwt.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.tistory.ozofweird.springboot_oauth2_jwt.security.oauth2.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity           // Spring Security 활성화
@EnableGlobalMethodSecurity( // SecurityMethod 활성화
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomUserDetailsService customUserDetailsService;

    private final CustomOAuth2UserService customOAuth2UserService;

    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter();
    }

    /*
      By default, Spring OAuth2 uses HttpSessionOAuth2AuthorizationRequestRepository to save
      the authorization request. But, since our service is stateless, we can't save it in
      the session. We'll save the request in a Base64 encoded cookie instead.

      HttpCookieOAuth2AuthorizationReqeustRepository
      - JWT를 사용하기 때문에 Session에 저장할 필요가 없어져, Authorization Request를 Based64 encoded cookie에 저장
    */

    @Bean
    public HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }

    // Authorization에 사용할 userDetailService와 password Encoder 정의
    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    // SecurityConfig에서 사용할 password encoder를 BCryptPasswordEncoder로 정의
    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    /*
      AuthenticationManager를 외부에서 사용하기 위해 AuthenticationManager Bean을 통해
      @Autowired가 아닌 @Bean 설정으로 Spring Security 밖으로 Authentication 추출
     */
    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // CORS 허용
                .cors()
                .and()
                // 토큰을 사용하기 위해 sessionCreationPolicy를 STATELESS로 설정 (Session 비활성화)
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // CSRF 비활성화
                    .csrf().disable()
                // 로그인폼 비활성화
                    .formLogin().disable()
                // 기본 로그인 창 비활성화
                    .httpBasic().disable()
                    .authorizeRequests()
                        .antMatchers("/", "/test").permitAll()
                        .antMatchers("/**").hasAnyRole(Role.GUEST.name() ,Role.USER.name(), Role.ADMIN.name())
                        .antMatchers("/auth/**", "/oauth2/**").permitAll()
                        .anyRequest().authenticated()
                .and()
                    .oauth2Login()
                        .authorizationEndpoint()
                // 클라이언트 처음 로그인 시도 URI
                        .baseUri("/oauth2/authorization")
                        .authorizationRequestRepository(cookieAuthorizationRequestRepository())
                .and()
                    .userInfoEndpoint()
                        .userService(customOAuth2UserService)
                .and()
                    .successHandler(oAuth2AuthenticationSuccessHandler)
                    .failureHandler(oAuth2AuthenticationFailureHandler);

        // Add our custom Token based authentication filter
        // UsernamePasswordAuthenticationFilter 앞에 custom 필터 추가!
        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}
